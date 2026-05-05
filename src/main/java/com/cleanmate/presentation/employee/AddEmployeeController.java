package com.cleanmate.presentation.employee;

import com.cleanmate.model.Employee;
import com.cleanmate.presentation.nav.BaseNavController;
import com.cleanmate.presentation.nav.LanguageManager;
import com.cleanmate.presentation.util.ChangeSummary;
import com.cleanmate.presentation.util.ConfirmDialog;
import com.cleanmate.presentation.util.ToastManager;
import com.cleanmate.service.ServiceLocator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.logging.Logger;

public class AddEmployeeController extends BaseNavController {

    private static final Logger LOG = Logger.getLogger(AddEmployeeController.class.getName());

    /** Null = add mode; otherwise the employee being viewed/edited. */
    public static EmployeeRow editTarget = null;

    @FXML private Label pageTitle;
    @FXML private Label pageSubtitle;
    @FXML private Label errorLabel;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private TextField startDateField;
    @FXML private TextField addressField;
    @FXML private TextArea notesArea;

    @FXML private Button editButton;
    @FXML private Button cancelEditButton;
    @FXML private Button confirmButton;
    @FXML private Button deleteButton;

    @FXML private VBox passwordSection;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    private EmployeeRow target;
    private boolean addMode;

    private String origFirst, origLast, origEmail, origPhone, origRole, origStart, origAddress, origNotes;

    @FXML
    public void initialize() {
        roleCombo.setItems(FXCollections.observableArrayList("CLEANER", "SUPERVISOR"));
        roleCombo.getSelectionModel().selectFirst();

        target  = editTarget;
        addMode = (target == null);
        editTarget = null;

        if (addMode) {
            pageTitle.setText(LanguageManager.getBundle().getString("add.employee.page.title"));
            pageSubtitle.setText(LanguageManager.getBundle().getString("add.employee.page.subtitle"));
            deleteButton.setVisible(false);
            deleteButton.setManaged(false);
            setEditMode(true);
            editButton.setVisible(false);
            editButton.setManaged(false);
            confirmButton.setVisible(true);
            confirmButton.setManaged(true);
            cancelEditButton.setVisible(true);
            cancelEditButton.setManaged(true);
            confirmButton.setText(LanguageManager.getBundle().getString("add.employee.save"));
            cancelEditButton.setText(LanguageManager.getBundle().getString("btn.cancel"));
            passwordSection.setVisible(true);
            passwordSection.setManaged(true);
        } else {
            pageTitle.setText(target.getName());
            pageSubtitle.setText(LanguageManager.getBundle().getString("add.employee.view.subtitle"));
            String[] parts = target.getName().split(" ", 2);
            firstNameField.setText(parts.length > 0 ? parts[0] : "");
            lastNameField.setText(parts.length > 1 ? parts[1] : "");
            roleCombo.setValue(target.getRole());
            setEditMode(false);
            passwordSection.setVisible(false);
            passwordSection.setManaged(false);
        }

        errorLabel.setText("");
    }

    private void snapshotOriginal() {
        origFirst   = firstNameField.getText();
        origLast    = lastNameField.getText();
        origEmail   = emailField.getText();
        origPhone   = phoneField.getText();
        origRole    = roleCombo.getValue();
        origStart   = startDateField.getText();
        origAddress = addressField.getText();
        origNotes   = notesArea.getText();
    }

    private void setEditMode(boolean editing) {
        firstNameField.setEditable(editing);
        lastNameField.setEditable(editing);
        emailField.setEditable(editing);
        phoneField.setEditable(editing);
        startDateField.setEditable(editing);
        addressField.setEditable(editing);
        notesArea.setEditable(editing);
        roleCombo.setDisable(!editing);

        if (!addMode) {
            editButton.setVisible(!editing);
            editButton.setManaged(!editing);
            cancelEditButton.setVisible(editing);
            cancelEditButton.setManaged(editing);
            confirmButton.setVisible(editing);
            confirmButton.setManaged(editing);
        }

        String styleOn  = "input-edit";
        String styleOff = "input-readonly";
        for (TextField f : new TextField[]{firstNameField, lastNameField, emailField, phoneField, startDateField, addressField}) {
            f.getStyleClass().removeAll(styleOn, styleOff);
            f.getStyleClass().add(editing ? styleOn : styleOff);
        }
        notesArea.getStyleClass().removeAll(styleOn, styleOff);
        notesArea.getStyleClass().add(editing ? styleOn : styleOff);
    }

    @FXML
    private void onEdit() {
        snapshotOriginal();
        setEditMode(true);
    }

    @FXML
    private void onCancelEdit() {
        if (addMode) { navEmployees(); return; }
        firstNameField.setText(origFirst);
        lastNameField.setText(origLast);
        emailField.setText(origEmail);
        phoneField.setText(origPhone);
        roleCombo.setValue(origRole);
        startDateField.setText(origStart);
        addressField.setText(origAddress);
        notesArea.setText(origNotes);
        setEditMode(false);
    }

    @FXML
    private void onConfirm() {
        errorLabel.setText("");

        String firstName = firstNameField.getText() == null ? "" : firstNameField.getText().trim();
        String lastName  = lastNameField.getText()  == null ? "" : lastNameField.getText().trim();
        String email     = emailField.getText()     == null ? "" : emailField.getText().trim();
        String phone     = phoneField.getText()     == null ? "" : phoneField.getText().trim();
        String role      = roleCombo.getValue();

        if (firstName.isEmpty()) { errorLabel.setText(LanguageManager.getBundle().getString("error.employee.firstname")); return; }
        if (lastName.isEmpty())  { errorLabel.setText(LanguageManager.getBundle().getString("error.employee.lastname")); return; }
        if (email.isEmpty())     { errorLabel.setText(LanguageManager.getBundle().getString("error.employee.email")); return; }
        if (phone.isEmpty())     { errorLabel.setText(LanguageManager.getBundle().getString("error.employee.phone")); return; }

        String fullName = firstName + " " + lastName;

        String address   = addressField.getText()   == null ? "" : addressField.getText().trim();
        String startText = startDateField.getText() == null ? "" : startDateField.getText().trim();
        String notes     = notesArea.getText()      == null ? "" : notesArea.getText().trim();

        if (addMode) {
            String pwd     = passwordField.getText() == null ? "" : passwordField.getText();
            String confirm = confirmPasswordField.getText() == null ? "" : confirmPasswordField.getText();
            if (pwd.length() < 4) { errorLabel.setText("Heslo musí mať aspoň 4 znaky."); return; }
            if (!pwd.equals(confirm)) { errorLabel.setText("Heslá sa nezhodujú."); return; }

            java.time.LocalDate startDate = null;
            if (!startText.isEmpty()) {
                try { startDate = java.time.LocalDate.parse(startText); } catch (Exception ignored) {}
            }
            Employee e = Employee.create(firstName, lastName, email, phone,
                    role == null ? "CLEANER" : role, address, startDate, notes);
            e.setPassword(pwd);
            ServiceLocator.employees().save(e);
            LOG.info("Created employee: " + fullName);
            toast(LanguageManager.getBundle().getString("toast.employee.saved"), ToastManager.Type.SUCCESS);
            navEmployees();
            return;
        }

        ChangeSummary diff = new ChangeSummary()
                .add("Meno",      origFirst + " " + origLast, fullName)
                .add("Rola",      origRole,    role)
                .add("Email",     origEmail,   email)
                .add("Telefón",   origPhone,   phone)
                .add("Nástup",    origStart,   startText)
                .add("Adresa",    origAddress, address)
                .add("Poznámky",  origNotes,   notes);

        ServiceLocator.employees().findById(target.getId()).ifPresent(e -> {
            String[] parts = fullName.split(" ", 2);
            e.setFirstName(parts[0]);
            e.setLastName(parts.length > 1 ? parts[1] : "");
            e.setEmail(email);
            e.setPhone(phone);
            e.setRole(role == null ? "CLEANER" : role);
            e.setAddress(address);
            e.setNotes(notes);
            if (!startText.isEmpty()) {
                try { e.setStartDate(java.time.LocalDate.parse(startText)); } catch (Exception ignored) {}
            }
            ServiceLocator.employees().save(e);
        });

        target.setName(fullName);
        target.setRole(role);

        setEditMode(false);
        pageTitle.setText(target.getName());
        toast(LanguageManager.getBundle().getString("toast.employee.saved"), ToastManager.Type.SUCCESS);
        diff.show("Úpravy zamestnanca");
    }

    @FXML
    private void onDelete() {
        if (target == null) return;
        String msg = java.text.MessageFormat.format(
                LanguageManager.getBundle().getString("confirm.delete.employee.content"),
                target.getName());
        if (!ConfirmDialog.show("confirm.delete.employee.header", msg)) return;
        ServiceLocator.employees().delete(target.getId());
        LOG.info("Deleted employee: " + target.getName());
        toast(LanguageManager.getBundle().getString("toast.employee.deleted"), ToastManager.Type.INFO);
        navEmployees();
    }

    @FXML
    private void onBack() { navEmployees(); }
}
