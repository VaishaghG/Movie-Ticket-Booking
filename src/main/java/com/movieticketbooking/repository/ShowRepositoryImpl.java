package com.movieticketbooking.repository;

import com.movieticketbooking.model.Show;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of ShowRepository.
 */
@Repository
public class ShowRepositoryImpl implements ShowRepository {

    private final JdbcTemplate jdbcTemplate;

    public ShowRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Show> showRowMapper = (rs, rowNum) -> {
        Show show = new Show();
        show.setId(rs.getLong("show_id"));
        show.setMovieId(rs.getLong("movie_id"));
        show.setTheatreId(rs.getLong("theatre_id"));
        Date sDate = rs.getDate("show_date");
        if (sDate != null) {
            show.setShowDate(sDate.toLocalDate());
        }
        Time sTime = rs.getTime("show_time");
        if (sTime != null) {
            show.setShowTime(sTime.toLocalTime());
        }
        show.setTicketPrice(rs.getDouble("ticket_price"));
        if (rs.getTimestamp("created_at") != null) {
            show.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }

        // Optional joined columns
        try {
            show.setMovieTitle(rs.getString("movie_title"));
        } catch (Exception ignored) {}
        try {
            show.setTheatreName(rs.getString("theatre_name"));
        } catch (Exception ignored) {}
        try {
            show.setTheatreLocation(rs.getString("theatre_location"));
        } catch (Exception ignored) {}

        return show;
    };

    private static final String BASE_SELECT =
            "SELECT s.*, m.title AS movie_title, t.name AS theatre_name, t.location AS theatre_location " +
            "FROM shows s " +
            "JOIN movies m ON s.movie_id = m.movie_id " +
            "JOIN theatres t ON s.theatre_id = t.theatre_id ";

    @Override
    public List<Show> findAll() {
        String sql = BASE_SELECT + "ORDER BY s.show_date ASC, s.show_time ASC";
        return jdbcTemplate.query(sql, showRowMapper);
    }

    @Override
    public Optional<Show> findById(Long id) {
        String sql = BASE_SELECT + "WHERE s.show_id = ?";
        List<Show> results = jdbcTemplate.query(sql, showRowMapper, id);
        return results.stream().findFirst();
    }

    @Override
    public Show save(Show show) {
        String sql = "INSERT INTO shows (movie_id, theatre_id, show_date, show_time, ticket_price) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, show.getMovieId());
            ps.setLong(2, show.getTheatreId());
            ps.setDate(3, Date.valueOf(show.getShowDate()));
            ps.setTime(4, Time.valueOf(show.getShowTime()));
            ps.setDouble(5, show.getTicketPrice());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            show.setId(key.longValue());
        }
        return show;
    }

    @Override
    public int update(Show show) {
        String sql = "UPDATE shows SET movie_id = ?, theatre_id = ?, show_date = ?, show_time = ?, ticket_price = ? WHERE show_id = ?";
        return jdbcTemplate.update(sql,
                show.getMovieId(),
                show.getTheatreId(),
                Date.valueOf(show.getShowDate()),
                Time.valueOf(show.getShowTime()),
                show.getTicketPrice(),
                show.getId()
        );
    }

    @Override
    public int deleteById(Long id) {
        String sql = "DELETE FROM shows WHERE show_id = ?";
        return jdbcTemplate.update(sql, id);
    }

    @Override
    public List<Show> findByMovieId(Long movieId) {
        String sql = BASE_SELECT + "WHERE s.movie_id = ? ORDER BY s.show_date ASC, s.show_time ASC";
        return jdbcTemplate.query(sql, showRowMapper, movieId);
    }

    @Override
    public List<Show> findByTheatreId(Long theatreId) {
        String sql = BASE_SELECT + "WHERE s.theatre_id = ? ORDER BY s.show_date ASC, s.show_time ASC";
        return jdbcTemplate.query(sql, showRowMapper, theatreId);
    }

    @Override
    public List<Show> findByMovieAndDate(Long movieId, LocalDate date) {
        String sql = BASE_SELECT + "WHERE s.movie_id = ? AND s.show_date = ? ORDER BY s.show_time ASC";
        return jdbcTemplate.query(sql, showRowMapper, movieId, Date.valueOf(date));
    }
}
