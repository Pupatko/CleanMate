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
    @FXML private TextField checkOutTime;
    @FXML private TextField checkInTime;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        propertyCombo.setItems(FXCollections.observableArrayList(
                "Panská 12, BA", "Hviezdoslavovo nám. 4", "Obchodná 27",
                "Panenská 8", "Laurinská 3", "Grösslingova 45",
                "Ventúrska 7", "Michalská 22", "Sedlárska 5", "Kapitulská 18"));

        employeeCombo.setItems(FXCollections.observableArrayList(
                "Anna Nová", "Peter Malý", "Eva Horváthová", "Ján Kováč",
                "Tomáš Urban", "Katarína Veselá", "Milan Dvořák"));

        statusCombo.setItems(FXCollections.observableArrayList("NEW", "ASSIGNED"));
        statusCombo.getSelectionModel().selectFirst();

        datePicker.setValue(LocalDate.now());
        checkOutTime.setText("09:00");
        checkInTime.setText("12:00");

        errorLabel.setText("");
    }

    @FXML
    private void onSave() {
        errorLabel.setText("");

        String property  = propertyCombo.getValue();
        String employee  = employeeCombo.getValue();
        String status    = statusCombo.getValue();
        LocalDate date   = datePicker.getValue();
        String coTimeRaw = checkOutTime.getText() == null ? "" : checkOutTime.getText().trim();
        String ciTimeRaw = checkInTime.getText()  == null ? "" : checkInTime.getText().trim();

        if (property == null)    { errorLabel.setText("Vyberte apartmán."); return; }
        if (date == null)        { errorLabel.setText("Zadajte dátum upratovania."); return; }
        if (coTimeRaw.isEmpty()) { errorLabel.setText("Zadajte čas CHECK-OUT (HH:mm)."); return; }
        if (ciTimeRaw.isEmpty()) { errorLabel.setText("Zadajte čas CHECK-IN (HH:mm)."); return; }

        LocalTime coTime;
        LocalTime ciTime;
        try { coTime = LocalTime.parse(coTimeRaw); }
        catch (DateTimeParseException e) { errorLabel.setText("Neplatný formát CHECK-OUT (napr. 09:00)."); return; }
        try { ciTime = LocalTime.parse(ciTimeRaw); }
        catch (DateTimeParseException e) { errorLabel.setText("Neplatný formát CHECK-IN (napr. 12:00)."); return; }

        if (!ciTime.isAfter(coTime)) {
            errorLabel.setText("Čas CHECK-IN musí byť neskôr ako CHECK-OUT.");
            return;
        }

        CleaningCalendarController.addEvent(
                new CleaningCalendarController.CalendarCleaningItem(
                        date, coTime, ciTime,
                        property,
                        "—",
                        employee == null ? "—" : employee,
                        status == null ? "NEW" : status));

        LOG.info(String.format("Saved new cleaning: %s | %s %s–%s | %s | %s",
                property, date, coTime, ciTime, employee, status));

        toast(com.cleanmate.presentation.nav.LanguageManager.getBundle().getString("toast.cleaning.saved"),
              com.cleanmate.presentation.util.ToastManager.Type.SUCCESS);
        navCalendar();
    }

    @FXML
    private void onBack() { navCalendar(); }
}
