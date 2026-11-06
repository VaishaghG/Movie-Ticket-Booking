package com.movieticketbooking.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * Demonstrates OOP: Classes, Encapsulation, Composition & Association.
 */
public class Show extends BaseEntity {

    private Long movieId;
    private Long theatreId;
    private LocalDate showDate;
    private LocalTime showTime;
    private Double ticketPrice;

    // Joined fields for display convenience
    private String movieTitle;
    private String theatreName;
    private String theatreLocation;

    public Show() {
        super();
    }

    public Show(Long showId, Long movieId, Long theatreId, LocalDate showDate, LocalTime showTime, Double ticketPrice) {
        super(showId);
        this.movieId = movieId;
        this.theatreId = theatreId;
        this.showDate = showDate;
        this.showTime = showTime;
        this.ticketPrice = ticketPrice;
    }

    public Show(Long showId, Long movieId, Long theatreId, LocalDate showDate, LocalTime showTime,
                Double ticketPrice, LocalDateTime createdAt) {
        super(showId, createdAt);
        this.movieId = movieId;
        this.theatreId = theatreId;
        this.showDate = showDate;
        this.showTime = showTime;
        this.ticketPrice = ticketPrice;
    }

    public Long getShowId() {
        return getId();
    }

    public void setShowId(Long showId) {
        setId(showId);
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public Long getTheatreId() {
        return theatreId;
    }

    public void setTheatreId(Long theatreId) {
        this.theatreId = theatreId;
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

    public Double getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(Double ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
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

    @Override
    public String toString() {
        return "Show{" +
                "id=" + getId() +
                ", movieId=" + movieId +
                ", theatreId=" + theatreId +
                ", showDate=" + showDate +
                ", showTime=" + showTime +
                ", ticketPrice=" + ticketPrice +
                '}';
    }
}
