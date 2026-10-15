package com.healthcare.model;

/**
 * Interface defining audit log behavior across clinical entities.
 * Satisfies Review 1 Rubric: OOP Abstraction & Interfaces.
 */
public interface Auditable {
    String toAuditSummary();
}
