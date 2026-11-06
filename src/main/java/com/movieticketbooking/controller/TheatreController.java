package com.movieticketbooking.controller;

import com.movieticketbooking.model.Theatre;
import com.movieticketbooking.service.TheatreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Theatre management.
 */
@RestController
@RequestMapping("/api/theatres")
public class TheatreController {

    private final TheatreService theatreService;

    public TheatreController(TheatreService theatreService) {
        this.theatreService = theatreService;
    }

    @GetMapping
    public ResponseEntity<List<Theatre>> getAllTheatres() {
        return ResponseEntity.ok(theatreService.getAllTheatres());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Theatre> getTheatreById(@PathVariable Long id) {
        return ResponseEntity.ok(theatreService.getTheatreById(id));
    }

    @PostMapping
    @com.movieticketbooking.config.RequireAdmin
    public ResponseEntity<Theatre> createTheatre(@RequestBody Theatre theatre) {
        Theatre created = theatreService.addTheatre(theatre);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @com.movieticketbooking.config.RequireAdmin
    public ResponseEntity<Theatre> updateTheatre(@PathVariable Long id, @RequestBody Theatre theatre) {
        Theatre updated = theatreService.updateTheatre(id, theatre);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @com.movieticketbooking.config.RequireAdmin
    public ResponseEntity<Void> deleteTheatre(@PathVariable Long id) {
        theatreService.deleteTheatre(id);
        return ResponseEntity.noContent().build();
    }
}
