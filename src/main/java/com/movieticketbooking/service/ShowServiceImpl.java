package com.movieticketbooking.service;

import com.movieticketbooking.exception.MovieNotFoundException;
import com.movieticketbooking.exception.ShowNotFoundException;
import com.movieticketbooking.model.Show;
import com.movieticketbooking.repository.MovieRepository;
import com.movieticketbooking.repository.SeatRepository;
import com.movieticketbooking.repository.ShowRepository;
import com.movieticketbooking.repository.TheatreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ShowServiceImpl implements ShowService {

    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final TheatreRepository theatreRepository;
    private final SeatRepository seatRepository;

    public ShowServiceImpl(ShowRepository showRepository,
                           MovieRepository movieRepository,
                           TheatreRepository theatreRepository,
                           SeatRepository seatRepository) {
        this.showRepository = showRepository;
        this.movieRepository = movieRepository;
        this.theatreRepository = theatreRepository;
        this.seatRepository = seatRepository;
    }

    @Override
    public List<Show> getAllShows() {
        return showRepository.findAll();
    }

    @Override
    public Show getShowById(Long id) {
        return showRepository.findById(id)
                .orElseThrow(() -> new ShowNotFoundException(id));
    }

    @Override
    @Transactional
    public Show addShow(Show show) {
        // Validation: Verify movie exists
        movieRepository.findById(show.getMovieId())
                .orElseThrow(() -> new MovieNotFoundException(show.getMovieId()));

        // Validation: Verify theatre exists
        theatreRepository.findById(show.getTheatreId())
                .orElseThrow(() -> new IllegalArgumentException("Theatre not found with ID: " + show.getTheatreId()));

        if (show.getTicketPrice() == null || show.getTicketPrice() <= 0) {
            throw new IllegalArgumentException("Ticket price must be greater than zero");
        }

        Show savedShow = showRepository.save(show);
        // Automatically generate 40 seats (A1-E8) for this new show
        seatRepository.createSeatsForShow(savedShow.getId());

        return savedShow;
    }

    @Override
    public Show updateShow(Long id, Show show) {
        getShowById(id);
        show.setId(id);
        showRepository.update(show);
        return show;
    }

    @Override
    public void deleteShow(Long id) {
        getShowById(id);
        showRepository.deleteById(id);
    }

    @Override
    public List<Show> getShowsByMovieId(Long movieId) {
        return showRepository.findByMovieId(movieId);
    }

    @Override
    public List<Show> getShowsByTheatreId(Long theatreId) {
        return showRepository.findByTheatreId(theatreId);
    }

    @Override
    public List<Show> getShowsByMovieAndDate(Long movieId, LocalDate date) {
        return showRepository.findByMovieAndDate(movieId, date);
    }
}
