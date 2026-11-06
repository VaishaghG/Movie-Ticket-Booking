package com.movieticketbooking.repository;

import com.movieticketbooking.model.Seat;
import com.movieticketbooking.model.enums.SeatStatus;
import java.util.List;
import java.util.Optional;

/**
 * Interface demonstrating Abstraction in Seat data access.
 */
public interface SeatRepository {

    List<Seat> findByShowId(Long showId);

    Optional<Seat> findById(Long id);

    List<Seat> findByIds(List<Long> ids);

    Seat save(Seat seat);

    void saveBatch(List<Seat> seats);

    int updateStatus(Long seatId, SeatStatus status);

    int updateStatusBatch(List<Long> seatIds, SeatStatus status);

    int reserveSeatsAtomically(List<Long> seatIds);

    int createSeatsForShow(Long showId);
}
