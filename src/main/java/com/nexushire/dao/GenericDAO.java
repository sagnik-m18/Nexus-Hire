package com.nexushire.dao;

import java.util.List;
import java.util.Optional;

import com.nexushire.exception.DatabaseException;

/** Common CRUD contract (interface + generics). Every table gets its own implementation. */
public interface GenericDAO<T> {
    int save(T item) throws DatabaseException;          // returns the new id
    Optional<T> findById(int id) throws DatabaseException;
    List<T> findAll() throws DatabaseException;
    boolean update(T item) throws DatabaseException;
    boolean delete(int id) throws DatabaseException;
}
