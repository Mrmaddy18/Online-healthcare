package com.healthcare.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a clinical appointment linking a Patient and a Doctor at a specific DateTime.
 * Implements Identifiable<Integer>, Auditable, and Comparable<Appointment>.
 */
public class Appointment implements Identifiable<Integer>, Auditable, Comparable<Appointment> {
    private Integer appointmentId;
    private Integer patientId;
    private Integer doctorId;
    private String patientName;
    private String doctorName;
    private String specialization;
    private LocalDateTime dateTime;
    private AppointmentStatus status;
    private String reason;
    private String doctorNotes;

    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public Appointment() {
        this.status = AppointmentStatus.PENDING;
    }

    public Appointment(Integer appointmentId, Integer patientId, Integer doctorId,
                       LocalDateTime dateTime, AppointmentStatus status, String reason) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.dateTime = dateTime;
        this.status = status != null ? status : AppointmentStatus.PENDING;
        this.reason = reason;
    }

    @Override
    public Integer getId() {
        return appointmentId;
    }

    @Override
    public void setId(Integer id) {
        this.appointmentId = id;
    }

    public Integer getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Integer appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public Integer getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Integer doctorId) {
        this.doctorId = doctorId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getFormattedDateTime() {
        return dateTime != null ? dateTime.format(DISPLAY_FMT) : "N/A";
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDoctorNotes() {
        return doctorNotes;
    }

    public void setDoctorNotes(String doctorNotes) {
        this.doctorNotes = doctorNotes;
    }

    @Override
    public int compareTo(Appointment other) {
        if (this.dateTime == null || other.dateTime == null) {
            return 0;
        }
        return this.dateTime.compareTo(other.dateTime);
    }

    @Override
    public String toAuditSummary() {
        return String.format("Appointment[id=%d, patientId=%d, doctorId=%d, slot=%s, status=%s]",
                appointmentId, patientId, doctorId, getFormattedDateTime(), status);
    }
}
