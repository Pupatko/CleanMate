package com.cleanmate.presentation.calendar;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

public class CleaningCalendarController extends com.cleanmate.presentation.nav.BaseNavController {

    private static final Logger LOG = Logger.getLogger(CleaningCalendarController.class.getName());
    private static final String ALL = "— všetci —";
    private static final String ALL_STATUS = "— všetky —";

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> employeeCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private ListView<CalendarCleaningItem> list;
    @FXML private Label resultsCountLabel;

    private final ObservableList<CalendarCleaningItem> data = FXCollections.observableArrayList();
    private FilteredList<CalendarCleaningItem> filtered;

    @FXML
    public void initialize() {
        LOG.info("Calendar initialized");

        data.setAll(sampleData());

        employeeCombo.setItems(FXCollections.observableArrayList(
                ALL, "Anna Nová", "Peter Malý", "Eva Horváthová", "Ján Kováč"));
        employeeCombo.getSelectionModel().selectFirst();

        statusCombo.setItems(FXCollections.observableArrayList(
                ALL_STATUS, "NEW", "ASSIGNED", "IN_PROGRESS", "DONE", "CANCELLED"));
        statusCombo.getSelectionModel().selectFirst();

        datePicker.setValue(LocalDate.now());

        filtered = new FilteredList<>(data, it -> true);
        list.setItems(filtered);
        list.setCellFactory(l -> new CleaningCell());

        datePicker.valueProperty().addListener((obs, o, n) -> applyFilter());
        employeeCombo.valueProperty().addListener((obs, o, n) -> applyFilter());
        statusCombo.valueProperty().addListener((obs, o, n) -> applyFilter());

        list.getSelectionModel().selectedItemProperty().addListener(
                (obs, o, n) -> { if (n != null) navCleaningDetail(); });

        applyFilter();
    }

    @FXML
    private void onAdd() {
        navAddCleaning();
    }

    @FXML
    private void onClearFilters() {
        datePicker.setValue(null);
        employeeCombo.getSelectionModel().selectFirst();
        statusCombo.getSelectionModel().selectFirst();
    }

    private void applyFilter() {
        LocalDate date = datePicker.getValue();
        String emp = employeeCombo.getValue();
        String st = statusCombo.getValue();

        filtered.setPredicate(item -> {
            if (date != null && !date.equals(item.date())) return false;
            if (emp != null && !ALL.equals(emp) && !emp.equals(item.employee())) return false;
            if (st != null && !ALL_STATUS.equals(st) && !st.equals(item.status())) return false;
            return true;
        });
        resultsCountLabel.setText("Zobrazených: " + filtered.size());
    }

    private ObservableList<CalendarCleaningItem> sampleData() {
        LocalDate today = LocalDate.now();
        return FXCollections.observableArrayList(
                new CalendarCleaningItem(today,              LocalTime.of(9,  0),  LocalTime.of(11, 30), "Panská 12, BA",           "Anna Nová",      "DONE"),
                new CalendarCleaningItem(today,              LocalTime.of(10, 30), LocalTime.of(13, 0),  "Hviezdoslavovo nám. 4",   "Peter Malý",     "IN_PROGRESS"),
                new CalendarCleaningItem(today,              LocalTime.of(11, 0),  LocalTime.of(14, 0),  "Obchodná 27",             "—",              "NEW"),
                new CalendarCleaningItem(today,              LocalTime.of(13, 0),  LocalTime.of(15, 30), "Panenská 8",              "Eva Horváthová", "ASSIGNED"),
                new CalendarCleaningItem(today,              LocalTime.of(15, 0),  LocalTime.of(17, 30), "Laurinská 3",             "Ján Kováč",      "CANCELLED"),
                new CalendarCleaningItem(today.plusDays(1),  LocalTime.of(9,  30), LocalTime.of(12, 0),  "Grösslingova 45",         "Anna Nová",      "ASSIGNED"),
                new CalendarCleaningItem(today.plusDays(1),  LocalTime.of(12, 0),  LocalTime.of(14, 30), "Ventúrska 7",             "Peter Malý",     "NEW"),
                new CalendarCleaningItem(today.plusDays(2),  LocalTime.of(10, 0),  LocalTime.of(12, 30), "Michalská 22",            "Eva Horváthová", "ASSIGNED"),
                new CalendarCleaningItem(today.minusDays(1), LocalTime.of(14, 0),  LocalTime.of(16, 30), "Sedlárska 5",             "Ján Kováč",      "DONE")
        );
    }

    private static final class CleaningCell extends ListCell<CalendarCleaningItem> {
        private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

        @Override
        protected void updateItem(CalendarCleaningItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
                return;
            }

            Label dateLbl = new Label(item.date().format(DATE_FMT));
            dateLbl.getStyleClass().add("cal-time");

            Label timeLbl = new Label(
                    "CHECK-OUT " + item.checkOut().format(TIME_FMT)
                    + "  →  CHECK-IN " + item.checkIn().format(TIME_FMT));
            timeLbl.getStyleClass().add("cal-checkin");

            Label propLbl = new Label(item.property());
            propLbl.getStyleClass().add("cal-property");

            Label empLbl = new Label("👤 " + item.employee());
            empLbl.getStyleClass().add("cal-employee");

            VBox textBox = new VBox(4, dateLbl, timeLbl, propLbl, empLbl);
            textBox.setAlignment(Pos.CENTER_LEFT);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label badge = new Label(item.status().replace('_', ' '));
            badge.getStyleClass().setAll("status-badge", "status-" + item.status().toLowerCase());

            HBox row = new HBox(16, textBox, spacer, badge);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("cal-row");

            setGraphic(row);
            setText(null);
        }
    }

    public record CalendarCleaningItem(
            LocalDate date,
            LocalTime checkOut,
            LocalTime checkIn,
            String property,
            String employee,
            String status) {}
}
