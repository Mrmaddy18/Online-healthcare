package com.healthcare.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Security utility for SHA-256 password hashing and server-side input sanitization.
 */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    public static String hashPassword(String plainText) {
        if (plainText == null || plainText.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(plainText.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encoded.length);
            for (byte b : encoded) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm not available.", ex);
        }
    }

    public static boolean verifyPassword(String plainText, String expectedHash) {
        if (plainText == null || expectedHash == null) {
            return false;
        }
        return hashPassword(plainText).equalsIgnoreCase(expectedHash.trim());
    }

    public static String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }
        return input.trim()
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}
