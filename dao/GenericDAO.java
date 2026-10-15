package com.healthcare.dao;

import com.healthcare.util.HealthcareException;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Generic Data Access Object contract for CRUD persistence operations.
 * Demonstrates Generics & Interfaces in the DAO layer.
 *
 * @param <T>  Model entity type
 * @param <ID> Primary key type
 */
public interface GenericDAO<T, ID extends Serializable> {
    T save(T entity) throws HealthcareException;
    Optional<T> findById(ID id) throws HealthcareException;
    List<T> findAll() throws HealthcareException;
    boolean update(T entity) throws HealthcareException;
    boolean deleteById(ID id) throws HealthcareException;
}
