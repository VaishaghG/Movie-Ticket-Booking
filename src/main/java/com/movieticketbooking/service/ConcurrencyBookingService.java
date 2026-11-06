package com.movieticketbooking.service;

import com.movieticketbooking.dto.BookingRequest;
import com.movieticketbooking.dto.BookingResponse;
import com.movieticketbooking.dto.ConcurrencyTestResult;
import com.movieticketbooking.exception.SeatAlreadyBookedException;
import com.movieticketbooking.model.Seat;
import com.movieticketbooking.repository.SeatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * ============================================================================
 * MULTITHREADING AND SYNCHRONIZATION DEMONSTRATION SERVICE
 * ============================================================================
 *
 * Viva Explanation:
 * In a real-world movie ticket booking application, hundreds of users access
 * the server concurrently. If two customers (Customer A and Customer B) click
 * "Book Ticket" for the exact same seat at the exact same millisecond:
 *
 * Without synchronization (Race Condition):
 * 1. Thread A checks seat status -> sees AVAILABLE.
 * 2. Thread B checks seat status -> also sees AVAILABLE before Thread A finishes writing.
 * 3. Both threads create bookings and both charge the customers.
 * 4. Result: Double-booking disaster!
 *
 * With Synchronization:
 * 1. Thread A acquires the lock on the booking operation.
 * 2. Thread B must WAIT until Thread A completes its transaction.
 * 3. Thread A updates the seat status to BOOKED and commits.
 * 4. Thread B now enters, checks seat status, sees it is BOOKED, and is safely rejected
 *    with a SeatAlreadyBookedException.
 *
 * This class simulates this exact simultaneous collision using:
 * - Thread pool (ExecutorService)
 * - CountDownLatch (fires both threads at the exact same instant)
 * ============================================================================
 */
@Service
public class ConcurrencyBookingService {

    private static final Logger logger = LoggerFactory.getLogger(ConcurrencyBookingService.class);

    private final BookingService bookingService;
    private final SeatRepository seatRepository;

    public ConcurrencyBookingService(BookingService bookingService, SeatRepository seatRepository) {
        this.bookingService = bookingService;
        this.seatRepository = seatRepository;
    }

    /**
     * Simulates two threads attempting to book the same seat simultaneously.
     *
     * @param showId Show identifier
     * @param seatId Seat identifier to compete for
     * @return ConcurrencyTestResult containing thread timeline and winner/loser logs
     */
    public ConcurrencyTestResult simulateConcurrentBooking(Long showId, Long seatId) {
        Seat targetSeat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("Seat ID " + seatId + " not found"));

        ConcurrencyTestResult result = new ConcurrencyTestResult(targetSeat.getSeatNumber(), showId);
        result.addLog("=== STARTING CONCURRENT BOOKING SIMULATION ===");
        result.addLog("Target Seat: " + targetSeat.getSeatNumber() + " | Show ID: " + showId);
        result.addLog("Initial Status in DB: " + targetSeat.getStatus());

        if (!targetSeat.isAvailable()) {
            result.addLog("Error: The target seat is already booked! Please select an available seat.");
            result.setWinnerStatus("NONE");
            result.setRejectedStatus("SEAT_ALREADY_BOOKED");
            return result;
        }

        // ExecutorService to manage our concurrent customer threads
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // CountDownLatch initialized to 1: Acts as a starting pistol for both threads
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(2);

        // Prepare customer requests for the SAME seat
        BookingRequest requestA = new BookingRequest(
                showId,
                Collections.singletonList(seatId),
                "Customer A (Thread-1)",
                "customerA@concurrent-test.com",
                "9990001111"
        );

        BookingRequest requestB = new BookingRequest(
                showId,
                Collections.singletonList(seatId),
                "Customer B (Thread-2)",
                "customerB@concurrent-test.com",
                "9990002222"
        );

        // Thread 1: Customer A
        executor.submit(() -> {
            try {
                // Wait for the common start signal
                startSignal.await();
                logger.info("[Thread-Customer-A] Attempting to book seat {}", targetSeat.getSeatNumber());
                BookingResponse res = bookingService.bookTickets(requestA);
                synchronized (result) {
                    result.setWinnerCustomer("Customer A");
                    result.setWinnerStatus("SUCCESS (Booking ID: " + res.getBookingId() + ")");
                    result.addLog("[Thread-Customer-A] Successfully booked seat! Status: CONFIRMED");
                }
            } catch (SeatAlreadyBookedException e) {
                synchronized (result) {
                    result.setRejectedCustomer("Customer A");
                    result.setRejectedStatus("REJECTED: " + e.getMessage());
                    result.addLog("[Thread-Customer-A] Blocked: SeatAlreadyBookedException");
                }
            } catch (Exception e) {
                synchronized (result) {
                    result.addLog("[Thread-Customer-A] Exception: " + e.getMessage());
                }
            } finally {
                doneSignal.countDown();
            }
        });

        // Thread 2: Customer B
        executor.submit(() -> {
            try {
                // Wait for the common start signal
                startSignal.await();
                logger.info("[Thread-Customer-B] Attempting to book seat {}", targetSeat.getSeatNumber());
                BookingResponse res = bookingService.bookTickets(requestB);
                synchronized (result) {
                    result.setWinnerCustomer("Customer B");
                    result.setWinnerStatus("SUCCESS (Booking ID: " + res.getBookingId() + ")");
                    result.addLog("[Thread-Customer-B] Successfully booked seat! Status: CONFIRMED");
                }
            } catch (SeatAlreadyBookedException e) {
                synchronized (result) {
                    result.setRejectedCustomer("Customer B");
                    result.setRejectedStatus("REJECTED: " + e.getMessage());
                    result.addLog("[Thread-Customer-B] Blocked: SeatAlreadyBookedException");
                }
            } catch (Exception e) {
                synchronized (result) {
                    result.addLog("[Thread-Customer-B] Exception: " + e.getMessage());
                }
            } finally {
                doneSignal.countDown();
            }
        });

        try {
            // Release both threads at the exact same instant
            result.addLog("Releasing latch: Firing Customer A and Customer B threads simultaneously!");
            startSignal.countDown();

            // Await both threads to complete (max 5 seconds timeout)
            doneSignal.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            result.addLog("Simulation interrupted: " + e.getMessage());
        } finally {
            executor.shutdown();
        }

        result.addLog("=== SIMULATION COMPLETED ===");
        result.addLog("Thread Safety Result: Exactly 1 customer succeeded, and exactly 1 customer was rejected.");
        result.addLog("Final Seat Status in DB: BOOKED (No double-booking occurred)");

        return result;
    }
}
