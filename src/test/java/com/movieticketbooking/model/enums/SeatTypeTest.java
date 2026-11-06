package com.movieticketbooking.model.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SeatType enum.
 *
 * Demonstrates:
 * - Testing enums with business logic
 * - Price multiplier verification
 * - Enum values and attributes
 */
@DisplayName("SeatType Enum — Unit Tests")
class SeatTypeTest {

    @Test
    @DisplayName("REGULAR seat should have price multiplier of 1.0")
    void regularSeatMultiplier() {
        assertThat(SeatType.REGULAR.getPriceMultiplier()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("PREMIUM seat should have price multiplier of 1.2")
    void premiumSeatMultiplier() {
        assertThat(SeatType.PREMIUM.getPriceMultiplier()).isEqualTo(1.2);
    }

    @Test
    @DisplayName("REGULAR seat price should equal base ticket price")
    void regularSeatPrice() {
        double basePrice = 200.0;
        double expected = 200.0;
        assertThat(SeatType.REGULAR.calculateSeatPrice(basePrice)).isEqualTo(expected);
    }

    @Test
    @DisplayName("PREMIUM seat price should be 1.2x the base ticket price")
    void premiumSeatPrice() {
        double basePrice = 200.0;
        double expected = 240.0;
        assertThat(SeatType.PREMIUM.calculateSeatPrice(basePrice)).isEqualTo(expected);
    }

    @Test
    @DisplayName("PREMIUM seat price should be rounded to 2 decimal places")
    void premiumSeatPriceRounding() {
        double basePrice = 150.0;
        double expected = 180.0; // 150 * 1.2
        assertThat(SeatType.PREMIUM.calculateSeatPrice(basePrice)).isEqualTo(expected);
    }

    @Test
    @DisplayName("REGULAR should have label 'Regular'")
    void regularLabel() {
        assertThat(SeatType.REGULAR.getLabel()).isEqualTo("Regular");
    }

    @Test
    @DisplayName("PREMIUM should have label 'Premium Recliner'")
    void premiumLabel() {
        assertThat(SeatType.PREMIUM.getLabel()).isEqualTo("Premium Recliner");
    }

    @Test
    @DisplayName("Enum should have exactly two values: REGULAR and PREMIUM")
    void enumValues() {
        SeatType[] values = SeatType.values();
        assertThat(values).hasSize(2);
        assertThat(values).contains(SeatType.REGULAR, SeatType.PREMIUM);
    }

    @Test
    @DisplayName("SeatType.valueOf should resolve REGULAR by name")
    void valueOfRegular() {
        assertThat(SeatType.valueOf("REGULAR")).isEqualTo(SeatType.REGULAR);
    }

    @Test
    @DisplayName("SeatType.valueOf should resolve PREMIUM by name")
    void valueOfPremium() {
        assertThat(SeatType.valueOf("PREMIUM")).isEqualTo(SeatType.PREMIUM);
    }
}
