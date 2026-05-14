package com.cleanmate.repository.jdbc;

import com.cleanmate.db.DatabaseManager;
import com.cleanmate.model.Customer;
import com.cleanmate.repository.CustomerRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class JdbcCustomerRepository implements CustomerRepository {

    private static final Logger LOG = Logger.getLogger(JdbcCustomerRepository.class.getName());
    private final ObservableList<Customer> cache = FXCollections.observableArrayList();

    public JdbcCustomerRepository() {
        reload();
    }

    @Override public ObservableList<Customer> findAll() { return cache; }

    @Override
    public Optional<Customer> findById(String id) {
        return cache.stream().filter(c -> c.getId().equals(id)).findFirst();
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        return cache.stream().filter(c -> c.getEmail().equalsIgnoreCase(email)).findFirst();
    }

    @Override
    public Customer save(Customer c) {
        String sql = """
                INSERT INTO customers (id, name, email, phone, notes, password)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    name     = EXCLUDED.name,
                    email    = EXCLUDED.email,
                    phone    = EXCLUDED.phone,
                    notes    = EXCLUDED.notes,
                    password = EXCLUDED.password
                """;
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getId());
            ps.setString(2, c.getName());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getPhone());
            ps.setString(5, c.getNotes());
            ps.setString(6, c.getPassword());
            ps.executeUpdate();
        } catch (SQLException e) {
            LOG.severe("save customer failed: " + e.getMessage());
        }
        cache.removeIf(x -> x.getId().equals(c.getId()));
        cache.add(c);
        return c;
    }

    @Override
    public void delete(String id) {
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM customers WHERE id = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOG.severe("delete customer failed: " + e.getMessage());
        }
        cache.removeIf(c -> c.getId().equals(id));
    }

    @Override
    public List<String> findAllNames() {
        return cache.stream().map(Customer::getName).toList();
    }

    private void reload() {
        List<Customer> loaded = new ArrayList<>();
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM customers ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) loaded.add(map(rs));
        } catch (SQLException e) {
            LOG.severe("reload customers failed: " + e.getMessage());
        }
        cache.setAll(loaded);
    }

    private Customer map(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("notes"),
                rs.getString("password")
        );
    }
}
