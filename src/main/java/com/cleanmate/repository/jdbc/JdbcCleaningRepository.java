package com.cleanmate.repository.jdbc;

import com.cleanmate.db.DatabaseManager;
import com.cleanmate.model.Cleaning;
import com.cleanmate.repository.CleaningRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class JdbcCleaningRepository implements CleaningRepository {

    private static final Logger LOG = Logger.getLogger(JdbcCleaningRepository.class.getName());
    private final ObservableList<Cleaning> cache = FXCollections.observableArrayList();

    public JdbcCleaningRepository() {
        reload();
    }

    // ── Public API ────────────────────────────────────────────────────────────

    @Override public ObservableList<Cleaning> findAll() { return cache; }

    @Override
    public Optional<Cleaning> findById(String id) {
        return cache.stream().filter(c -> c.id().equals(id)).findFirst();
    }

    @Override
    public Cleaning save(Cleaning c) {
        String sql = """
                INSERT INTO cleanings (id, date, check_out, check_in, property, customer, employee, status, qc_rating, qc_note)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    date      = EXCLUDED.date,
                    check_out = EXCLUDED.check_out,
                    check_in  = EXCLUDED.check_in,
                    property  = EXCLUDED.property,
                    customer  = EXCLUDED.customer,
                    employee  = EXCLUDED.employee,
                    status    = EXCLUDED.status,
                    qc_rating = EXCLUDED.qc_rating,
                    qc_note   = EXCLUDED.qc_note
                """;
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.id());
            ps.setDate(2, Date.valueOf(c.date()));
            ps.setTime(3, Time.valueOf(c.checkOut()));
            ps.setTime(4, Time.valueOf(c.checkIn()));
            ps.setString(5, c.property());
            ps.setString(6, c.customer());
            ps.setString(7, c.employee());
            ps.setString(8, c.status());
            ps.setInt(9, c.qcRating());
            ps.setString(10, c.qcNote());
            ps.executeUpdate();
        } catch (SQLException e) {
            LOG.severe("save cleaning failed: " + e.getMessage());
        }
        cache.removeIf(x -> x.id().equals(c.id()));
        cache.add(c);
        return c;
    }

    @Override
    public void delete(String id) {
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM cleanings WHERE id = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOG.severe("delete cleaning failed: " + e.getMessage());
        }
        cache.removeIf(c -> c.id().equals(id));
    }

    @Override
    public List<Cleaning> findByDate(LocalDate date) {
        return cache.stream().filter(c -> c.date().equals(date)).toList();
    }

    @Override
    public List<Cleaning> findByDateBetween(LocalDate from, LocalDate to) {
        return cache.stream()
                .filter(c -> !c.date().isBefore(from) && !c.date().isAfter(to))
                .toList();
    }

    @Override
    public List<Cleaning> findByEmployee(String employeeName) {
        return cache.stream().filter(c -> employeeName.equals(c.employee())).toList();
    }

    @Override
    public List<Cleaning> findByCustomer(String customerName) {
        return cache.stream().filter(c -> customerName.equals(c.customer())).toList();
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private void reload() {
        List<Cleaning> loaded = new ArrayList<>();
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM cleanings ORDER BY date, check_out");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) loaded.add(map(rs));
        } catch (SQLException e) {
            LOG.severe("reload cleanings failed: " + e.getMessage());
        }
        cache.setAll(loaded);
    }

    private Cleaning map(ResultSet rs) throws SQLException {
        return new Cleaning(
                rs.getString("id"),
                rs.getDate("date").toLocalDate(),
                rs.getTime("check_out").toLocalTime(),
                rs.getTime("check_in").toLocalTime(),
                rs.getString("property"),
                rs.getString("customer"),
                rs.getString("employee"),
                rs.getString("status"),
                rs.getInt("qc_rating"),
                rs.getString("qc_note")
        );
    }
}
