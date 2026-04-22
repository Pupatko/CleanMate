package com.cleanmate.presentation.plan;

import javafx.beans.property.SimpleStringProperty;

public class PlanStep {
    private final SimpleStringProperty name = new SimpleStringProperty();
    private final SimpleStringProperty type = new SimpleStringProperty();

    public PlanStep(String name, String type) {
        this.name.set(name);
        this.type.set(type);
    }

    public String getName() { return name.get(); }
    public String getType() { return type.get(); }
    public SimpleStringProperty nameProperty() { return name; }
    public SimpleStringProperty typeProperty() { return type; }
}
