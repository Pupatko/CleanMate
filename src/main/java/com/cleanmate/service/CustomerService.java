package com.cleanmate.service;

import com.cleanmate.model.Customer;
import com.cleanmate.repository.CustomerRepository;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Optional;

public class CustomerService {

    private final CustomerRepository repo;

    public CustomerService(CustomerRepository repo) {
        this.repo = repo;
    }

    public ObservableList<Customer> getAll() { return repo.findAll(); }

    public Optional<Customer> findById(String id) { return repo.findById(id); }

    public List<String> getAllNames() { return repo.findAllNames(); }

    public Customer save(Customer customer) { return repo.save(customer); }

    public void delete(String id) { repo.delete(id); }
}
