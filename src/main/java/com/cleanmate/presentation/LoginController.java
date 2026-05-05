package com.cleanmate.presentation;

import com.cleanmate.model.Employee;
import com.cleanmate.presentation.history.EmployeeHistoryController;
import com.cleanmate.presentation.myschedule.MyScheduleController;
import com.cleanmate.presentation.nav.LanguageManager;
import com.cleanmate.presentation.nav.Route;
import com.cleanmate.presentation.nav.ViewRouter;
import com.cleanmate.presentation.profile.EmployeeProfileController;
import com.cleanmate.service.ServiceLocator;
import com.cleanmate.service.Session;
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

    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Label         errorLabel;

    @FXML
    public void initialize() {
        roleCombo.setItems(FXCollections.observableArrayList("FIRMA", "ZAMESTNANEC", "ZAKAZNIK"));
        roleCombo.getSelectionModel().selectFirst();
        errorLabel.setVisible(false);
    }

    @FXML
    private void onLogin(ActionEvent event) {
        String username = usernameField.getText()  == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText()  == null ? "" : passwordField.getText().trim();
        String role     = roleCombo.getValue();

        if (username.isEmpty() || password.isEmpty() || role == null) {
            showError(LanguageManager.getBundle().getString("login.error.fields"));
            return;
        }

        switch (role) {
            case "FIRMA"       -> loginFirma(username, password);
            case "ZAMESTNANEC" -> loginEmployee(username, password);
            case "ZAKAZNIK"    -> loginCustomer(username, password);
            default -> showError(LanguageManager.getBundle().getString("login.error.credentials"));
        }
    }

    // ── FIRMA ─────────────────────────────────────────────────────────────────

    private void loginFirma(String username, String password) {
        if (!username.equals("admin") || !password.equals("admin")) {
            showError(LanguageManager.getBundle().getString("login.error.credentials"));
            LOG.warning("FIRMA login failed for user='" + username + "'");
            return;
        }
        Session.login(Session.Role.FIRMA, "Administrator", "admin");
        LOG.info("FIRMA login OK");
        navigateTo(Route.DASHBOARD);
    }

    // ── ZAMESTNANEC — reálny login cez email + heslo z DB ────────────────────

    private void loginEmployee(String username, String password) {
        Employee emp = ServiceLocator.employees().findByEmail(username).orElse(null);

        if (emp == null || !emp.getPassword().equals(password)) {
            showError(LanguageManager.getBundle().getString("login.error.credentials"));
            LOG.warning("ZAMESTNANEC login failed for email='" + username + "'");
            return;
        }
        if (!emp.isActive()) {
            showError(LanguageManager.getBundle().getString("login.error.credentials"));
            LOG.warning("ZAMESTNANEC login refused — inactive: " + username);
            return;
        }

        Session.login(Session.Role.ZAMESTNANEC, emp.getFullName(), emp.getId());
        String name = emp.getFullName();
        MyScheduleController.employeeName      = name;
        EmployeeHistoryController.employeeName = name;
        EmployeeProfileController.employeeName = name;
        LOG.info("ZAMESTNANEC login OK: " + name);
        navigateTo(Route.MY_SCHEDULE);
    }

    // ── ZAKAZNIK ──────────────────────────────────────────────────────────────

    private void loginCustomer(String username, String password) {
        if (!username.equals("test3") || !password.equals("test3")) {
            showError(LanguageManager.getBundle().getString("login.error.credentials"));
            return;
        }
        Session.login(Session.Role.ZAKAZNIK, "", "");
        navigateTo(Route.PORTAL);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void navigateTo(Route route) {
        errorLabel.setVisible(false);
        ViewRouter.get().navigate(route);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    @FXML public void onLangSk() {
        LanguageManager.setLocale(Locale.forLanguageTag("sk"));
        ViewRouter.get().navigate(Route.LOGIN);
    }

    @FXML public void onLangEn() {
        LanguageManager.setLocale(Locale.ENGLISH);
        ViewRouter.get().navigate(Route.LOGIN);
    }
}
