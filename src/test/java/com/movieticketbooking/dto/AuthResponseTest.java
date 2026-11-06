package com.movieticketbooking.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AuthResponse DTO factory methods.
 *
 * Demonstrates:
 * - Testing static factory methods
 * - DTO field verification
 * - Boundary conditions for optional fields
 */
@DisplayName("AuthResponse DTO — Unit Tests")
class AuthResponseTest {

    @Test
    @DisplayName("success() factory should set all fields correctly")
    void successFactoryMethod() {
        AuthResponse response = AuthResponse.success(
                1L, "Priya Patel", "priya@example.com", "9000000001", "CUSTOMER", "Welcome!"
        );

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.isRegistered()).isTrue();
        assertThat(response.getCustomerId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Priya Patel");
        assertThat(response.getEmail()).isEqualTo("priya@example.com");
        assertThat(response.getPhone()).isEqualTo("9000000001");
        assertThat(response.getRole()).isEqualTo("CUSTOMER");
        assertThat(response.getMessage()).isEqualTo("Welcome!");
    }

    @Test
    @DisplayName("success() with null role should default to CUSTOMER")
    void successFactoryNullRoleDefaultsToCustomer() {
        AuthResponse response = AuthResponse.success(
                2L, "Admin User", "admin@example.com", "9111111111", null, "Welcome Admin!"
        );

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getRole()).isEqualTo("CUSTOMER"); // default
    }

    @Test
    @DisplayName("success() 5-arg variant should default role to CUSTOMER")
    void successFiveArgVariant() {
        AuthResponse response = AuthResponse.success(
                3L, "Karan", "karan@example.com", "9222222222", "Signed in!"
        );

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getRole()).isEqualTo("CUSTOMER");
    }

    @Test
    @DisplayName("notRegistered() should mark success=false and registered=false")
    void notRegisteredFactory() {
        AuthResponse response = AuthResponse.notRegistered("No account found.");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.isRegistered()).isFalse();
        assertThat(response.getMessage()).isEqualTo("No account found.");
        assertThat(response.getCustomerId()).isNull();
    }

    @Test
    @DisplayName("error(registered=true) should mark success=false but registered=true")
    void errorFactoryRegisteredTrue() {
        AuthResponse response = AuthResponse.error("Wrong password.", true);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.isRegistered()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Wrong password.");
    }

    @Test
    @DisplayName("error(registered=false) should mark both success and registered as false")
    void errorFactoryRegisteredFalse() {
        AuthResponse response = AuthResponse.error("Missing email.", false);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.isRegistered()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Missing email.");
    }

    @Test
    @DisplayName("Default constructor should create response with all null/false defaults")
    void defaultConstructor() {
        AuthResponse response = new AuthResponse();

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.isRegistered()).isFalse();
        assertThat(response.getCustomerId()).isNull();
        assertThat(response.getName()).isNull();
        assertThat(response.getEmail()).isNull();
    }
}
