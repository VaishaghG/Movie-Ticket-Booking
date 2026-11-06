package com.movieticketbooking.service;

import com.movieticketbooking.dto.AuthResponse;
import com.movieticketbooking.dto.LoginRequest;
import com.movieticketbooking.dto.RegisterRequest;
import com.movieticketbooking.model.Customer;
import com.movieticketbooking.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Implementation of AuthService for customer authentication and registration.
 * Validates user existence, verifies passwords with BCrypt hashing, migrates
 * legacy plain-text passwords,
 * and manages new user onboarding.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+ -]{7,15}$");

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().isBlank()) {
            return AuthResponse.error("Email is required for authentication.", false);
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            return AuthResponse.error("Password is required for authentication.", false);
        }

        String email = request.getEmail().trim().toLowerCase();
        Optional<Customer> optionalCustomer = customerRepository.findByEmail(email);

        if (optionalCustomer.isEmpty()) {
            return AuthResponse.notRegistered(
                    "No registered account found with email: " + email + ". Please sign up to create an account.");
        }

        Customer customer = optionalCustomer.get();
        String storedPassword = customer.getPassword();

        // If no password was set, reject rather than silently accepting a default
        // password
        if (storedPassword == null || storedPassword.isBlank()) {
            return AuthResponse.error("No password configured for this account. Please re-register or contact support.",
                    true);
        }

        String providedPassword = request.getPassword();
        boolean passwordMatches = false;
        boolean needsMigration = false;

        // Check if stored password is a BCrypt hash (starts with $2a$, $2b$, or $2y$)
        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$")
                || storedPassword.startsWith("$2y$")) {
            passwordMatches = passwordEncoder.matches(providedPassword, storedPassword);
        } else {
            // Safe migration strategy for legacy plain-text passwords:
            // Verify against plain text; if matched, migrate immediately to BCrypt
            if (storedPassword.equals(providedPassword)) {
                passwordMatches = true;
                needsMigration = true;
            }
        }

        if (!passwordMatches) {
            return AuthResponse.error("Incorrect password for account: " + customer.getEmail() + ". Please try again.",
                    true);
        }

        // Migrate plain-text password to BCrypt hash
        if (needsMigration) {
            logger.info("Migrating plain-text password to BCrypt hash for customer: {}", customer.getEmail());
            String hashed = passwordEncoder.encode(providedPassword);
            customer.setPassword(hashed);
            customerRepository.update(customer);
        }

        String role = customer.getRole() != null && !customer.getRole().isBlank() ? customer.getRole() : "CUSTOMER";

        return AuthResponse.success(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                role,
                "Welcome back, " + customer.getName() + "!");
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request == null) {
            return AuthResponse.error("Registration details are missing.", false);
        }

        String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";
        if (email.isBlank() || !EMAIL_PATTERN.matcher(email).matches()) {
            return AuthResponse.error("A valid email address is required.", false);
        }

        String name = request.getName() != null ? request.getName().trim() : "";
        if (name.length() < 2) {
            return AuthResponse.error("Full name must be at least 2 characters.", false);
        }

        String phone = request.getPhone() != null ? request.getPhone().trim() : "";
        if (phone.isBlank() || !PHONE_PATTERN.matcher(phone).matches()) {
            return AuthResponse.error("A valid phone number (7-15 digits) is required.", false);
        }

        String password = request.getPassword() != null ? request.getPassword() : "";
        if (password.length() < 6) {
            return AuthResponse.error("Password must be at least 6 characters.", false);
        }

        Optional<Customer> existing = customerRepository.findByEmail(email);
        if (existing.isPresent()) {
            return AuthResponse.error("An account with email " + email + " already exists. Please sign in instead.",
                    true);
        }

        // Never store new passwords as plain text: hash with BCrypt
        String hashedPassword = passwordEncoder.encode(password);

        Customer customer = new Customer();
        customer.setName(name);
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setPassword(hashedPassword);
        customer.setRole("CUSTOMER");

        Customer saved = customerRepository.save(customer);

        return AuthResponse.success(
                saved.getId(),
                saved.getName(),
                saved.getEmail(),
                saved.getPhone(),
                saved.getRole(),
                "Account created successfully! Welcome to CinePass, " + saved.getName() + ".");
    }

    @Override
    public boolean isEmailRegistered(String email) {
        if (email == null || email.isBlank())
            return false;
        return customerRepository.findByEmail(email.trim().toLowerCase()).isPresent();
    }
}
