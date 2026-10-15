package com.healthcare.dao;

import com.healthcare.model.Appointment;
import com.healthcare.model.AppointmentStatus;
import com.healthcare.util.DBConnection;
import com.healthcare.util.HealthcareException;
import com.healthcare.util.SlotConflictException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * JDBC Data Access Object for clinical appointments.
 * Implements PreparedStatement CRUD, slot conflict verification, and ACID Transactions.
 */
public class AppointmentDAO implements GenericDAO<Appointment, Integer> {

    /**
     * Books an appointment inside an explicit JDBC Transaction.
     * Checks if the doctor already has an active appointment at the requested slot;
     * if free, inserts the booking and commits; otherwise rolls back and throws SlotConflictException.
     */
    @Override
    public Appointment save(Appointment appt) throws HealthcareException {
        String conflictCheckSql = "SELECT COUNT(*) FROM appointments " +
                                  "WHERE doctor_id = ? AND date_time = ? AND status <> 'CANCELLED' FOR UPDATE";
        String insertSql = "INSERT INTO appointments (patient_id, doctor_id, date_time, status, reason, doctor_notes) " +
                           "VALUES (?, ?, ?, ?, ?, ?)";

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false); // Start ACID Transaction

            try (PreparedStatement psCheck = con.prepareStatement(conflictCheckSql)) {
                psCheck.setInt(1, appt.getDoctorId());
                psCheck.setTimestamp(2, Timestamp.valueOf(appt.getDateTime()));
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        con.rollback();
                        throw new SlotConflictException("Selected time slot is already booked for this doctor.");
                    }
                }
            }

            try (PreparedStatement psInsert = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                psInsert.setInt(1, appt.getPatientId());
                psInsert.setInt(2, appt.getDoctorId());
                psInsert.setTimestamp(3, Timestamp.valueOf(appt.getDateTime()));
                psInsert.setString(4, appt.getStatus().name());
                psInsert.setString(5, appt.getReason());
                psInsert.setString(6, appt.getDoctorNotes() != null ? appt.getDoctorNotes() : "");
                psInsert.executeUpdate();

                try (ResultSet keys = psInsert.getGeneratedKeys()) {
                    if (keys.next()) {
                        appt.setAppointmentId(keys.getInt(1));
                    }
                }
            }

            con.commit(); // Commit transaction
            return appt;
        } catch (SlotConflictException ex) {
            throw ex;
        } catch (SQLException ex) {
            DBConnection.rollbackQuietly(con);
            throw new HealthcareException("Database transaction failed while booking appointment.", ex);
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
    public Optional<Appointment> findById(Integer id) throws HealthcareException {
        String sql = baseSelectJoinSql() + " WHERE a.appointment_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToAppointment(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException ex) {
            throw new HealthcareException("Error retrieving appointment by ID.", ex);
        }
    }

    @Override
    public List<Appointment> findAll() throws HealthcareException {
        String sql = baseSelectJoinSql() + " ORDER BY a.date_time DESC";
        List<Appointment> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowToAppointment(rs));
            }
            return list;
        } catch (SQLException ex) {
            throw new HealthcareException("Error loading all appointments.", ex);
        }
    }

    public List<Appointment> findByPatientId(int patientId) throws HealthcareException {
        // Matches Slide 8 parameterized query pattern
        String sql = baseSelectJoinSql() + " WHERE a.patient_id = ? ORDER BY a.date_time DESC";
        List<Appointment> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToAppointment(rs));
                }
            }
            return list;
        } catch (SQLException ex) {
            throw new HealthcareException("Error loading patient appointments.", ex);
        }
    }

    public List<Appointment> findByDoctorId(int doctorId) throws HealthcareException {
        String sql = baseSelectJoinSql() + " WHERE a.doctor_id = ? ORDER BY a.date_time ASC";
        List<Appointment> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToAppointment(rs));
                }
            }
            return list;
        } catch (SQLException ex) {
            throw new HealthcareException("Error loading doctor schedule appointments.", ex);
        }
    }

    public boolean updateStatusAndNotes(int appointmentId, AppointmentStatus newStatus, String notes) throws HealthcareException {
        String sql = "UPDATE appointments SET status = ?, doctor_notes = ? WHERE appointment_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newStatus.name());
            ps.setString(2, notes != null ? notes : "");
            ps.setInt(3, appointmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new HealthcareException("Failed to update appointment status.", ex);
        }
    }

    public boolean rescheduleAppointment(int appointmentId, LocalDateTime newDateTime) throws HealthcareException {
        String sql = "UPDATE appointments SET date_time = ?, status = 'RESCHEDULED' WHERE appointment_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(newDateTime));
            ps.setInt(2, appointmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new HealthcareException("Failed to reschedule appointment.", ex);
        }
    }

    @Override
    public boolean update(Appointment appt) throws HealthcareException {
        return updateStatusAndNotes(appt.getAppointmentId(), appt.getStatus(), appt.getDoctorNotes());
    }

    @Override
    public boolean deleteById(Integer id) throws HealthcareException {
        String sql = "DELETE FROM appointments WHERE appointment_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new HealthcareException("Failed to delete appointment.", ex);
        }
    }

    /**
     * Aggregates appointment counts by status into a Map<String, Integer> for Admin audit metrics.
     */
    public Map<String, Integer> getStatusMetrics() throws HealthcareException {
        Map<String, Integer> metrics = new LinkedHashMap<>();
        for (AppointmentStatus st : AppointmentStatus.values()) {
            metrics.put(st.name(), 0);
        }
        String sql = "SELECT status, COUNT(*) AS cnt FROM appointments GROUP BY status";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                metrics.put(rs.getString("status"), rs.getInt("cnt"));
            }
            return metrics;
        } catch (SQLException ex) {
            throw new HealthcareException("Failed to compute appointment status metrics.", ex);
        }
    }

    private String baseSelectJoinSql() {
        return "SELECT a.appointment_id, a.patient_id, a.doctor_id, a.date_time, a.status, a.reason, a.doctor_notes, " +
               "up.name AS patient_name, ud.name AS doctor_name, d.specialization " +
               "FROM appointments a " +
               "JOIN patients p ON a.patient_id = p.patient_id " +
               "JOIN users up ON p.user_id = up.user_id " +
               "JOIN doctors d ON a.doctor_id = d.doctor_id " +
               "JOIN users ud ON d.user_id = ud.user_id";
    }

    private Appointment mapRowToAppointment(ResultSet rs) throws SQLException {
        Appointment appt = new Appointment(
                rs.getInt("appointment_id"),
                rs.getInt("patient_id"),
                rs.getInt("doctor_id"),
                rs.getTimestamp("date_time").toLocalDateTime(),
                AppointmentStatus.fromString(rs.getString("status")),
                rs.getString("reason")
        );
        appt.setPatientName(rs.getString("patient_name"));
        appt.setDoctorName(rs.getString("doctor_name"));
        appt.setSpecialization(rs.getString("specialization"));
        appt.setDoctorNotes(rs.getString("doctor_notes"));
        return appt;
    }
}
