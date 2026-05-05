package com.cleanmate.repository;

import com.cleanmate.model.Employee;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository {
    ObservableList<Employee> findAll();
    Optional<Employee> findById(String id);
    Optional<Employee> findByEmail(String email);
    Employee save(Employee employee);
    void delete(String id);
    List<Employee> findActive();
    List<String> findAllNames();
}
