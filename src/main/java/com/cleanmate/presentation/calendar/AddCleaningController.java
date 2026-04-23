package com.cleanmate.presentation.calendar;

import com.cleanmate.presentation.nav.BaseNavController;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.logging.Logger;

public class AddCleaningController extends BaseNavController {

    private static final Logger LOG = Logger.getLogger(AddCleaningController.class.getName());

    @FXML private ComboBox<String> propertyCombo;
    @FXML private ComboBox<String> employeeCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private DatePicker datePicker;
    @FXML private TextField checkOutField;
    @FXML private TextField checkInField;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        propertyCombo.setItems(FXCollections.observableArrayList(
                "Panská 12, BA", "Hviezdoslavovo nám. 4", "Obchodná 27",
                "Panenská 8", "Laurinská 3", "Grösslingova 45",
                "Ventúrska 7", "Michalská 22", "Sedlárska 5"));

        employeeCombo.setItems(FXCollections.observableArrayList(
                "Anna Nová", "Peter Malý", "Eva Horváthová", "Ján Kováč",
                "Tomáš Urban", "Katarína Veselá", "Milan Dvořák"));

        statusCombo.setItems(FXCollections.observableArrayList("NEW", "ASSIGNED"));
        statusCombo.getSelectionModel().selectFirst();

        datePicker.setValue(LocalDate.now());
        errorLabel.setText("");
    }

    @FXML
    private void onSave() {
        errorLabel.setText("");

        String property = propertyCombo.getValue();
        String employee = employeeCombo.getValue();
        LocalDate date = datePicker.getValue();
        String checkOutRaw = checkOutField.getText().trim();
        String checkInRaw  = checkInField.getText().trim();

        if (property == null) {
            errorLabel.setText("Vyberte apartmán.");
            return;
        }
        if (date == null) {
            errorLabel.setText("Zadajte dátum.");
            return;
        }
        if (checkOutRaw.isEmpty()) {
            errorLabel.setText("Zadajte čas CHECK-OUT (napr. 09:00).");
            return;
        }
        if (checkInRaw.isEmpty()) {
            errorLabel.setText("Zadajte čas CHECK-IN (napr. 12:00).");
            return;
        }

        LocalTime checkOut;
        LocalTime checkIn;
        try {
            checkOut = LocalTime.parse(checkOutRaw);
        } catch (DateTimeParseException e) {
            errorLabel.setText("Neplatný formát CHECK-OUT. Použite HH:mm (napr. 09:00).");
            return;
        }
        try {
            checkIn = LocalTime.parse(checkInRaw);
        } catch (DateTimeParseException e) {
            errorLabel.setText("Neplatný formát CHECK-IN. Použite HH:mm (napr. 12:00).");
            return;
        }

        if (!checkIn.isAfter(checkOut)) {
            errorLabel.setText("CHECK-IN musí byť neskôr ako CHECK-OUT.");
            return;
        }

        LOG.info(String.format("New cleaning saved: %s | %s | %s | CO %s → CI %s | status %s",
                date, property, employee == null ? "—" : employee,
                checkOut, checkIn, statusCombo.getValue()));

        navCalendar();
    }

    @FXML
    private void onBack() {
        navCalendar();
    }
}
