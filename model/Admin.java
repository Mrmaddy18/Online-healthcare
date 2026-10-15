package com.healthcare.model;

/**
 * Concrete subclass representing a System Administrator.
 */
public class Admin extends User {
    private String department;

    public Admin() {
        super();
        setRole(Role.ADMIN);
        this.department = "Clinical Operations & Governance";
    }

    public Admin(Integer userId, String name, String email, String passwordHash) {
        super(userId, name, email, passwordHash, Role.ADMIN);
        this.department = "Clinical Operations & Governance";
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String getDashboardViewPath() {
        return "/WEB-INF/views/admin-dashboard.jsp";
    }

    @Override
    public String getRoleSummary() {
        return "Full system oversight: manage users, roles, global settings, and appointment audit logs.";
    }
}
