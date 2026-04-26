package com.cleanmate.presentation;

import com.cleanmate.presentation.nav.LanguageManager;
import com.cleanmate.presentation.nav.Route;
import com.cleanmate.presentation.nav.ViewRouter;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.Locale;
import java.util.logging.Logger;

public class LoginController {

    private static final Logger LOG = Logger.getLogger(LoginController.class.getName());

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        roleCombo.setItems(FXCollections.observableArrayList("FIRMA", "ZAMESTNANEC", "ZAKAZNIK"));
        roleCombo.getSelectionModel().selectFirst();
        errorLabel.setVisible(false);
    }

    @FXML
    private void onLogin(ActionEvent event) {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();
        String role = roleCombo.getValue();

        if (username.isEmpty() || password.isEmpty() || role == null) {
            showError(LanguageManager.getBundle().getString("login.error.fields"));
            LOG.warning("Login attempt failed: missing fields (user='" + username + "', role=" + role + ")");
            return;
        }

        LOG.info("Login OK: user='" + username + "', role=" + role);
        errorLabel.setVisible(false);

        Route target = switch (role) {
            case "ZAMESTNANEC" -> Route.MY_SCHEDULE;
            case "ZAKAZNIK"    -> Route.PORTAL;
            default            -> Route.DASHBOARD;
        };
        ViewRouter.get().navigate(target);
    }

    @FXML
    public void onLangSk() {
        LanguageManager.setLocale(Locale.forLanguageTag("sk"));
        ViewRouter.get().navigate(Route.LOGIN);
    }

    @FXML
    public void onLangEn() {
        LanguageManager.setLocale(Locale.ENGLISH);
        ViewRouter.get().navigate(Route.LOGIN);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
