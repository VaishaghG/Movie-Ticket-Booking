package com.movieticketbooking.model;

import java.time.LocalDateTime;

/**
 * Demonstrates OOP: Encapsulation, Inheritance, Constructors.
 */
public class Theatre extends BaseEntity {

    private String name;
    private String location;
    private Integer totalSeats;

    public Theatre() {
        super();
    }

    public Theatre(Long theatreId, String name, String location, Integer totalSeats) {
        super(theatreId);
        this.name = name;
        this.location = location;
        this.totalSeats = totalSeats;
    }

    public Theatre(Long theatreId, String name, String location, Integer totalSeats, LocalDateTime createdAt) {
        super(theatreId, createdAt);
        this.name = name;
        this.location = location;
        this.totalSeats = totalSeats;
    }

    public Long getTheatreId() {
        return getId();
    }

    public void setTheatreId(Long theatreId) {
        setId(theatreId);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(Integer totalSeats) {
        this.totalSeats = totalSeats;
    }

    @Override
    public String toString() {
        return "Theatre{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", totalSeats=" + totalSeats +
                '}';
    }
}
