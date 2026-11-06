package com.movieticketbooking.exception;

/**
 * Demonstrates Custom Exception handling in Java.
 */
public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException(String message) {
        super(message);
    }

    public BookingNotFoundException(Long bookingId) {
        super("Booking not found with ID: " + bookingId);
    }
}
