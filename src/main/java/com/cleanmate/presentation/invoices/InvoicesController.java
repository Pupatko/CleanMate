package com.cleanmate.presentation.invoices;

import com.cleanmate.service.ServiceLocator;
import com.cleanmate.presentation.nav.BaseNavController;
import com.cleanmate.presentation.util.EmptyState;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

public class InvoicesController extends BaseNavController {

    private static final Logger LOG = Logger.getLogger(InvoicesController.class.getName());
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @FXML private Label yearTotalLabel;
    @FXML private Label unpaidLabel;
    @FXML private Label countLabel;

    @FXML private TableView<Invoice> table;
    @FXML private TableColumn<Invoice, String> colNumber;
    @FXML private TableColumn<Invoice, String> colPeriod;
    @FXML private TableColumn<Invoice, LocalDate> colDueDate;
    @FXML private TableColumn<Invoice, Number> colCleanings;
    @FXML private TableColumn<Invoice, Number> colAmount;
    @FXML private TableColumn<Invoice, String> colStatus;

    @FXML private Button downloadButton;

    private final ObservableList<Invoice> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        LOG.info("Invoices initialized");

        final double RATE = 40.0;
        var locale = java.util.Locale.of("sk");
        var monthFmt = java.time.format.DateTimeFormatter.ofPattern("LLLL yyyy", locale);
        LocalDate current = LocalDate.now().withDayOfMonth(1);

        ServiceLocator.cleanings().getAll().stream()
                .filter(c -> "DONE".equals(c.status()))
                .collect(java.util.stream.Collectors.groupingBy(
                        c -> c.date().withDayOfMonth(1)))
                .entrySet().stream()
                .sorted((a, b) -> b.getKey().compareTo(a.getKey()))
                .forEach(entry -> {
                    LocalDate month = entry.getKey();
                    int count = entry.getValue().size();
                    String number = String.format("%d-%02d-001", month.getYear(), month.getMonthValue());
                    String period = month.format(monthFmt);
                    period = period.substring(0, 1).toUpperCase() + period.substring(1);
                    LocalDate due = month.plusMonths(1).withDayOfMonth(15);
                    String status = month.isBefore(current) ? "PAID" : "UNPAID";
                    data.add(new Invoice(number, period, due, count, count * RATE, status));
                });

        colNumber.setCellValueFactory(new PropertyValueFactory<>("number"));
        colPeriod.setCellValueFactory(new PropertyValueFactory<>("period"));
        colDueDate.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        colDueDate.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v.format(DF));
            }
        });
        colCleanings.setCellValueFactory(new PropertyValueFactory<>("cleanings"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colAmount.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Number v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("%.2f €", v.doubleValue()));
                setStyle("-fx-font-weight: bold;");
            }
        });
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(v);
                badge.getStyleClass().setAll("status-badge",
                        "PAID".equals(v) ? "avail-available" : "avail-on_duty");
                setGraphic(badge);
                setText(null);
            }
        });

        table.setItems(data);
        table.setPlaceholder(EmptyState.build("🧾", "empty.invoices"));
        table.getSelectionModel().selectedItemProperty().addListener(
                (obs, o, n) -> downloadButton.setDisable(n == null));
        downloadButton.setDisable(true);

        int currentYear = LocalDate.now().getYear();
        double yearTotal = data.stream()
                .filter(i -> i.getDueDate().getYear() == currentYear)
                .mapToDouble(Invoice::getAmount).sum();
        double unpaid = data.stream()
                .filter(i -> "UNPAID".equals(i.getStatus()))
                .mapToDouble(Invoice::getAmount).sum();

        yearTotalLabel.setText(String.format("%.2f €", yearTotal));
        unpaidLabel.setText(String.format("%.2f €", unpaid));
        countLabel.setText(String.valueOf(data.size()));
    }

    @FXML
    private void onDownload() {
        Invoice sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        FileChooser fc = new FileChooser();
        fc.setTitle("Uložiť faktúru");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML", "*.xml"));
        fc.setInitialFileName("faktura-" + sel.getNumber() + ".xml");
        File out = fc.showSaveDialog(table.getScene().getWindow());
        if (out == null) return;
        try {
            Files.writeString(out.toPath(), buildXml(sel), StandardCharsets.UTF_8);
            LOG.info("Invoice exported: " + out.getAbsolutePath());
        } catch (IOException ex) {
            LOG.warning("Download failed: " + ex.getMessage());
        }
    }

    private String buildXml(Invoice i) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
             + "<invoice number=\"" + i.getNumber() + "\">\n"
             + "  <period>" + i.getPeriod() + "</period>\n"
             + "  <dueDate>" + i.getDueDate() + "</dueDate>\n"
             + "  <cleanings>" + i.getCleanings() + "</cleanings>\n"
             + "  <amount currency=\"EUR\">" + String.format("%.2f", i.getAmount()) + "</amount>\n"
             + "  <status>" + i.getStatus() + "</status>\n"
             + "</invoice>\n";
    }

    public static class Invoice {
        private final SimpleStringProperty number;
        private final SimpleStringProperty period;
        private final SimpleObjectProperty<LocalDate> dueDate;
        private final SimpleIntegerProperty cleanings;
        private final SimpleDoubleProperty amount;
        private final SimpleStringProperty status;

        public Invoice(String number, String period, LocalDate dueDate,
                       int cleanings, double amount, String status) {
            this.number = new SimpleStringProperty(number);
            this.period = new SimpleStringProperty(period);
            this.dueDate = new SimpleObjectProperty<>(dueDate);
            this.cleanings = new SimpleIntegerProperty(cleanings);
            this.amount = new SimpleDoubleProperty(amount);
            this.status = new SimpleStringProperty(status);
        }
        public String getNumber() { return number.get(); }
        public String getPeriod() { return period.get(); }
        public LocalDate getDueDate() { return dueDate.get(); }
        public int getCleanings() { return cleanings.get(); }
        public double getAmount() { return amount.get(); }
        public String getStatus() { return status.get(); }
    }
}
