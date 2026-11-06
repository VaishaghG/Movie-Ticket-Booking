package com.movieticketbooking.service;

import com.movieticketbooking.dto.BookingRequest;
import com.movieticketbooking.dto.BookingResponse;
import com.movieticketbooking.exception.BookingNotFoundException;
import com.movieticketbooking.exception.InvalidBookingException;
import com.movieticketbooking.exception.MovieNotFoundException;
import com.movieticketbooking.exception.SeatAlreadyBookedException;
import com.movieticketbooking.exception.ShowNotFoundException;
import com.movieticketbooking.model.Booking;
import com.movieticketbooking.model.Customer;
import com.movieticketbooking.model.Movie;
import com.movieticketbooking.model.Seat;
import com.movieticketbooking.model.Show;
import com.movieticketbooking.model.Theatre;
import com.movieticketbooking.model.enums.BookingStatus;
import com.movieticketbooking.model.enums.SeatStatus;
import com.movieticketbooking.repository.BookingRepository;
import com.movieticketbooking.repository.CustomerRepository;
import com.movieticketbooking.repository.MovieRepository;
import com.movieticketbooking.repository.SeatRepository;
import com.movieticketbooking.repository.ShowRepository;
import com.movieticketbooking.repository.TheatreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Demonstrates:
 * - OOP: Abstraction, Interface Implementation
 * - Collections: List, Set (HashSet), ArrayList
 * - Java Streams & Lambdas: map(), mapToDouble(), collect(), forEach()
 * - Custom Exceptions: MovieNotFoundException, ShowNotFoundException,
 *   SeatAlreadyBookedException, InvalidBookingException, BookingNotFoundException
 * - Database Transactions: @Transactional (atomicity of 11-step booking workflow)
 */
@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final TheatreRepository theatreRepository;
    private final SeatRepository seatRepository;
    private final CustomerRepository customerRepository;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              ShowRepository showRepository,
                              MovieRepository movieRepository,
                              TheatreRepository theatreRepository,
                              SeatRepository seatRepository,
                              CustomerRepository customerRepository) {
        this.bookingRepository = bookingRepository;
        this.showRepository = showRepository;
        this.movieRepository = movieRepository;
        this.theatreRepository = theatreRepository;
        this.seatRepository = seatRepository;
        this.customerRepository = customerRepository;
    }

    /**
     * Complete 11-Step Booking Workflow:
     * 1. Verify show exists
     * 2. Verify movie exists
     * 3. Verify selected seats exist
     * 4. Verify seats belong to selected show
     * 5. Verify seats are available
     * 6. Calculate total price using Streams
     * 7. Create/fetch customer
     * 8. Create booking record
     * 9. Mark seats as BOOKED in MySQL
     * 10. Save booking_seats relationships
     * 11. Return detailed confirmation DTO
     */
    private static final Object GLOBAL_BOOKING_LOCK = new Object();

    @Override
    @Transactional
    public BookingResponse bookTickets(BookingRequest request) {
        // Thread-safe synchronization across concurrent requests
        synchronized (GLOBAL_BOOKING_LOCK) {
            // Validation: Request payload
            if (request == null) {
                throw new InvalidBookingException("Booking request cannot be null");
            }
            if (request.getSeatIds() == null || request.getSeatIds().isEmpty()) {
                throw new InvalidBookingException("At least one seat must be selected");
            }

            // Java Collections (HashSet): Check for duplicate seat IDs in the request
            Set<Long> uniqueSeatIds = new HashSet<>(request.getSeatIds());
            if (uniqueSeatIds.size() != request.getSeatIds().size()) {
                throw new InvalidBookingException("Duplicate seat selections found in the request");
            }

            // Step 1: Verify the show exists
            Show show = showRepository.findById(request.getShowId())
                    .orElseThrow(() -> new ShowNotFoundException(request.getShowId()));

            // Step 2: Verify the movie exists
            Movie movie = movieRepository.findById(show.getMovieId())
                    .orElseThrow(() -> new MovieNotFoundException(show.getMovieId()));

            Theatre theatre = theatreRepository.findById(show.getTheatreId())
                    .orElseThrow(() -> new IllegalArgumentException("Theatre not found with ID: " + show.getTheatreId()));

            // Step 3: Verify selected seats exist
            List<Seat> seats = seatRepository.findByIds(request.getSeatIds());
            if (seats.size() != request.getSeatIds().size()) {
                throw new InvalidBookingException("One or more selected seats do not exist");
            }

            // Step 4: Verify seats belong to the selected show
            for (Seat seat : seats) {
                if (!seat.getShowId().equals(show.getId())) {
                    throw new InvalidBookingException("Seat " + seat.getSeatNumber() + " does not belong to show ID: " + show.getId());
                }
            }

            // Step 5: Verify seats are currently AVAILABLE
            for (Seat seat : seats) {
                if (seat.getStatus() != SeatStatus.AVAILABLE) {
                    throw new SeatAlreadyBookedException(seat.getSeatNumber(), show.getId());
                }
            }

            // Step 6: Atomic Reservation in MySQL: Ensures race conditions fail atomically
            int reservedCount = seatRepository.reserveSeatsAtomically(request.getSeatIds());
            if (reservedCount != request.getSeatIds().size()) {
                throw new SeatAlreadyBookedException("One or more selected seats have already been booked by another customer", show.getId());
            }

            // Step 7: Calculate total price using Java Streams & Lambdas
            double totalAmount = seats.stream()
                    .mapToDouble(seat -> seat.getSeatType().calculateSeatPrice(show.getTicketPrice()))
                    .sum();
            totalAmount = Math.round(totalAmount * 100.0) / 100.0;

            // Step 8: Create customer if not exists, or fetch existing
            Customer customer;
            Optional<Customer> existingCustomer = customerRepository.findByEmail(request.getCustomerEmail().trim());
            if (existingCustomer.isPresent()) {
                customer = existingCustomer.get();
                customer.setName(request.getCustomerName().trim());
                customer.setPhone(request.getCustomerPhone().trim());
                customerRepository.update(customer);
            } else {
                customer = new Customer();
                customer.setName(request.getCustomerName().trim());
                customer.setEmail(request.getCustomerEmail().trim());
                customer.setPhone(request.getCustomerPhone().trim());
                customer = customerRepository.save(customer);
            }

            // Step 9: Create booking record
            Booking booking = new Booking();
            booking.setCustomerId(customer.getId());
            booking.setShowId(show.getId());
            booking.setBookingDate(LocalDateTime.now());
            booking.setTotalAmount(totalAmount);
            booking.setBookingStatus(BookingStatus.CONFIRMED);

            // Step 10: Save booking and save booking_seats relations
            booking = bookingRepository.save(booking, request.getSeatIds());

            // Step 11: Return complete confirmation response DTO
            List<String> seatNumbers = seats.stream()
                    .map(Seat::getSeatNumber)
                    .sorted()
                    .collect(Collectors.toList());

            BookingResponse response = new BookingResponse();
            response.setBookingId(booking.getId());
            response.setCustomerId(customer.getId());
            response.setCustomerName(customer.getName());
            response.setCustomerEmail(customer.getEmail());
            response.setCustomerPhone(customer.getPhone());
            response.setShowId(show.getId());
            response.setMovieTitle(movie.getTitle());
            response.setPosterUrl(movie.getPosterUrl());
            response.setTheatreName(theatre.getName());
            response.setTheatreLocation(theatre.getLocation());
            response.setShowDate(show.getShowDate());
            response.setShowTime(show.getShowTime());
            response.setSeatNumbers(seatNumbers);
            response.setTotalAmount(totalAmount);
            response.setBookingStatus(BookingStatus.CONFIRMED);
            response.setBookingDate(booking.getBookingDate());

            return response;
        }
    }

    @Override
    public BookingResponse getBookingById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
        return mapToBookingResponse(booking);
    }

    @Override
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponse> getBookingsByCustomerId(Long customerId) {
        return bookingRepository.findByCustomerId(customerId).stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        // Mark booking status as CANCELLED
        bookingRepository.updateStatus(bookingId, BookingStatus.CANCELLED);

        // Re-release the booked seats back to AVAILABLE
        List<Seat> showSeats = seatRepository.findByShowId(booking.getShowId());
        List<Long> seatIdsToRelease = showSeats.stream()
                .filter(s -> booking.getSeatNumbers().contains(s.getSeatNumber()))
                .map(Seat::getId)
                .collect(Collectors.toList());

        if (!seatIdsToRelease.isEmpty()) {
            seatRepository.updateStatusBatch(seatIdsToRelease, SeatStatus.AVAILABLE);
        }
    }

    private BookingResponse mapToBookingResponse(Booking b) {
        BookingResponse r = new BookingResponse();
        r.setBookingId(b.getId());
        r.setCustomerId(b.getCustomerId());
        r.setCustomerName(b.getCustomerName());
        r.setCustomerEmail(b.getCustomerEmail());
        r.setCustomerPhone(b.getCustomerPhone());
        r.setShowId(b.getShowId());
        r.setMovieTitle(b.getMovieTitle());
        r.setPosterUrl(b.getMoviePosterUrl());
        r.setTheatreName(b.getTheatreName());
        r.setTheatreLocation(b.getTheatreLocation());
        r.setShowDate(b.getShowDate());
        r.setShowTime(b.getShowTime());
        r.setSeatNumbers(b.getSeatNumbers());
        r.setTotalAmount(b.getTotalAmount());
        r.setBookingStatus(b.getBookingStatus());
        r.setBookingDate(b.getBookingDate());
        return r;
    }
}
