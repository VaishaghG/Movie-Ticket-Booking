package com.movieticketbooking.controller;

import com.movieticketbooking.dto.BookingResponse;
import com.movieticketbooking.model.enums.BookingStatus;
import com.movieticketbooking.service.BookingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Spring MVC Controller serving the server-rendered JSP user dashboard.
 * Explicitly satisfies syllabus requirement for JSP views and HttpSession tracking.
 */
@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final BookingService bookingService;

    public DashboardController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Renders the customer dashboard view (/WEB-INF/jsp/dashboard.jsp).
     * If unauthenticated, safely redirects to the login page.
     */
    @GetMapping
    public String viewDashboard(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("CUSTOMER_ID") == null) {
            return "redirect:/login.html";
        }

        Long customerId = (Long) session.getAttribute("CUSTOMER_ID");
        String name = (String) session.getAttribute("CUSTOMER_NAME");
        String email = (String) session.getAttribute("CUSTOMER_EMAIL");
        String phone = (String) session.getAttribute("CUSTOMER_PHONE");
        String role = (String) session.getAttribute("CUSTOMER_ROLE");

        List<BookingResponse> customerBookings = bookingService.getBookingsByCustomerId(customerId);

        long confirmedCount = customerBookings.stream()
                .filter(b -> b.getBookingStatus() == BookingStatus.CONFIRMED)
                .count();

        double totalSpent = customerBookings.stream()
                .filter(b -> b.getBookingStatus() == BookingStatus.CONFIRMED)
                .mapToDouble(BookingResponse::getTotalAmount)
                .sum();
        totalSpent = Math.round(totalSpent * 100.0) / 100.0;

        model.addAttribute("customerId", customerId);
        model.addAttribute("customerName", name != null ? name : "CinePass Guest");
        model.addAttribute("customerEmail", email != null ? email : "");
        model.addAttribute("customerPhone", phone != null ? phone : "");
        model.addAttribute("customerRole", role != null ? role : "CUSTOMER");
        model.addAttribute("bookings", customerBookings);
        model.addAttribute("totalBookings", customerBookings.size());
        model.addAttribute("confirmedBookings", confirmedCount);
        model.addAttribute("totalSpent", totalSpent);

        return "dashboard";
    }
}
