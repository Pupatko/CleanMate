package com.cleanmate.presentation.statistics;

import com.cleanmate.model.Cleaning;
import com.cleanmate.service.ServiceLocator;
import com.cleanmate.presentation.nav.BaseNavController;
import com.cleanmate.presentation.nav.LanguageManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class StatisticsController extends BaseNavController {

    private static final Logger LOG = Logger.getLogger(StatisticsController.class.getName());

    @FXML private ComboBox<String> periodCombo;

    @FXML private Label cardTotalValue;
    @FXML private Label cardDoneValue;
    @FXML private Label cardHoursValue;
    @FXML private Label cardRatingValue;

    @FXML private BarChart<String, Number> monthlyChart;

    @FXML private PieChart statusPie;

    @FXML private VBox employeeLeaderboard;

    @FXML
    public void initialize() {
        LOG.info("Statistics initialized");

        var b = LanguageManager.getBundle();
        periodCombo.setItems(FXCollections.observableArrayList(
                b.getString("stats.period.week"),
                b.getString("stats.period.month"),
                b.getString("stats.period.quarter"),
                b.getString("stats.period.half")
        ));
        periodCombo.getSelectionModel().select(1); // default: this month
        periodCombo.valueProperty().addListener((obs, o, n) -> refresh());

        refresh();
    }

    private void refresh() {
        LocalDate cutoff = cutoffDate();
        List<Cleaning> all = ServiceLocator.cleanings().getAll().stream()
                .filter(e -> !e.date().isAfter(LocalDate.now()))
                .filter(e -> !e.date().isBefore(cutoff))
                .toList();

        updateCards(all);
        updateMonthlyChart(all, cutoff);
        updateStatusPie(all);
        updateLeaderboard(all);
    }

    private LocalDate cutoffDate() {
        LocalDate today = LocalDate.now();
        var b = LanguageManager.getBundle();
        String sel = periodCombo.getValue();
        if (sel == null) return today.withDayOfMonth(1);
        if (sel.equals(b.getString("stats.period.week")))    return today.with(java.time.DayOfWeek.MONDAY);
        if (sel.equals(b.getString("stats.period.quarter"))) return today.minusMonths(3).withDayOfMonth(1);
        if (sel.equals(b.getString("stats.period.half")))    return today.minusMonths(6).withDayOfMonth(1);
        return today.withDayOfMonth(1); // this month
    }

    private void updateCards(List<Cleaning> items) {
        long total  = items.size();
        long done   = items.stream().filter(e -> "DONE".equals(e.status())).count();
        double hours = items.stream()
                .mapToDouble(e -> Duration.between(e.checkOut(), e.checkIn()).toMinutes() / 60.0)
                .sum();

        cardTotalValue.setText(String.valueOf(total));
        cardDoneValue.setText(String.valueOf(done));
        cardHoursValue.setText(String.format("%.1fh", hours));

        // Mock avg rating: 4.2–4.8 range based on done count
        double rating = done == 0 ? 0 : 3.8 + (done % 5) * 0.2;
        cardRatingValue.setText(done == 0 ? "—" : String.format("★ %.1f", Math.min(rating, 5.0)));
    }

    private void updateMonthlyChart(List<Cleaning> items, LocalDate cutoff) {
        monthlyChart.getData().clear();

        var b = LanguageManager.getBundle();
        String sel = periodCombo.getValue();

        XYChart.Series<String, Number> doneSeries = new XYChart.Series<>();
        doneSeries.setName(b.getString("stats.legend.done"));
        XYChart.Series<String, Number> cancelSeries = new XYChart.Series<>();
        cancelSeries.setName(b.getString("stats.legend.cancelled"));

        if (sel != null && sel.equals(b.getString("stats.period.week"))) {
            // Group by day (Mon–Sun of current week)
            DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("EEE d.M.", LanguageManager.getLocale());
            LocalDate monday = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
            for (int i = 0; i < 7; i++) {
                LocalDate day = monday.plusDays(i);
                String label = capitalize(day.format(dayFmt));
                long done = items.stream()
                        .filter(e -> e.date().equals(day) && "DONE".equals(e.status())).count();
                long cancelled = items.stream()
                        .filter(e -> e.date().equals(day) && "CANCELLED".equals(e.status())).count();
                doneSeries.getData().add(new XYChart.Data<>(label, done));
                cancelSeries.getData().add(new XYChart.Data<>(label, cancelled));
            }

        } else if (sel != null && sel.equals(b.getString("stats.period.month"))) {
            // Group by week within the current month
            LocalDate firstOfMonth = LocalDate.now().withDayOfMonth(1);
            LocalDate lastOfMonth  = firstOfMonth.withDayOfMonth(firstOfMonth.lengthOfMonth());
            LocalDate weekStart = firstOfMonth.with(java.time.DayOfWeek.MONDAY);
            if (weekStart.isAfter(firstOfMonth)) weekStart = weekStart.minusWeeks(1);
            DateTimeFormatter wFmt = DateTimeFormatter.ofPattern("d.M.");
            while (!weekStart.isAfter(lastOfMonth)) {
                LocalDate weekEnd = weekStart.plusDays(6);
                final LocalDate ws = weekStart, we = weekEnd;
                String label = ws.format(wFmt) + "–" + we.format(wFmt);
                long done = items.stream()
                        .filter(e -> !e.date().isBefore(ws) && !e.date().isAfter(we) && "DONE".equals(e.status())).count();
                long cancelled = items.stream()
                        .filter(e -> !e.date().isBefore(ws) && !e.date().isAfter(we) && "CANCELLED".equals(e.status())).count();
                doneSeries.getData().add(new XYChart.Data<>(label, done));
                cancelSeries.getData().add(new XYChart.Data<>(label, cancelled));
                weekStart = weekStart.plusWeeks(1);
            }

        } else {
            // Quarter / Half-year: group by month
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("LLL yy", LanguageManager.getLocale());
            Map<YearMonth, Long> doneByMonth = items.stream()
                    .filter(e -> "DONE".equals(e.status()))
                    .collect(Collectors.groupingBy(e -> YearMonth.from(e.date()), Collectors.counting()));
            Map<YearMonth, Long> cancelByMonth = items.stream()
                    .filter(e -> "CANCELLED".equals(e.status()))
                    .collect(Collectors.groupingBy(e -> YearMonth.from(e.date()), Collectors.counting()));
            YearMonth start = YearMonth.from(cutoff);
            YearMonth end   = YearMonth.now();
            for (YearMonth ym = start; !ym.isAfter(end); ym = ym.plusMonths(1)) {
                String label = capitalize(ym.format(fmt));
                doneSeries.getData().add(new XYChart.Data<>(label, doneByMonth.getOrDefault(ym, 0L)));
                cancelSeries.getData().add(new XYChart.Data<>(label, cancelByMonth.getOrDefault(ym, 0L)));
            }
        }

        monthlyChart.getData().addAll(List.of(doneSeries, cancelSeries));
    }

    private void updateStatusPie(List<Cleaning> items) {
        Map<String, Long> byStatus = items.stream()
                .collect(Collectors.groupingBy(Cleaning::status, Collectors.counting()));

        statusPie.getData().clear();
        statusPie.getData().addAll(
                byStatus.entrySet().stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .map(e -> new PieChart.Data(e.getKey() + " (" + e.getValue() + ")", e.getValue()))
                        .toList()
        );
    }

    private void updateLeaderboard(List<Cleaning> items) {
        employeeLeaderboard.getChildren().clear();

        Map<String, Long> byEmployee = items.stream()
                .filter(e -> "DONE".equals(e.status()))
                .collect(Collectors.groupingBy(Cleaning::employee, Collectors.counting()));

        if (byEmployee.isEmpty()) {
            Label empty = new Label("—");
            empty.getStyleClass().add("empty-hint");
            employeeLeaderboard.getChildren().add(empty);
            return;
        }

        long max = byEmployee.values().stream().max(Long::compareTo).orElse(1L);

        byEmployee.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(8)
                .forEach(entry -> employeeLeaderboard.getChildren().add(buildLeaderRow(entry.getKey(), entry.getValue(), max)));
    }

    private HBox buildLeaderRow(String name, long count, long max) {
        Label nameLabel = new Label(name);
        nameLabel.setMinWidth(150);
        nameLabel.getStyleClass().add("stats-leader-name");

        Region bar = new Region();
        bar.getStyleClass().add("stats-leader-bar");
        double pct = max > 0 ? (double) count / max : 0;
        bar.setPrefWidth(pct * 160);
        bar.setMinHeight(20);
        bar.setMaxHeight(20);

        Label countLabel = new Label(String.valueOf(count));
        countLabel.getStyleClass().add("stats-leader-count");
        countLabel.setMinWidth(30);

        HBox row = new HBox(10, nameLabel, bar, countLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(4, 0, 4, 0));
        HBox.setHgrow(bar, Priority.NEVER);
        return row;
    }

    private String capitalize(String s) {
        return s == null || s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
