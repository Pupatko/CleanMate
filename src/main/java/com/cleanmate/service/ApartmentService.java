package com.cleanmate.service;

import com.cleanmate.model.Apartment;
import com.cleanmate.repository.ApartmentRepository;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Optional;

public class ApartmentService {

    private final ApartmentRepository repo;

    public ApartmentService(ApartmentRepository repo) {
        this.repo = repo;
    }

    public ObservableList<Apartment> getAll() { return repo.findAll(); }

    public Optional<Apartment> findById(String id) { return repo.findById(id); }

    public List<Apartment> getByCustomer(String customerId) { return repo.findByCustomer(customerId); }

    public List<String> getAllAddresses() { return repo.findAllAddresses(); }

    public Apartment save(Apartment apartment) { return repo.save(apartment); }

    public void delete(String id) { repo.delete(id); }
}
