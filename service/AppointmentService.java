package com.healthcare.service;

import com.healthcare.dao.AppointmentDAO;
import com.healthcare.model.Appointment;
import com.healthcare.model.AppointmentStatus;
import com.healthcare.model.ServiceResult;
import com.healthcare.util.AppointmentNotificationWorker;
import com.healthcare.util.HealthcareException;
import com.healthcare.util.PasswordUtil;
import com.healthcare.util.SlotConflictException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Business service coordinating slot validation, synchronized concurrency locks,
 * JDBC transactions, and asynchronous background notifications.
 */
public class AppointmentService {
    private final AppointmentDAO appointmentDAO;
    private final AppointmentNotificationWorker notificationWorker;

    public AppointmentService() {
        this.appointmentDAO = new AppointmentDAO();
        this.notificationWorker = AppointmentNotificationWorker.getInstance();
    }

    public ServiceResult<Appointment> bookAppointment(int patientId, int doctorId,
                                                      LocalDateTime slotDateTime, String reason) {
        if (slotDateTime == null || slotDateTime.isBefore(LocalDateTime.now())) {
            return ServiceResult.fail("Appointment date and time must be in the future.");
        }
        String sanitizedReason = PasswordUtil.sanitizeInput(reason);
        if (sanitizedReason.isBlank()) {
            return ServiceResult.fail("Clinical reason for visit is mandatory.");
        }

        // Synchronized block prevents concurrent threads from racing on the same slot
        synchronized (notificationWorker.getSlotLock()) {
            try {
                Appointment draft = new Appointment(null, patientId, doctorId,
                        slotDateTime, AppointmentStatus.PENDING, sanitizedReason);
                Appointment saved = appointmentDAO.save(draft);
                notificationWorker.enqueueNotification(saved);
                return ServiceResult.ok("Appointment slot booked and queued for doctor confirmation.", saved);
            } catch (SlotConflictException ex) {
                return ServiceResult.fail(ex.getMessage());
            } catch (HealthcareException ex) {
                return ServiceResult.fail("Unable to complete booking: " + ex.getMessage());
            }
        }
    }

    public ServiceResult<Boolean> updateAppointmentReview(int appointmentId, AppointmentStatus status, String doctorNotes) {
        try {
            String cleanNotes = PasswordUtil.sanitizeInput(doctorNotes);
            boolean updated = appointmentDAO.updateStatusAndNotes(appointmentId, status, cleanNotes);
            if (updated) {
                appointmentDAO.findById(appointmentId).ifPresent(notificationWorker::enqueueNotification);
                return ServiceResult.ok("Appointment status updated to " + status.name() + ".", true);
            }
            return ServiceResult.fail("Appointment record not found.");
        } catch (HealthcareException ex) {
            return ServiceResult.fail("Error updating appointment: " + ex.getMessage());
        }
    }

    public List<Appointment> getAppointmentsForPatient(int patientId) throws HealthcareException {
        List<Appointment> list = appointmentDAO.findByPatientId(patientId);
        Collections.sort(list);
        return list;
    }

    public List<Appointment> getAppointmentsForDoctor(int doctorId) throws HealthcareException {
        List<Appointment> list = appointmentDAO.findByDoctorId(doctorId);
        Collections.sort(list);
        return list;
    }
}
