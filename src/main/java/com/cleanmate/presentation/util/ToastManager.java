package com.cleanmate.presentation.util;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

public final class ToastManager {

    public enum Type { SUCCESS, ERROR, INFO, WARNING }

    private ToastManager() {}

    public static void show(Window owner, String message, Type type) {
        String icon = switch (type) {
            case SUCCESS -> "✓";
            case ERROR   -> "✕";
            case INFO    -> "ℹ";
            case WARNING -> "⚠";
        };

        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("toast-icon");

        Label textLabel = new Label(message);
        textLabel.getStyleClass().add("toast-text");
        textLabel.setWrapText(true);
        textLabel.setMaxWidth(340);

        HBox box = new HBox(10, iconLabel, textLabel);
        box.getStyleClass().addAll("toast", "toast-" + type.name().toLowerCase());
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(14, 20, 14, 16));
        String css = ToastManager.class.getResource("/css/styles.css").toExternalForm();
        box.getStylesheets().add(css);

        Popup popup = new Popup();
        popup.setAutoFix(false);
        popup.getContent().add(box);
        popup.show(owner);

        Platform.runLater(() -> {
            double x = owner.getX() + (owner.getWidth()  - popup.getWidth())  / 2;
            double y = owner.getY() + owner.getHeight()  - popup.getHeight()  - 60;
            popup.setX(x);
            popup.setY(y);

            box.setOpacity(0);
            box.setTranslateY(16);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(220), box);
            fadeIn.setToValue(1);
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(220), box);
            slideIn.setToY(0);

            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), box);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(ev -> popup.hide());

            new SequentialTransition(
                    new ParallelTransition(fadeIn, slideIn),
                    new PauseTransition(Duration.seconds(2.8)),
                    fadeOut
            ).play();
        });
    }

    public static void success(Window owner, String message) { show(owner, message, Type.SUCCESS); }
    public static void error(Window owner, String message)   { show(owner, message, Type.ERROR); }
    public static void info(Window owner, String message)    { show(owner, message, Type.INFO); }
    public static void warning(Window owner, String message) { show(owner, message, Type.WARNING); }
}
