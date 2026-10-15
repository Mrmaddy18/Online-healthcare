package com.healthcare.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Abstract base class representing an authenticated user in the system.
 * Demonstrates OOP Encapsulation, Abstraction, Inheritance, and Polymorphism.
 */
public abstract class User implements Identifiable<Integer>, Auditable {
    private Integer userId;
    private String name;
    private String email;
    private String passwordHash;
    private Role role;
    private LocalDateTime createdAt;

    protected User() {
        this.createdAt = LocalDateTime.now();
    }

    protected User(Integer userId, String name, String email, String passwordHash, Role role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Polymorphic method overridden by Admin, Doctor, and Patient subclasses
     * to route authenticated sessions to their respective JSP views.
     */
    public abstract String getDashboardViewPath();

    /**
     * Polymorphic method returning role-specific permission descriptions.
     */
    public abstract String getRoleSummary();

    @Override
    public Integer getId() {
        return userId;
    }

    @Override
    public void setId(Integer id) {
        this.userId = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("User name cannot be empty.");
        }
        this.name = name.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Valid email address is required.");
        }
        this.email = email.trim().toLowerCase();
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toAuditSummary() {
        return String.format("User[id=%d, name=%s, email=%s, role=%s]", userId, name, email, role);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId) && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, email);
    }
}
