package com.cleanmate.presentation.util;

import com.cleanmate.presentation.nav.LanguageManager;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public final class EmptyState {

    private EmptyState() {}

    public static Node build(String icon, String messageKey) {
        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("empty-state-icon");

        Label msgLabel = new Label(LanguageManager.getBundle().getString(messageKey));
        msgLabel.getStyleClass().add("empty-state-label");
        msgLabel.setWrapText(true);
        msgLabel.setTextAlignment(TextAlignment.CENTER);
        msgLabel.setMaxWidth(300);

        VBox box = new VBox(10, iconLabel, msgLabel);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("empty-state");
        return box;
    }
}
