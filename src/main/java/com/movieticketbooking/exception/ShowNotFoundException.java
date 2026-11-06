package com.movieticketbooking.exception;

/**
 * Demonstrates Custom Exception handling in Java.
 */
public class ShowNotFoundException extends RuntimeException {
    public ShowNotFoundException(String message) {
        super(message);
    }

    public ShowNotFoundException(Long showId) {
        super("Show not found with ID: " + showId);
    }
}
