package com.movieticketbooking.exception;

/**
 * Demonstrates Custom Exception handling in Java.
 */
public class MovieNotFoundException extends RuntimeException {
    public MovieNotFoundException(String message) {
        super(message);
    }

    public MovieNotFoundException(Long movieId) {
        super("Movie not found with ID: " + movieId);
    }
}
