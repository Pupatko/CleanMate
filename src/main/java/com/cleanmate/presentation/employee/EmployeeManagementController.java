package com.cleanmate.presentation.employee;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.logging.Logger;

public class EmployeeManagementController {

    private static final Logger LOG = Logger.getLogger(EmployeeManagementController.class.getName());
    private static final String ALL = "— všetci —";

    @FXML private TextField searchField;
    @FXML private ComboBox<String> availabilityFilter;

    @FXML private TableView<EmployeeRow> table;
    @FXML private TableColumn<EmployeeRow, String> colName;
    @FXML private TableColumn<EmployeeRow, String> colRole;
    @FXML private TableColumn<EmployeeRow, Number> colHours;
    @FXML private TableColumn<EmployeeRow, String> colAvailability;
    @FXML private TableColumn<EmployeeRow, Boolean> colActive;

    @FXML private Label countLabel;
    @FXML private Button editButton;
    @FXML private Button deactivateButton;

    private final ObservableList<EmployeeRow> data = FXCollections.observableArrayList();
    private FilteredList<EmployeeRow> filtered;

    @FXML
    public void initialize() {
        LOG.info("Employee management initialized");

        data.setAll(
                new EmployeeRow("Anna Nová",       "CLEANER",    128.5, true,  "AVAILABLE"),
                new EmployeeRow("Peter Malý",      "CLEANER",    142.0, true,  "ON_DUTY"),
                new EmployeeRow("Eva Horváthová",  "SUPERVISOR", 160.0, true,  "ON_DUTY"),
                new EmployeeRow("Ján Kováč",       "CLEANER",     94.5, true,  "AVAILABLE"),
                new EmployeeRow("Mária Tóthová",   "CLEANER",      0.0, false, "INACTIVE"),
                new EmployeeRow("Tomáš Urban",     "CLEANER",     72.0, true,  "OFF_DUTY"),
                new EmployeeRow("Katarína Veselá", "SUPERVISOR", 148.0, true,  "AVAILABLE"),
                new EmployeeRow("Milan Dvořák",    "CLEANER",     38.0, true,  "OFF_DUTY")
        );

        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colHours.setCellValueFactory(new PropertyValueFactory<>("monthHours"));
        colAvailability.setCellValueFactory(new PropertyValueFactory<>("availability"));

        colHours.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Number v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("%.1f h", v.doubleValue()));
            }
        });

        colAvailability.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(v.replace('_', ' '));
                badge.getStyleClass().setAll("status-badge", "avail-" + v.toLowerCase());
                setGraphic(badge);
                setText(null);
            }
        });

        colActive.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().isActive()));
        colActive.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setGraphic(null); return; }
                Label l = new Label(v ? "● Aktívny" : "○ Neaktívny");
                l.getStyleClass().setAll(v ? "active-yes" : "active-no");
                setGraphic(l);
                setText(null);
            }
        });

        availabilityFilter.setItems(FXCollections.observableArrayList(
                ALL, "AVAILABLE", "ON_DUTY", "OFF_DUTY", "INACTIVE"));
        availabilityFilter.getSelectionModel().selectFirst();

        filtered = new FilteredList<>(data, r -> true);
        table.setItems(filtered);

        searchField.textProperty().addListener((obs, o, n) -> applyFilter());
        availabilityFilter.valueProperty().addListener((obs, o, n) -> applyFilter());
        table.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            editButton.setDisable(n == null);
            deactivateButton.setDisable(n == null);
        });
        editButton.setDisable(true);
        deactivateButton.setDisable(true);

        applyFilter();
    }

    private void applyFilter() {
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String avail = availabilityFilter.getValue();

        filtered.setPredicate(r -> {
            if (!q.isEmpty()
                    && !r.getName().toLowerCase().contains(q)
                    && !r.getRole().toLowerCase().contains(q)) {
                return false;
            }
            if (avail != null && !ALL.equals(avail) && !avail.equals(r.getAvailability())) {
                return false;
            }
            return true;
        });

        countLabel.setText("Zobrazených: " + filtered.size() + " / " + data.size());
    }

    @FXML private void onAdd() { LOG.info("Add employee clicked"); }
    @FXML private void onEdit() {
        EmployeeRow r = table.getSelectionModel().getSelectedItem();
        if (r != null) LOG.info("Edit employee: " + r.getName());
    }
    @FXML private void onDeactivate() {
        EmployeeRow r = table.getSelectionModel().getSelectedItem();
        if (r != null) {
            r.activeProperty().set(false);
            r.availabilityProperty().set("INACTIVE");
            table.refresh();
            LOG.info("Deactivated: " + r.getName());
        }
    }
}
