package com.cleanmate.presentation.employee;

import com.cleanmate.presentation.nav.BaseNavController;
import com.cleanmate.presentation.nav.LanguageManager;
import com.cleanmate.presentation.util.ChangeSummary;
import com.cleanmate.presentation.util.ToastManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

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
            setEditMode(true);
            editButton.setVisible(false);
            editButton.setManaged(false);
            confirmButton.setText(LanguageManager.getBundle().getString("add.employee.save"));
            cancelEditButton.setText(LanguageManager.getBundle().getString("btn.cancel"));
        } else {
            pageTitle.setText(target.getName());
            pageSubtitle.setText(LanguageManager.getBundle().getString("add.employee.view.subtitle"));
            String[] parts = target.getName().split(" ", 2);
            firstNameField.setText(parts.length > 0 ? parts[0] : "");
            lastNameField.setText(parts.length > 1 ? parts[1] : "");
            roleCombo.setValue(target.getRole());
            setEditMode(false);
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

        if (addMode) {
            EmployeeManagementController.addEmployee(
                    new EmployeeRow(fullName, role == null ? "CLEANER" : role, 0.0, true, "AVAILABLE"));
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
                .add("Nástup",    origStart,   startDateField.getText())
                .add("Adresa",    origAddress, addressField.getText())
                .add("Poznámky",  origNotes,   notesArea.getText());

        target.setName(fullName);
        target.setRole(role);

        setEditMode(false);
        pageTitle.setText(target.getName());
        toast(LanguageManager.getBundle().getString("toast.employee.saved"), ToastManager.Type.SUCCESS);
        diff.show("Úpravy zamestnanca");
    }

    @FXML
    private void onBack() { navEmployees(); }
}
