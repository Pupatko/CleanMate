package com.cleanmate.presentation.profile;

import com.cleanmate.presentation.nav.BaseNavController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.logging.Logger;

public class EmployeeProfileController extends BaseNavController {

    private static final Logger LOG = Logger.getLogger(EmployeeProfileController.class.getName());

    @FXML private Label avatarLabel;
    @FXML private Label nameLabel;
    @FXML private Label roleLabel;

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;

    @FXML private Label totalHoursLabel;
    @FXML private Label totalTasksLabel;
    @FXML private Label avgRatingLabel;
    @FXML private Label sinceLabel;

    @FXML private Label saveStatusLabel;
    @FXML private Button saveButton;

    @FXML
    public void initialize() {
        LOG.info("Employee profile initialized");

        firstNameField.setText("Peter");
        lastNameField.setText("Malý");
        emailField.setText("peter.maly@cleanmate.sk");
        phoneField.setText("+421 905 123 456");

        refreshHeader();
        roleLabel.setText("CLEANER");

        totalHoursLabel.setText("142.0 h");
        totalTasksLabel.setText("58");
        avgRatingLabel.setText("4.7 / 5");
        sinceLabel.setText("Člen od 2024-09-01");

        saveStatusLabel.setVisible(false);

        firstNameField.textProperty().addListener((o, a, b) -> { refreshHeader(); hideStatus(); });
        lastNameField.textProperty().addListener((o, a, b) -> { refreshHeader(); hideStatus(); });
        emailField.textProperty().addListener((o, a, b) -> hideStatus());
        phoneField.textProperty().addListener((o, a, b) -> hideStatus());
    }

    private void refreshHeader() {
        String fn = firstNameField.getText() == null ? "" : firstNameField.getText().trim();
        String ln = lastNameField.getText() == null ? "" : lastNameField.getText().trim();
        nameLabel.setText((fn + " " + ln).trim());
        String initials = (fn.isEmpty() ? "" : fn.substring(0, 1))
                        + (ln.isEmpty() ? "" : ln.substring(0, 1));
        avatarLabel.setText(initials.toUpperCase());
    }

    @FXML
    private void onSave() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        if (!email.matches("^[\\w.+-]+@[\\w-]+\\.[A-Za-z]{2,}$")) {
            showStatus("Neplatný email.", true);
            return;
        }
        LOG.info("Profile saved: " + nameLabel.getText() + " / " + email);
        showStatus("Profil uložený.", false);
    }

    @FXML
    private void onChangePassword() {
        LOG.info("Change password clicked");
        showStatus("Otvorí sa dialóg pre zmenu hesla (TODO).", false);
    }

    private void showStatus(String msg, boolean error) {
        saveStatusLabel.setText(msg);
        saveStatusLabel.getStyleClass().removeAll("error-label", "success-label");
        saveStatusLabel.getStyleClass().add(error ? "error-label" : "success-label");
        saveStatusLabel.setVisible(true);
        saveStatusLabel.setManaged(true);
    }

    private void hideStatus() {
        saveStatusLabel.setVisible(false);
        saveStatusLabel.setManaged(false);
    }
}
