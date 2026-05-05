package com.cleanmate.model;

import java.time.LocalDate;
import java.util.UUID;

public class Employee {
    private final String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String role;
    private String address;
    private LocalDate startDate;
    private String notes;
    private boolean active;
    private String availability;
    private String password;

    public Employee(String id, String firstName, String lastName, String email, String phone,
                    String role, String address, LocalDate startDate, String notes,
                    boolean active, String availability) {
        this(id, firstName, lastName, email, phone, role, address, startDate, notes,
                active, availability, "cleanmate");
    }

    public Employee(String id, String firstName, String lastName, String email, String phone,
                    String role, String address, LocalDate startDate, String notes,
                    boolean active, String availability, String password) {
        this.id           = id;
        this.firstName    = firstName;
        this.lastName     = lastName;
        this.email        = email;
        this.phone        = phone;
        this.role         = role;
        this.address      = address;
        this.startDate    = startDate;
        this.notes        = notes;
        this.active       = active;
        this.availability = availability;
        this.password     = password != null ? password : "cleanmate";
    }

    public static Employee create(String firstName, String lastName, String email, String phone,
                                   String role, String address, LocalDate startDate, String notes) {
        return new Employee(UUID.randomUUID().toString(), firstName, lastName, email, phone,
                role, address, startDate, notes, true, "AVAILABLE", "cleanmate");
    }

    public String getId()           { return id; }
    public String getFirstName()    { return firstName; }
    public String getLastName()     { return lastName; }
    public String getFullName()     { return firstName + " " + lastName; }
    public String getEmail()        { return email; }
    public String getPhone()        { return phone; }
    public String getRole()         { return role; }
    public String getAddress()      { return address; }
    public LocalDate getStartDate() { return startDate; }
    public String getNotes()        { return notes; }
    public boolean isActive()       { return active; }
    public String getAvailability() { return availability; }
    public String getPassword()     { return password; }

    public void setFirstName(String v)    { firstName    = v; }
    public void setLastName(String v)     { lastName     = v; }
    public void setEmail(String v)        { email        = v; }
    public void setPhone(String v)        { phone        = v; }
    public void setRole(String v)         { role         = v; }
    public void setAddress(String v)      { address      = v; }
    public void setStartDate(LocalDate v) { startDate    = v; }
    public void setNotes(String v)        { notes        = v; }
    public void setActive(boolean v)      { active       = v; }
    public void setAvailability(String v) { availability = v; }
    public void setPassword(String v)     { password     = v; }
}
