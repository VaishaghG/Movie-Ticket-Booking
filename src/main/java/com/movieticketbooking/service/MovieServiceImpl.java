package com.movieticketbooking.service;

import com.movieticketbooking.exception.MovieNotFoundException;
import com.movieticketbooking.model.Movie;
import com.movieticketbooking.model.enums.MovieGenre;
import com.movieticketbooking.repository.MovieRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Demonstrates:
 * - OOP: Interface Implementation & Loose Coupling
 * - Java Streams: filter(), sorted(), collect(), map()
 * - Lambda Expressions: (m -> ...)
 * - Method Overloading implementation
 * - Exception Handling with custom MovieNotFoundException
 */
@Service
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;

    public MovieServiceImpl(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Override
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    @Override
    public Movie getMovieById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(id));
    }

    @Override
    public Movie addMovie(Movie movie) {
        if (movie.getTitle() == null || movie.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Movie title cannot be empty");
        }
        return movieRepository.save(movie);
    }

    @Override
    public Movie updateMovie(Long id, Movie movie) {
        getMovieById(id); // ensures movie exists, throws MovieNotFoundException if not
        movie.setId(id);
        movieRepository.update(movie);
        return movie;
    }

    @Override
    public void deleteMovie(Long id) {
        getMovieById(id);
        movieRepository.deleteById(id);
    }

    // Method Overloading 1: Search by title keyword using Java Streams & Lambdas
    @Override
    public List<Movie> searchMovies(String keyword) {
        List<Movie> allMovies = movieRepository.findAll();
        if (keyword == null || keyword.trim().isEmpty()) {
            return allMovies;
        }
        String term = keyword.trim().toLowerCase();
        return allMovies.stream()
                .filter(m -> m.getTitle().toLowerCase().contains(term) ||
                             m.getDescription().toLowerCase().contains(term))
                .collect(Collectors.toList());
    }

    // Method Overloading 2: Search with title keyword AND filter by genre
    @Override
    public List<Movie> searchMovies(String keyword, MovieGenre genre) {
        List<Movie> matchedByTitle = searchMovies(keyword);
        if (genre == null) {
            return matchedByTitle;
        }
        return matchedByTitle.stream()
                .filter(m -> m.getGenre() == genre)
                .collect(Collectors.toList());
    }

    @Override
    public List<Movie> getMoviesSortedByRating() {
        return movieRepository.findAll().stream()
                .sorted(Comparator.comparing(Movie::getRating, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    @Override
    public List<Movie> filterMovies(String genreStr, String language, Double minRating) {
        return movieRepository.findAll().stream()
                .filter(m -> genreStr == null || genreStr.isEmpty() || "ALL".equalsIgnoreCase(genreStr) ||
                             m.getGenre().name().equalsIgnoreCase(genreStr))
                .filter(m -> language == null || language.isEmpty() || "ALL".equalsIgnoreCase(language) ||
                             m.getLanguage().equalsIgnoreCase(language))
                .filter(m -> minRating == null || (m.getRating() != null && m.getRating() >= minRating))
                .sorted(Comparator.comparing(Movie::getRating, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }
}
