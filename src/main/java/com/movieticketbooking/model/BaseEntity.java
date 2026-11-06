package com.movieticketbooking.model;

import java.time.LocalDateTime;

/**
 * Demonstrates:
 * - OOP: Abstraction (abstract class)
 * - OOP: Encapsulation (private fields with public getters/setters)
 * - OOP: Inheritance (base class for all domain entities)
 * - Java Constructors and 'this' keyword
 */
public abstract class BaseEntity {

    private Long id;
    private LocalDateTime createdAt;

    // Default Constructor
    public BaseEntity() {
        this.createdAt = LocalDateTime.now();
    }

    // Overloaded Constructor
    public BaseEntity(Long id) {
        this();
        this.id = id;
    }

    public BaseEntity(Long id, LocalDateTime createdAt) {
        this.id = id;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
