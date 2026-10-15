package com.healthcare.model;

/**
 * Lifecycle states of a clinical appointment.
 */
public enum AppointmentStatus {
    PENDING,
    CONFIRMED,
    RESCHEDULED,
    CANCELLED,
    COMPLETED;

    public static AppointmentStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            return PENDING;
        }
        return AppointmentStatus.valueOf(value.trim().toUpperCase());
    }
}
