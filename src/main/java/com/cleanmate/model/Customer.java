package com.cleanmate.model;

import java.util.UUID;

public class Customer {
    private final String id;
    private String name;
    private String email;
    private String phone;
    private String notes;

    public Customer(String id, String name, String email, String phone, String notes) {
        this.id    = id;
        this.name  = name;
        this.email = email;
        this.phone = phone;
        this.notes = notes;
    }

    public static Customer create(String name, String email, String phone, String notes) {
        return new Customer(UUID.randomUUID().toString(), name, email, phone, notes);
    }

    public String getId()    { return id; }
    public String getName()  { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getNotes() { return notes; }

    public void setName(String v)  { name  = v; }
    public void setEmail(String v) { email = v; }
    public void setPhone(String v) { phone = v; }
    public void setNotes(String v) { notes = v; }
}
