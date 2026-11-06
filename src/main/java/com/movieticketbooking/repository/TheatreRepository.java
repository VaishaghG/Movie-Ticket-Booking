package com.movieticketbooking.repository;

import com.movieticketbooking.model.Theatre;
import java.util.List;
import java.util.Optional;

/**
 * Interface demonstrating Abstraction in Theatre data access.
 */
public interface TheatreRepository {

    List<Theatre> findAll();

    Optional<Theatre> findById(Long id);

    Theatre save(Theatre theatre);

    int update(Theatre theatre);

    int deleteById(Long id);
}
