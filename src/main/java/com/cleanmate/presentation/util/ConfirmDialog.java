package com.cleanmate.presentation.util;

import com.cleanmate.presentation.nav.LanguageManager;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.util.Optional;
import java.util.ResourceBundle;

public final class ConfirmDialog {

    private ConfirmDialog() {}

    /**
     * Shows a localised confirmation dialog.
     * @param headerKey  bundle key for the header line
     * @param content    already-formatted body text (use MessageFormat before calling)
     * @return true if the user clicked the affirmative button
     */
    public static boolean show(String headerKey, String content) {
        ResourceBundle b = LanguageManager.getBundle();

        ButtonType yes = new ButtonType(b.getString("confirm.btn.yes"), ButtonBar.ButtonData.OK_DONE);
        ButtonType no  = new ButtonType(b.getString("confirm.btn.no"),  ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle(b.getString("confirm.title"));
        alert.setHeaderText(b.getString(headerKey));
        alert.setContentText(content);
        alert.getButtonTypes().setAll(yes, no);
        alert.getDialogPane().getStyleClass().add("confirm-dialog");

        String css = ConfirmDialog.class.getResource("/css/styles.css").toExternalForm();
        alert.getDialogPane().getStylesheets().add(css);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == yes;
    }
}
