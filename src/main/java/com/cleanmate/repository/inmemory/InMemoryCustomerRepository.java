package com.cleanmate.repository.inmemory;

import com.cleanmate.model.Customer;
import com.cleanmate.repository.CustomerRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Optional;

public class InMemoryCustomerRepository implements CustomerRepository {

    private final ObservableList<Customer> data = FXCollections.observableArrayList(
            new Customer("c1", "Acme Rentals s.r.o.",   "acme@rentals.sk",   "+421 2 1234 5678", "Hlavný zákazník, dlhodobá zmluva"),
            new Customer("c2", "BNB Slovakia s.r.o.",   "info@bnbsk.sk",     "+421 2 8765 4321", ""),
            new Customer("c3", "City Stays Ltd.",        "stays@city.sk",     "+421 910 123 456", "Platba vopred"),
            new Customer("c4", "Nomad Homes s.r.o.",     "nomad@homes.sk",    "+421 910 654 321", ""),
            new Customer("c5", "Ivan Novák",             "ivan@novak.sk",     "+421 905 111 222", "Súkromný zákazník")
    );

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
    public void delete(String id) { data.removeIf(c -> c.getId().equals(id)); }

    @Override
    public List<String> findAllNames() {
        return data.stream().map(Customer::getName).toList();
    }
}
