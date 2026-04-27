package com.cleanmate.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Apartment {
    private final String id;
    private String address;
    private String customerId;
    private String customerName;
    private int rooms;
    private double area;
    private String note;
    private final List<String> taskNames = new ArrayList<>();

    public Apartment(String id, String address, String customerId, String customerName,
                     int rooms, double area, String note) {
        this.id           = id;
        this.address      = address;
        this.customerId   = customerId;
        this.customerName = customerName;
        this.rooms        = rooms;
        this.area         = area;
        this.note         = note;
    }

    public static Apartment create(String address, String customerId, String customerName,
                                    int rooms, double area, String note) {
        return new Apartment(UUID.randomUUID().toString(), address, customerId, customerName,
                rooms, area, note);
    }

    public String getId()           { return id; }
    public String getAddress()      { return address; }
    public String getCustomerId()   { return customerId; }
    public String getCustomerName() { return customerName; }
    public int getRooms()           { return rooms; }
    public double getArea()         { return area; }
    public String getNote()         { return note; }
    public List<String> getTaskNames() { return taskNames; }
    public int getTaskCount()       { return taskNames.size(); }

    public void setAddress(String v)      { address      = v; }
    public void setCustomerId(String v)   { customerId   = v; }
    public void setCustomerName(String v) { customerName = v; }
    public void setRooms(int v)           { rooms        = v; }
    public void setArea(double v)         { area         = v; }
    public void setNote(String v)         { note         = v; }
}
