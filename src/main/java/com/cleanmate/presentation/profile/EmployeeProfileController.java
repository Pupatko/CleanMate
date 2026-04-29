package com.cleanmate.presentation.profile;

import com.cleanmate.model.Employee;
import com.cleanmate.presentation.nav.BaseNavController;
import com.cleanmate.service.ServiceLocator;
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

    /** Set before navigating here to show a specific employee's profile. */
    public static String employeeName = null;

    @FXML
    public void initialize() {
        LOG.info("Employee profile initialized");

        String emp = employeeName;
        employeeName = null;

        Employee found = emp == null ? null : ServiceLocator.employees().getAll().stream()
                .filter(e -> e.getFullName().equals(emp)).findFirst().orElse(null);

        if (found != null) {
            firstNameField.setText(found.getFirstName());
            lastNameField.setText(found.getLastName());
            emailField.setText(found.getEmail() != null ? found.getEmail() : "");
            phoneField.setText(found.getPhone() != null ? found.getPhone() : "");
            roleLabel.setText(found.getRole());
            sinceLabel.setText(found.getStartDate() != null
                    ? "Člen od " + found.getStartDate() : "");

            java.time.LocalDate now = java.time.LocalDate.now();
            long tasks = ServiceLocator.cleanings().getAll().stream()
                    .filter(c -> "DONE".equals(c.status()) && found.getFullName().equals(c.employee()))
                    .count();
            double hours = ServiceLocator.cleanings().getAll().stream()
                    .filter(c -> "DONE".equals(c.status()) && found.getFullName().equals(c.employee()))
                    .mapToDouble(c -> java.time.Duration.between(c.checkOut(), c.checkIn()).toMinutes() / 60.0)
                    .sum();
            double avg = ServiceLocator.cleanings().getAll().stream()
                    .filter(c -> "DONE".equals(c.status()) && found.getFullName().equals(c.employee()) && c.qcRating() > 0)
                    .mapToInt(c -> c.qcRating()).average().orElse(0);

            totalHoursLabel.setText(String.format("%.1f h", hours));
            totalTasksLabel.setText(String.valueOf(tasks));
            avgRatingLabel.setText(avg == 0 ? "—" : String.format("%.1f / 5", avg));
        } else {
            firstNameField.setText("");
            lastNameField.setText("");
            emailField.setText("");
            phoneField.setText("");
            roleLabel.setText("—");
            totalHoursLabel.setText("0.0 h");
            totalTasksLabel.setText("0");
            avgRatingLabel.setText("—");
            sinceLabel.setText("");
        }

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
