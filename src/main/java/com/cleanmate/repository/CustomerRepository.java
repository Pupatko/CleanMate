package com.cleanmate.repository;

import com.cleanmate.model.Customer;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository {
    ObservableList<Customer> findAll();
    Optional<Customer> findById(String id);
    Optional<Customer> findByEmail(String email);
    Customer save(Customer customer);
    void delete(String id);
    List<String> findAllNames();
}
