package com.movieticketbooking.dto;

/**
 * Response DTO for authentication operations (Login, Register, Status Check).
 */
public class AuthResponse {

    private boolean success;
    private boolean registered;
    private String message;
    private Long customerId;
    private String name;
    private String email;
    private String phone;
    private String role;

    public AuthResponse() {
    }

    public static AuthResponse success(Long customerId, String name, String email, String phone, String message) {
        return success(customerId, name, email, phone, "CUSTOMER", message);
    }

    public static AuthResponse success(Long customerId, String name, String email, String phone, String role, String message) {
        AuthResponse response = new AuthResponse();
        response.setSuccess(true);
        response.setRegistered(true);
        response.setCustomerId(customerId);
        response.setName(name);
        response.setEmail(email);
        response.setPhone(phone);
        response.setRole(role != null ? role : "CUSTOMER");
        response.setMessage(message);
        return response;
    }

    public static AuthResponse notRegistered(String message) {
        AuthResponse response = new AuthResponse();
        response.setSuccess(false);
        response.setRegistered(false);
        response.setMessage(message);
        return response;
    }

    public static AuthResponse error(String message, boolean registered) {
        AuthResponse response = new AuthResponse();
        response.setSuccess(false);
        response.setRegistered(registered);
        response.setMessage(message);
        return response;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
