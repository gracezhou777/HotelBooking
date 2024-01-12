package com.grace.staybooking.service;

import com.grace.staybooking.exception.StayNotExistException;
import com.grace.staybooking.model.Location;
import com.grace.staybooking.model.Reservation;
import com.grace.staybooking.model.Stay;
import com.grace.staybooking.model.StayImage;
import com.grace.staybooking.model.User;
import com.grace.staybooking.repository.LocationRepository;
import com.grace.staybooking.repository.ReservationRepository;
import com.grace.staybooking.exception.StayDeleteException;
import com.grace.staybooking.model.*;
import com.grace.staybooking.repository.StayRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StayService {
    private final StayRepository stayRepository;

    private final ImageStorageService imageStorageService;

    private final LocationRepository locationRepository;

    private final GeoCodingService geoCodingService;

    private final ReservationRepository reservationRepository;

    public StayService(StayRepository stayRepository, LocationRepository locationRepository, ImageStorageService imageStorageService, GeoCodingService geoCodingService, ReservationRepository reservationRepository) {
        this.stayRepository = stayRepository;
        this.locationRepository = locationRepository;
        this.imageStorageService = imageStorageService;
        this.geoCodingService = geoCodingService;
        this.reservationRepository = reservationRepository;
    }

    public List<Stay> listByUser(String username) {
        return stayRepository.findByHost(new User.Builder().setUsername(username).build());
    }

    public Stay findByIdAndHost(Long stayId, String username) throws StayNotExistException {
        User user = new User.Builder().setUsername(username).build();
        Stay stay = stayRepository.findByIdAndHost(stayId, user);
        if (stay == null) {
            throw new StayNotExistException("Stay doesn't exist");
        }

        return stay;
    }

    @Transactional
    public void add(Stay stay, MultipartFile[] images) {
        List<String> mediaLinks = Arrays.stream(images).parallel().map(
            image -> imageStorageService.save(image)
        ).collect(Collectors.toList());

        List<StayImage> stayImages = new ArrayList<>();
        for (String mediaLink : mediaLinks) {
            stayImages.add(new StayImage(mediaLink, stay));
        }

        stay.setImages(stayImages);
        stayRepository.save(stay);

        Location location = geoCodingService.getLatLng(stay.getId(), stay.getAddress());
        locationRepository.save(location);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void delete(Long stayId, String username) throws StayNotExistException, StayDeleteException {
        Stay stay = stayRepository.findByIdAndHost(stayId, new User.Builder().setUsername(username).build());
        if (stay == null) {
            throw new StayNotExistException("Stay doesn't exist");
        }
        List<Reservation> reservations = reservationRepository.findByStayAndCheckoutDateAfter(stay, LocalDate.now());
        if (reservations != null && reservations.size() > 0) {
            throw new StayDeleteException("Cannot delete stay with active reservation");
        }

        stayRepository.deleteById(stayId);
    }
}
