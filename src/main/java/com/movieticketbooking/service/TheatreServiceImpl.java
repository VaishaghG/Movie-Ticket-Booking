package com.movieticketbooking.service;

import com.movieticketbooking.model.Theatre;
import com.movieticketbooking.repository.TheatreRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TheatreServiceImpl implements TheatreService {

    private final TheatreRepository theatreRepository;

    public TheatreServiceImpl(TheatreRepository theatreRepository) {
        this.theatreRepository = theatreRepository;
    }

    @Override
    public List<Theatre> getAllTheatres() {
        return theatreRepository.findAll();
    }

    @Override
    public Theatre getTheatreById(Long id) {
        return theatreRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Theatre not found with ID: " + id));
    }

    @Override
    public Theatre addTheatre(Theatre theatre) {
        if (theatre.getName() == null || theatre.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Theatre name cannot be empty");
        }
        return theatreRepository.save(theatre);
    }

    @Override
    public Theatre updateTheatre(Long id, Theatre theatre) {
        getTheatreById(id);
        theatre.setId(id);
        theatreRepository.update(theatre);
        return theatre;
    }

    @Override
    public void deleteTheatre(Long id) {
        getTheatreById(id);
        theatreRepository.deleteById(id);
    }
}
