package com.movieticketbooking.controller;

import com.movieticketbooking.config.RequireAdmin;
import com.movieticketbooking.config.RequireAuth;
import com.movieticketbooking.dto.BookingRequest;
import com.movieticketbooking.dto.BookingResponse;
import com.movieticketbooking.service.BookingService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Ticket Booking operations.
 * Requires authenticated session for customer operations and admin role for system-wide access.
 */
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Create a ticket booking for the authenticated customer.
     * The customer identity is derived from the verified server-side session.
     */
    @PostMapping
    @RequireAuth
    public ResponseEntity<BookingResponse> bookTickets(
            @Valid @RequestBody BookingRequest request,
            HttpSession session) {

        // Derive verified customer identity from server-side session
        String sessionEmail = (String) session.getAttribute("CUSTOMER_EMAIL");
        String sessionName = (String) session.getAttribute("CUSTOMER_NAME");
        String sessionPhone = (String) session.getAttribute("CUSTOMER_PHONE");

        if (sessionEmail != null && !sessionEmail.isBlank()) {
            request.setCustomerEmail(sessionEmail);
        }
        if (sessionName != null && !sessionName.isBlank()) {
            request.setCustomerName(sessionName);
        }
        if (sessionPhone != null && !sessionPhone.isBlank()) {
            request.setCustomerPhone(sessionPhone);
        }

        BookingResponse response = bookingService.bookTickets(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get a specific booking by ID.
     * Only the customer who owns the booking or an administrator can access it.
     */
    @GetMapping("/{id}")
    @RequireAuth
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id, HttpSession session) {
        BookingResponse booking = bookingService.getBookingById(id);
        Long sessionCustomerId = (Long) session.getAttribute("CUSTOMER_ID");
        String sessionRole = (String) session.getAttribute("CUSTOMER_ROLE");

        if (sessionCustomerId == null || (!booking.getCustomerId().equals(sessionCustomerId) && !"ADMIN".equalsIgnoreCase(sessionRole))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(booking);
    }

    /**
     * Get all bookings across all customers in the system.
     * Restricted strictly to administrators.
     */
    @GetMapping
    @RequireAdmin
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    /**
     * Get bookings belonging to the currently authenticated session user.
     */
    @GetMapping("/my")
    @RequireAuth
    public ResponseEntity<List<BookingResponse>> getMyBookings(HttpSession session) {
        Long sessionCustomerId = (Long) session.getAttribute("CUSTOMER_ID");
        return ResponseEntity.ok(bookingService.getBookingsByCustomerId(sessionCustomerId));
    }

    /**
     * Get bookings for a specific customer ID.
     * Only the matching authenticated customer or an administrator can view these.
     */
    @GetMapping("/customer/{customerId}")
    @RequireAuth
    public ResponseEntity<List<BookingResponse>> getBookingsByCustomer(
            @PathVariable Long customerId,
            HttpSession session) {

        Long sessionCustomerId = (Long) session.getAttribute("CUSTOMER_ID");
        String sessionRole = (String) session.getAttribute("CUSTOMER_ROLE");

        if (sessionCustomerId == null || (!customerId.equals(sessionCustomerId) && !"ADMIN".equalsIgnoreCase(sessionRole))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(bookingService.getBookingsByCustomerId(customerId));
    }

    /**
     * Cancel a booking by ID and release reserved seats.
     * Customers can only cancel their own bookings; administrators can cancel any.
     */
    @DeleteMapping("/{id}")
    @RequireAuth
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id, HttpSession session) {
        BookingResponse booking = bookingService.getBookingById(id);
        Long sessionCustomerId = (Long) session.getAttribute("CUSTOMER_ID");
        String sessionRole = (String) session.getAttribute("CUSTOMER_ROLE");

        if (sessionCustomerId == null || (!booking.getCustomerId().equals(sessionCustomerId) && !"ADMIN".equalsIgnoreCase(sessionRole))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }
}
