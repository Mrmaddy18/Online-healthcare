package com.healthcare.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete subclass representing a Patient profile linked to a User account.
 * Uses List<String> collection to maintain chronological medical history entries.
 */
public class Patient extends User {
    private Integer patientId;
    private LocalDate dateOfBirth;
    private String contact;
    private final List<String> medicalHistoryLogs = new ArrayList<>();

    public Patient() {
        super();
        setRole(Role.PATIENT);
    }

    public Patient(Integer userId, Integer patientId, String name, String email,
                   String passwordHash, LocalDate dateOfBirth, String contact) {
        super(userId, name, email, passwordHash, Role.PATIENT);
        this.patientId = patientId;
        this.dateOfBirth = dateOfBirth;
        this.contact = contact;
    }

    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public List<String> getMedicalHistoryLogs() {
        return medicalHistoryLogs;
    }

    public void addMedicalHistoryEntry(String entry) {
        if (entry != null && !entry.isBlank()) {
            medicalHistoryLogs.add(entry.trim());
        }
    }

    @Override
    public String getDashboardViewPath() {
        return "/WEB-INF/views/patient-dashboard.jsp";
    }

    @Override
    public String getRoleSummary() {
        return "Patient Portal: book clinical slots, track confirmation statuses, and inspect personal medical history.";
    }
}
