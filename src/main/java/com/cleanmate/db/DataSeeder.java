package com.cleanmate.db;

import com.cleanmate.model.Apartment;
import com.cleanmate.model.Cleaning;
import com.cleanmate.model.Customer;
import com.cleanmate.model.Employee;
import com.cleanmate.service.ServiceLocator;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.logging.Logger;

public final class DataSeeder {

    private static final Logger LOG = Logger.getLogger(DataSeeder.class.getName());

    private DataSeeder() {}

    public static void seedIfEmpty() {
        if (!ServiceLocator.cleanings().getAll().isEmpty()) return;
        LOG.info("Database is empty — seeding sample data.");
        seedCustomers();
        seedEmployees();
        seedApartments();
        seedCleanings();
        LOG.info("Seed complete.");
    }

    private static void seedCustomers() {
        List.of(
                new Customer("c1", "Acme Rentals s.r.o.",  "acme@rentals.sk",  "+421 2 1234 5678", "Hlavný zákazník"),
                new Customer("c2", "BNB Slovakia s.r.o.",  "info@bnbsk.sk",    "+421 2 8765 4321", ""),
                new Customer("c3", "City Stays Ltd.",       "stays@city.sk",    "+421 910 123 456", "Platba vopred"),
                new Customer("c4", "Nomad Homes s.r.o.",    "nomad@homes.sk",   "+421 910 654 321", ""),
                new Customer("c5", "Ivan Novák",            "ivan@novak.sk",    "+421 905 111 222", "Súkromný zákazník")
        ).forEach(ServiceLocator.customers()::save);
    }

    private static void seedEmployees() {
        List.of(
                new Employee("e1", "Anna",    "Nová",       "anna@cleanmate.sk",  "+421 900 111 222", "CLEANER",    "Hlavná 5, BA",     LocalDate.of(2023, 3, 1),  "", true,  "Dostupný"),
                new Employee("e2", "Peter",   "Malý",       "peter@cleanmate.sk", "+421 900 222 333", "CLEANER",    "Obchodná 12, BA",  LocalDate.of(2022, 6, 15), "", true,  "Dostupný"),
                new Employee("e3", "Eva",     "Horváthová", "eva@cleanmate.sk",   "+421 900 333 444", "SUPERVISOR", "Miletičova 3, BA", LocalDate.of(2021, 1, 10), "", true,  "Na dovolenke"),
                new Employee("e4", "Ján",     "Kováč",      "jan@cleanmate.sk",   "+421 900 444 555", "CLEANER",    "Šancová 7, BA",    LocalDate.of(2023, 9, 1),  "", true,  "Dostupný"),
                new Employee("e5", "Tomáš",   "Urban",      "tomas@cleanmate.sk", "+421 900 555 666", "CLEANER",    "Kolárska 2, BA",   LocalDate.of(2024, 2, 1),  "", true,  "Dostupný"),
                new Employee("e6", "Katarína","Veselá",     "katka@cleanmate.sk", "+421 900 666 777", "CLEANER",    "Dunajská 9, BA",   LocalDate.of(2022, 11, 1), "", false, "Neaktívny")
        ).forEach(ServiceLocator.employees()::save);
    }

    private static void seedApartments() {
        Apartment a1 = Apartment.create("Panská 12, BA",         "c1", "Acme Rentals s.r.o.",  2, 55.0, "Kód trezora: 1234");
        a1.getTaskNames().addAll(List.of("Vysávanie obývačky", "Výmena bielizne", "Čistenie kúpeľne", "Doplnenie hygieniky"));

        Apartment a2 = Apartment.create("Hviezdoslavovo nám. 4", "c2", "BNB Slovakia s.r.o.",  3, 78.0, "");
        a2.getTaskNames().addAll(List.of("Umytie riadu", "Vysávanie", "Výmena uterákov", "Kontrola chladničky", "Vyhodenie odpadu"));

        Apartment a3 = Apartment.create("Obchodná 27",           "c3", "City Stays Ltd.",       1, 38.0, "Štúdio");
        a3.getTaskNames().addAll(List.of("Umytie kúpeľne", "Vysávanie", "Výmena bielizne"));

        Apartment a4 = Apartment.create("Panenská 8",            "c4", "Nomad Homes s.r.o.",    2, 62.0, "");
        a4.getTaskNames().addAll(List.of("Vysávanie", "Čistenie kúpeľne", "Výmena bielizne", "Umytie okien"));

        Apartment a5 = Apartment.create("Laurinská 3",           "c1", "Acme Rentals s.r.o.",   3, 95.0, "Duplex, 2 kúpeľne");
        a5.getTaskNames().addAll(List.of("Vysávanie obývačky", "Vysávanie spální", "Čistenie 2x kúpeľne", "Výmena bielizne", "Doplnenie hygieniky"));

        Apartment a6 = Apartment.create("Grösslingova 45",       "c2", "BNB Slovakia s.r.o.",   1, 42.0, "");
        a6.getTaskNames().addAll(List.of("Umytie kúpeľne", "Vysávanie", "Výmena bielizne", "Vyhodenie odpadu"));

        Apartment a7 = Apartment.create("Ventúrska 7",           "c3", "City Stays Ltd.",        2, 58.0, "");
        a7.getTaskNames().addAll(List.of("Vysávanie", "Čistenie kúpeľne", "Výmena uterákov a bielizne"));

        Apartment a8 = Apartment.create("Michalská 22",          "c4", "Nomad Homes s.r.o.",     2, 67.0, "Historická budova");
        a8.getTaskNames().addAll(List.of("Vysávanie", "Výmena bielizne", "Čistenie kúpeľne", "Doplnenie hygieniky"));

        Apartment a9 = Apartment.create("Sedlárska 5",           "c1", "Acme Rentals s.r.o.",    1, 35.0, "");
        a9.getTaskNames().addAll(List.of("Umytie kúpeľne", "Vysávanie", "Výmena bielizne"));

        Apartment a10 = Apartment.create("Kapitulská 18",        "c3", "City Stays Ltd.",         3, 88.0, "Terasa");
        a10.getTaskNames().addAll(List.of("Vysávanie celého bytu", "Čistenie 2x kúpeľne", "Výmena bielizne", "Umytie terasy", "Kontrola chladničky"));

        List.of(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10).forEach(ServiceLocator.apartments()::save);
    }

    private static void seedCleanings() {
        LocalDate t = LocalDate.now();
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

        // Today's cleanings
        ServiceLocator.cleanings().save(Cleaning.of(t, LocalTime.of(9, 0),   LocalTime.of(11, 30), "Panská 12, BA",         "Acme Rentals s.r.o.",  "Anna Nová",      "DONE"));
        ServiceLocator.cleanings().save(Cleaning.of(t, LocalTime.of(10, 30), LocalTime.of(13, 0),  "Hviezdoslavovo nám. 4", "BNB Slovakia s.r.o.",  "Peter Malý",     "IN_PROGRESS"));
        ServiceLocator.cleanings().save(Cleaning.of(t, LocalTime.of(11, 0),  LocalTime.of(14, 0),  "Obchodná 27",           "City Stays Ltd.",       "Peter Malý",     "NEW"));
        ServiceLocator.cleanings().save(Cleaning.of(t, LocalTime.of(13, 0),  LocalTime.of(15, 30), "Panenská 8",            "Nomad Homes s.r.o.",   "Eva Horváthová", "ASSIGNED"));
        ServiceLocator.cleanings().save(Cleaning.of(t, LocalTime.of(15, 0),  LocalTime.of(17, 30), "Laurinská 3",           "Acme Rentals s.r.o.",  "Ján Kováč",      "CANCELLED"));

        // Historical data
        int[] monthsBack    = {6, 5, 4, 3, 2, 1};
        int[] perMonthCount = {8, 10, 12, 11, 14, 13};
        for (int m = 0; m < monthsBack.length; m++) {
            LocalDate anchor = t.minusMonths(monthsBack[m]).withDayOfMonth(1);
            int days = anchor.lengthOfMonth();
            for (int i = 0; i < perMonthCount[m]; i++) {
                int dom = 1 + ((i * 7 + m * 3) % days);
                String[] pair = assignments[(i + m) % assignments.length];
                LocalTime[] s = slots[(i + m * 2) % slots.length];
                String status = (i % 11 == 0) ? "CANCELLED" : "DONE";
                ServiceLocator.cleanings().save(Cleaning.of(anchor.withDayOfMonth(dom), s[0], s[1], pair[0], pair[1], pair[2], status));
            }
        }
    }
}
