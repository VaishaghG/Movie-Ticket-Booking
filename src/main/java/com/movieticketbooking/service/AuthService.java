package com.movieticketbooking.service;

import com.movieticketbooking.dto.AuthResponse;
import com.movieticketbooking.dto.LoginRequest;
import com.movieticketbooking.dto.RegisterRequest;

/**
 * Service interface for customer authentication and registration.
 */
public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse register(RegisterRequest request);

    boolean isEmailRegistered(String email);
}
