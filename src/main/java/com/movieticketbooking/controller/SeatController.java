package com.movieticketbooking.controller;

import com.movieticketbooking.model.Seat;
import com.movieticketbooking.service.SeatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Seat layouts and availability.
 */
@RestController
@RequestMapping("/api/shows/{showId}/seats")
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @GetMapping
    public ResponseEntity<List<Seat>> getSeatsByShow(@PathVariable Long showId) {
        return ResponseEntity.ok(seatService.getSeatsByShowId(showId));
    }

    /**
     * Demonstrates Collections: Returns Map<String, List<Seat>> grouped by row letter (e.g. "A", "B", "C")
     */
    @GetMapping("/grouped")
    public ResponseEntity<Map<String, List<Seat>>> getGroupedSeatsByRow(@PathVariable Long showId) {
        return ResponseEntity.ok(seatService.getGroupedSeatsByRow(showId));
    }

    @GetMapping("/available")
    public ResponseEntity<List<Seat>> getAvailableSeats(@PathVariable Long showId) {
        return ResponseEntity.ok(seatService.getAvailableSeats(showId));
    }
}
