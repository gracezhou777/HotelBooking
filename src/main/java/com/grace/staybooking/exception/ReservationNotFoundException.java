package com.grace.staybooking.exception;

public class ReservationNotFoundException extends RuntimeException {
  public ReservationNotFoundException(String message) {
    super(message);
  }
}