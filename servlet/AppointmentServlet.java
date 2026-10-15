package com.healthcare.servlet;

import com.healthcare.model.Appointment;
import com.healthcare.model.AppointmentStatus;
import com.healthcare.model.Patient;
import com.healthcare.model.Role;
import com.healthcare.model.ServiceResult;
import com.healthcare.model.User;
import com.healthcare.service.AppointmentService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * Controller handling appointment creation, confirmation, rescheduling, and cancellation.
 * Enforces role-based authorization checks on every action.
 */
@WebServlet(name = "AppointmentServlet", urlPatterns = {"/appointments"})
public class AppointmentServlet extends HttpServlet {
    private final AppointmentService appointmentService = new AppointmentService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized session.");
            return;
        }

        User user = (User) session.getAttribute("currentUser");
        String action = req.getParameter("action");

        if ("book".equalsIgnoreCase(action)) {
            if (user.getRole() != Role.PATIENT) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Only patients can book appointments.");
                return;
            }
            handlePatientBooking(req, resp, (Patient) user);
        } else if ("review".equalsIgnoreCase(action)) {
            if (user.getRole() != Role.DOCTOR && user.getRole() != Role.ADMIN) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Only doctors or admins can update appointment status.");
                return;
            }
            handleDoctorReview(req, resp);
        } else {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
        }
    }

    private void handlePatientBooking(HttpServletRequest req, HttpServletResponse resp, Patient patient) throws IOException {
        try {
            int doctorId = Integer.parseInt(req.getParameter("doctorId"));
            String dateTimeRaw = req.getParameter("dateTime"); // Format: yyyy-MM-ddTHH:mm
            String reason = req.getParameter("reason");
            LocalDateTime slotTime = LocalDateTime.parse(dateTimeRaw);

            ServiceResult<Appointment> res = appointmentService.bookAppointment(
                    patient.getPatientId(), doctorId, slotTime, reason);

            String param = res.isSuccess() ? "msg=" : "error=";
            resp.sendRedirect(req.getContextPath() + "/dashboard?" + param + java.net.URLEncoder.encode(res.getMessage(), "UTF-8"));
        } catch (NumberFormatException | DateTimeParseException | NullPointerException ex) {
            resp.sendRedirect(req.getContextPath() + "/dashboard?error=Invalid+booking+date+or+doctor+selection");
        }
    }

    private void handleDoctorReview(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int appointmentId = Integer.parseInt(req.getParameter("appointmentId"));
            AppointmentStatus status = AppointmentStatus.fromString(req.getParameter("status"));
            String notes = req.getParameter("doctorNotes");

            ServiceResult<Boolean> res = appointmentService.updateAppointmentReview(appointmentId, status, notes);
            String param = res.isSuccess() ? "msg=" : "error=";
            resp.sendRedirect(req.getContextPath() + "/dashboard?" + param + java.net.URLEncoder.encode(res.getMessage(), "UTF-8"));
        } catch (IllegalArgumentException ex) {
            resp.sendRedirect(req.getContextPath() + "/dashboard?error=Invalid+status+update+parameters");
        }
    }
}
