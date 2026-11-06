package com.movieticketbooking.repository;

import com.movieticketbooking.model.Movie;
import java.util.List;
import java.util.Optional;

/**
 * Demonstrates:
 * - OOP: Interfaces and Abstraction
 * - Loose coupling in Repository layer
 */
public interface MovieRepository {

    List<Movie> findAll();

    Optional<Movie> findById(Long id);

    Movie save(Movie movie);

    int update(Movie movie);

    int deleteById(Long id);

    List<Movie> findByGenre(String genre);

    List<Movie> searchByTitle(String title);
}
