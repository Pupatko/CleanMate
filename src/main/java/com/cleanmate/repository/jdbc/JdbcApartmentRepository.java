package com.cleanmate.repository.jdbc;

import com.cleanmate.db.DatabaseManager;
import com.cleanmate.model.Apartment;
import com.cleanmate.repository.ApartmentRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class JdbcApartmentRepository implements ApartmentRepository {

    private static final Logger LOG = Logger.getLogger(JdbcApartmentRepository.class.getName());
    private final ObservableList<Apartment> cache = FXCollections.observableArrayList();

    public JdbcApartmentRepository() {
        reload();
    }

    @Override public ObservableList<Apartment> findAll() { return cache; }

    @Override
    public Optional<Apartment> findById(String id) {
        return cache.stream().filter(a -> a.getId().equals(id)).findFirst();
    }

    @Override
    public Apartment save(Apartment a) {
        String sql = """
                INSERT INTO apartments (id, address, customer_id, customer_name, rooms, area, note)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    address       = EXCLUDED.address,
                    customer_id   = EXCLUDED.customer_id,
                    customer_name = EXCLUDED.customer_name,
                    rooms         = EXCLUDED.rooms,
                    area          = EXCLUDED.area,
                    note          = EXCLUDED.note
                """;
        try (Connection con = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, a.getId());
                ps.setString(2, a.getAddress());
                ps.setString(3, a.getCustomerId());
                ps.setString(4, a.getCustomerName());
                ps.setInt(5, a.getRooms());
                ps.setDouble(6, a.getArea());
                ps.setString(7, a.getNote());
                ps.executeUpdate();
            }
            saveTasks(con, a);
        } catch (SQLException e) {
            LOG.severe("save apartment failed: " + e.getMessage());
        }
        cache.removeIf(x -> x.getId().equals(a.getId()));
        cache.add(a);
        return a;
    }

    @Override
    public void delete(String id) {
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM apartments WHERE id = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOG.severe("delete apartment failed: " + e.getMessage());
        }
        cache.removeIf(a -> a.getId().equals(id));
    }

    @Override
    public List<Apartment> findByCustomer(String customerId) {
        return cache.stream().filter(a -> customerId.equals(a.getCustomerId())).toList();
    }

    @Override
    public List<String> findAllAddresses() {
        return cache.stream().map(Apartment::getAddress).toList();
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private void reload() {
        // Load apartments
        Map<String, Apartment> map = new LinkedHashMap<>();
        try (Connection con = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM apartments ORDER BY address");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Apartment a = mapApartment(rs);
                    map.put(a.getId(), a);
                }
            }
            // Load tasks
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM apartment_tasks ORDER BY apartment_id, position");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Apartment a = map.get(rs.getString("apartment_id"));
                    if (a != null) a.getTaskNames().add(rs.getString("task_name"));
                }
            }
        } catch (SQLException e) {
            LOG.severe("reload apartments failed: " + e.getMessage());
        }
        cache.setAll(map.values());
    }

    private void saveTasks(Connection con, Apartment a) throws SQLException {
        try (PreparedStatement del = con.prepareStatement(
                "DELETE FROM apartment_tasks WHERE apartment_id = ?")) {
            del.setString(1, a.getId());
            del.executeUpdate();
        }
        if (a.getTaskNames().isEmpty()) return;
        try (PreparedStatement ins = con.prepareStatement(
                "INSERT INTO apartment_tasks (apartment_id, task_name, position) VALUES (?, ?, ?)")) {
            for (int i = 0; i < a.getTaskNames().size(); i++) {
                ins.setString(1, a.getId());
                ins.setString(2, a.getTaskNames().get(i));
                ins.setInt(3, i);
                ins.addBatch();
            }
            ins.executeBatch();
        }
    }

    private Apartment mapApartment(ResultSet rs) throws SQLException {
        return new Apartment(
                rs.getString("id"),
                rs.getString("address"),
                rs.getString("customer_id"),
                rs.getString("customer_name"),
                rs.getInt("rooms"),
                rs.getDouble("area"),
                rs.getString("note")
        );
    }
}
