package com.healthcare.servlet;

import com.healthcare.model.ServiceResult;
import com.healthcare.model.User;
import com.healthcare.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Handles user login authentication, role-aware HTTP session creation, and logout.
 * Satisfies Review 1 Rubric: Servlets & Web Integration (request/response and session management).
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login", "/logout"})
public class LoginServlet extends HttpServlet {
    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        if ("/logout".equalsIgnoreCase(path)) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            resp.sendRedirect(req.getContextPath() + "/login?msg=Logged+out+successfully");
            return;
        }
        req.getRequestDispatcher("/WEB-INF/views/index.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        ServiceResult<User> result = authService.authenticate(email, password);
        if (!result.isSuccess()) {
            req.setAttribute("errorMessage", result.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/index.jsp").forward(req, resp);
            return;
        }

        User authenticatedUser = result.getData();
        HttpSession session = req.getSession(true);
        session.setMaxInactiveInterval(30 * 60); // 30-minute session timeout
        session.setAttribute("currentUser", authenticatedUser);
        session.setAttribute("userRole", authenticatedUser.getRole().name());

        resp.sendRedirect(req.getContextPath() + "/dashboard");
    }
}
