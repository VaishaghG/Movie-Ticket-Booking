package com.movieticketbooking.exception;

/**
 * Demonstrates Custom Exception handling in Java.
 */
public class InvalidBookingException extends RuntimeException {
    public InvalidBookingException(String message) {
        super(message);
    }
}
