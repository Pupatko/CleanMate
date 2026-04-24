package com.cleanmate.presentation.util;

import javafx.scene.control.Alert;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Small helper that collects field changes and shows them in a confirmation Alert. */
public class ChangeSummary {

    private final List<String> lines = new ArrayList<>();

    public ChangeSummary add(String field, Object before, Object after) {
        if (!Objects.equals(before, after)) {
            String b = before == null ? "—" : String.valueOf(before);
            String a = after  == null ? "—" : String.valueOf(after);
            if (b.isEmpty()) b = "—";
            if (a.isEmpty()) a = "—";
            lines.add("• " + field + ": \"" + b + "\"  →  \"" + a + "\"");
        }
        return this;
    }

    public boolean isEmpty() { return lines.isEmpty(); }

    public void show(String title) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        if (lines.isEmpty()) {
            alert.setHeaderText("Žiadne zmeny");
            alert.setContentText("Neboli vykonané žiadne zmeny.");
        } else {
            alert.setHeaderText("Vykonané zmeny:");
            alert.setContentText(String.join("\n", lines));
        }
        alert.showAndWait();
    }
}
