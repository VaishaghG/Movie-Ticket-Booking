package com.movieticketbooking.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movieticketbooking.dto.AuthResponse;
import com.movieticketbooking.dto.RegisterRequest;
import com.movieticketbooking.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/** Traditional Servlet endpoint for CinePass customer registration. */
public class RegisterServlet extends HttpServlet {
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    public RegisterServlet(AuthService authService, ObjectMapper objectMapper) {
        this.authService = authService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/signup.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        final RegisterRequest registerRequest;
        try {
            registerRequest = objectMapper.readValue(request.getInputStream(), RegisterRequest.class);
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(), Map.of("success", false, "registered", false,
                    "message", "Invalid JSON registration request."));
            return;
        }

        if (registerRequest.getEmail() != null && authService.isEmailRegistered(registerRequest.getEmail())) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            objectMapper.writeValue(response.getWriter(), AuthResponse.error(
                    "An account with this email is already registered. Please sign in.", true));
            return;
        }

        AuthResponse result = authService.register(registerRequest);
        if (result.isSuccess()) {
            response.setStatus(HttpServletResponse.SC_CREATED);
        } else if (result.isRegistered()) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        objectMapper.writeValue(response.getWriter(), result);
    }
}
