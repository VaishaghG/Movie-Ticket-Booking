package com.movieticketbooking.model;

import com.movieticketbooking.model.enums.BookingStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates:
 * - OOP: Classes & Encapsulation
 * - Collections: List<String> / List<Seat>
 * - Enums: BookingStatus
 */
public class Booking extends BaseEntity {

    private Long customerId;
    private Long showId;
    private LocalDateTime bookingDate;
    private Double totalAmount;
    private BookingStatus bookingStatus;

    // Additional fields for API response / display
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String movieTitle;
    private String moviePosterUrl;
    private String theatreName;
    private String theatreLocation;
    private LocalDate showDate;
    private LocalTime showTime;
    private List<String> seatNumbers = new ArrayList<>();

    public Booking() {
        super();
        this.bookingDate = LocalDateTime.now();
        this.bookingStatus = BookingStatus.CONFIRMED;
    }

    public Booking(Long bookingId, Long customerId, Long showId, LocalDateTime bookingDate,
                   Double totalAmount, BookingStatus bookingStatus) {
        super(bookingId);
        this.customerId = customerId;
        this.showId = showId;
        this.bookingDate = bookingDate != null ? bookingDate : LocalDateTime.now();
        this.totalAmount = totalAmount;
        this.bookingStatus = bookingStatus;
    }

    public Long getBookingId() {
        return getId();
    }

    public void setBookingId(Long bookingId) {
        setId(bookingId);
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getShowId() {
        return showId;
    }

    public void setShowId(Long showId) {
        this.showId = showId;
    }

    public LocalDateTime getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDateTime bookingDate) {
        this.bookingDate = bookingDate;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BookingStatus getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(BookingStatus bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getMoviePosterUrl() {
        return moviePosterUrl;
    }

    public void setMoviePosterUrl(String moviePosterUrl) {
        this.moviePosterUrl = moviePosterUrl;
    }

    public String getTheatreName() {
        return theatreName;
    }

    public void setTheatreName(String theatreName) {
        this.theatreName = theatreName;
    }

    public String getTheatreLocation() {
        return theatreLocation;
    }

    public void setTheatreLocation(String theatreLocation) {
        this.theatreLocation = theatreLocation;
    }

    public LocalDate getShowDate() {
        return showDate;
    }

    public void setShowDate(LocalDate showDate) {
        this.showDate = showDate;
    }

    public LocalTime getShowTime() {
        return showTime;
    }

    public void setShowTime(LocalTime showTime) {
        this.showTime = showTime;
    }

    public List<String> getSeatNumbers() {
        return seatNumbers;
    }

    public void setSeatNumbers(List<String> seatNumbers) {
        this.seatNumbers = seatNumbers != null ? seatNumbers : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + getId() +
                ", customerId=" + customerId +
                ", showId=" + showId +
                ", totalAmount=" + totalAmount +
                ", status=" + bookingStatus +
                '}';
    }
}
