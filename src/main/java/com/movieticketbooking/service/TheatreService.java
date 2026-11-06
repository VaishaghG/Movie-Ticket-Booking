package com.movieticketbooking.service;

import com.movieticketbooking.model.Theatre;
import java.util.List;

/**
 * Interface demonstrating Abstraction in Theatre business logic.
 */
public interface TheatreService {

    List<Theatre> getAllTheatres();

    Theatre getTheatreById(Long id);

    Theatre addTheatre(Theatre theatre);

    Theatre updateTheatre(Long id, Theatre theatre);

    void deleteTheatre(Long id);
}
