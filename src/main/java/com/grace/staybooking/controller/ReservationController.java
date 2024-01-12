package com.grace.staybooking.controller;

import com.grace.staybooking.service.ReservationService;
import org.springframework.web.bind.annotation.RestController;
import com.grace.staybooking.exception.InvalidReservationDateException;
import com.grace.staybooking.model.Reservation;
import com.grace.staybooking.model.User;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;


@RestController
public class ReservationController {
  private final ReservationService reservationService;

  public ReservationController(ReservationService reservationService) {
    this.reservationService = reservationService;
  }

  @GetMapping(value = "/reservations")
  public List<Reservation> listReservations(Principal principal) {
    return reservationService.listByGuest(principal.getName());
  }

  @PostMapping("/reservations")
  public void addReservation(@RequestBody Reservation reservation, Principal principal) {
    LocalDate checkinDate = reservation.getCheckinDate();
    LocalDate checkoutDate = reservation.getCheckoutDate();
    if (checkinDate.equals(checkoutDate) || checkinDate.isAfter(checkoutDate) || checkinDate.isBefore(LocalDate.now())) {
      throw new InvalidReservationDateException("Invalid date for reservation");
    }
    reservation.setGuest(new User.Builder().setUsername(principal.getName()).build());
    reservationService.add(reservation);
  }

  @DeleteMapping("/reservations/{reservationId}")
  public void deleteReservation(@PathVariable Long reservationId, Principal principal) {
    reservationService.delete(reservationId, principal.getName());
  }
}
