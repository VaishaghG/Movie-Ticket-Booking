package com.movieticketbooking.repository;

import com.movieticketbooking.model.Show;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Interface demonstrating Abstraction in Show data access.
 */
public interface ShowRepository {

    List<Show> findAll();

    Optional<Show> findById(Long id);

    Show save(Show show);

    int update(Show show);

    int deleteById(Long id);

    List<Show> findByMovieId(Long movieId);

    List<Show> findByTheatreId(Long theatreId);

    List<Show> findByMovieAndDate(Long movieId, LocalDate date);
}
