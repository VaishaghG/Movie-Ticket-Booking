package com.movieticketbooking.service;

import com.movieticketbooking.model.Movie;
import com.movieticketbooking.model.enums.MovieGenre;
import java.util.List;

/**
 * Demonstrates:
 * - OOP: Interfaces and Abstraction
 * - Loose Coupling
 * - Method Overloading (searchMovies)
 */
public interface MovieService {

    List<Movie> getAllMovies();

    Movie getMovieById(Long id);

    Movie addMovie(Movie movie);

    Movie updateMovie(Long id, Movie movie);

    void deleteMovie(Long id);

    // Method Overloading 1: Search by title only
    List<Movie> searchMovies(String keyword);

    // Method Overloading 2: Search by title and filter by genre using Java Streams
    List<Movie> searchMovies(String keyword, MovieGenre genre);

    List<Movie> getMoviesSortedByRating();

    List<Movie> filterMovies(String genre, String language, Double minRating);
}
