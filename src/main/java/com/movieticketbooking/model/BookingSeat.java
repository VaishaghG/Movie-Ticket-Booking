package com.movieticketbooking.model;

/**
 * Demonstrates:
 * - OOP: Classes & Encapsulation
 * - Many-to-Many join entity representing Booking and Seat relationship
 */
public class BookingSeat extends BaseEntity {

    private Long bookingId;
    private Long seatId;

    public BookingSeat() {
        super();
    }

    public BookingSeat(Long bookingSeatId, Long bookingId, Long seatId) {
        super(bookingSeatId);
        this.bookingId = bookingId;
        this.seatId = seatId;
    }

    public Long getBookingSeatId() {
        return getId();
    }

    public void setBookingSeatId(Long bookingSeatId) {
        setId(bookingSeatId);
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long seatId) {
        this.seatId = seatId;
    }

    @Override
    public String toString() {
        return "BookingSeat{" +
                "id=" + getId() +
                ", bookingId=" + bookingId +
                ", seatId=" + seatId +
                '}';
    }
}
