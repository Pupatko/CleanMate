package com.cleanmate.repository.inmemory;

import com.cleanmate.model.Cleaning;
import com.cleanmate.repository.CleaningRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class InMemoryCleaningRepository implements CleaningRepository {

    private final ObservableList<Cleaning> data = FXCollections.observableArrayList(buildSampleData());

    @Override public ObservableList<Cleaning> findAll() { return data; }

    @Override
    public Optional<Cleaning> findById(String id) {
        return data.stream().filter(c -> c.id().equals(id)).findFirst();
    }

    @Override
    public Cleaning save(Cleaning cleaning) {
        data.removeIf(c -> c.id().equals(cleaning.id()));
        data.add(cleaning);
        return cleaning;
    }

    @Override
    public void delete(String id) { data.removeIf(c -> c.id().equals(id)); }

    @Override
    public List<Cleaning> findByDate(LocalDate date) {
        return data.stream().filter(c -> c.date().equals(date)).toList();
    }

    @Override
    public List<Cleaning> findByDateBetween(LocalDate from, LocalDate to) {
        return data.stream()
                .filter(c -> !c.date().isBefore(from) && !c.date().isAfter(to))
                .toList();
    }

    @Override
    public List<Cleaning> findByEmployee(String employeeName) {
        return data.stream().filter(c -> employeeName.equals(c.employee())).toList();
    }

    @Override
    public List<Cleaning> findByCustomer(String customerName) {
        return data.stream().filter(c -> customerName.equals(c.customer())).toList();
    }

    // ── Sample data ───────────────────────────────────────────────────────────

    private static List<Cleaning> buildSampleData() {
        LocalDate t = LocalDate.now();
        java.util.List<Cleaning> list = new java.util.ArrayList<>();

        list.add(Cleaning.of(t,             LocalTime.of(9, 0),   LocalTime.of(11, 30), "Panská 12, BA",         "Acme Rentals s.r.o.",  "Anna Nová",      "DONE"));
        list.add(Cleaning.of(t,             LocalTime.of(10, 30), LocalTime.of(13, 0),  "Hviezdoslavovo nám. 4", "BNB Slovakia s.r.o.",  "Peter Malý",     "IN_PROGRESS"));
        list.add(Cleaning.of(t,             LocalTime.of(11, 0),  LocalTime.of(14, 0),  "Obchodná 27",           "City Stays Ltd.",       "Peter Malý",     "NEW"));
        list.add(Cleaning.of(t,             LocalTime.of(13, 0),  LocalTime.of(15, 30), "Panenská 8",            "Nomad Homes s.r.o.",   "Eva Horváthová", "ASSIGNED"));
        list.add(Cleaning.of(t,             LocalTime.of(15, 0),  LocalTime.of(17, 30), "Laurinská 3",           "Acme Rentals s.r.o.",  "Ján Kováč",      "CANCELLED"));
        list.add(Cleaning.of(t.plusDays(1), LocalTime.of(9, 0),   LocalTime.of(11, 30), "Grösslingova 45",       "BNB Slovakia s.r.o.",  "Anna Nová",      "ASSIGNED"));
        list.add(Cleaning.of(t.plusDays(1), LocalTime.of(12, 0),  LocalTime.of(14, 30), "Ventúrska 7",           "City Stays Ltd.",       "Peter Malý",     "NEW"));
        list.add(Cleaning.of(t.plusDays(2), LocalTime.of(10, 0),  LocalTime.of(12, 30), "Michalská 22",          "Nomad Homes s.r.o.",   "Eva Horváthová", "ASSIGNED"));
        list.add(Cleaning.of(t.minusDays(1),LocalTime.of(14, 0),  LocalTime.of(16, 30), "Sedlárska 5",           "Acme Rentals s.r.o.",  "Ján Kováč",      "DONE"));
        list.add(Cleaning.of(t.plusDays(3), LocalTime.of(9, 0),   LocalTime.of(11, 0),  "Kapitulská 18",         "City Stays Ltd.",       "Anna Nová",      "NEW"));

        addHistoricalData(list, t);
        return list;
    }

    private static void addHistoricalData(java.util.List<Cleaning> list, LocalDate today) {
        String[][] assignments = {
                {"Panská 12, BA",         "Acme Rentals s.r.o.",  "Anna Nová"},
                {"Hviezdoslavovo nám. 4", "BNB Slovakia s.r.o.",  "Anna Nová"},
                {"Obchodná 27",           "City Stays Ltd.",       "Peter Malý"},
                {"Panenská 8",            "Nomad Homes s.r.o.",   "Peter Malý"},
                {"Laurinská 3",           "Acme Rentals s.r.o.",  "Eva Horváthová"},
                {"Grösslingova 45",       "BNB Slovakia s.r.o.",  "Eva Horváthová"},
                {"Ventúrska 7",           "City Stays Ltd.",       "Ján Kováč"},
                {"Michalská 22",          "Nomad Homes s.r.o.",   "Ján Kováč"},
                {"Sedlárska 5",           "Acme Rentals s.r.o.",  "Anna Nová"},
                {"Kapitulská 18",         "City Stays Ltd.",       "Peter Malý"}
        };
        LocalTime[][] slots = {
                {LocalTime.of(9, 0),   LocalTime.of(11, 30)},
                {LocalTime.of(10, 0),  LocalTime.of(13, 0)},
                {LocalTime.of(11, 30), LocalTime.of(14, 0)},
                {LocalTime.of(13, 0),  LocalTime.of(16, 0)},
                {LocalTime.of(14, 30), LocalTime.of(17, 0)}
        };
        int[] monthsBack    = {6, 5, 4, 3, 2, 1};
        int[] perMonthCount = {8, 10, 12, 11, 14, 13};

        for (int m = 0; m < monthsBack.length; m++) {
            LocalDate anchor = today.minusMonths(monthsBack[m]).withDayOfMonth(1);
            int days = anchor.lengthOfMonth();
            for (int i = 0; i < perMonthCount[m]; i++) {
                int dom = 1 + ((i * 7 + m * 3) % days);
                String[] pair = assignments[(i + m) % assignments.length];
                LocalTime[] s = slots[(i + m * 2) % slots.length];
                String status = (i % 11 == 0) ? "CANCELLED" : "DONE";
                list.add(Cleaning.of(anchor.withDayOfMonth(dom), s[0], s[1], pair[0], pair[1], pair[2], status));
            }
        }

        for (int m : new int[]{1, 2}) {
            LocalDate anchor = today.plusMonths(m).withDayOfMonth(1);
            int days = anchor.lengthOfMonth();
            for (int i = 0; i < 6; i++) {
                int dom = 1 + ((i * 5 + m * 2) % days);
                String[] pair = assignments[(i + m) % assignments.length];
                LocalTime[] s = slots[(i + m) % slots.length];
                list.add(Cleaning.of(anchor.withDayOfMonth(dom), s[0], s[1], pair[0], pair[1], pair[2],
                        (i % 2 == 0) ? "ASSIGNED" : "NEW"));
            }
        }
    }
}
