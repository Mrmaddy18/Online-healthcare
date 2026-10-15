package com.healthcare.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Centralized JDBC Connection Manager for MySQL 8.0 persistence.
 * Supports environment variables and safe transaction rollback helpers.
 */
public final class DBConnection {
    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/healthcare_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "root";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            LOGGER.log(Level.WARNING, "MySQL JDBC Driver not found on classpath; verify pom.xml dependencies.", ex);
        }
    }

    private DBConnection() {
        // Utility class
    }

    public static Connection getConnection() throws SQLException {
        String url = System.getenv().getOrDefault("HEALTHCARE_DB_URL", DEFAULT_URL);
        String user = System.getenv().getOrDefault("HEALTHCARE_DB_USER", DEFAULT_USER);
        String pass = System.getenv().getOrDefault("HEALTHCARE_DB_PASS", DEFAULT_PASSWORD);
        return DriverManager.getConnection(url, user, pass);
    }

    public static void rollbackQuietly(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Failed to rollback JDBC transaction.", ex);
            }
        }
    }

    public static void resetAutoCommit(Connection connection) {
        if (connection != null) {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                LOGGER.log(Level.WARNING, "Failed to restore auto-commit mode.", ex);
            }
        }
    }
}
