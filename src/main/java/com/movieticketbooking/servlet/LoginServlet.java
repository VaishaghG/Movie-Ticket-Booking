package com.movieticketbooking.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movieticketbooking.dto.AuthResponse;
import com.movieticketbooking.dto.LoginRequest;
import com.movieticketbooking.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;

/**
 * Traditional Servlet endpoint for CinePass login.
 * POST accepts a JSON LoginRequest; GET opens the existing login page.
 */
public class LoginServlet extends HttpServlet {
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    public LoginServlet(AuthService authService, ObjectMapper objectMapper) {
        this.authService = authService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/login.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        final LoginRequest loginRequest;
        try {
            loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(), Map.of("success", false, "registered", false,
                    "message", "Invalid JSON login request."));
            return;
        }

        AuthResponse result = authService.login(loginRequest);
        if (!result.isRegistered()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(response.getWriter(), result);
            return;
        }
        if (!result.isSuccess()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(response.getWriter(), result);
            return;
        }

        // Prevent session fixation by replacing any existing session after authentication.
        HttpSession previous = request.getSession(false);
        if (previous != null) previous.invalidate();

        HttpSession session = request.getSession(true);
        session.setAttribute("CUSTOMER_ID", result.getCustomerId());
        session.setAttribute("CUSTOMER_NAME", result.getName());
        session.setAttribute("CUSTOMER_EMAIL", result.getEmail());
        session.setAttribute("CUSTOMER_PHONE", result.getPhone());
        session.setAttribute("CUSTOMER_ROLE", result.getRole());

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), result);
    }
}