package com.cleanmate.repository;

import com.cleanmate.model.Cleaning;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CleaningRepository {
    ObservableList<Cleaning> findAll();
    Optional<Cleaning> findById(String id);
    Cleaning save(Cleaning cleaning);
    void delete(String id);
    List<Cleaning> findByDate(LocalDate date);
    List<Cleaning> findByDateBetween(LocalDate from, LocalDate to);
    List<Cleaning> findByEmployee(String employeeName);
    List<Cleaning> findByCustomer(String customerName);
}
