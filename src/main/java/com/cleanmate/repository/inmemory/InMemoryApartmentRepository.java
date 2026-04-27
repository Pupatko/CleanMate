package com.cleanmate.repository.inmemory;

import com.cleanmate.model.Apartment;
import com.cleanmate.repository.ApartmentRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Optional;

public class InMemoryApartmentRepository implements ApartmentRepository {

    private final ObservableList<Apartment> data = FXCollections.observableArrayList(buildSampleData());

    @Override public ObservableList<Apartment> findAll() { return data; }

    @Override
    public Optional<Apartment> findById(String id) {
        return data.stream().filter(a -> a.getId().equals(id)).findFirst();
    }

    @Override
    public Apartment save(Apartment apartment) {
        data.removeIf(a -> a.getId().equals(apartment.getId()));
        data.add(apartment);
        return apartment;
    }

    @Override
    public void delete(String id) { data.removeIf(a -> a.getId().equals(id)); }

    @Override
    public List<Apartment> findByCustomer(String customerId) {
        return data.stream().filter(a -> customerId.equals(a.getCustomerId())).toList();
    }

    @Override
    public List<String> findAllAddresses() {
        return data.stream().map(Apartment::getAddress).toList();
    }

    private static List<Apartment> buildSampleData() {
        List<Apartment> list = new java.util.ArrayList<>();

        Apartment a1 = Apartment.create("Panská 12, BA",         "c1", "Acme Rentals s.r.o.",  2, 55.0, "Kód trezora: 1234");
        a1.getTaskNames().addAll(List.of("Vysávanie obývačky", "Výmena bielizne", "Čistenie kúpeľne", "Doplnenie hygieniky"));
        list.add(a1);

        Apartment a2 = Apartment.create("Hviezdoslavovo nám. 4", "c2", "BNB Slovakia s.r.o.",  3, 78.0, "");
        a2.getTaskNames().addAll(List.of("Umytie riadu", "Vysávanie", "Výmena uterákov", "Kontrola chladničky", "Vyhodenie odpadu"));
        list.add(a2);

        Apartment a3 = Apartment.create("Obchodná 27",           "c3", "City Stays Ltd.",       1, 38.0, "Štúdio");
        a3.getTaskNames().addAll(List.of("Umytie kúpeľne", "Vysávanie", "Výmena bielizne"));
        list.add(a3);

        Apartment a4 = Apartment.create("Panenská 8",            "c4", "Nomad Homes s.r.o.",    2, 62.0, "");
        a4.getTaskNames().addAll(List.of("Vysávanie", "Čistenie kúpeľne", "Výmena bielizne", "Umytie okien"));
        list.add(a4);

        Apartment a5 = Apartment.create("Laurinská 3",           "c1", "Acme Rentals s.r.o.",   3, 95.0, "Duplex, 2 kúpeľne");
        a5.getTaskNames().addAll(List.of("Vysávanie obývačky", "Vysávanie spální", "Čistenie 2x kúpeľne", "Výmena bielizne", "Doplnenie hygieniky", "Kontrola balkóna"));
        list.add(a5);

        Apartment a6 = Apartment.create("Grösslingova 45",       "c2", "BNB Slovakia s.r.o.",    1, 42.0, "");
        a6.getTaskNames().addAll(List.of("Umytie kúpeľne", "Vysávanie", "Výmena bielizne", "Vyhodenie odpadu"));
        list.add(a6);

        Apartment a7 = Apartment.create("Ventúrska 7",           "c3", "City Stays Ltd.",         2, 58.0, "");
        a7.getTaskNames().addAll(List.of("Vysávanie", "Čistenie kúpeľne", "Výmena uterákov a bielizne"));
        list.add(a7);

        Apartment a8 = Apartment.create("Michalská 22",          "c4", "Nomad Homes s.r.o.",      2, 67.0, "Historická budova");
        a8.getTaskNames().addAll(List.of("Vysávanie", "Výmena bielizne", "Čistenie kúpeľne", "Doplnenie hygieniky"));
        list.add(a8);

        Apartment a9 = Apartment.create("Sedlárska 5",           "c1", "Acme Rentals s.r.o.",     1, 35.0, "");
        a9.getTaskNames().addAll(List.of("Umytie kúpeľne", "Vysávanie", "Výmena bielizne"));
        list.add(a9);

        Apartment a10 = Apartment.create("Kapitulská 18",        "c3", "City Stays Ltd.",          3, 88.0, "Terasa");
        a10.getTaskNames().addAll(List.of("Vysávanie celého bytu", "Čistenie 2x kúpeľne", "Výmena bielizne", "Umytie terasy", "Kontrola chladničky"));
        list.add(a10);

        return list;
    }
}
