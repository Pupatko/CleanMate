package com.cleanmate.repository.inmemory;

import com.cleanmate.model.Customer;
import com.cleanmate.repository.CustomerRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Optional;

public class InMemoryCustomerRepository implements CustomerRepository {

    private final ObservableList<Customer> data = FXCollections.observableArrayList();

    @Override public ObservableList<Customer> findAll() { return data; }

    @Override
    public Optional<Customer> findById(String id) {
        return data.stream().filter(c -> c.getId().equals(id)).findFirst();
    }

    @Override
    public Customer save(Customer customer) {
        data.removeIf(c -> c.getId().equals(customer.getId()));
        data.add(customer);
        return customer;
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        return data.stream().filter(c -> c.getEmail().equalsIgnoreCase(email)).findFirst();
    }

    @Override
    public void delete(String id) { data.removeIf(c -> c.getId().equals(id)); }

    @Override
    public List<String> findAllNames() {
        return data.stream().map(Customer::getName).toList();
    }
}
