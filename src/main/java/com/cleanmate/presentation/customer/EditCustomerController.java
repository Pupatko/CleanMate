package com.cleanmate.presentation.customer;

import com.cleanmate.presentation.nav.BaseNavController;
import com.cleanmate.presentation.nav.LanguageManager;
import com.cleanmate.presentation.util.ChangeSummary;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.logging.Logger;

public class EditCustomerController extends BaseNavController {

    private static final Logger LOG = Logger.getLogger(EditCustomerController.class.getName());

    /** Null = add mode; otherwise the customer being viewed/edited. */
    public static CustomerRow editTarget = null;

    @FXML private Label pageTitle;
    @FXML private Label pageSubtitle;
    @FXML private Label errorLabel;

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextArea  noteArea;

    @FXML private Button editButton;
    @FXML private Button cancelEditButton;
    @FXML private Button confirmButton;
    @FXML private Button invoicesButton;

    private CustomerRow target;
    private boolean addMode;

    private String origName, origEmail, origPhone, origNote;

    @FXML
    public void initialize() {
        target  = editTarget;
        addMode = (target == null);
        editTarget = null;

        if (addMode) {
            pageTitle.setText(LanguageManager.getBundle().getString("edit.customer.new.title"));
            pageSubtitle.setText(LanguageManager.getBundle().getString("edit.customer.new.subtitle"));
            setEditMode(true);
            editButton.setVisible(false);
            editButton.setManaged(false);
            confirmButton.setText(LanguageManager.getBundle().getString("edit.customer.save"));
            cancelEditButton.setText(LanguageManager.getBundle().getString("btn.cancel"));
            invoicesButton.setVisible(false);
            invoicesButton.setManaged(false);
        } else {
            pageTitle.setText(target.getName());
            pageSubtitle.setText(LanguageManager.getBundle().getString("edit.customer.view.subtitle"));
            nameField.setText(target.getName());
            emailField.setText(target.getEmail());
            phoneField.setText(target.getPhone());
            noteArea.setText(target.getNote());
            setEditMode(false);
        }

        errorLabel.setText("");
    }

    private void snapshotOriginal() {
        origName  = nameField.getText();
        origEmail = emailField.getText();
        origPhone = phoneField.getText();
        origNote  = noteArea.getText();
    }

    private void setEditMode(boolean editing) {
        nameField.setEditable(editing);
        emailField.setEditable(editing);
        phoneField.setEditable(editing);
        noteArea.setEditable(editing);

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
        for (TextField f : new TextField[]{nameField, emailField, phoneField}) {
            f.getStyleClass().removeAll(styleOn, styleOff);
            f.getStyleClass().add(editing ? styleOn : styleOff);
        }
        noteArea.getStyleClass().removeAll(styleOn, styleOff);
        noteArea.getStyleClass().add(editing ? styleOn : styleOff);
    }

    @FXML
    private void onEdit() {
        snapshotOriginal();
        setEditMode(true);
    }

    @FXML
    private void onCancelEdit() {
        if (addMode) { navCustomers(); return; }
        nameField.setText(origName);
        emailField.setText(origEmail);
        phoneField.setText(origPhone);
        noteArea.setText(origNote);
        setEditMode(false);
    }

    @FXML
    private void onConfirm() {
        errorLabel.setText("");
        String name  = nameField.getText() == null ? "" : nameField.getText().trim();
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String phone = phoneField.getText() == null ? "" : phoneField.getText().trim();
        String note  = noteArea.getText() == null ? "" : noteArea.getText().trim();

        if (name.isEmpty())  { errorLabel.setText(LanguageManager.getBundle().getString("error.customer.name")); return; }
        if (email.isEmpty()) { errorLabel.setText(LanguageManager.getBundle().getString("error.customer.email")); return; }
        if (phone.isEmpty()) { errorLabel.setText(LanguageManager.getBundle().getString("error.customer.phone")); return; }

        if (addMode) {
            CustomerManagementController.addCustomer(new CustomerRow(name, email, phone, 0, note));
            LOG.info("Created customer: " + name);
            navCustomers();
            return;
        }

        ChangeSummary diff = new ChangeSummary()
                .add("Meno",     origName,  name)
                .add("Email",    origEmail, email)
                .add("Telefón",  origPhone, phone)
                .add("Poznámka", origNote,  note);

        target.setName(name);
        target.setEmail(email);
        target.setPhone(phone);
        target.setNote(note);

        setEditMode(false);
        pageTitle.setText(target.getName());
        diff.show("Úpravy zákazníka");
    }

    @FXML
    private void onBack() { navCustomers(); }

    @FXML
    private void onShowInvoices() {
        if (addMode || target == null) return;
        CustomerInvoicesController.target = target;
        navOwnerInvoices();
    }
}
