package com.healthcare.model;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Concrete subclass representing a Doctor profile linked to a User account.
 * Uses Set<String> collection to manage unique availability time slots.
 */
public class Doctor extends User {
    private Integer doctorId;
    private String specialization;
    private String availability;
    private final Set<String> availableTimeSlots = new LinkedHashSet<>();

    public Doctor() {
        super();
        setRole(Role.DOCTOR);
    }

    public Doctor(Integer userId, Integer doctorId, String name, String email,
                  String passwordHash, String specialization, String availability) {
        super(userId, name, email, passwordHash, Role.DOCTOR);
        this.doctorId = doctorId;
        this.specialization = specialization;
        this.availability = availability;
        initializeDefaultSlots();
    }

    private void initializeDefaultSlots() {
        availableTimeSlots.add("09:00");
        availableTimeSlots.add("10:00");
        availableTimeSlots.add("11:30");
        availableTimeSlots.add("14:00");
        availableTimeSlots.add("16:00");
    }

    public Integer getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Integer doctorId) {
        this.doctorId = doctorId;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public Set<String> getAvailableTimeSlots() {
        return availableTimeSlots;
    }

    public void addAvailableSlot(String slot) {
        if (slot != null && !slot.isBlank()) {
            availableTimeSlots.add(slot.trim());
        }
    }

    @Override
    public String getDashboardViewPath() {
        return "/WEB-INF/views/doctor-dashboard.jsp";
    }

    @Override
    public String getRoleSummary() {
        return "Doctor Portal (" + specialization + "): manage availability slots, review bookings, and update clinical records.";
    }
}
