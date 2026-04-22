package com.cleanmate.presentation.customer;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class CustomerRow {
    private final SimpleStringProperty name = new SimpleStringProperty();
    private final SimpleStringProperty email = new SimpleStringProperty();
    private final SimpleStringProperty phone = new SimpleStringProperty();
    private final SimpleIntegerProperty propertyCount = new SimpleIntegerProperty();
    private final SimpleStringProperty note = new SimpleStringProperty();

    public CustomerRow(String name, String email, String phone, int propertyCount, String note) {
        this.name.set(name);
        this.email.set(email);
        this.phone.set(phone);
        this.propertyCount.set(propertyCount);
        this.note.set(note);
    }

    public SimpleStringProperty nameProperty() { return name; }
    public SimpleStringProperty emailProperty() { return email; }
    public SimpleStringProperty phoneProperty() { return phone; }
    public SimpleIntegerProperty propertyCountProperty() { return propertyCount; }
    public SimpleStringProperty noteProperty() { return note; }

    public String getName() { return name.get(); }
    public String getEmail() { return email.get(); }
    public String getPhone() { return phone.get(); }
    public int getPropertyCount() { return propertyCount.get(); }
    public String getNote() { return note.get(); }
}
