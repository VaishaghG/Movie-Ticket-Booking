package com.movieticketbooking.repository;

import com.movieticketbooking.model.Seat;
import com.movieticketbooking.model.enums.SeatStatus;
import com.movieticketbooking.model.enums.SeatType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of SeatRepository using Spring JdbcTemplate.
 */
@Repository
public class SeatRepositoryImpl implements SeatRepository {

    private final JdbcTemplate jdbcTemplate;

    public SeatRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Seat> seatRowMapper = (rs, rowNum) -> {
        Seat seat = new Seat();
        seat.setId(rs.getLong("seat_id"));
        seat.setShowId(rs.getLong("show_id"));
        seat.setSeatNumber(rs.getString("seat_number"));
        seat.setSeatType(SeatType.valueOf(rs.getString("seat_type")));
        seat.setStatus(SeatStatus.valueOf(rs.getString("status")));
        if (rs.getTimestamp("created_at") != null) {
            seat.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        return seat;
    };

    @Override
    public List<Seat> findByShowId(Long showId) {
        String sql = "SELECT * FROM seats WHERE show_id = ? ORDER BY seat_number ASC";
        return jdbcTemplate.query(sql, seatRowMapper, showId);
    }

    @Override
    public Optional<Seat> findById(Long id) {
        String sql = "SELECT * FROM seats WHERE seat_id = ?";
        List<Seat> results = jdbcTemplate.query(sql, seatRowMapper, id);
        return results.stream().findFirst();
    }

    @Override
    public List<Seat> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        String inSql = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sql = String.format("SELECT * FROM seats WHERE seat_id IN (%s)", inSql);
        return jdbcTemplate.query(sql, seatRowMapper, ids.toArray());
    }

    @Override
    public Seat save(Seat seat) {
        String sql = "INSERT INTO seats (show_id, seat_number, seat_type, status) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, seat.getShowId());
            ps.setString(2, seat.getSeatNumber());
            ps.setString(3, seat.getSeatType().name());
            ps.setString(4, seat.getStatus().name());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            seat.setId(key.longValue());
        }
        return seat;
    }

    @Override
    public void saveBatch(List<Seat> seats) {
        String sql = "INSERT INTO seats (show_id, seat_number, seat_type, status) VALUES (?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(sql, seats, seats.size(), (ps, seat) -> {
            ps.setLong(1, seat.getShowId());
            ps.setString(2, seat.getSeatNumber());
            ps.setString(3, seat.getSeatType().name());
            ps.setString(4, seat.getStatus().name());
        });
    }

    @Override
    public int updateStatus(Long seatId, SeatStatus status) {
        String sql = "UPDATE seats SET status = ? WHERE seat_id = ?";
        return jdbcTemplate.update(sql, status.name(), seatId);
    }

    @Override
    public int updateStatusBatch(List<Long> seatIds, SeatStatus status) {
        if (seatIds == null || seatIds.isEmpty()) {
            return 0;
        }
        String inSql = String.join(",", Collections.nCopies(seatIds.size(), "?"));
        String sql = String.format("UPDATE seats SET status = ? WHERE seat_id IN (%s)", inSql);
        List<Object> params = new ArrayList<>();
        params.add(status.name());
        params.addAll(seatIds);
        return jdbcTemplate.update(sql, params.toArray());
    }

    @Override
    public int reserveSeatsAtomically(List<Long> seatIds) {
        if (seatIds == null || seatIds.isEmpty()) {
            return 0;
        }
        String inSql = String.join(",", Collections.nCopies(seatIds.size(), "?"));
        String sql = String.format("UPDATE seats SET status = 'BOOKED' WHERE seat_id IN (%s) AND status = 'AVAILABLE'", inSql);
        return jdbcTemplate.update(sql, seatIds.toArray());
    }

    @Override
    public int createSeatsForShow(Long showId) {
        List<Seat> seats = new ArrayList<>();
        // Regular rows: A, B, C (1-8)
        String[] regularRows = {"A", "B", "C"};
        for (String row : regularRows) {
            for (int i = 1; i <= 8; i++) {
                seats.add(new Seat(null, showId, row + i, SeatType.REGULAR, SeatStatus.AVAILABLE));
            }
        }
        // Premium rows: D, E (1-8)
        String[] premiumRows = {"D", "E"};
        for (String row : premiumRows) {
            for (int i = 1; i <= 8; i++) {
                seats.add(new Seat(null, showId, row + i, SeatType.PREMIUM, SeatStatus.AVAILABLE));
            }
        }
        saveBatch(seats);
        return seats.size();
    }
}
