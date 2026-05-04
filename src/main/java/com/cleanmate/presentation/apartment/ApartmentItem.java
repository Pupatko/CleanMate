package com.cleanmate.presentation.apartment;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(exclude = "tasks")
public class ApartmentItem {

    private String id;
    private String address;
    private String customer;
    private int    rooms;
    private double area;
    private String note;

    private final ObservableList<ApartmentTask> tasks = FXCollections.observableArrayList();

    public ApartmentItem(String address, String customer, int rooms, double area, String note,
                         ApartmentTask... initialTasks) {
        this.address  = address;
        this.customer = customer;
        this.rooms    = rooms;
        this.area     = area;
        this.note     = note;
        for (ApartmentTask t : initialTasks) tasks.add(t);
    }

    /** Computed — not a plain field, so Lombok can't generate this. */
    public int getStepCount() { return tasks.size(); }
}
