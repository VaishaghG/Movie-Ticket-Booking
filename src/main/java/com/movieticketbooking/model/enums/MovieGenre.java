package com.movieticketbooking.model.enums;

/**
 * Demonstrates Java Enum concept.
 * Represents standard movie genres with human-readable display names.
 */
public enum MovieGenre {
    ACTION("Action"),
    COMEDY("Comedy"),
    DRAMA("Drama"),
    SCI_FI("Sci-Fi"),
    HORROR("Horror"),
    THRILLER("Thriller"),
    ANIMATION("Animation"),
    ROMANCE("Romance");

    private final String displayName;

    MovieGenre(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static MovieGenre fromString(String text) {
        for (MovieGenre g : MovieGenre.values()) {
            if (g.name().equalsIgnoreCase(text) || g.displayName.equalsIgnoreCase(text)) {
                return g;
            }
        }
        return ACTION; // default fallback
    }
}
