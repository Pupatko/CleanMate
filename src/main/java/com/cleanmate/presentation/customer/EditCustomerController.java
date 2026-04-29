package com.cleanmate.presentation.customer;

import com.cleanmate.model.Customer;
import com.cleanmate.presentation.nav.BaseNavController;
import com.cleanmate.presentation.nav.LanguageManager;
import com.cleanmate.presentation.util.ChangeSummary;
import com.cleanmate.presentation.util.ConfirmDialog;
import com.cleanmate.presentation.util.ToastManager;
import com.cleanmate.service.ServiceLocator;
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
    @FXML private Button deleteButton;
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
            deleteButton.setVisible(false);
            deleteButton.setManaged(false);
            setEditMode(true);
            editButton.setVisible(false);
            editButton.setManaged(false);
            confirmButton.setVisible(true);
            confirmButton.setManaged(true);
            cancelEditButton.setVisible(true);
            cancelEditButton.setManaged(true);
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
            Customer c = Customer.create(name, email, phone, note);
            ServiceLocator.customers().save(c);
            LOG.info("Created customer: " + name);
            toast(LanguageManager.getBundle().getString("toast.customer.saved"), ToastManager.Type.SUCCESS);
            navCustomers();
            return;
        }

        ChangeSummary diff = new ChangeSummary()
                .add("Meno",     origName,  name)
                .add("Email",    origEmail, email)
                .add("Telefón",  origPhone, phone)
                .add("Poznámka", origNote,  note);

        ServiceLocator.customers().findById(target.getId()).ifPresent(c -> {
            c.setName(name);
            c.setEmail(email);
            c.setPhone(phone);
            c.setNotes(note);
            ServiceLocator.customers().save(c);
        });

        target.setName(name);
        target.setEmail(email);
        target.setPhone(phone);
        target.setNote(note);

        setEditMode(false);
        pageTitle.setText(target.getName());
        toast(LanguageManager.getBundle().getString("toast.customer.saved"), ToastManager.Type.SUCCESS);
        diff.show("Úpravy zákazníka");
    }

    @FXML
    private void onDelete() {
        if (target == null) return;
        String msg = java.text.MessageFormat.format(
                LanguageManager.getBundle().getString("confirm.delete.customer.content"),
                target.getName());
        if (!ConfirmDialog.show("confirm.delete.customer.header", msg)) return;
        ServiceLocator.customers().delete(target.getId());
        LOG.info("Deleted customer: " + target.getName());
        toast(LanguageManager.getBundle().getString("toast.customer.deleted"), ToastManager.Type.INFO);
        navCustomers();
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
