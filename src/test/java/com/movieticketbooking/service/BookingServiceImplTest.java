package com.movieticketbooking.service;

import com.movieticketbooking.dto.BookingRequest;
import com.movieticketbooking.dto.BookingResponse;
import com.movieticketbooking.exception.InvalidBookingException;
import com.movieticketbooking.exception.SeatAlreadyBookedException;
import com.movieticketbooking.exception.ShowNotFoundException;
import com.movieticketbooking.model.*;
import com.movieticketbooking.model.enums.BookingStatus;
import com.movieticketbooking.model.enums.SeatStatus;
import com.movieticketbooking.model.enums.SeatType;
import com.movieticketbooking.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BookingServiceImpl.
 *
 * Demonstrates:
 * - JUnit 5 @Nested test classes and @DisplayName
 * - Mockito mocking of multiple repository dependencies
 * - Custom exception assertion with assertThatThrownBy
 * - Booking workflow validation (11-step process)
 * - Seat availability checks and atomic reservation
 * - Booking cancellation and seat release
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookingServiceImpl — Unit Tests")
class BookingServiceImplTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private ShowRepository showRepository;
    @Mock private MovieRepository movieRepository;
    @Mock private TheatreRepository theatreRepository;
    @Mock private SeatRepository seatRepository;
    @Mock private CustomerRepository customerRepository;

    private BookingServiceImpl bookingService;

    // Test fixtures
    private Show testShow;
    private Movie testMovie;
    private Theatre testTheatre;
    private Seat testSeat1;
    private Seat testSeat2;

    @BeforeEach
    void setUp() {
        bookingService = new BookingServiceImpl(
                bookingRepository, showRepository, movieRepository,
                theatreRepository, seatRepository, customerRepository
        );

        // Build test fixtures
        testMovie = new Movie();
        testMovie.setId(1L);
        testMovie.setTitle("Interstellar");
        testMovie.setPosterUrl("https://example.com/poster.jpg");

        testTheatre = new Theatre();
        testTheatre.setId(1L);
        testTheatre.setName("IMAX PVR");
        testTheatre.setLocation("Bangalore");

        testShow = new Show();
        testShow.setId(10L);
        testShow.setMovieId(1L);
        testShow.setTheatreId(1L);
        testShow.setTicketPrice(200.0);
        testShow.setShowDate(LocalDate.now().plusDays(1));
        testShow.setShowTime(LocalTime.of(18, 30));

        testSeat1 = new Seat();
        testSeat1.setId(101L);
        testSeat1.setShowId(10L);
        testSeat1.setSeatNumber("A1");
        testSeat1.setStatus(SeatStatus.AVAILABLE);
        testSeat1.setSeatType(SeatType.REGULAR);

        testSeat2 = new Seat();
        testSeat2.setId(102L);
        testSeat2.setShowId(10L);
        testSeat2.setSeatNumber("A2");
        testSeat2.setStatus(SeatStatus.AVAILABLE);
        testSeat2.setSeatType(SeatType.PREMIUM);
    }

    // =========================================================================
    // BOOK TICKETS TESTS
    // =========================================================================

    @Nested
    @DisplayName("bookTickets() — 11-step booking workflow")
    class BookTicketsTests {

        @Test
        @DisplayName("Should complete a successful booking for one REGULAR seat")
        void bookSingleRegularSeat() {
            // Arrange
            List<Long> seatIds = List.of(101L);
            BookingRequest request = new BookingRequest(10L, seatIds, "Test Customer", "test@example.com", "9876543210");

            when(showRepository.findById(10L)).thenReturn(Optional.of(testShow));
            when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));
            when(theatreRepository.findById(1L)).thenReturn(Optional.of(testTheatre));
            when(seatRepository.findByIds(seatIds)).thenReturn(List.of(testSeat1));
            when(seatRepository.reserveSeatsAtomically(seatIds)).thenReturn(1);

            Customer customer = new Customer();
            customer.setId(200L);
            customer.setName("Test Customer");
            customer.setEmail("test@example.com");
            customer.setPhone("9876543210");
            when(customerRepository.findByEmail("test@example.com")).thenReturn(Optional.of(customer));

            Booking savedBooking = new Booking();
            savedBooking.setId(999L);
            savedBooking.setCustomerId(200L);
            savedBooking.setShowId(10L);
            savedBooking.setBookingDate(LocalDateTime.now());
            savedBooking.setTotalAmount(200.0);
            savedBooking.setBookingStatus(BookingStatus.CONFIRMED);
            when(bookingRepository.save(any(Booking.class), eq(seatIds))).thenReturn(savedBooking);

            // Act
            BookingResponse response = bookingService.bookTickets(request);

            // Assert
            assertThat(response.getBookingId()).isEqualTo(999L);
            assertThat(response.getMovieTitle()).isEqualTo("Interstellar");
            assertThat(response.getTheatreName()).isEqualTo("IMAX PVR");
            assertThat(response.getTotalAmount()).isEqualTo(200.0); // 200 * 1.0 = 200
            assertThat(response.getBookingStatus()).isEqualTo(BookingStatus.CONFIRMED);
        }

        @Test
        @DisplayName("Should calculate correct total for PREMIUM seat (1.2x multiplier)")
        void premiumSeatPriceCalculation() {
            // Arrange
            List<Long> seatIds = List.of(102L);
            BookingRequest request = new BookingRequest(10L, seatIds, "Premium User", "premium@example.com", "9000000001");

            when(showRepository.findById(10L)).thenReturn(Optional.of(testShow));
            when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));
            when(theatreRepository.findById(1L)).thenReturn(Optional.of(testTheatre));
            when(seatRepository.findByIds(seatIds)).thenReturn(List.of(testSeat2));
            when(seatRepository.reserveSeatsAtomically(seatIds)).thenReturn(1);

            Customer customer = new Customer();
            customer.setId(201L);
            customer.setName("Premium User");
            customer.setEmail("premium@example.com");
            customer.setPhone("9000000001");
            when(customerRepository.findByEmail("premium@example.com")).thenReturn(Optional.of(customer));

            Booking savedBooking = new Booking();
            savedBooking.setId(1000L);
            savedBooking.setTotalAmount(240.0); // 200 * 1.2
            savedBooking.setBookingStatus(BookingStatus.CONFIRMED);
            when(bookingRepository.save(any(Booking.class), eq(seatIds))).thenReturn(savedBooking);

            // Act
            BookingResponse response = bookingService.bookTickets(request);

            // Assert — PREMIUM seat: 200 * 1.2 = 240
            assertThat(response.getTotalAmount()).isEqualTo(240.0);
        }

        @Test
        @DisplayName("Should throw ShowNotFoundException when show does not exist")
        void bookTicketsShowNotFound() {
            when(showRepository.findById(999L)).thenReturn(Optional.empty());

            BookingRequest request = new BookingRequest(999L, List.of(101L), "X", "x@example.com", "9999999999");

            assertThatThrownBy(() -> bookingService.bookTickets(request))
                    .isInstanceOf(ShowNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw InvalidBookingException when no seats are selected")
        void bookTicketsNoSeats() {
            BookingRequest request = new BookingRequest(10L, Collections.emptyList(), "X", "x@example.com", "9999999999");

            assertThatThrownBy(() -> bookingService.bookTickets(request))
                    .isInstanceOf(InvalidBookingException.class)
                    .hasMessageContaining("seat");
        }

        @Test
        @DisplayName("Should throw InvalidBookingException when null request is passed")
        void bookTicketsNullRequest() {
            assertThatThrownBy(() -> bookingService.bookTickets(null))
                    .isInstanceOf(InvalidBookingException.class);
        }

        @Test
        @DisplayName("Should throw InvalidBookingException for duplicate seat IDs in request")
        void bookTicketsDuplicateSeatIds() {
            // Duplicate seatId 101 appears twice
            // Note: duplicate check happens BEFORE show lookup in BookingServiceImpl
            BookingRequest request = new BookingRequest(10L, Arrays.asList(101L, 101L), "X", "x@example.com", "9000000001");

            // No stubs needed — exception is thrown before any repository calls
            assertThatThrownBy(() -> bookingService.bookTickets(request))
                    .isInstanceOf(InvalidBookingException.class)
                    .hasMessageContaining("uplicate"); // 'D' may be uppercase or lowercase
        }

        @Test
        @DisplayName("Should throw SeatAlreadyBookedException when seat is not AVAILABLE")
        void bookTicketsSeatAlreadyBooked() {
            testSeat1.setStatus(SeatStatus.BOOKED);

            List<Long> seatIds = List.of(101L);
            BookingRequest request = new BookingRequest(10L, seatIds, "X", "x@example.com", "9000000001");

            when(showRepository.findById(10L)).thenReturn(Optional.of(testShow));
            when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));
            when(theatreRepository.findById(1L)).thenReturn(Optional.of(testTheatre));
            when(seatRepository.findByIds(seatIds)).thenReturn(List.of(testSeat1));

            assertThatThrownBy(() -> bookingService.bookTickets(request))
                    .isInstanceOf(SeatAlreadyBookedException.class);
        }

        @Test
        @DisplayName("Should throw SeatAlreadyBookedException when atomic reservation fails (race condition)")
        void bookTicketsAtomicReservationFails() {
            List<Long> seatIds = List.of(101L);
            BookingRequest request = new BookingRequest(10L, seatIds, "X", "x@example.com", "9000000001");

            when(showRepository.findById(10L)).thenReturn(Optional.of(testShow));
            when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));
            when(theatreRepository.findById(1L)).thenReturn(Optional.of(testTheatre));
            when(seatRepository.findByIds(seatIds)).thenReturn(List.of(testSeat1));
            // Simulates race condition: atomic reserve returns 0 instead of 1
            when(seatRepository.reserveSeatsAtomically(seatIds)).thenReturn(0);

            assertThatThrownBy(() -> bookingService.bookTickets(request))
                    .isInstanceOf(SeatAlreadyBookedException.class);
        }
    }

    // =========================================================================
    // CANCEL BOOKING TESTS
    // =========================================================================

    @Nested
    @DisplayName("cancelBooking() — booking cancellation and seat release")
    class CancelBookingTests {

        @Test
        @DisplayName("Should cancel a booking and release its seats back to AVAILABLE")
        void cancelBookingReleasesSeats() {
            // Arrange
            Booking booking = new Booking();
            booking.setId(500L);
            booking.setShowId(10L);
            booking.setBookingStatus(BookingStatus.CONFIRMED);
            booking.setSeatNumbers(List.of("A1"));

            when(bookingRepository.findById(500L)).thenReturn(Optional.of(booking));

            Seat seat = new Seat();
            seat.setId(101L);
            seat.setShowId(10L);
            seat.setSeatNumber("A1");
            seat.setStatus(SeatStatus.BOOKED);
            when(seatRepository.findByShowId(10L)).thenReturn(List.of(seat));

            // Act
            bookingService.cancelBooking(500L);

            // Assert
            verify(bookingRepository).updateStatus(500L, BookingStatus.CANCELLED);
            verify(seatRepository).updateStatusBatch(List.of(101L), SeatStatus.AVAILABLE);
        }

        @Test
        @DisplayName("Should throw BookingNotFoundException when cancelling non-existent booking")
        void cancelNonExistentBooking() {
            when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.cancelBooking(999L))
                    .isInstanceOf(com.movieticketbooking.exception.BookingNotFoundException.class);
        }
    }

    // =========================================================================
    // GET BOOKINGS TESTS
    // =========================================================================

    @Nested
    @DisplayName("getBookingsByCustomerId() — booking retrieval")
    class GetBookingsTests {

        @Test
        @DisplayName("Should return empty list when customer has no bookings")
        void getBookingsByCustomerIdEmpty() {
            when(bookingRepository.findByCustomerId(999L)).thenReturn(Collections.emptyList());

            List<BookingResponse> result = bookingService.getBookingsByCustomerId(999L);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return all bookings for a given customer")
        void getBookingsByCustomerIdReturnsResults() {
            Booking booking = new Booking();
            booking.setId(1L);
            booking.setCustomerId(10L);
            booking.setBookingStatus(BookingStatus.CONFIRMED);
            booking.setSeatNumbers(List.of("B3"));

            when(bookingRepository.findByCustomerId(10L)).thenReturn(List.of(booking));

            List<BookingResponse> result = bookingService.getBookingsByCustomerId(10L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getBookingId()).isEqualTo(1L);
        }
    }
}
