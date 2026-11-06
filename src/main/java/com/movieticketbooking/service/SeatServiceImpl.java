package com.movieticketbooking.service;

import com.movieticketbooking.exception.ShowNotFoundException;
import com.movieticketbooking.model.Seat;
import com.movieticketbooking.model.Show;
import com.movieticketbooking.repository.SeatRepository;
import com.movieticketbooking.repository.ShowRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Demonstrates:
 * - Collections: Map, List, LinkedHashMap
 * - Java Streams: filter(), map(), sorted(), collect(Collectors.groupingBy())
 * - Lambdas
 */
@Service
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;
    private final ShowRepository showRepository;

    public SeatServiceImpl(SeatRepository seatRepository, ShowRepository showRepository) {
        this.seatRepository = seatRepository;
        this.showRepository = showRepository;
    }

    @Override
    public List<Seat> getSeatsByShowId(Long showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ShowNotFoundException(showId));

        List<Seat> seats = seatRepository.findByShowId(showId);

        // Java Streams: Set the dynamically calculated price for each seat
        seats.forEach(seat -> {
            double price = seat.getSeatType().calculateSeatPrice(show.getTicketPrice());
            seat.setPrice(price);
        });

        return seats;
    }

    @Override
    public Map<String, List<Seat>> getGroupedSeatsByRow(Long showId) {
        List<Seat> seats = getSeatsByShowId(showId);

        // Java Streams & Collections: Group seats by Row (e.g. "A", "B", "C") preserving alphabetical order
        return seats.stream()
                .sorted(Comparator.comparing(Seat::getSeatNumber))
                .collect(Collectors.groupingBy(
                        seat -> seat.getSeatNumber().substring(0, 1), // Row letter
                        LinkedHashMap::new,                            // Preserves key insertion order
                        Collectors.toList()
                ));
    }

    @Override
    public List<Seat> getAvailableSeats(Long showId) {
        // Java Streams: filter() available seats
        return getSeatsByShowId(showId).stream()
                .filter(Seat::isAvailable)
                .collect(Collectors.toList());
    }

    @Override
    public Seat getSeatById(Long seatId) {
        return seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("Seat not found with ID: " + seatId));
    }
}
