package com.cleanmate.presentation.customer;

import com.cleanmate.presentation.apartment.ApartmentItem;
import com.cleanmate.presentation.calendar.CleaningCalendarController;
import com.cleanmate.presentation.calendar.CleaningCalendarController.CalendarCleaningItem;
import com.cleanmate.presentation.nav.BaseNavController;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;

import java.time.Duration;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class CustomerInvoicesController extends BaseNavController {

    /** Set before navigating to this screen. */
    public static CustomerRow target = null;

    private static final String ALL_APARTMENTS = "— všetky apartmány —";
    private static final String ALL_PERIODS   = "— celé obdobie —";
    private static final Locale SK = Locale.of("sk");
    private static final DateTimeFormatter DATE_FMT  = DateTimeFormatter.ofPattern("d.M.yyyy");
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("LLL yyyy", SK);

    // Pricing model (mock): base 25 € + 12 €/hour
    private static final double BASE_PRICE  = 25.0;
    private static final double HOURLY_RATE = 12.0;

    @FXML private Label pageTitle;
    @FXML private Label pageSubtitle;
    @FXML private ComboBox<String> apartmentCombo;
    @FXML private ComboBox<String> periodCombo;

    @FXML private Label statTotal;
    @FXML private Label statDone;
    @FXML private Label statHours;
    @FXML private Label statRevenue;

    @FXML private BarChart<String, Number> monthlyChart;

    @FXML private TableView<InvoiceRow> itemsTable;
    @FXML private TableColumn<InvoiceRow, String> colDate;
    @FXML private TableColumn<InvoiceRow, String> colProperty;
    @FXML private TableColumn<InvoiceRow, Number> colHours;
    @FXML private TableColumn<InvoiceRow, String> colStatus;
    @FXML private TableColumn<InvoiceRow, Number> colPrice;

    private CustomerRow customer;
    private Set<String> customerProperties = new HashSet<>();
    private final ObservableList<InvoiceRow> items = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        customer = target;
        target = null;

        if (customer == null) {
            pageTitle.setText("Fakturácia");
            pageSubtitle.setText("Žiadny zákazník nie je vybraný — vráťte sa späť.");
            return;
        }

        pageTitle.setText("Fakturácia — " + customer.getName());
        pageSubtitle.setText("Prehľad upratovaní a faktúr pre zákazníka");

        customerProperties = apartmentsForCustomer(customer.getName());

        // Apartment filter
        List<String> apartmentOptions = new ArrayList<>();
        apartmentOptions.add(ALL_APARTMENTS);
        apartmentOptions.addAll(new ArrayList<>(customerProperties));
        apartmentCombo.setItems(FXCollections.observableArrayList(apartmentOptions));
        apartmentCombo.getSelectionModel().selectFirst();

        // Period filter: derive months present
        List<String> periodOptions = new ArrayList<>();
        periodOptions.add(ALL_PERIODS);
        for (YearMonth ym : distinctMonths()) periodOptions.add(ym.format(MONTH_FMT));
        periodCombo.setItems(FXCollections.observableArrayList(periodOptions));
        periodCombo.getSelectionModel().selectFirst();

        // Table columns
        colDate.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().date));
        colProperty.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().property));
        colHours.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().hours));
        colStatus.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().status));
        colPrice.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().price));
        colHours.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Number v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("%.1f h", v.doubleValue()));
            }
        });
        colPrice.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Number v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("%.2f €", v.doubleValue()));
            }
        });
        itemsTable.setItems(items);

        apartmentCombo.valueProperty().addListener((obs, o, n) -> refresh());
        periodCombo.valueProperty().addListener((obs, o, n) -> refresh());

        refresh();
    }

    private Set<String> apartmentsForCustomer(String customerName) {
        Set<String> addresses = new HashSet<>();
        for (ApartmentItem a : com.cleanmate.presentation.apartment.ApartmentManagementController.data()) {
            if (customerName.equalsIgnoreCase(a.getCustomer())) addresses.add(a.getAddress());
        }
        return addresses;
    }

    private List<YearMonth> distinctMonths() {
        TreeMap<YearMonth, Integer> months = new TreeMap<>();
        for (CalendarCleaningItem c : allCleaningsForCustomer()) {
            months.merge(YearMonth.from(c.date()), 1, Integer::sum);
        }
        return new ArrayList<>(months.keySet());
    }

    private List<CalendarCleaningItem> allCleaningsForCustomer() {
        List<CalendarCleaningItem> out = new ArrayList<>();
        for (CalendarCleaningItem c : CleaningCalendarController.data()) {
            if (customerProperties.contains(c.property())) out.add(c);
        }
        return out;
    }

    private void refresh() {
        String selectedApartment = apartmentCombo.getValue();
        String selectedPeriod    = periodCombo.getValue();

        items.clear();
        int total = 0, done = 0;
        double totalHours = 0, totalRevenue = 0;
        Map<String, Integer> monthlyCounts = new LinkedHashMap<>();
        for (YearMonth ym : distinctMonths()) monthlyCounts.put(ym.format(MONTH_FMT), 0);

        for (CalendarCleaningItem c : allCleaningsForCustomer()) {
            if (selectedApartment != null && !ALL_APARTMENTS.equals(selectedApartment)
                    && !selectedApartment.equals(c.property())) continue;
            String ymLabel = YearMonth.from(c.date()).format(MONTH_FMT);
            if (selectedPeriod != null && !ALL_PERIODS.equals(selectedPeriod)
                    && !selectedPeriod.equals(ymLabel)) continue;

            double hours = hoursBetween(c.checkOut(), c.checkIn());
            double price = BASE_PRICE + hours * HOURLY_RATE;

            total++;
            if ("DONE".equalsIgnoreCase(c.status())) done++;
            totalHours   += hours;
            totalRevenue += "CANCELLED".equalsIgnoreCase(c.status()) ? 0 : price;

            monthlyCounts.merge(ymLabel, 1, Integer::sum);
            items.add(new InvoiceRow(c.date().format(DATE_FMT), c.property(), hours, c.status(), price));
        }

        statTotal.setText(String.valueOf(total));
        statDone.setText(done + " (" + percent(done, total) + "%)");
        statHours.setText(String.format("%.1f h", totalHours));
        statRevenue.setText(String.format("%.2f €", totalRevenue));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        monthlyCounts.forEach((label, count) -> series.getData().add(new XYChart.Data<>(label, count)));
        monthlyChart.getData().setAll(series);
    }

    private double hoursBetween(LocalTime a, LocalTime b) {
        return Duration.between(a, b).toMinutes() / 60.0;
    }

    private int percent(int part, int whole) {
        if (whole == 0) return 0;
        return (int) Math.round(100.0 * part / whole);
    }

    @FXML
    private void onGenerateInvoice() {
        if (customer == null || items.isEmpty()) {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Faktúra");
            a.setHeaderText("Nemožno vygenerovať faktúru");
            a.setContentText("Pre zvolené filtre neexistujú žiadne položky.");
            a.showAndWait();
            return;
        }

        double total = items.stream()
                .filter(r -> !"CANCELLED".equalsIgnoreCase(r.status))
                .mapToDouble(r -> r.price.doubleValue()).sum();

        StringBuilder body = new StringBuilder();
        body.append("Zákazník: ").append(customer.getName()).append("\n");
        body.append("Apartmán: ").append(apartmentCombo.getValue()).append("\n");
        body.append("Obdobie:  ").append(periodCombo.getValue()).append("\n");
        body.append("────────────────────────────────────────\n");
        for (InvoiceRow r : items) {
            if ("CANCELLED".equalsIgnoreCase(r.status)) continue;
            body.append(String.format("%-12s %-28s %6.1fh  %8.2f €%n",
                    r.date, truncate(r.property, 28), r.hours.doubleValue(), r.price.doubleValue()));
        }
        body.append("────────────────────────────────────────\n");
        body.append(String.format("CELKOM: %.2f € (bez DPH)%n", total));
        body.append(String.format("Sadzba DPH 20%%: %.2f €%n", total * 0.20));
        body.append(String.format("NA ÚHRADU: %.2f €%n", total * 1.20));

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Faktúra (náhľad)");
        alert.setHeaderText("Náhľad faktúry — " + customer.getName());
        TextArea area = new TextArea(body.toString());
        area.setEditable(false);
        area.setWrapText(false);
        area.setStyle("-fx-font-family: 'monospace'; -fx-font-size: 12px;");
        area.setPrefSize(560, 360);
        alert.getDialogPane().setContent(area);
        alert.showAndWait();
    }

    private String truncate(String s, int n) {
        return s.length() <= n ? s : s.substring(0, n - 1) + "…";
    }

    @FXML
    private void onBack() { navCustomers(); }

    public static class InvoiceRow {
        public final String  date;
        public final String  property;
        public final Number  hours;
        public final String  status;
        public final Number  price;
        public InvoiceRow(String date, String property, double hours, String status, double price) {
            this.date = date; this.property = property; this.hours = hours;
            this.status = status; this.price = price;
        }
    }
}
