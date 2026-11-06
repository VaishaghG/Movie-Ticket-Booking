package com.movieticketbooking.repository;

import com.movieticketbooking.model.Booking;
import com.movieticketbooking.model.enums.BookingStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of BookingRepository using Spring JdbcTemplate.
 */
@Repository
public class BookingRepositoryImpl implements BookingRepository {

    private final JdbcTemplate jdbcTemplate;

    public BookingRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Booking> bookingRowMapper = (rs, rowNum) -> {
        Booking booking = new Booking();
        booking.setId(rs.getLong("booking_id"));
        booking.setCustomerId(rs.getLong("customer_id"));
        booking.setShowId(rs.getLong("show_id"));
        Timestamp bDate = rs.getTimestamp("booking_date");
        if (bDate != null) {
            booking.setBookingDate(bDate.toLocalDateTime());
        }
        booking.setTotalAmount(rs.getDouble("total_amount"));
        booking.setBookingStatus(BookingStatus.valueOf(rs.getString("booking_status")));
        if (rs.getTimestamp("created_at") != null) {
            booking.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }

        // Joined fields
        try {
            booking.setCustomerName(rs.getString("customer_name"));
            booking.setCustomerEmail(rs.getString("customer_email"));
            booking.setCustomerPhone(rs.getString("customer_phone"));
            booking.setMovieTitle(rs.getString("movie_title"));
            booking.setMoviePosterUrl(rs.getString("poster_url"));
            booking.setTheatreName(rs.getString("theatre_name"));
            booking.setTheatreLocation(rs.getString("theatre_location"));
            Date sDate = rs.getDate("show_date");
            if (sDate != null) {
                booking.setShowDate(sDate.toLocalDate());
            }
            Time sTime = rs.getTime("show_time");
            if (sTime != null) {
                booking.setShowTime(sTime.toLocalTime());
            }
        } catch (Exception ignored) {}

        return booking;
    };

    private static final String BASE_SELECT =
            "SELECT b.*, c.name AS customer_name, c.email AS customer_email, c.phone AS customer_phone, " +
            "m.title AS movie_title, m.poster_url, t.name AS theatre_name, t.location AS theatre_location, " +
            "s.show_date, s.show_time " +
            "FROM bookings b " +
            "JOIN customers c ON b.customer_id = c.customer_id " +
            "JOIN shows s ON b.show_id = s.show_id " +
            "JOIN movies m ON s.movie_id = m.movie_id " +
            "JOIN theatres t ON s.theatre_id = t.theatre_id ";

    @Override
    public List<Booking> findAll() {
        String sql = BASE_SELECT + "ORDER BY b.booking_date DESC";
        List<Booking> list = jdbcTemplate.query(sql, bookingRowMapper);
        for (Booking b : list) {
            b.setSeatNumbers(findSeatNumbersByBookingId(b.getId()));
        }
        return list;
    }

    @Override
    public Optional<Booking> findById(Long id) {
        String sql = BASE_SELECT + "WHERE b.booking_id = ?";
        List<Booking> list = jdbcTemplate.query(sql, bookingRowMapper, id);
        if (list.isEmpty()) {
            return Optional.empty();
        }
        Booking b = list.get(0);
        b.setSeatNumbers(findSeatNumbersByBookingId(b.getId()));
        return Optional.of(b);
    }

    @Override
    public List<Booking> findByCustomerId(Long customerId) {
        String sql = BASE_SELECT + "WHERE b.customer_id = ? ORDER BY b.booking_date DESC";
        List<Booking> list = jdbcTemplate.query(sql, bookingRowMapper, customerId);
        for (Booking b : list) {
            b.setSeatNumbers(findSeatNumbersByBookingId(b.getId()));
        }
        return list;
    }

    @Override
    public Booking save(Booking booking, List<Long> seatIds) {
        String sql = "INSERT INTO bookings (customer_id, show_id, booking_date, total_amount, booking_status) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, booking.getCustomerId());
            ps.setLong(2, booking.getShowId());
            ps.setTimestamp(3, Timestamp.valueOf(booking.getBookingDate()));
            ps.setDouble(4, booking.getTotalAmount());
            ps.setString(5, booking.getBookingStatus().name());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            booking.setId(key.longValue());
        }

        // Insert into booking_seats
        String bsSql = "INSERT INTO booking_seats (booking_id, seat_id) VALUES (?, ?)";
        for (Long seatId : seatIds) {
            jdbcTemplate.update(bsSql, booking.getId(), seatId);
        }

        return booking;
    }

    @Override
    public int updateStatus(Long bookingId, BookingStatus status) {
        String sql = "UPDATE bookings SET booking_status = ? WHERE booking_id = ?";
        return jdbcTemplate.update(sql, status.name(), bookingId);
    }

    @Override
    public int deleteById(Long bookingId) {
        String sql = "DELETE FROM bookings WHERE booking_id = ?";
        return jdbcTemplate.update(sql, bookingId);
    }

    @Override
    public List<String> findSeatNumbersByBookingId(Long bookingId) {
        String sql = "SELECT s.seat_number FROM seats s " +
                "JOIN booking_seats bs ON s.seat_id = bs.seat_id " +
                "WHERE bs.booking_id = ? ORDER BY s.seat_number ASC";
        return jdbcTemplate.queryForList(sql, String.class, bookingId);
    }
}
