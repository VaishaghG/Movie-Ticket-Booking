package com.movieticketbooking.service;

import com.movieticketbooking.dto.BookingRequest;
import com.movieticketbooking.dto.BookingResponse;
import com.movieticketbooking.model.Booking;
import java.util.List;

/**
 * Interface demonstrating Abstraction and Business Workflow in Ticket Booking.
 */
public interface BookingService {

    BookingResponse bookTickets(BookingRequest request);

    BookingResponse getBookingById(Long bookingId);

    List<BookingResponse> getAllBookings();

    List<BookingResponse> getBookingsByCustomerId(Long customerId);

    void cancelBooking(Long bookingId);
}
