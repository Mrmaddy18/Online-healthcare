package com.healthcare.model;

/**
 * Enumeration of system access roles matching MySQL users.role ENUM.
 */
public enum Role {
    ADMIN,
    DOCTOR,
    PATIENT;

    public static Role fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Role value cannot be null or blank.");
        }
        return Role.valueOf(value.trim().toUpperCase());
    }
}
