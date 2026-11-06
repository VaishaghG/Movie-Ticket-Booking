package com.movieticketbooking.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor for enforcing session-based authentication and role-based authorization.
 * Checks for @RequireAuth and @RequireAdmin annotations on controllers and handler methods.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        boolean requireAdmin = handlerMethod.hasMethodAnnotation(RequireAdmin.class)
                || handlerMethod.getBeanType().isAnnotationPresent(RequireAdmin.class);

        boolean requireAuth = requireAdmin
                || handlerMethod.hasMethodAnnotation(RequireAuth.class)
                || handlerMethod.getBeanType().isAnnotationPresent(RequireAuth.class);

        if (!requireAuth) {
            return true;
        }

        HttpSession session = request.getSession(false);
        Long customerId = session != null ? (Long) session.getAttribute("CUSTOMER_ID") : null;

        if (customerId == null) {
            String accept = request.getHeader("Accept");
            if (accept != null && accept.contains("text/html") && !request.getRequestURI().startsWith("/api/")) {
                response.sendRedirect("/login.html");
                return false;
            }

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication required. Please sign in to access this resource.\"}");
            return false;
        }

        if (requireAdmin) {
            String role = (String) session.getAttribute("CUSTOMER_ROLE");
            if (!"ADMIN".equalsIgnoreCase(role)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access denied. Administrator privileges required.\"}");
                return false;
            }
        }

        return true;
    }
}
