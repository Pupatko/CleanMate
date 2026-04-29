package com.cleanmate.presentation.calendar;

import com.cleanmate.model.Cleaning;
import com.cleanmate.presentation.detail.CleaningDetailController;
import com.cleanmate.presentation.nav.BaseNavController;
import com.cleanmate.presentation.nav.LanguageManager;
import com.cleanmate.presentation.util.ToastManager;
import com.cleanmate.service.ServiceLocator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

public class CleaningCalendarController extends BaseNavController {

    private static final Logger LOG = Logger.getLogger(CleaningCalendarController.class.getName());
    private static final String ALL        = "— všetci —";
    private static final String ALL_STATUS = "— všetky —";
    private static final Locale SK         = Locale.of("sk");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("d.M.yyyy");

    @FXML private ComboBox<String> employeeCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Label resultsCountLabel;
    @FXML private Label dateRangeLabel;
    @FXML private ToggleButton dayToggle;
    @FXML private ToggleButton weekToggle;
    @FXML private ToggleButton monthToggle;
    @FXML private StackPane calendarHolder;

    // ── Assign panel ──────────────────────────────────────────────────────────
    @FXML private VBox assignPanel;
    @FXML private Label panelProperty;
    @FXML private Label panelDateTime;
    @FXML private Label panelStatus;
    @FXML private ComboBox<String> assignCombo;

    private Cleaning selectedEvent = null;

    private enum ViewMode { DAY, WEEK, MONTH }
    private ViewMode viewMode = ViewMode.WEEK;
    private LocalDate anchorDate = LocalDate.now();

    @FXML
    public void initialize() {
        LOG.info("Calendar initialized");

        java.util.List<String> empNames = new java.util.ArrayList<>();
        empNames.add(ALL);
        empNames.addAll(ServiceLocator.employees().getAllNames());
        employeeCombo.setItems(FXCollections.observableArrayList(empNames));
        employeeCombo.getSelectionModel().selectFirst();

        statusCombo.setItems(FXCollections.observableArrayList(
                ALL_STATUS, "NEW", "ASSIGNED", "IN_PROGRESS", "DONE", "CANCELLED"));
        statusCombo.getSelectionModel().selectFirst();

        ToggleGroup viewGroup = new ToggleGroup();
        dayToggle.setToggleGroup(viewGroup);
        weekToggle.setToggleGroup(viewGroup);
        monthToggle.setToggleGroup(viewGroup);

        for (ToggleButton t : new ToggleButton[]{dayToggle, weekToggle, monthToggle}) {
            t.addEventFilter(MouseEvent.MOUSE_PRESSED, ev -> { if (t.isSelected()) ev.consume(); });
        }

        viewGroup.selectedToggleProperty().addListener((obs, o, n) -> {
            if      (n == dayToggle)   viewMode = ViewMode.DAY;
            else if (n == weekToggle)  viewMode = ViewMode.WEEK;
            else if (n == monthToggle) viewMode = ViewMode.MONTH;
            renderCalendar();
        });

        employeeCombo.valueProperty().addListener((obs, o, n) -> renderCalendar());
        statusCombo.valueProperty().addListener((obs, o, n) -> renderCalendar());
        ServiceLocator.cleanings().getAll()
                .addListener((javafx.collections.ListChangeListener<Cleaning>) c -> renderCalendar());

        weekToggle.setSelected(true);
        renderCalendar();
    }

    @FXML private void onAdd()   { navAddCleaning(); }
    @FXML private void onToday() { anchorDate = LocalDate.now(); renderCalendar(); }

    @FXML
    private void onPrev() {
        anchorDate = switch (viewMode) {
            case DAY   -> anchorDate.minusDays(1);
            case WEEK  -> anchorDate.minusWeeks(1);
            case MONTH -> anchorDate.minusMonths(1);
        };
        renderCalendar();
    }

    @FXML
    private void onNext() {
        anchorDate = switch (viewMode) {
            case DAY   -> anchorDate.plusDays(1);
            case WEEK  -> anchorDate.plusWeeks(1);
            case MONTH -> anchorDate.plusMonths(1);
        };
        renderCalendar();
    }

    @FXML
    private void onClearFilters() {
        employeeCombo.getSelectionModel().selectFirst();
        statusCombo.getSelectionModel().selectFirst();
    }

    // ── Assign panel handlers ─────────────────────────────────────────────────

    private void selectEvent(Cleaning e) {
        selectedEvent = e;
        panelProperty.setText(e.property());
        panelDateTime.setText(e.date().format(DATE_FMT) + "  " +
                e.checkOut().format(TIME_FMT) + " → " + e.checkIn().format(TIME_FMT));
        panelStatus.setText(e.status().replace('_', ' '));
        panelStatus.getStyleClass().setAll("status-badge", "status-" + e.status().toLowerCase());

        assignCombo.setItems(FXCollections.observableArrayList(
                ServiceLocator.employees().getAllNames()));
        assignCombo.setValue(e.employee());

        assignPanel.setVisible(true);
        assignPanel.setManaged(true);
        renderCalendar();
    }

    @FXML private void onClosePanel() {
        assignPanel.setVisible(false);
        assignPanel.setManaged(false);
        selectedEvent = null;
        renderCalendar();
    }

    @FXML private void onAssign() {
        if (selectedEvent == null) return;
        String newEmployee = assignCombo.getValue();
        if (newEmployee == null || newEmployee.isBlank()) return;

        String newStatus = "NEW".equals(selectedEvent.status()) ? "ASSIGNED" : selectedEvent.status();
        Cleaning updated = selectedEvent.withEmployee(newEmployee, newStatus);
        ServiceLocator.cleanings().save(updated);
        selectedEvent = updated;

        panelStatus.setText(updated.status().replace('_', ' '));
        panelStatus.getStyleClass().setAll("status-badge", "status-" + updated.status().toLowerCase());
        toast(LanguageManager.getBundle().getString("toast.changes.saved"), ToastManager.Type.SUCCESS);
    }

    @FXML private void onOpenDetail() {
        if (selectedEvent == null) return;
        CleaningDetailController.selected = selectedEvent;
        onClosePanel();
        navCleaningDetail();
    }

    // ── Filtering ─────────────────────────────────────────────────────────────

    private List<Cleaning> filteredEvents() {
        String emp = employeeCombo.getValue();
        String st  = statusCombo.getValue();
        List<Cleaning> out = new ArrayList<>();
        for (Cleaning it : ServiceLocator.cleanings().getAll()) {
            if (emp != null && !ALL.equals(emp) && !emp.equals(it.employee())) continue;
            if (st  != null && !ALL_STATUS.equals(st) && !st.equals(it.status()))  continue;
            out.add(it);
        }
        return out;
    }

    private void renderCalendar() {
        if (calendarHolder == null) return;
        List<Cleaning> events = filteredEvents();
        resultsCountLabel.setText(LanguageManager.getBundle().getString("calendar.count.prefix") + " " + events.size());

        Node view = switch (viewMode) {
            case DAY   -> buildDayView(anchorDate, events);
            case WEEK  -> buildWeekView(startOfWeek(anchorDate), events);
            case MONTH -> buildMonthView(YearMonth.from(anchorDate), events);
        };
        if (view instanceof Region r) {
            r.setMaxWidth(Double.MAX_VALUE);
            r.setMaxHeight(Double.MAX_VALUE);
        }
        calendarHolder.getChildren().setAll(view);
        updateDateRangeLabel();
    }

    private LocalDate startOfWeek(LocalDate d) { return d.with(DayOfWeek.MONDAY); }

    private void updateDateRangeLabel() {
        DateTimeFormatter dayFmt   = DateTimeFormatter.ofPattern("EEEE, d. MMMM yyyy", SK);
        DateTimeFormatter weekFmt  = DateTimeFormatter.ofPattern("d. MMM", SK);
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("LLLL yyyy", SK);

        switch (viewMode) {
            case DAY -> dateRangeLabel.setText(capitalize(anchorDate.format(dayFmt)));
            case WEEK -> {
                LocalDate s = startOfWeek(anchorDate);
                LocalDate e = s.plusDays(6);
                dateRangeLabel.setText(s.format(weekFmt) + " – " + e.format(weekFmt) + " " + e.getYear());
            }
            case MONTH -> dateRangeLabel.setText(capitalize(anchorDate.format(monthFmt)));
        }
    }

    private String capitalize(String s) {
        return s == null || s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    /* ========================= DAY VIEW ========================= */
    private Node buildDayView(LocalDate day, List<Cleaning> all) {
        VBox root = new VBox(14);
        root.getStyleClass().add("calendar-root");
        root.setPadding(new Insets(18, 20, 18, 20));

        Label header = new Label(capitalize(day.format(DateTimeFormatter.ofPattern("EEEE, d. MMMM yyyy", SK))));
        header.getStyleClass().add("cal-day-title");
        if (day.equals(LocalDate.now())) header.getStyleClass().add("cal-today-title");
        root.getChildren().add(header);

        List<Cleaning> onDay = all.stream()
                .filter(e -> e.date().equals(day))
                .sorted(Comparator.comparing(Cleaning::checkOut))
                .toList();

        if (onDay.isEmpty()) {
            Label empty = new Label("Žiadne upratovania v tento deň.");
            empty.getStyleClass().add("empty-hint");
            empty.setMaxWidth(Double.MAX_VALUE);
            empty.setAlignment(Pos.CENTER);
            VBox.setVgrow(empty, Priority.ALWAYS);
            root.getChildren().add(empty);
            return root;
        }

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("cal-scroll");

        VBox list = new VBox(10);
        list.setPadding(new Insets(2, 2, 2, 2));
        for (Cleaning e : onDay) list.getChildren().add(buildLargeEventCard(e));

        scroll.setContent(list);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        root.getChildren().add(scroll);
        return root;
    }

    private Node buildLargeEventCard(Cleaning e) {
        HBox card = new HBox(18);
        card.getStyleClass().setAll("cal-event-card", "event-" + e.status().toLowerCase());
        if (e.equals(selectedEvent)) card.getStyleClass().add("cal-event-selected");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(14, 18, 14, 18));

        VBox timeCol = new VBox(2);
        timeCol.setAlignment(Pos.CENTER);
        timeCol.setMinWidth(120);
        timeCol.setPrefWidth(120);
        timeCol.getStyleClass().add("event-card-time-col");

        Label coLabel = new Label("CHECK-OUT"); coLabel.getStyleClass().add("event-card-label");
        Label coTime  = new Label(e.checkOut().format(TIME_FMT)); coTime.getStyleClass().add("event-card-time");
        Label sep     = new Label("↓"); sep.getStyleClass().add("event-card-sep");
        Label ciTime  = new Label(e.checkIn().format(TIME_FMT)); ciTime.getStyleClass().add("event-card-time");
        Label ciLabel = new Label("CHECK-IN"); ciLabel.getStyleClass().add("event-card-label");
        timeCol.getChildren().addAll(coLabel, coTime, sep, ciTime, ciLabel);

        VBox content = new VBox(4);
        HBox.setHgrow(content, Priority.ALWAYS);
        content.setAlignment(Pos.CENTER_LEFT);
        Label prop = new Label(e.property()); prop.getStyleClass().add("event-card-property"); prop.setWrapText(true);
        Label emp  = new Label("👤 " + e.employee()); emp.getStyleClass().add("event-card-employee");
        content.getChildren().addAll(prop, emp);

        Label badge = new Label(e.status().replace('_', ' '));
        badge.getStyleClass().setAll("status-badge", "status-" + e.status().toLowerCase());

        card.getChildren().addAll(timeCol, content, badge);
        card.setOnMouseClicked(ev -> {
            if (ev.getClickCount() == 1) selectEvent(e);
            else { CleaningDetailController.selected = e; navCleaningDetail(); }
        });
        return card;
    }

    /* ========================= WEEK VIEW ========================= */
    private Node buildWeekView(LocalDate weekStart, List<Cleaning> all) {
        VBox root = new VBox();
        root.getStyleClass().add("calendar-root");

        GridPane headerRow = new GridPane();
        headerRow.getStyleClass().add("cal-week-header");
        for (int i = 0; i < 7; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 7);
            cc.setHgrow(Priority.ALWAYS);
            headerRow.getColumnConstraints().add(cc);
        }
        DateTimeFormatter headFmt = DateTimeFormatter.ofPattern("EEE d.M.", SK);
        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            Label h = new Label(capitalize(day.format(headFmt)));
            h.getStyleClass().add("cal-day-header");
            if (day.equals(LocalDate.now())) h.getStyleClass().add("cal-day-today");
            h.setAlignment(Pos.CENTER);
            h.setMaxWidth(Double.MAX_VALUE);
            headerRow.add(h, i, 0);
        }
        root.getChildren().add(headerRow);

        GridPane body = new GridPane();
        body.getStyleClass().add("cal-week-body");
        for (int i = 0; i < 7; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 7);
            cc.setHgrow(Priority.ALWAYS);
            cc.setFillWidth(true);
            body.getColumnConstraints().add(cc);
        }
        RowConstraints rc = new RowConstraints();
        rc.setVgrow(Priority.ALWAYS);
        rc.setValignment(VPos.TOP);
        rc.setFillHeight(true);
        body.getRowConstraints().add(rc);

        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            List<Cleaning> onDay = all.stream()
                    .filter(e -> e.date().equals(day))
                    .sorted(Comparator.comparing(Cleaning::checkOut))
                    .toList();

            VBox eventsList = new VBox(6);
            eventsList.setPadding(new Insets(8, 6, 8, 6));
            if (onDay.isEmpty()) {
                Label empty = new Label("—");
                empty.getStyleClass().add("cal-week-empty");
                empty.setMaxWidth(Double.MAX_VALUE);
                empty.setAlignment(Pos.CENTER);
                eventsList.getChildren().add(empty);
            } else {
                for (Cleaning e : onDay) eventsList.getChildren().add(buildSmallEventCard(e));
            }

            ScrollPane colScroll = new ScrollPane(eventsList);
            colScroll.setFitToWidth(true);
            colScroll.getStyleClass().add("cal-day-scroll");
            colScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            colScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

            VBox col = new VBox();
            col.getStyleClass().add("cal-week-day");
            if (day.equals(LocalDate.now())) col.getStyleClass().add("cal-week-day-today");
            col.setMaxWidth(Double.MAX_VALUE);
            VBox.setVgrow(colScroll, Priority.ALWAYS);
            col.getChildren().add(colScroll);
            body.add(col, i, 0);
        }

        VBox.setVgrow(body, Priority.ALWAYS);
        root.getChildren().add(body);
        return root;
    }

    private Node buildSmallEventCard(Cleaning e) {
        VBox card = new VBox(3);
        card.getStyleClass().setAll("cal-small-event", "event-" + e.status().toLowerCase());
        if (e.equals(selectedEvent)) card.getStyleClass().add("cal-event-selected");
        card.setPadding(new Insets(8, 10, 8, 10));
        card.setMaxWidth(Double.MAX_VALUE);

        Label time = new Label(e.checkOut().format(TIME_FMT) + " → " + e.checkIn().format(TIME_FMT));
        time.getStyleClass().add("cal-small-time");
        Label prop = new Label(e.property()); prop.getStyleClass().add("cal-small-property"); prop.setWrapText(true);
        Label emp  = new Label(e.employee()); emp.getStyleClass().add("cal-small-employee");  emp.setWrapText(true);

        card.getChildren().addAll(time, prop, emp);
        card.setOnMouseClicked(ev -> {
            if (ev.getClickCount() == 1) selectEvent(e);
            else { CleaningDetailController.selected = e; navCleaningDetail(); }
        });
        return card;
    }

    /* ========================= MONTH VIEW ========================= */
    private Node buildMonthView(YearMonth ym, List<Cleaning> all) {
        VBox root = new VBox();
        root.getStyleClass().add("calendar-root");

        GridPane weekdayRow = new GridPane();
        weekdayRow.getStyleClass().add("cal-week-header");
        for (int c = 0; c < 7; c++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 7);
            cc.setHgrow(Priority.ALWAYS);
            weekdayRow.getColumnConstraints().add(cc);
        }
        String[] days = {"Pondelok", "Utorok", "Streda", "Štvrtok", "Piatok", "Sobota", "Nedeľa"};
        for (int c = 0; c < 7; c++) {
            Label l = new Label(days[c]); l.getStyleClass().add("cal-month-weekday");
            l.setMaxWidth(Double.MAX_VALUE); l.setAlignment(Pos.CENTER);
            weekdayRow.add(l, c, 0);
        }
        root.getChildren().add(weekdayRow);

        GridPane grid = new GridPane();
        grid.getStyleClass().add("cal-month-grid");
        for (int c = 0; c < 7; c++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 7);
            cc.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(cc);
        }
        for (int w = 0; w < 6; w++) {
            RowConstraints rrc = new RowConstraints();
            rrc.setPercentHeight(100.0 / 6);
            rrc.setVgrow(Priority.ALWAYS);
            rrc.setValignment(VPos.TOP);
            grid.getRowConstraints().add(rrc);
        }

        LocalDate first     = ym.atDay(1);
        LocalDate gridStart = first.with(DayOfWeek.MONDAY);
        if (gridStart.isAfter(first)) gridStart = gridStart.minusWeeks(1);

        for (int w = 0; w < 6; w++) {
            for (int d = 0; d < 7; d++) {
                grid.add(buildMonthCell(gridStart.plusDays(w * 7L + d), ym, all), d, w);
            }
        }
        VBox.setVgrow(grid, Priority.ALWAYS);
        root.getChildren().add(grid);
        return root;
    }

    private Node buildMonthCell(LocalDate day, YearMonth currentMonth, List<Cleaning> all) {
        VBox cell = new VBox(3);
        cell.getStyleClass().add("cal-month-cell");
        cell.setPadding(new Insets(6, 6, 6, 8));
        if (!YearMonth.from(day).equals(currentMonth)) cell.getStyleClass().add("cal-month-other");

        Label num = new Label(String.valueOf(day.getDayOfMonth()));
        num.getStyleClass().add("cal-month-day-number");
        if (day.equals(LocalDate.now())) num.getStyleClass().add("cal-month-today-num");
        cell.getChildren().add(num);

        List<Cleaning> onDay = all.stream()
                .filter(e -> e.date().equals(day))
                .sorted(Comparator.comparing(Cleaning::checkOut))
                .toList();

        int shown = Math.min(3, onDay.size());
        for (int i = 0; i < shown; i++) {
            Cleaning e = onDay.get(i);
            Label chip = new Label(e.checkOut().format(TIME_FMT) + " " + e.property());
            chip.getStyleClass().setAll("cal-month-event", "event-" + e.status().toLowerCase());
            if (e.equals(selectedEvent)) chip.getStyleClass().add("cal-event-selected");
            chip.setMaxWidth(Double.MAX_VALUE);
            chip.setOnMouseClicked(ev -> {
                if (ev.getClickCount() == 1) selectEvent(e);
                else { CleaningDetailController.selected = e; navCleaningDetail(); }
            });
            cell.getChildren().add(chip);
        }
        if (onDay.size() > shown) {
            Label more = new Label("+ " + (onDay.size() - shown) + " ďalšie");
            more.getStyleClass().add("cal-month-more");
            cell.getChildren().add(more);
        }
        return cell;
    }

    // kept for backward compat — AddCleaningController calls this
    public static void addEvent(Cleaning item) {
        ServiceLocator.cleanings().save(item);
    }
}
