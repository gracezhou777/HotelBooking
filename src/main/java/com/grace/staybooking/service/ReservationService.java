package com.grace.staybooking.service;

import com.grace.staybooking.model.Reservation;
import com.grace.staybooking.model.Stay;
import com.grace.staybooking.model.StayReservedDate;
import com.grace.staybooking.model.StayReservedDateKey;
import com.grace.staybooking.model.User;
import com.grace.staybooking.model.*;
import org.springframework.stereotype.Service;
import com.grace.staybooking.repository.ReservationRepository;
import com.grace.staybooking.repository.StayReservationDateRepository;
import com.grace.staybooking.exception.ReservationCollisionException;
import com.grace.staybooking.exception.ReservationNotFoundException;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class ReservationService {
  private final ReservationRepository reservationRepository;
  private final StayReservationDateRepository stayReservationDateRepository;

  public ReservationService(ReservationRepository reservationRepository, StayReservationDateRepository stayReservationDateRepository) {
    this.reservationRepository = reservationRepository;
    this.stayReservationDateRepository = stayReservationDateRepository;
  }

  public List<Reservation> listByGuest(String username) {
    //method chain写法，节省代码
    return reservationRepository.findByGuest(new User.Builder().setUsername(username).build());
  }

  public List<Reservation> listByStay(Long stayId) {
    return reservationRepository.findByStay(new Stay.Builder().setId(stayId).build());
  }

  @Transactional(isolation = Isolation.SERIALIZABLE)
  public void add(Reservation reservation) throws ReservationCollisionException {
    Set<Long> stayIds = stayReservationDateRepository.findByIdInAndDateBetween(
        Arrays.asList(reservation.getStay().getId()),
        reservation.getCheckinDate(),
        reservation.getCheckoutDate().minusDays(1));

    if (!stayIds.isEmpty()) {
      throw new ReservationCollisionException("Duplicate reservation");
    }

    List<StayReservedDate> reservedDates = new ArrayList<>();
    for (LocalDate date = reservation.getCheckinDate(); date.isBefore(reservation.getCheckoutDate()); date = date.plusDays(1)) {
      //写到stay_reserved_date table
      reservedDates.add(
          new StayReservedDate(
              new StayReservedDateKey(reservation.getStay().getId(), date),
              reservation.getStay()
          )
      );
    }
    //写到reservation table
    stayReservationDateRepository.saveAll(reservedDates);
    reservationRepository.save(reservation);
  }

  @Transactional(isolation = Isolation.SERIALIZABLE)
  public void delete(Long reservationId, String username) {
    Reservation reservation = reservationRepository.findByIdAndGuest(
        reservationId,
        new User.Builder().setUsername(username).build()
    );

    if (reservation == null) {
      throw new ReservationNotFoundException("Reservation is not available");
    }
    for (LocalDate date = reservation.getCheckinDate(); date.isBefore(reservation.getCheckoutDate()); date = date.plusDays(1)) {
      stayReservationDateRepository.deleteById(new StayReservedDateKey(reservation.getStay().getId(), date));
    }
    reservationRepository.deleteById(reservationId);
  }
}
