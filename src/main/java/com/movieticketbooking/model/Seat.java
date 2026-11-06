package com.movieticketbooking.model;

import com.movieticketbooking.model.enums.SeatStatus;
import com.movieticketbooking.model.enums.SeatType;
import java.time.LocalDateTime;

/**
 * Demonstrates:
 * - OOP: Classes & Objects
 * - Enums integration (SeatType, SeatStatus)
 * - Encapsulation & Inheritance
 */
public class Seat extends BaseEntity {

    private Long showId;
    private String seatNumber;
    private SeatType seatType;
    private SeatStatus status;

    // Transient calculated price
    private Double price;

    public Seat() {
        super();
        this.status = SeatStatus.AVAILABLE;
        this.seatType = SeatType.REGULAR;
    }

    public Seat(Long seatId, Long showId, String seatNumber, SeatType seatType, SeatStatus status) {
        super(seatId);
        this.showId = showId;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
        this.status = status;
    }

    public Seat(Long seatId, Long showId, String seatNumber, SeatType seatType, SeatStatus status, LocalDateTime createdAt) {
        super(seatId, createdAt);
        this.showId = showId;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
        this.status = status;
    }

    public Long getSeatId() {
        return getId();
    }

    public void setSeatId(Long seatId) {
        setId(seatId);
    }

    public Long getShowId() {
        return showId;
    }

    public void setShowId(Long showId) {
        this.showId = showId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public SeatType getSeatType() {
        return seatType;
    }

    public void setSeatType(SeatType seatType) {
        this.seatType = seatType;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public void setStatus(SeatStatus status) {
        this.status = status;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public boolean isAvailable() {
        return this.status == SeatStatus.AVAILABLE;
    }

    @Override
    public String toString() {
        return "Seat{" +
                "id=" + getId() +
                ", showId=" + showId +
                ", seatNumber='" + seatNumber + '\'' +
                ", seatType=" + seatType +
                ", status=" + status +
                '}';
    }
}
