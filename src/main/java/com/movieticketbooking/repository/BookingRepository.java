package com.movieticketbooking.repository;

import com.movieticketbooking.model.Booking;
import com.movieticketbooking.model.enums.BookingStatus;
import java.util.List;
import java.util.Optional;

/**
 * Interface demonstrating Abstraction in Booking data access.
 */
public interface BookingRepository {

    List<Booking> findAll();

    Optional<Booking> findById(Long id);

    List<Booking> findByCustomerId(Long customerId);

    Booking save(Booking booking, List<Long> seatIds);

    int updateStatus(Long bookingId, BookingStatus status);

    int deleteById(Long bookingId);

    List<String> findSeatNumbersByBookingId(Long bookingId);
}
