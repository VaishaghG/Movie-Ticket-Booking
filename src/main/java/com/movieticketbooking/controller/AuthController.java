package com.movieticketbooking.controller;

import com.movieticketbooking.dto.AuthResponse;
import com.movieticketbooking.dto.LoginRequest;
import com.movieticketbooking.dto.RegisterRequest;
import com.movieticketbooking.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for User Authentication, Registration, and Session Management.
 * Uses jakarta.servlet.http.HttpSession for stateful server-side session tracking.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Authenticate user with email and password.
     * On success, creates a secure server-side HttpSession with session fixation protection.
     * If user does not exist, returns 404 with registered=false.
     * If password is incorrect, returns 401 with registered=true.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        AuthResponse response = authService.login(request);

        if (!response.isRegistered()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // Session fixation protection: invalidate any existing unauthenticated session
        HttpSession oldSession = httpRequest.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        // Create new server-side session and store minimum necessary identity information
        HttpSession newSession = httpRequest.getSession(true);
        newSession.setAttribute("CUSTOMER_ID", response.getCustomerId());
        newSession.setAttribute("CUSTOMER_NAME", response.getName());
        newSession.setAttribute("CUSTOMER_EMAIL", response.getEmail());
        newSession.setAttribute("CUSTOMER_PHONE", response.getPhone());
        newSession.setAttribute("CUSTOMER_ROLE", response.getRole());

        return ResponseEntity.ok(response);
    }

    /**
     * Register a new user with name, email, phone, and password.
     * Passwords are encrypted with BCrypt.
     * If email is already in use, returns 409 Conflict.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (authService.isEmailRegistered(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(AuthResponse.error("An account with email " + request.getEmail() + " is already registered. Please sign in.", true));
        }

        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Check if an email is already registered in the system.
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam String email) {
        boolean registered = authService.isEmailRegistered(email);
        return ResponseEntity.ok(Map.of(
                "email", email,
                "registered", registered
        ));
    }

    /**
     * Return safe authenticated user profile from current server-side HttpSession.
     * Returns 401 Unauthorized if no active session exists.
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("CUSTOMER_ID") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "authenticated", false,
                            "message", "No active authenticated session found. Please sign in."
                    ));
        }

        Map<String, Object> profile = new HashMap<>();
        profile.put("authenticated", true);
        profile.put("customerId", session.getAttribute("CUSTOMER_ID"));
        profile.put("name", session.getAttribute("CUSTOMER_NAME"));
        profile.put("email", session.getAttribute("CUSTOMER_EMAIL"));
        profile.put("phone", session.getAttribute("CUSTOMER_PHONE"));
        profile.put("role", session.getAttribute("CUSTOMER_ROLE"));
        return ResponseEntity.ok(profile);
    }

    /**
     * Invalidate the authenticated user's server-side session and clear cookie.
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        Cookie cookie = new Cookie("JSESSIONID", "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Logged out successfully"
        ));
    }
}
