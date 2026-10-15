package com.healthcare.dao;

import com.healthcare.model.Admin;
import com.healthcare.model.Doctor;
import com.healthcare.model.Patient;
import com.healthcare.model.Role;
import com.healthcare.model.User;
import com.healthcare.util.DBConnection;
import com.healthcare.util.HealthcareException;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * JDBC Data Access Object for User, Doctor, and Patient entities.
 * Uses PreparedStatement for SQL injection prevention and ACID Transactions
 * when creating multi-table user + profile records.
 */
public class UserDAO implements GenericDAO<User, Integer> {

    public Optional<User> findByEmail(String email) throws HealthcareException {
        String sql = "SELECT u.user_id, u.name, u.email, u.password_hash, u.role, " +
                     "d.doctor_id, d.specialization, d.availability, " +
                     "p.patient_id, p.date_of_birth, p.contact, p.medical_history " +
                     "FROM users u " +
                     "LEFT JOIN doctors d ON u.user_id = d.user_id " +
                     "LEFT JOIN patients p ON u.user_id = p.user_id " +
                     "WHERE u.email = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException ex) {
            throw new HealthcareException("Database error while looking up user by email.", ex);
        }
    }

    /**
     * Multi-table ACID Transaction: Inserts into `users` and then into `doctors` or `patients`
     * atomically using setAutoCommit(false), commit(), and rollback().
     */
    @Override
    public User save(User user) throws HealthcareException {
        String insertUserSql = "INSERT INTO users (name, email, password_hash, role) VALUES (?, ?, ?, ?)";
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false); // Begin explicit JDBC transaction

            try (PreparedStatement psUser = con.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                psUser.setString(1, user.getName());
                psUser.setString(2, user.getEmail());
                psUser.setString(3, user.getPasswordHash());
                psUser.setString(4, user.getRole().name());
                psUser.executeUpdate();

                try (ResultSet keys = psUser.getGeneratedKeys()) {
                    if (keys.next()) {
                        user.setUserId(keys.getInt(1));
                    } else {
                        throw new SQLException("Failed to retrieve generated user_id.");
                    }
                }
            }

            if (user instanceof Doctor) {
                Doctor doc = (Doctor) user;
                String insertDocSql = "INSERT INTO doctors (user_id, specialization, availability) VALUES (?, ?, ?)";
                try (PreparedStatement psDoc = con.prepareStatement(insertDocSql, Statement.RETURN_GENERATED_KEYS)) {
                    psDoc.setInt(1, user.getUserId());
                    psDoc.setString(2, doc.getSpecialization());
                    psDoc.setString(3, doc.getAvailability());
                    psDoc.executeUpdate();
                    try (ResultSet docKeys = psDoc.getGeneratedKeys()) {
                        if (docKeys.next()) {
                            doc.setDoctorId(docKeys.getInt(1));
                        }
                    }
                }
            } else if (user instanceof Patient) {
                Patient pat = (Patient) user;
                String insertPatSql = "INSERT INTO patients (user_id, date_of_birth, contact, medical_history) VALUES (?, ?, ?, ?)";
                try (PreparedStatement psPat = con.prepareStatement(insertPatSql, Statement.RETURN_GENERATED_KEYS)) {
                    psPat.setInt(1, user.getUserId());
                    psPat.setDate(2, Date.valueOf(pat.getDateOfBirth() != null ? pat.getDateOfBirth() : LocalDate.of(2000, 1, 1)));
                    psPat.setString(3, pat.getContact());
                    String history = pat.getMedicalHistoryLogs().isEmpty() ? "Initial registration." : String.join("; ", pat.getMedicalHistoryLogs());
                    psPat.setString(4, history);
                    psPat.executeUpdate();
                    try (ResultSet patKeys = psPat.getGeneratedKeys()) {
                        if (patKeys.next()) {
                            pat.setPatientId(patKeys.getInt(1));
                        }
                    }
                }
            }

            con.commit(); // Commit atomic transaction
            return user;
        } catch (SQLException ex) {
            DBConnection.rollbackQuietly(con);
            throw new HealthcareException("Transaction rolled back while registering user profile.", ex);
        } finally {
            DBConnection.resetAutoCommit(con);
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    @Override
    public Optional<User> findById(Integer id) throws HealthcareException {
        String sql = "SELECT u.user_id, u.name, u.email, u.password_hash, u.role, " +
                     "d.doctor_id, d.specialization, d.availability, " +
                     "p.patient_id, p.date_of_birth, p.contact, p.medical_history " +
                     "FROM users u " +
                     "LEFT JOIN doctors d ON u.user_id = d.user_id " +
                     "LEFT JOIN patients p ON u.user_id = p.user_id " +
                     "WHERE u.user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException ex) {
            throw new HealthcareException("Failed to fetch user by ID.", ex);
        }
    }

    @Override
    public List<User> findAll() throws HealthcareException {
        String sql = "SELECT u.user_id, u.name, u.email, u.password_hash, u.role, " +
                     "d.doctor_id, d.specialization, d.availability, " +
                     "p.patient_id, p.date_of_birth, p.contact, p.medical_history " +
                     "FROM users u " +
                     "LEFT JOIN doctors d ON u.user_id = d.user_id " +
                     "LEFT JOIN patients p ON u.user_id = p.user_id " +
                     "ORDER BY u.user_id ASC";
        List<User> users = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapRowToUser(rs));
            }
            return users;
        } catch (SQLException ex) {
            throw new HealthcareException("Failed to list users.", ex);
        }
    }

    public List<Doctor> findAllDoctors() throws HealthcareException {
        List<Doctor> doctors = new ArrayList<>();
        for (User u : findAll()) {
            if (u instanceof Doctor) {
                doctors.add((Doctor) u);
            }
        }
        return doctors;
    }

    /**
     * Groups all users by Role using a Map<Role, List<User>> collection.
     * Demonstrates Java Collections & Generics for the Admin Dashboard.
     */
    public Map<Role, List<User>> groupUsersByRole() throws HealthcareException {
        Map<Role, List<User>> grouped = new EnumMap<>(Role.class);
        for (Role r : Role.values()) {
            grouped.put(r, new ArrayList<>());
        }
        for (User u : findAll()) {
            grouped.get(u.getRole()).add(u);
        }
        return grouped;
    }

    @Override
    public boolean update(User user) throws HealthcareException {
        String sql = "UPDATE users SET name = ?, email = ? WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setInt(3, user.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new HealthcareException("Failed to update user record.", ex);
        }
    }

    @Override
    public boolean deleteById(Integer id) throws HealthcareException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new HealthcareException("Failed to delete user record.", ex);
        }
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        int userId = rs.getInt("user_id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String hash = rs.getString("password_hash");
        Role role = Role.fromString(rs.getString("role"));

        switch (role) {
            case DOCTOR:
                int docId = rs.getInt("doctor_id");
                String spec = rs.getString("specialization");
                String avail = rs.getString("availability");
                return new Doctor(userId, docId, name, email, hash, spec, avail);
            case PATIENT:
                int patId = rs.getInt("patient_id");
                Date dobSql = rs.getDate("date_of_birth");
                LocalDate dob = dobSql != null ? dobSql.toLocalDate() : LocalDate.of(2000, 1, 1);
                String contact = rs.getString("contact");
                Patient patient = new Patient(userId, patId, name, email, hash, dob, contact);
                String history = rs.getString("medical_history");
                if (history != null && !history.isBlank()) {
                    patient.addMedicalHistoryEntry(history);
                }
                return patient;
            case ADMIN:
            default:
                return new Admin(userId, name, email, hash);
        }
    }
}
