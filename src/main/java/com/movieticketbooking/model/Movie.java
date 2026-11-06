package com.movieticketbooking.model;

import com.movieticketbooking.model.enums.MovieGenre;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Demonstrates:
 * - OOP: Classes & Objects
 * - OOP: Encapsulation (private fields, accessors, mutators)
 * - OOP: Inheritance (extends BaseEntity)
 * - OOP: Method Overriding (toString, equals, hashCode)
 * - OOP: Constructor Overloading
 */
public class Movie extends BaseEntity {

    private String title;
    private MovieGenre genre;
    private String language;
    private Integer duration; // in minutes
    private String description;
    private LocalDate releaseDate;
    private Double rating;
    private String posterUrl;

    // Default Constructor
    public Movie() {
        super();
    }

    // Overloaded Constructor 1: Common fields
    public Movie(String title, MovieGenre genre, String language, Integer duration,
                 String description, LocalDate releaseDate, Double rating, String posterUrl) {
        super();
        this.title = title;
        this.genre = genre;
        this.language = language;
        this.duration = duration;
        this.description = description;
        this.releaseDate = releaseDate;
        this.rating = rating;
        this.posterUrl = posterUrl;
    }

    // Overloaded Constructor 2: Full fields including ID and createdAt
    public Movie(Long movieId, String title, MovieGenre genre, String language, Integer duration,
                 String description, LocalDate releaseDate, Double rating, String posterUrl,
                 LocalDateTime createdAt) {
        super(movieId, createdAt);
        this.title = title;
        this.genre = genre;
        this.language = language;
        this.duration = duration;
        this.description = description;
        this.releaseDate = releaseDate;
        this.rating = rating;
        this.posterUrl = posterUrl;
    }

    // Convenience alias for movieId
    public Long getMovieId() {
        return getId();
    }

    public void setMovieId(Long movieId) {
        setId(movieId);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public MovieGenre getGenre() {
        return genre;
    }

    public void setGenre(MovieGenre genre) {
        this.genre = genre;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "id=" + getId() +
                ", title='" + title + '\'' +
                ", genre=" + genre +
                ", language='" + language + '\'' +
                ", duration=" + duration +
                ", rating=" + rating +
                '}';
    }
}
