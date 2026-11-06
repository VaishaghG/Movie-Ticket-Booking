package com.movieticketbooking.service;

import com.movieticketbooking.dto.AuthResponse;
import com.movieticketbooking.dto.LoginRequest;
import com.movieticketbooking.dto.RegisterRequest;
import com.movieticketbooking.model.Customer;
import com.movieticketbooking.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthServiceImpl.
 *
 * Demonstrates:
 * - JUnit 5 @Test, @BeforeEach, @Nested, @DisplayName
 * - Mockito mocking and verification
 * - AssertJ fluent assertions
 * - BCrypt password hashing verification
 * - Legacy plain-text password migration testing
 * - Edge cases: null input, empty strings, duplicate email
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl — Unit Tests")
class AuthServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    // Real BCryptPasswordEncoder for accurate hashing behaviour
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        // Manually inject real encoder so BCrypt round-trips work correctly
        authService = new AuthServiceImpl(customerRepository, passwordEncoder);
    }

    // =========================================================================
    // REGISTRATION TESTS
    // =========================================================================

    @Nested
    @DisplayName("register() — new user onboarding")
    class RegisterTests {

        @Test
        @DisplayName("Should register a new customer with BCrypt-hashed password")
        void registerSuccess() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setName("Ananya Sharma");
            request.setEmail("ananya@example.com");
            request.setPhone("9876543210");
            request.setPassword("securePass1");

            when(customerRepository.findByEmail("ananya@example.com")).thenReturn(Optional.empty());

            Customer saved = new Customer();
            saved.setId(1L);
            saved.setName("Ananya Sharma");
            saved.setEmail("ananya@example.com");
            saved.setPhone("9876543210");
            saved.setRole("CUSTOMER");
            saved.setPassword(passwordEncoder.encode("securePass1"));
            when(customerRepository.save(any(Customer.class))).thenReturn(saved);

            // Act
            AuthResponse response = authService.register(request);

            // Assert
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getEmail()).isEqualTo("ananya@example.com");
            assertThat(response.getName()).isEqualTo("Ananya Sharma");
            assertThat(response.getRole()).isEqualTo("CUSTOMER");
            assertThat(response.getMessage()).contains("Welcome to CinePass");

            // Verify password was stored as BCrypt hash, not plain text
            ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
            verify(customerRepository).save(captor.capture());
            String storedPassword = captor.getValue().getPassword();
            assertThat(storedPassword).startsWith("$2a$");
            assertThat(passwordEncoder.matches("securePass1", storedPassword)).isTrue();
        }

        @Test
        @DisplayName("Should reject registration when email is already taken")
        void registerDuplicateEmail() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setName("Duplicate User");
            request.setEmail("existing@example.com");
            request.setPhone("9000000000");
            request.setPassword("password123");

            Customer existing = new Customer();
            existing.setEmail("existing@example.com");
            when(customerRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existing));

            // Act
            AuthResponse response = authService.register(request);

            // Assert
            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).contains("already exists");
            verify(customerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject registration with invalid email format")
        void registerInvalidEmail() {
            RegisterRequest request = new RegisterRequest();
            request.setName("Bad Email");
            request.setEmail("not-an-email");
            request.setPhone("9999999999");
            request.setPassword("password123");

            AuthResponse response = authService.register(request);

            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).containsIgnoringCase("email");
            verify(customerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject registration with password shorter than 6 characters")
        void registerShortPassword() {
            RegisterRequest request = new RegisterRequest();
            request.setName("Short Pass");
            request.setEmail("shortpass@example.com");
            request.setPhone("9111111111");
            request.setPassword("abc");

            AuthResponse response = authService.register(request);

            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).containsIgnoringCase("password");
            verify(customerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject registration with name shorter than 2 characters")
        void registerShortName() {
            RegisterRequest request = new RegisterRequest();
            request.setName("X");
            request.setEmail("x@example.com");
            request.setPhone("9111111111");
            request.setPassword("password123");

            AuthResponse response = authService.register(request);

            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).containsIgnoringCase("name");
        }

        @Test
        @DisplayName("Should reject registration with invalid phone number")
        void registerInvalidPhone() {
            RegisterRequest request = new RegisterRequest();
            request.setName("Valid Name");
            request.setEmail("validphone@example.com");
            request.setPhone("abc");
            request.setPassword("password123");

            AuthResponse response = authService.register(request);

            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).containsIgnoringCase("phone");
        }

        @Test
        @DisplayName("Should handle null registration request gracefully")
        void registerNullRequest() {
            AuthResponse response = authService.register(null);

            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).isNotBlank();
        }
    }

    // =========================================================================
    // LOGIN TESTS
    // =========================================================================

    @Nested
    @DisplayName("login() — user authentication")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with correct BCrypt-hashed password")
        void loginSuccessWithBCryptPassword() {
            // Arrange
            String plainPassword = "mySecurePass";
            String hashed = passwordEncoder.encode(plainPassword);

            Customer customer = new Customer();
            customer.setId(42L);
            customer.setName("Ravi Kumar");
            customer.setEmail("ravi@example.com");
            customer.setPhone("9876500000");
            customer.setPassword(hashed);
            customer.setRole("CUSTOMER");

            when(customerRepository.findByEmail("ravi@example.com")).thenReturn(Optional.of(customer));

            LoginRequest request = new LoginRequest();
            request.setEmail("ravi@example.com");
            request.setPassword(plainPassword);

            // Act
            AuthResponse response = authService.login(request);

            // Assert
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.isRegistered()).isTrue();
            assertThat(response.getCustomerId()).isEqualTo(42L);
            assertThat(response.getName()).isEqualTo("Ravi Kumar");
            assertThat(response.getEmail()).isEqualTo("ravi@example.com");
            assertThat(response.getMessage()).contains("Welcome back");
        }

        @Test
        @DisplayName("Should reject login with wrong password")
        void loginWrongPassword() {
            // Arrange
            String hashed = passwordEncoder.encode("correctPassword");

            Customer customer = new Customer();
            customer.setId(10L);
            customer.setEmail("user@example.com");
            customer.setPassword(hashed);

            when(customerRepository.findByEmail("user@example.com")).thenReturn(Optional.of(customer));

            LoginRequest request = new LoginRequest();
            request.setEmail("user@example.com");
            request.setPassword("wrongPassword");

            // Act
            AuthResponse response = authService.login(request);

            // Assert
            assertThat(response.isSuccess()).isFalse();
            assertThat(response.isRegistered()).isTrue();
            assertThat(response.getMessage()).containsIgnoringCase("incorrect password");
        }

        @Test
        @DisplayName("Should return not-registered when email is unknown")
        void loginUnknownEmail() {
            when(customerRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            LoginRequest request = new LoginRequest();
            request.setEmail("ghost@example.com");
            request.setPassword("anyPass");

            AuthResponse response = authService.login(request);

            assertThat(response.isSuccess()).isFalse();
            assertThat(response.isRegistered()).isFalse();
            assertThat(response.getMessage()).containsIgnoringCase("no registered account");
        }

        @Test
        @DisplayName("Should migrate legacy plain-text password to BCrypt on successful login")
        void loginMigratesPlainTextToBCrypt() {
            // Arrange — stored password is plain text (legacy)
            String plainPassword = "legacyPass";
            Customer customer = new Customer();
            customer.setId(5L);
            customer.setName("Legacy User");
            customer.setEmail("legacy@example.com");
            customer.setPhone("9000000001");
            customer.setPassword(plainPassword); // NOT a BCrypt hash
            customer.setRole("CUSTOMER");

            when(customerRepository.findByEmail("legacy@example.com")).thenReturn(Optional.of(customer));
            when(customerRepository.update(any(Customer.class))).thenReturn(1); // returns rows updated

            LoginRequest request = new LoginRequest();
            request.setEmail("legacy@example.com");
            request.setPassword(plainPassword);

            // Act
            AuthResponse response = authService.login(request);

            // Assert — login should succeed AND password should be migrated
            assertThat(response.isSuccess()).isTrue();

            ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
            verify(customerRepository).update(captor.capture());

            String newStoredPassword = captor.getValue().getPassword();
            assertThat(newStoredPassword).startsWith("$2a$"); // Now BCrypt!
            assertThat(passwordEncoder.matches(plainPassword, newStoredPassword)).isTrue();
        }

        @Test
        @DisplayName("Should reject login when email is blank")
        void loginBlankEmail() {
            LoginRequest request = new LoginRequest();
            request.setEmail("   ");
            request.setPassword("somePass");

            AuthResponse response = authService.login(request);

            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).containsIgnoringCase("email");
        }

        @Test
        @DisplayName("Should reject login when password is blank")
        void loginBlankPassword() {
            LoginRequest request = new LoginRequest();
            request.setEmail("test@example.com");
            request.setPassword("");

            AuthResponse response = authService.login(request);

            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).containsIgnoringCase("password");
        }

        @Test
        @DisplayName("Should normalise email to lowercase before lookup")
        void loginEmailNormalisation() {
            // Arrange
            String hashed = passwordEncoder.encode("pass123");
            Customer customer = new Customer();
            customer.setId(99L);
            customer.setName("Test User");
            customer.setEmail("test@example.com");
            customer.setPassword(hashed);
            customer.setRole("CUSTOMER");

            when(customerRepository.findByEmail("test@example.com")).thenReturn(Optional.of(customer));

            LoginRequest request = new LoginRequest();
            request.setEmail("TEST@EXAMPLE.COM"); // uppercase
            request.setPassword("pass123");

            AuthResponse response = authService.login(request);

            assertThat(response.isSuccess()).isTrue();
            verify(customerRepository).findByEmail("test@example.com");
        }

        @Test
        @DisplayName("Should handle null login request gracefully")
        void loginNullRequest() {
            AuthResponse response = authService.login(null);

            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).isNotBlank();
        }
    }

    // =========================================================================
    // isEmailRegistered() TESTS
    // =========================================================================

    @Nested
    @DisplayName("isEmailRegistered() — email lookup")
    class IsEmailRegisteredTests {

        @Test
        @DisplayName("Should return true for a known registered email")
        void registeredEmail() {
            Customer customer = new Customer();
            customer.setEmail("known@example.com");
            when(customerRepository.findByEmail("known@example.com")).thenReturn(Optional.of(customer));

            assertThat(authService.isEmailRegistered("known@example.com")).isTrue();
        }

        @Test
        @DisplayName("Should return false for an unknown email")
        void unknownEmail() {
            when(customerRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            assertThat(authService.isEmailRegistered("nobody@example.com")).isFalse();
        }

        @Test
        @DisplayName("Should return false for null email without throwing")
        void nullEmail() {
            assertThat(authService.isEmailRegistered(null)).isFalse();
            verify(customerRepository, never()).findByEmail(any());
        }

        @Test
        @DisplayName("Should return false for blank email without throwing")
        void blankEmail() {
            assertThat(authService.isEmailRegistered("   ")).isFalse();
            verify(customerRepository, never()).findByEmail(any());
        }
    }
}
