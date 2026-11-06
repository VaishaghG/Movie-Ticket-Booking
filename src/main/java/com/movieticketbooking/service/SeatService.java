package com.movieticketbooking.service;

import com.movieticketbooking.model.Seat;
import java.util.List;
import java.util.Map;

/**
 * Interface demonstrating Abstraction in Seat business operations.
 */
public interface SeatService {

    List<Seat> getSeatsByShowId(Long showId);

    // Demonstrates Collections: Map<String, List<Seat>> grouping seats by row letter (A, B, C...)
    Map<String, List<Seat>> getGroupedSeatsByRow(Long showId);

    // Demonstrates Java Streams: filter() for available seats
    List<Seat> getAvailableSeats(Long showId);

    Seat getSeatById(Long seatId);
}
