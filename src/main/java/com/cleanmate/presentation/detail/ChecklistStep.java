package com.cleanmate.presentation.detail;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ChecklistStep {
    private final StringProperty name = new SimpleStringProperty();
    private final BooleanProperty done = new SimpleBooleanProperty(false);

    public ChecklistStep(String name, boolean done) {
        this.name.set(name);
        this.done.set(done);
    }

    public StringProperty nameProperty() { return name; }
    public BooleanProperty doneProperty() { return done; }

    @Override public String toString() { return name.get(); }
}
