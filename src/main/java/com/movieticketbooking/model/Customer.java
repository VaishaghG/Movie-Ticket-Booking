package com.movieticketbooking.model;

/**
 * Demonstrates:
 * - OOP: Inheritance (extends Person -> extends BaseEntity)
 * - OOP: Polymorphism & Method Overriding (@Override getRoleDescription, toString)
 * - OOP: Super constructor invocation
 */
public class Customer extends Person {

    public Customer() {
        super();
    }

    public Customer(Long customerId, String name, String email, String phone) {
        super(customerId, name, email, phone);
    }

    public Customer(Long customerId, String name, String email, String phone, String password) {
        super(customerId, name, email, phone, password);
    }

    public Customer(Long customerId, String name, String email, String phone, String password, String role) {
        super(customerId, name, email, phone, password, role);
    }

    // Convenience alias for customerId matching prompt
    public Long getCustomerId() {
        return getId();
    }

    public void setCustomerId(Long customerId) {
        setId(customerId);
    }

    @Override
    public String getRoleDescription() {
        return "Customer: " + ("ADMIN".equalsIgnoreCase(getRole()) ? "Administrator" : "Ticket purchaser");
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", phone='" + getPhone() + '\'' +
                ", role='" + getRole() + '\'' +
                '}';
    }
}
