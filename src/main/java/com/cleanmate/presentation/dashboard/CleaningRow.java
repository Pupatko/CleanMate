package com.cleanmate.presentation.dashboard;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CleaningRow {

    private final StringProperty time = new SimpleStringProperty();
    private final StringProperty property = new SimpleStringProperty();
    private final StringProperty employee = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();

    public CleaningRow(String time, String property, String employee, String status) {
        this.time.set(time);
        this.property.set(property);
        this.employee.set(employee);
        this.status.set(status);
    }

    public StringProperty timeProperty() { return time; }
    public StringProperty propertyProperty() { return property; }
    public StringProperty employeeProperty() { return employee; }
    public StringProperty statusProperty() { return status; }
}
