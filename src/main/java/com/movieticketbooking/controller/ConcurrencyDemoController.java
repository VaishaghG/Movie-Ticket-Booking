package com.movieticketbooking.controller;

import com.movieticketbooking.dto.ConcurrencyTestResult;
import com.movieticketbooking.service.ConcurrencyBookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for demonstrating Multithreading, Synchronization, and Thread Safety.
 */
@RestController
@RequestMapping("/api/concurrency")
public class ConcurrencyDemoController {

    private final ConcurrencyBookingService concurrencyBookingService;

    public ConcurrencyDemoController(ConcurrencyBookingService concurrencyBookingService) {
        this.concurrencyBookingService = concurrencyBookingService;
    }

    /**
     * Triggers 2 simultaneous threads attempting to book the exact same seat.
     */
    @PostMapping("/test")
    public ResponseEntity<ConcurrencyTestResult> runConcurrencyTest(
            @RequestParam Long showId,
            @RequestParam Long seatId) {
        ConcurrencyTestResult result = concurrencyBookingService.simulateConcurrentBooking(showId, seatId);
        return ResponseEntity.ok(result);
    }
}
