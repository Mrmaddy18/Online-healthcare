package com.healthcare.util;

/**
 * Thrown when a patient attempts to book an unavailable or already-booked doctor slot.
 */
public class SlotConflictException extends HealthcareException {
    public SlotConflictException(String message) {
        super(message);
    }
}
