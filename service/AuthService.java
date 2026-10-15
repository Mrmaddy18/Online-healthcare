package com.healthcare.service;

import com.healthcare.dao.UserDAO;
import com.healthcare.model.ServiceResult;
import com.healthcare.model.User;
import com.healthcare.util.HealthcareException;
import com.healthcare.util.PasswordUtil;

import java.util.Optional;

/**
 * Business service layer for authentication, credential verification, and user registration.
 */
public class AuthService {
    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public ServiceResult<User> authenticate(String email, String plainPassword) {
        if (email == null || email.isBlank() || plainPassword == null || plainPassword.isBlank()) {
            return ServiceResult.fail("Email and password are required.");
        }
        try {
            Optional<User> userOpt = userDAO.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ServiceResult.fail("Invalid email or password.");
            }
            User user = userOpt.get();
            if (!PasswordUtil.verifyPassword(plainPassword, user.getPasswordHash())) {
                return ServiceResult.fail("Invalid email or password.");
            }
            return ServiceResult.ok("Authentication successful.", user);
        } catch (HealthcareException ex) {
            return ServiceResult.fail("Authentication service temporarily unavailable: " + ex.getMessage());
        }
    }

    public ServiceResult<User> registerUser(User newUser, String rawPassword) {
        try {
            newUser.setPasswordHash(PasswordUtil.hashPassword(rawPassword));
            User saved = userDAO.save(newUser);
            return ServiceResult.ok("User registered successfully.", saved);
        } catch (HealthcareException ex) {
            return ServiceResult.fail(ex.getMessage());
        }
    }
}
