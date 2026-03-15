package service;

import java.util.List;

/**
 * Generic CRUD service to demonstrate abstraction & polymorphism.
 */
public interface CrudService<T> {
    T add(T item);
    boolean update(T item);
    boolean delete(int id);
    List<T> list();
    T findById(int id);
}
