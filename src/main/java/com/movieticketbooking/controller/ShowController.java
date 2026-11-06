package com.movieticketbooking.controller;

import com.movieticketbooking.model.Show;
import com.movieticketbooking.service.ShowService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Show schedules.
 */
@RestController
@RequestMapping("/api/shows")
public class ShowController {

    private final ShowService showService;

    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    @GetMapping
    public ResponseEntity<List<Show>> getAllShows() {
        return ResponseEntity.ok(showService.getAllShows());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Show> getShowById(@PathVariable Long id) {
        return ResponseEntity.ok(showService.getShowById(id));
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<Show>> getShowsByMovieId(
            @PathVariable Long movieId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date != null) {
            return ResponseEntity.ok(showService.getShowsByMovieAndDate(movieId, date));
        }
        return ResponseEntity.ok(showService.getShowsByMovieId(movieId));
    }

    @GetMapping("/theatre/{theatreId}")
    public ResponseEntity<List<Show>> getShowsByTheatreId(@PathVariable Long theatreId) {
        return ResponseEntity.ok(showService.getShowsByTheatreId(theatreId));
    }

    @PostMapping
    @com.movieticketbooking.config.RequireAdmin
    public ResponseEntity<Show> createShow(@RequestBody Show show) {
        Show created = showService.addShow(show);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @com.movieticketbooking.config.RequireAdmin
    public ResponseEntity<Show> updateShow(@PathVariable Long id, @RequestBody Show show) {
        Show updated = showService.updateShow(id, show);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @com.movieticketbooking.config.RequireAdmin
    public ResponseEntity<Void> deleteShow(@PathVariable Long id) {
        showService.deleteShow(id);
        return ResponseEntity.noContent().build();
    }
}
