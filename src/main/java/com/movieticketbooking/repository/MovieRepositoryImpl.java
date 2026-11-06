package com.movieticketbooking.repository;

import com.movieticketbooking.model.Movie;
import com.movieticketbooking.model.enums.MovieGenre;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

/**
 * Demonstrates:
 * - Interface Implementation (Polymorphism)
 * - JDBC / Database connectivity using Spring JdbcTemplate
 * - Parameterized Queries / Prepared Statements (preventing SQL Injection)
 * - Custom RowMapper implementation
 */
@Repository
public class MovieRepositoryImpl implements MovieRepository {

    private final JdbcTemplate jdbcTemplate;

    public MovieRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // RowMapper for mapping SQL result sets to Movie objects
    private final RowMapper<Movie> movieRowMapper = (rs, rowNum) -> {
        Movie movie = new Movie();
        movie.setId(rs.getLong("movie_id"));
        movie.setTitle(rs.getString("title"));
        movie.setGenre(MovieGenre.fromString(rs.getString("genre")));
        movie.setLanguage(rs.getString("language"));
        movie.setDuration(rs.getInt("duration"));
        movie.setDescription(rs.getString("description"));
        Date relDate = rs.getDate("release_date");
        if (relDate != null) {
            movie.setReleaseDate(relDate.toLocalDate());
        }
        movie.setRating(rs.getDouble("rating"));
        movie.setPosterUrl(rs.getString("poster_url"));
        if (rs.getTimestamp("created_at") != null) {
            movie.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        return movie;
    };

    @Override
    public List<Movie> findAll() {
        String sql = "SELECT * FROM movies ORDER BY rating DESC";
        return jdbcTemplate.query(sql, movieRowMapper);
    }

    @Override
    public Optional<Movie> findById(Long id) {
        String sql = "SELECT * FROM movies WHERE movie_id = ?";
        List<Movie> results = jdbcTemplate.query(sql, movieRowMapper, id);
        return results.stream().findFirst();
    }

    @Override
    public Movie save(Movie movie) {
        String sql = "INSERT INTO movies (title, genre, language, duration, description, release_date, rating, poster_url) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, movie.getTitle());
            ps.setString(2, movie.getGenre() != null ? movie.getGenre().name() : MovieGenre.ACTION.name());
            ps.setString(3, movie.getLanguage());
            ps.setInt(4, movie.getDuration());
            ps.setString(5, movie.getDescription());
            ps.setDate(6, movie.getReleaseDate() != null ? Date.valueOf(movie.getReleaseDate()) : new Date(System.currentTimeMillis()));
            ps.setDouble(7, movie.getRating() != null ? movie.getRating() : 0.0);
            ps.setString(8, movie.getPosterUrl());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            movie.setId(key.longValue());
        }
        return movie;
    }

    @Override
    public int update(Movie movie) {
        String sql = "UPDATE movies SET title = ?, genre = ?, language = ?, duration = ?, description = ?, " +
                "release_date = ?, rating = ?, poster_url = ? WHERE movie_id = ?";
        return jdbcTemplate.update(sql,
                movie.getTitle(),
                movie.getGenre() != null ? movie.getGenre().name() : MovieGenre.ACTION.name(),
                movie.getLanguage(),
                movie.getDuration(),
                movie.getDescription(),
                movie.getReleaseDate() != null ? Date.valueOf(movie.getReleaseDate()) : new Date(System.currentTimeMillis()),
                movie.getRating(),
                movie.getPosterUrl(),
                movie.getId()
        );
    }

    @Override
    public int deleteById(Long id) {
        String sql = "DELETE FROM movies WHERE movie_id = ?";
        return jdbcTemplate.update(sql, id);
    }

    @Override
    public List<Movie> findByGenre(String genre) {
        String sql = "SELECT * FROM movies WHERE UPPER(genre) = UPPER(?) ORDER BY rating DESC";
        return jdbcTemplate.query(sql, movieRowMapper, genre);
    }

    @Override
    public List<Movie> searchByTitle(String title) {
        String sql = "SELECT * FROM movies WHERE LOWER(title) LIKE LOWER(?) ORDER BY rating DESC";
        return jdbcTemplate.query(sql, movieRowMapper, "%" + title + "%");
    }
}
