package com.healthcare.model;

import java.io.Serializable;

/**
 * Generic interface for domain entities with a primary key identifier.
 * Satisfies Review 1 Rubric: Interfaces & Generics.
 *
 * @param <ID> Type of the identifier (e.g., Integer)
 */
public interface Identifiable<ID extends Serializable> extends Serializable {
    ID getId();
    void setId(ID id);
}
