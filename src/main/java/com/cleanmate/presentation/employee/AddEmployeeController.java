package com.cleanmate.presentation.employee;

import com.cleanmate.presentation.nav.BaseNavController;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.logging.Logger;

public class AddEmployeeController extends BaseNavController {

    private static final Logger LOG = Logger.getLogger(AddEmployeeController.class.getName());

    /** Pre-populate fields when editing an existing employee. Null = add mode. */
    public static EmployeeRow editTarget = null;

    @FXML private Label pageTitle;
    @FXML private Label pageSubtitle;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private TextField startDateField;
    @FXML private TextField addressField;
    @FXML private TextArea notesArea;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        roleCombo.setItems(FXCollections.observableArrayList("CLEANER", "SUPERVISOR"));
        roleCombo.getSelectionModel().selectFirst();

        if (editTarget != null) {
            pageTitle.setText("Upraviť zamestnanca");
            pageSubtitle.setText("Zmena informácií zamestnanca");
            String[] parts = editTarget.getName().split(" ", 2);
            firstNameField.setText(parts.length > 0 ? parts[0] : "");
            lastNameField.setText(parts.length > 1 ? parts[1] : "");
            roleCombo.setValue(editTarget.getRole());
            editTarget = null;
        }

        errorLabel.setText("");
    }

    @FXML
    private void onSave() {
        errorLabel.setText("");

        String firstName = firstNameField.getText() == null ? "" : firstNameField.getText().trim();
        String lastName  = lastNameField.getText()  == null ? "" : lastNameField.getText().trim();
        String email     = emailField.getText()     == null ? "" : emailField.getText().trim();
        String phone     = phoneField.getText()     == null ? "" : phoneField.getText().trim();
        String role      = roleCombo.getValue();

        if (firstName.isEmpty()) { errorLabel.setText("Zadajte meno."); return; }
        if (lastName.isEmpty())  { errorLabel.setText("Zadajte priezvisko."); return; }
        if (email.isEmpty())     { errorLabel.setText("Zadajte emailovú adresu."); return; }
        if (phone.isEmpty())     { errorLabel.setText("Zadajte telefónne číslo."); return; }

        String fullName = firstName + " " + lastName;
        LOG.info(String.format("Saved employee: %s | %s | %s | %s", fullName, role, email, phone));

        navEmployees();
    }

    @FXML
    private void onBack() { navBack(); }
}
