package com.healthcare.servlet;

import com.healthcare.dao.AppointmentDAO;
import com.healthcare.dao.UserDAO;
import com.healthcare.model.Doctor;
import com.healthcare.model.Patient;
import com.healthcare.model.User;
import com.healthcare.util.HealthcareException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Role-aware controller that loads dashboard models from DAO layer and forwards
 * to the polymorphic JSP view corresponding to ADMIN, DOCTOR, or PATIENT.
 */
@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard"})
public class DashboardServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login?error=Please+sign+in+to+continue");
            return;
        }

        User currentUser = (User) session.getAttribute("currentUser");
        try {
            switch (currentUser.getRole()) {
                case ADMIN:
                    req.setAttribute("allUsers", userDAO.findAll());
                    req.setAttribute("allAppointments", appointmentDAO.findAll());
                    req.setAttribute("statusMetrics", appointmentDAO.getStatusMetrics());
                    break;
                case DOCTOR:
                    Doctor doc = (Doctor) currentUser;
                    req.setAttribute("doctorAppointments", appointmentDAO.findByDoctorId(doc.getDoctorId()));
                    req.setAttribute("availableSlots", doc.getAvailableTimeSlots());
                    break;
                case PATIENT:
                    Patient pat = (Patient) currentUser;
                    req.setAttribute("patientAppointments", appointmentDAO.findByPatientId(pat.getPatientId()));
                    req.setAttribute("doctorsList", userDAO.findAllDoctors());
                    break;
            }
        } catch (HealthcareException ex) {
            req.setAttribute("errorMessage", "Unable to load live dashboard data: " + ex.getMessage());
        }

        // Polymorphic dispatch to role-specific JSP
        req.getRequestDispatcher(currentUser.getDashboardViewPath()).forward(req, resp);
    }
}
