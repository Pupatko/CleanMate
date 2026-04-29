package com.cleanmate.presentation.history;

import com.cleanmate.model.Cleaning;
import com.cleanmate.presentation.nav.BaseNavController;
import com.cleanmate.presentation.util.EmptyState;
import com.cleanmate.service.ServiceLocator;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

public class EmployeeHistoryController extends BaseNavController {

    private static final Logger LOG = Logger.getLogger(EmployeeHistoryController.class.getName());
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @FXML private ComboBox<String> rangeCombo;
    @FXML private Label countLabel;
    @FXML private Label hoursLabel;
    @FXML private Label avgRatingLabel;

    @FXML private TableView<Row> table;
    @FXML private TableColumn<Row, LocalDate> colDate;
    @FXML private TableColumn<Row, String> colProperty;
    @FXML private TableColumn<Row, String> colTime;
    @FXML private TableColumn<Row, String> colStatus;
    @FXML private TableColumn<Row, Number> colHours;
    @FXML private TableColumn<Row, Number> colRating;

    /** Set before navigating here to filter history by employee. */
    public static String employeeName = null;

    private final ObservableList<Row> data = FXCollections.observableArrayList();
    private FilteredList<Row> filtered;

    @FXML
    public void initialize() {
        LOG.info("Employee history initialized");

        String emp = employeeName;
        employeeName = null;

        var timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        java.util.stream.Stream<Cleaning> stream = ServiceLocator.cleanings().getAll().stream()
                .filter(c -> "DONE".equals(c.status()));
        if (emp != null) stream = stream.filter(c -> emp.equals(c.employee()));
        stream.sorted((a, b) -> b.date().compareTo(a.date()))
              .forEach(c -> {
                  double hours = java.time.Duration.between(c.checkOut(), c.checkIn()).toMinutes() / 60.0;
                  data.add(new Row(c.date(), c.property(), c.checkOut().format(timeFmt),
                          c.status(), hours, c.qcRating()));
              });

        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDate.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v.format(DF));
            }
        });
        colProperty.setCellValueFactory(new PropertyValueFactory<>("property"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(v);
                badge.getStyleClass().setAll("status-badge", "status-" + v.toLowerCase());
                setGraphic(badge);
                setText(null);
            }
        });
        colHours.setCellValueFactory(new PropertyValueFactory<>("hours"));
        colHours.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Number v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("%.1f h", v.doubleValue()));
            }
        });
        colRating.setCellValueFactory(new PropertyValueFactory<>("rating"));
        colRating.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Number v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); return; }
                int r = v.intValue();
                setText("★".repeat(r) + "☆".repeat(5 - r));
                setStyle("-fx-text-fill: #F59E0B;");
            }
        });

        rangeCombo.setItems(FXCollections.observableArrayList(
                "Posledných 7 dní", "Posledných 30 dní", "Všetko"));
        rangeCombo.getSelectionModel().select("Posledných 30 dní");
        rangeCombo.valueProperty().addListener((obs, o, n) -> applyFilter());

        filtered = new FilteredList<>(data, r -> true);
        table.setItems(filtered);
        table.setPlaceholder(EmptyState.build("📋", "empty.history"));
        applyFilter();
    }

    private void applyFilter() {
        String r = rangeCombo.getValue();
        LocalDate cutoff = switch (r) {
            case "Posledných 7 dní"  -> LocalDate.now().minusDays(7);
            case "Posledných 30 dní" -> LocalDate.now().minusDays(30);
            default                  -> LocalDate.MIN;
        };
        filtered.setPredicate(row -> !row.getDate().isBefore(cutoff));

        double totalHours = filtered.stream().mapToDouble(Row::getHours).sum();
        double avgRating = filtered.stream().mapToInt(Row::getRating).average().orElse(0);
        countLabel.setText(String.valueOf(filtered.size()));
        hoursLabel.setText(String.format("%.1f h", totalHours));
        avgRatingLabel.setText(String.format("%.2f / 5", avgRating));
    }

    public static class Row {
        private final SimpleObjectProperty<LocalDate> date;
        private final SimpleStringProperty property;
        private final SimpleStringProperty time;
        private final SimpleStringProperty status;
        private final javafx.beans.property.SimpleDoubleProperty hours;
        private final SimpleIntegerProperty rating;

        public Row(LocalDate date, String property, String time, String status, double hours, int rating) {
            this.date = new SimpleObjectProperty<>(date);
            this.property = new SimpleStringProperty(property);
            this.time = new SimpleStringProperty(time);
            this.status = new SimpleStringProperty(status);
            this.hours = new javafx.beans.property.SimpleDoubleProperty(hours);
            this.rating = new SimpleIntegerProperty(rating);
        }
        public LocalDate getDate() { return date.get(); }
        public String getProperty() { return property.get(); }
        public String getTime() { return time.get(); }
        public String getStatus() { return status.get(); }
        public double getHours() { return hours.get(); }
        public int getRating() { return rating.get(); }
    }
}
