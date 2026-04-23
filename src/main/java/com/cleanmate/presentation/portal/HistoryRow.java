package com.cleanmate.presentation.portal;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;

public class HistoryRow {
    private final SimpleObjectProperty<LocalDate> date = new SimpleObjectProperty<>();
    private final SimpleStringProperty property = new SimpleStringProperty();
    private final SimpleStringProperty employee = new SimpleStringProperty();
    private final SimpleStringProperty status = new SimpleStringProperty();
    private final SimpleIntegerProperty photoCount = new SimpleIntegerProperty();
    private final SimpleIntegerProperty rating = new SimpleIntegerProperty();
    private final SimpleStringProperty note = new SimpleStringProperty();

    public HistoryRow(LocalDate date, String property, String employee,
                      String status, int photoCount, int rating, String note) {
        this.date.set(date);
        this.property.set(property);
        this.employee.set(employee);
        this.status.set(status);
        this.photoCount.set(photoCount);
        this.rating.set(rating);
        this.note.set(note);
    }

    public LocalDate getDate() { return date.get(); }
    public String getProperty() { return property.get(); }
    public String getEmployee() { return employee.get(); }
    public String getStatus() { return status.get(); }
    public int getPhotoCount() { return photoCount.get(); }
    public int getRating() { return rating.get(); }
    public String getNote() { return note.get(); }

    public SimpleObjectProperty<LocalDate> dateProperty() { return date; }
    public SimpleStringProperty propertyProperty() { return property; }
    public SimpleStringProperty employeeProperty() { return employee; }
    public SimpleStringProperty statusProperty() { return status; }
    public SimpleIntegerProperty photoCountProperty() { return photoCount; }
    public SimpleIntegerProperty ratingProperty() { return rating; }
    public SimpleStringProperty noteProperty() { return note; }
}
