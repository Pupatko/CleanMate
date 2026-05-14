package com.cleanmate.repository.inmemory;

import com.cleanmate.model.Employee;
import com.cleanmate.repository.EmployeeRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class InMemoryEmployeeRepository implements EmployeeRepository {

    private final ObservableList<Employee> data = FXCollections.observableArrayList(
            new Employee("e1", "Anna",   "Nová",       "anna@cleanmate.sk",   "+421 900 111 222", "CLEANER",    "Hlavná 5, BA",    LocalDate.of(2023, 3, 1),  "", true,  "AVAILABLE"),
            new Employee("e2", "Peter",  "Malý",       "peter@cleanmate.sk",  "+421 900 222 333", "CLEANER",    "Obchodná 12, BA", LocalDate.of(2022, 6, 15), "", true,  "AVAILABLE"),
            new Employee("e3", "Eva",    "Horváthová", "eva@cleanmate.sk",    "+421 900 333 444", "CLEANER",    "Miletičova 3, BA",LocalDate.of(2021, 1, 10), "", true,  "OFF_DUTY"),
            new Employee("e4", "Ján",    "Kováč",      "jan@cleanmate.sk",    "+421 900 444 555", "CLEANER",    "Šancová 7, BA",   LocalDate.of(2023, 9, 1),  "", true,  "AVAILABLE"),
            new Employee("e5", "Tomáš",  "Urban",      "tomas@cleanmate.sk",  "+421 900 555 666", "CLEANER",    "Kolárska 2, BA",  LocalDate.of(2024, 2, 1),  "", true,  "AVAILABLE"),
            new Employee("e6", "Katarína","Veselá",    "katka@cleanmate.sk",  "+421 900 666 777", "CLEANER",    "Dunajská 9, BA",  LocalDate.of(2022, 11, 1), "", false, "INACTIVE")
    );

    @Override public ObservableList<Employee> findAll() { return data; }

    @Override
    public Optional<Employee> findById(String id) {
        return data.stream().filter(e -> e.getId().equals(id)).findFirst();
    }

    @Override
    public Optional<Employee> findByEmail(String email) {
        return data.stream()
                .filter(e -> e.getEmail() != null && e.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    @Override
    public Employee save(Employee employee) {
        data.removeIf(e -> e.getId().equals(employee.getId()));
        data.add(employee);
        return employee;
    }

    @Override
    public void delete(String id) { data.removeIf(e -> e.getId().equals(id)); }

    @Override
    public List<Employee> findActive() {
        return data.stream().filter(Employee::isActive).toList();
    }

    @Override
    public List<String> findAllNames() {
        return data.stream().filter(Employee::isActive).map(Employee::getFullName).toList();
    }
}
