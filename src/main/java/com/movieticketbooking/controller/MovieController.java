package com.movieticketbooking.controller;

import com.movieticketbooking.model.Movie;
import com.movieticketbooking.model.enums.MovieGenre;
import com.movieticketbooking.service.MovieService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Movie resource CRUD and search operations.
 */
@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public ResponseEntity<List<Movie>> getAllMovies(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Double minRating) {
        if (genre != null || language != null || minRating != null) {
            return ResponseEntity.ok(movieService.filterMovies(genre, language, minRating));
        }
        return ResponseEntity.ok(movieService.getAllMovies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Movie> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Movie>> searchMovies(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String genre) {
        if (genre != null && !genre.trim().isEmpty() && !"ALL".equalsIgnoreCase(genre)) {
            MovieGenre g = MovieGenre.fromString(genre);
            return ResponseEntity.ok(movieService.searchMovies(query, g));
        }
        return ResponseEntity.ok(movieService.searchMovies(query));
    }

    @PostMapping
    @com.movieticketbooking.config.RequireAdmin
    public ResponseEntity<Movie> createMovie(@RequestBody Movie movie) {
        Movie created = movieService.addMovie(movie);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @com.movieticketbooking.config.RequireAdmin
    public ResponseEntity<Movie> updateMovie(@PathVariable Long id, @RequestBody Movie movie) {
        Movie updated = movieService.updateMovie(id, movie);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @com.movieticketbooking.config.RequireAdmin
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }
}
