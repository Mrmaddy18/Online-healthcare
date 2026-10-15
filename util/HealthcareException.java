package com.healthcare.util;

/**
 * Custom checked exception for clinical workflow and persistence errors.
 * Prevents raw SQL stack traces from leaking to the presentation tier.
 */
public class HealthcareException extends Exception {
    public HealthcareException(String message) {
        super(message);
    }

    public HealthcareException(String message, Throwable cause) {
        super(message, cause);
    }
}
