package com.cleanmate.presentation.profile;

import com.cleanmate.model.Employee;
import com.cleanmate.presentation.nav.BaseNavController;
import com.cleanmate.service.ServiceLocator;
import com.cleanmate.service.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
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

    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label passwordStatusLabel;

    private Employee currentEmployee = null;

    @FXML
    public void initialize() {
        LOG.info("Employee profile initialized");

        String userId = Session.getUserId();
        Employee found = userId == null ? null : ServiceLocator.employees().findById(userId).orElse(null);
        currentEmployee = found;

        if (found != null) {
            firstNameField.setText(found.getFirstName());
            lastNameField.setText(found.getLastName());
            emailField.setText(found.getEmail() != null ? found.getEmail() : "");
            phoneField.setText(found.getPhone() != null ? found.getPhone() : "");
            roleLabel.setText(found.getRole());
            sinceLabel.setText(found.getStartDate() != null
                    ? "Člen od " + found.getStartDate() : "");

            long tasks =ServiceLocator.cleanings().getAll().stream()
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
            refreshHeader();
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
        String firstName = firstNameField.getText() == null ? "" : firstNameField.getText().trim();
        String lastName  = lastNameField.getText()  == null ? "" : lastNameField.getText().trim();
        String email     = emailField.getText()     == null ? "" : emailField.getText().trim();
        String phone     = phoneField.getText()     == null ? "" : phoneField.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty()) {
            showStatus("Meno a priezvisko sú povinné.", true);
            return;
        }
        if (!email.matches("^[\\w.+-]+@[\\w-]+\\.[A-Za-z]{2,}$")) {
            showStatus("Neplatný email.", true);
            return;
        }

        if (currentEmployee != null) {
            currentEmployee.setFirstName(firstName);
            currentEmployee.setLastName(lastName);
            currentEmployee.setEmail(email);
            currentEmployee.setPhone(phone);
            ServiceLocator.employees().save(currentEmployee);
        }

        LOG.info("Profile saved: " + firstName + " " + lastName + " / " + email);
        showStatus("Profil uložený.", false);
    }

    @FXML
    private void onSavePassword() {
        String current = currentPasswordField.getText() == null ? "" : currentPasswordField.getText();
        String newPwd  = newPasswordField.getText()     == null ? "" : newPasswordField.getText();
        String confirm = confirmPasswordField.getText() == null ? "" : confirmPasswordField.getText();

        if (current.isEmpty() || newPwd.isEmpty() || confirm.isEmpty()) {
            showPasswordStatus("Vyplňte všetky polia.", true); return;
        }
        if (newPwd.length() < 4) {
            showPasswordStatus("Nové heslo musí mať aspoň 4 znaky.", true); return;
        }
        if (!newPwd.equals(confirm)) {
            showPasswordStatus("Nové heslá sa nezhodujú.", true); return;
        }
        if (currentEmployee == null) {
            showPasswordStatus("Profil nenájdený.", true); return;
        }

        boolean ok = ServiceLocator.employees().changePassword(currentEmployee.getId(), current, newPwd);
        if (!ok) {
            showPasswordStatus("Aktuálne heslo je nesprávne.", true); return;
        }

        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
        LOG.info("Password changed for: " + currentEmployee.getFullName());
        showPasswordStatus("Heslo úspešne zmenené.", false);
    }

    private void showPasswordStatus(String msg, boolean error) {
        passwordStatusLabel.setText(msg);
        passwordStatusLabel.getStyleClass().removeAll("error-label", "success-label");
        passwordStatusLabel.getStyleClass().add(error ? "error-label" : "success-label");
        passwordStatusLabel.setVisible(true);
        passwordStatusLabel.setManaged(true);
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
