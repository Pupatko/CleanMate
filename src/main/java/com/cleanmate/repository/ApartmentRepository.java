package com.cleanmate.repository;

import com.cleanmate.model.Apartment;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Optional;

public interface ApartmentRepository {
    ObservableList<Apartment> findAll();
    Optional<Apartment> findById(String id);
    Apartment save(Apartment apartment);
    void delete(String id);
    List<Apartment> findByCustomer(String customerId);
    List<String> findAllAddresses();
}
