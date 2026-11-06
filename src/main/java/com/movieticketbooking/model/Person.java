package com.movieticketbooking.model;

/**
 * Demonstrates:
 * - OOP: Inheritance (extends BaseEntity)
 * - OOP: Constructor Chaining using 'super'
 * - OOP: Abstraction & Encapsulation
 */
public abstract class Person extends BaseEntity {

    private String name;
    private String email;
    private String phone;
    private String password;
    private String role = "CUSTOMER";

    public Person() {
        super();
    }

    public Person(Long id, String name, String email, String phone) {
        super(id); // invoking superclass constructor
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public Person(Long id, String name, String email, String phone, String password) {
        super(id);
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
    }

    public Person(Long id, String name, String email, String phone, String password, String role) {
        super(id);
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.role = role != null && !role.isBlank() ? role : "CUSTOMER";
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // Abstract method to be overridden by subclasses (Polymorphism)
    public abstract String getRoleDescription();
}
