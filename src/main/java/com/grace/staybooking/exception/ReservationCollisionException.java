package com.grace.staybooking.exception;

public class ReservationCollisionException extends RuntimeException {
  public ReservationCollisionException(String message) {
    super(message);
  }
}