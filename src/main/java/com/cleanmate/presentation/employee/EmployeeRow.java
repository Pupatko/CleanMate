package com.cleanmate.presentation.employee;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class EmployeeRow {
    private final SimpleStringProperty name = new SimpleStringProperty();
    private final SimpleStringProperty role = new SimpleStringProperty();
    private final SimpleDoubleProperty monthHours = new SimpleDoubleProperty();
    private final SimpleBooleanProperty active = new SimpleBooleanProperty();
    private final SimpleStringProperty availability = new SimpleStringProperty();

    public EmployeeRow(String name, String role, double monthHours, boolean active, String availability) {
        this.name.set(name);
        this.role.set(role);
        this.monthHours.set(monthHours);
        this.active.set(active);
        this.availability.set(availability);
    }

    public String getName() { return name.get(); }
    public String getRole() { return role.get(); }
    public double getMonthHours() { return monthHours.get(); }
    public boolean isActive() { return active.get(); }
    public String getAvailability() { return availability.get(); }

    public SimpleStringProperty nameProperty() { return name; }
    public SimpleStringProperty roleProperty() { return role; }
    public SimpleDoubleProperty monthHoursProperty() { return monthHours; }
    public SimpleBooleanProperty activeProperty() { return active; }
    public SimpleStringProperty availabilityProperty() { return availability; }

    public void setName(String v)         { name.set(v); }
    public void setRole(String v)         { role.set(v); }
    public void setAvailability(String v) { availability.set(v); }
}
