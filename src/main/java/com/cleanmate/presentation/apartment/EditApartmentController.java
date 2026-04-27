package com.cleanmate.presentation.apartment;

import com.cleanmate.presentation.nav.BaseNavController;
import com.cleanmate.presentation.nav.LanguageManager;
import com.cleanmate.presentation.util.ChangeSummary;
import com.cleanmate.presentation.util.ConfirmDialog;
import com.cleanmate.presentation.util.ToastManager;

import java.text.MessageFormat;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class EditApartmentController extends BaseNavController {

    private static final Logger LOG = Logger.getLogger(EditApartmentController.class.getName());

    /** Null = add mode; otherwise the apartment being viewed/edited. */
    public static ApartmentItem editTarget = null;

    @FXML private Label pageTitle;
    @FXML private Label pageSubtitle;
    @FXML private Label planHint;
    @FXML private Label errorLabel;

    @FXML private TextField addressField;
    @FXML private TextField customerField;
    @FXML private TextField roomsField;
    @FXML private TextField areaField;
    @FXML private TextArea noteArea;

    @FXML private ListView<ApartmentTask> tasksView;
    @FXML private VBox taskEditor;
    @FXML private TextField taskNameField;
    @FXML private ComboBox<ApartmentTask.Type> taskTypeCombo;
    @FXML private TextArea taskDescArea;

    @FXML private Button editButton;
    @FXML private Button cancelEditButton;
    @FXML private Button confirmButton;
    @FXML private Button deleteButton;

    private ApartmentItem target;
    private boolean addMode;

    // Snapshot of original state (for change diff and cancel)
    private String origAddress, origCustomer, origNote;
    private String origRooms, origArea;
    private List<ApartmentTask> origTasks;

    @FXML
    public void initialize() {
        taskTypeCombo.setItems(FXCollections.observableArrayList(ApartmentTask.Type.values()));
        taskTypeCombo.getSelectionModel().selectFirst();

        target  = editTarget;
        addMode = (target == null);
        editTarget = null;

        if (addMode) {
            pageTitle.setText(LanguageManager.getBundle().getString("edit.apartment.new.title"));
            pageSubtitle.setText(LanguageManager.getBundle().getString("edit.apartment.new.subtitle"));
            deleteButton.setVisible(false);
            deleteButton.setManaged(false);
            // In add mode, start directly in edit mode so user can fill in
            tasksView.setItems(FXCollections.observableArrayList());
            setEditMode(true);
            // Replace Upraviť with nothing, Confirm becomes Save
            editButton.setVisible(false);
            editButton.setManaged(false);
            confirmButton.setText(LanguageManager.getBundle().getString("edit.apartment.save"));
            cancelEditButton.setText(LanguageManager.getBundle().getString("btn.cancel"));
        } else {
            populateFromTarget();
            setEditMode(false);
            deleteButton.setVisible(true);
            deleteButton.setManaged(true);
        }

        errorLabel.setText("");
    }

    private void populateFromTarget() {
        pageTitle.setText(target.getAddress());
        pageSubtitle.setText(LanguageManager.getBundle().getString("edit.apartment.view.subtitle.prefix") + " " + target.getCustomer());

        addressField.setText(target.getAddress());
        customerField.setText(target.getCustomer());
        roomsField.setText(String.valueOf(target.getRooms()));
        areaField.setText(String.valueOf(target.getArea()));
        noteArea.setText(target.getNote());
        tasksView.setItems(target.getTasks());
    }

    private void snapshotOriginal() {
        origAddress  = addressField.getText();
        origCustomer = customerField.getText();
        origRooms    = roomsField.getText();
        origArea     = areaField.getText();
        origNote     = noteArea.getText();
        origTasks    = new ArrayList<>(tasksView.getItems());
    }

    private void setEditMode(boolean editing) {
        addressField.setEditable(editing);
        customerField.setEditable(editing);
        roomsField.setEditable(editing);
        areaField.setEditable(editing);
        noteArea.setEditable(editing);

        taskEditor.setVisible(editing);
        taskEditor.setManaged(editing);
        planHint.setVisible(!editing);
        planHint.setManaged(!editing);

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
        for (TextField f : new TextField[]{addressField, customerField, roomsField, areaField}) {
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
        if (addMode) {
            navApartments();
            return;
        }
        addressField.setText(origAddress);
        customerField.setText(origCustomer);
        roomsField.setText(origRooms);
        areaField.setText(origArea);
        noteArea.setText(origNote);
        target.getTasks().setAll(origTasks);
        setEditMode(false);
    }

    @FXML
    private void onConfirm() {
        errorLabel.setText("");

        String address  = addressField.getText() == null ? "" : addressField.getText().trim();
        String customer = customerField.getText() == null ? "" : customerField.getText().trim();
        String note     = noteArea.getText() == null ? "" : noteArea.getText().trim();

        if (address.isEmpty())  { errorLabel.setText(LanguageManager.getBundle().getString("error.apartment.address")); return; }
        if (customer.isEmpty()) { errorLabel.setText(LanguageManager.getBundle().getString("error.apartment.customer")); return; }

        int rooms;
        double area;
        try { rooms = Integer.parseInt(roomsField.getText().trim()); }
        catch (NumberFormatException ex) { errorLabel.setText(LanguageManager.getBundle().getString("error.apartment.rooms")); return; }
        try { area = Double.parseDouble(areaField.getText().trim()); }
        catch (NumberFormatException ex) { errorLabel.setText(LanguageManager.getBundle().getString("error.apartment.area")); return; }

        if (addMode) {
            ApartmentItem created = new ApartmentItem(address, customer, rooms, area, note);
            created.getTasks().setAll(tasksView.getItems());
            ApartmentManagementController.addApartment(created);
            LOG.info("Created apartment: " + address);
            toast(LanguageManager.getBundle().getString("toast.apartment.saved"), ToastManager.Type.SUCCESS);
            navApartments();
            return;
        }

        ChangeSummary diff = new ChangeSummary()
                .add("Adresa",   origAddress,  address)
                .add("Zákazník", origCustomer, customer)
                .add("Izby",     origRooms,    String.valueOf(rooms))
                .add("Plocha",   origArea,     String.valueOf(area))
                .add("Poznámka", origNote,     note);

        int before = origTasks.size();
        int after  = tasksView.getItems().size();
        if (before != after) diff.add("Počet úloh v pláne", before, after);

        target.setAddress(address);
        target.setCustomer(customer);
        target.setRooms(rooms);
        target.setArea(area);
        target.setNote(note);

        setEditMode(false);
        pageTitle.setText(target.getAddress());
        pageSubtitle.setText(LanguageManager.getBundle().getString("edit.apartment.view.subtitle.prefix") + " " + target.getCustomer());
        toast(LanguageManager.getBundle().getString("toast.apartment.saved"), ToastManager.Type.SUCCESS);
        diff.show("Úpravy apartmánu");
    }

    @FXML
    private void onAddTask() {
        String name = taskNameField.getText() == null ? "" : taskNameField.getText().trim();
        if (name.isEmpty()) { errorLabel.setText(LanguageManager.getBundle().getString("error.apartment.task.name")); return; }
        errorLabel.setText("");
        ApartmentTask.Type type = taskTypeCombo.getValue();
        String desc = taskDescArea.getText() == null ? "" : taskDescArea.getText().trim();

        ObservableList<ApartmentTask> items = tasksView.getItems();
        items.add(new ApartmentTask(name, desc, type));

        taskNameField.clear();
        taskDescArea.clear();
        taskTypeCombo.getSelectionModel().selectFirst();
    }

    @FXML
    private void onRemoveTask() {
        ApartmentTask sel = tasksView.getSelectionModel().getSelectedItem();
        if (sel != null) tasksView.getItems().remove(sel);
    }

    @FXML
    private void onDelete() {
        if (target == null) return;
        String msg = MessageFormat.format(
                LanguageManager.getBundle().getString("confirm.delete.apartment.content"),
                target.getAddress());
        if (!ConfirmDialog.show("confirm.delete.apartment.header", msg)) return;
        ApartmentManagementController.removeApartment(target);
        LOG.info("Deleted apartment: " + target.getAddress());
        toast(LanguageManager.getBundle().getString("toast.apartment.deleted"), ToastManager.Type.INFO);
        navApartments();
    }

    @FXML
    private void onBack() { navApartments(); }
}
