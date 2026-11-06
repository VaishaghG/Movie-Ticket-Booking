package com.movieticketbooking.model.enums;

/**
 * Demonstrates Java Enum with custom attributes and methods.
 * Premium seats apply a 1.2x price multiplier.
 */
public enum SeatType {
    REGULAR(1.0, "Regular"),
    PREMIUM(1.2, "Premium Recliner");

    private final double priceMultiplier;
    private final String label;

    SeatType(double priceMultiplier, String label) {
        this.priceMultiplier = priceMultiplier;
        this.label = label;
    }

    public double getPriceMultiplier() {
        return priceMultiplier;
    }

    public String getLabel() {
        return label;
    }

    public double calculateSeatPrice(double baseTicketPrice) {
        return Math.round(baseTicketPrice * priceMultiplier * 100.0) / 100.0;
    }
}
