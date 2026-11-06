package com.movieticketbooking.repository;

import com.movieticketbooking.model.Theatre;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of TheatreRepository using Spring JdbcTemplate.
 */
@Repository
public class TheatreRepositoryImpl implements TheatreRepository {

    private final JdbcTemplate jdbcTemplate;

    public TheatreRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Theatre> theatreRowMapper = (rs, rowNum) -> {
        Theatre theatre = new Theatre();
        theatre.setId(rs.getLong("theatre_id"));
        theatre.setName(rs.getString("name"));
        theatre.setLocation(rs.getString("location"));
        theatre.setTotalSeats(rs.getInt("total_seats"));
        if (rs.getTimestamp("created_at") != null) {
            theatre.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        return theatre;
    };

    @Override
    public List<Theatre> findAll() {
        String sql = "SELECT * FROM theatres ORDER BY name ASC";
        return jdbcTemplate.query(sql, theatreRowMapper);
    }

    @Override
    public Optional<Theatre> findById(Long id) {
        String sql = "SELECT * FROM theatres WHERE theatre_id = ?";
        List<Theatre> results = jdbcTemplate.query(sql, theatreRowMapper, id);
        return results.stream().findFirst();
    }

    @Override
    public Theatre save(Theatre theatre) {
        String sql = "INSERT INTO theatres (name, location, total_seats) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, theatre.getName());
            ps.setString(2, theatre.getLocation());
            ps.setInt(3, theatre.getTotalSeats() != null ? theatre.getTotalSeats() : 40);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            theatre.setId(key.longValue());
        }
        return theatre;
    }

    @Override
    public int update(Theatre theatre) {
        String sql = "UPDATE theatres SET name = ?, location = ?, total_seats = ? WHERE theatre_id = ?";
        return jdbcTemplate.update(sql, theatre.getName(), theatre.getLocation(), theatre.getTotalSeats(), theatre.getId());
    }

    @Override
    public int deleteById(Long id) {
        String sql = "DELETE FROM theatres WHERE theatre_id = ?";
        return jdbcTemplate.update(sql, id);
    }
}
