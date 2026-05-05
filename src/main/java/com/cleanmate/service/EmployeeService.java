package com.cleanmate.service;

import com.cleanmate.model.Employee;
import com.cleanmate.repository.EmployeeRepository;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Optional;

public class EmployeeService {

    private final EmployeeRepository repo;

    public EmployeeService(EmployeeRepository repo) {
        this.repo = repo;
    }

    public ObservableList<Employee> getAll() { return repo.findAll(); }

    public Optional<Employee> findById(String id) { return repo.findById(id); }

    public List<Employee> getActive() { return repo.findActive(); }

    public List<String> getAllNames() { return repo.findAllNames(); }

    public Optional<Employee> findByEmail(String email) { return repo.findByEmail(email); }

    public Employee save(Employee employee) { return repo.save(employee); }

    public void delete(String id) { repo.delete(id); }

    public boolean changePassword(String id, String currentPwd, String newPwd) {
        Optional<Employee> opt = repo.findById(id);
        if (opt.isEmpty()) return false;
        Employee e = opt.get();
        if (!e.getPassword().equals(currentPwd)) return false;
        e.setPassword(newPwd);
        repo.save(e);
        return true;
    }

    public void deactivate(String id) {
        repo.findById(id).ifPresent(e -> {
            e.setActive(false);
            e.setAvailability("INACTIVE");
            repo.save(e);
        });
    }
}
