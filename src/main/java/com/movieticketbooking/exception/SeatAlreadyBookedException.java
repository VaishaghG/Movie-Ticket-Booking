package com.movieticketbooking.exception;

/**
 * Demonstrates Custom Exception handling in Java.
 * Thrown when an attempt is made to reserve an already-booked seat.
 */
public class SeatAlreadyBookedException extends RuntimeException {
    public SeatAlreadyBookedException(String message) {
        super(message);
    }

    public SeatAlreadyBookedException(String seatNumber, Long showId) {
        super("Seat " + seatNumber + " is already booked for show ID: " + showId);
    }
}
