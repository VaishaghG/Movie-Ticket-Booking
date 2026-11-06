package com.movieticketbooking.service;

import com.movieticketbooking.model.Show;
import java.time.LocalDate;
import java.util.List;

/**
 * Interface demonstrating Abstraction in Show business logic.
 */
public interface ShowService {

    List<Show> getAllShows();

    Show getShowById(Long id);

    Show addShow(Show show);

    Show updateShow(Long id, Show show);

    void deleteShow(Long id);

    List<Show> getShowsByMovieId(Long movieId);

    List<Show> getShowsByTheatreId(Long theatreId);

    List<Show> getShowsByMovieAndDate(Long movieId, LocalDate date);
}
