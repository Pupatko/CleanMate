package com.cleanmate.repository.jdbc;

import com.cleanmate.db.DatabaseManager;
import com.cleanmate.model.Employee;
import com.cleanmate.repository.EmployeeRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class JdbcEmployeeRepository implements EmployeeRepository {

    private static final Logger LOG = Logger.getLogger(JdbcEmployeeRepository.class.getName());
    private final ObservableList<Employee> cache = FXCollections.observableArrayList();

    public JdbcEmployeeRepository() {
        reload();
    }

    @Override public ObservableList<Employee> findAll() { return cache; }

    @Override
    public Optional<Employee> findById(String id) {
        return cache.stream().filter(e -> e.getId().equals(id)).findFirst();
    }

    @Override
    public Employee save(Employee e) {
        String sql = """
                INSERT INTO employees (id, first_name, last_name, email, phone, role, address, start_date, notes, active, availability)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    first_name   = EXCLUDED.first_name,
                    last_name    = EXCLUDED.last_name,
                    email        = EXCLUDED.email,
                    phone        = EXCLUDED.phone,
                    role         = EXCLUDED.role,
                    address      = EXCLUDED.address,
                    start_date   = EXCLUDED.start_date,
                    notes        = EXCLUDED.notes,
                    active       = EXCLUDED.active,
                    availability = EXCLUDED.availability
                """;
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getId());
            ps.setString(2, e.getFirstName());
            ps.setString(3, e.getLastName());
            ps.setString(4, e.getEmail());
            ps.setString(5, e.getPhone());
            ps.setString(6, e.getRole());
            ps.setString(7, e.getAddress());
            ps.setDate(8, e.getStartDate() != null ? Date.valueOf(e.getStartDate()) : null);
            ps.setString(9, e.getNotes());
            ps.setBoolean(10, e.isActive());
            ps.setString(11, e.getAvailability());
            ps.executeUpdate();
        } catch (SQLException ex) {
            LOG.severe("save employee failed: " + ex.getMessage());
        }
        cache.removeIf(x -> x.getId().equals(e.getId()));
        cache.add(e);
        return e;
    }

    @Override
    public void delete(String id) {
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM employees WHERE id = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOG.severe("delete employee failed: " + e.getMessage());
        }
        cache.removeIf(e -> e.getId().equals(id));
    }

    @Override
    public List<Employee> findActive() {
        return cache.stream().filter(Employee::isActive).toList();
    }

    @Override
    public List<String> findAllNames() {
        return cache.stream().filter(Employee::isActive).map(Employee::getFullName).toList();
    }

    private void reload() {
        List<Employee> loaded = new ArrayList<>();
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM employees ORDER BY last_name, first_name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) loaded.add(map(rs));
        } catch (SQLException e) {
            LOG.severe("reload employees failed: " + e.getMessage());
        }
        cache.setAll(loaded);
    }

    private Employee map(ResultSet rs) throws SQLException {
        Date sd = rs.getDate("start_date");
        return new Employee(
                rs.getString("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("role"),
                rs.getString("address"),
                sd != null ? sd.toLocalDate() : null,
                rs.getString("notes"),
                rs.getBoolean("active"),
                rs.getString("availability")
        );
    }
}
