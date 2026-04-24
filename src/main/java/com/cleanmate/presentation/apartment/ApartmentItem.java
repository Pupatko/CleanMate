package com.cleanmate.presentation.apartment;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ApartmentItem {
    private String address;
    private String customer;
    private int rooms;
    private double area;
    private String note;
    private final ObservableList<String> steps = FXCollections.observableArrayList();

    public ApartmentItem(String address, String customer, int rooms, double area, String note, String... initialSteps) {
        this.address  = address;
        this.customer = customer;
        this.rooms    = rooms;
        this.area     = area;
        this.note     = note;
        steps.addAll(initialSteps);
    }

    public String getAddress()  { return address; }
    public String getCustomer() { return customer; }
    public int    getRooms()    { return rooms; }
    public double getArea()     { return area; }
    public String getNote()     { return note; }
    public int    getStepCount(){ return steps.size(); }
    public ObservableList<String> getSteps() { return steps; }

    public void setAddress(String v)  { address  = v; }
    public void setCustomer(String v) { customer = v; }
    public void setRooms(int v)       { rooms    = v; }
    public void setArea(double v)     { area     = v; }
    public void setNote(String v)     { note     = v; }
}
