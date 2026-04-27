package com.cleanmate.service;

import com.cleanmate.model.Cleaning;
import com.cleanmate.repository.CleaningRepository;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class CleaningService {

    private final CleaningRepository repo;

    public CleaningService(CleaningRepository repo) {
        this.repo = repo;
    }

    public ObservableList<Cleaning> getAll() { return repo.findAll(); }

    public Optional<Cleaning> findById(String id) { return repo.findById(id); }

    public Cleaning save(Cleaning cleaning) { return repo.save(cleaning); }

    public void delete(String id) { repo.delete(id); }

    public List<Cleaning> getByDate(LocalDate date) { return repo.findByDate(date); }

    public List<Cleaning> getByDateRange(LocalDate from, LocalDate to) {
        return repo.findByDateBetween(from, to);
    }

    public List<Cleaning> getTodayCleanings() { return repo.findByDate(LocalDate.now()); }

    public List<Cleaning> getByEmployee(String employeeName) {
        return repo.findByEmployee(employeeName);
    }

    public List<Cleaning> getByCustomer(String customerName) {
        return repo.findByCustomer(customerName);
    }

    public void cancel(String id) {
        repo.findById(id).ifPresent(c -> repo.save(c.withStatus("CANCELLED")));
    }

    public void assignEmployee(String id, String employee) {
        repo.findById(id).ifPresent(c -> {
            String newStatus = "NEW".equals(c.status()) ? "ASSIGNED" : c.status();
            repo.save(c.withEmployee(employee, newStatus));
        });
    }

    public void saveQc(String id, int rating, String note) {
        repo.findById(id).ifPresent(c -> repo.save(c.withQc(rating, note)));
    }

    public long countDone(List<Cleaning> items) {
        return items.stream().filter(c -> "DONE".equals(c.status())).count();
    }

    public double sumHours(List<Cleaning> items) {
        return items.stream()
                .mapToDouble(c -> java.time.Duration.between(c.checkOut(), c.checkIn()).toMinutes() / 60.0)
                .sum();
    }
}
