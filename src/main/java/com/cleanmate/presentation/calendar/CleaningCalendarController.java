package com.cleanmate.presentation.calendar;

import com.cleanmate.presentation.nav.BaseNavController;
import com.cleanmate.presentation.nav.LanguageManager;
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
    private static final String ALL = "— všetci —";
    private static final String ALL_STATUS = "— všetky —";
    private static final Locale SK = Locale.of("sk");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private static final ObservableList<CalendarCleaningItem> DATA = FXCollections.observableArrayList();
    static { DATA.addAll(sampleData()); }

    public static void addEvent(CalendarCleaningItem item) { DATA.add(item); }
    public static ObservableList<CalendarCleaningItem> data() { return DATA; }

    @FXML private ComboBox<String> employeeCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Label resultsCountLabel;
    @FXML private Label dateRangeLabel;
    @FXML private ToggleButton dayToggle;
    @FXML private ToggleButton weekToggle;
    @FXML private ToggleButton monthToggle;
    @FXML private StackPane calendarHolder;

    private enum ViewMode { DAY, WEEK, MONTH }
    private ViewMode viewMode = ViewMode.WEEK;
    private LocalDate anchorDate = LocalDate.now();

    @FXML
    public void initialize() {
        LOG.info("Calendar initialized");

        employeeCombo.setItems(FXCollections.observableArrayList(
                ALL, "Anna Nová", "Peter Malý", "Eva Horváthová", "Ján Kováč"));
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
        DATA.addListener((javafx.collections.ListChangeListener<CalendarCleaningItem>) c -> renderCalendar());

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

    private List<CalendarCleaningItem> filteredEvents() {
        String emp = employeeCombo.getValue();
        String st  = statusCombo.getValue();
        List<CalendarCleaningItem> out = new ArrayList<>();
        for (CalendarCleaningItem it : DATA) {
            if (emp != null && !ALL.equals(emp) && !emp.equals(it.employee())) continue;
            if (st  != null && !ALL_STATUS.equals(st) && !st.equals(it.status())) continue;
            out.add(it);
        }
        return out;
    }

    private void renderCalendar() {
        if (calendarHolder == null) return;
        List<CalendarCleaningItem> events = filteredEvents();
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
    private Node buildDayView(LocalDate day, List<CalendarCleaningItem> all) {
        VBox root = new VBox(14);
        root.getStyleClass().add("calendar-root");
        root.setPadding(new Insets(18, 20, 18, 20));

        Label header = new Label(capitalize(day.format(DateTimeFormatter.ofPattern("EEEE, d. MMMM yyyy", SK))));
        header.getStyleClass().add("cal-day-title");
        if (day.equals(LocalDate.now())) header.getStyleClass().add("cal-today-title");
        root.getChildren().add(header);

        List<CalendarCleaningItem> onDay = all.stream()
                .filter(e -> e.date().equals(day))
                .sorted(Comparator.comparing(CalendarCleaningItem::checkOut))
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
        for (CalendarCleaningItem e : onDay) list.getChildren().add(buildLargeEventCard(e));

        scroll.setContent(list);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        root.getChildren().add(scroll);
        return root;
    }

    private Node buildLargeEventCard(CalendarCleaningItem e) {
        HBox card = new HBox(18);
        card.getStyleClass().setAll("cal-event-card", "event-" + e.status().toLowerCase());
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(14, 18, 14, 18));

        VBox timeCol = new VBox(2);
        timeCol.setAlignment(Pos.CENTER);
        timeCol.setMinWidth(120);
        timeCol.setPrefWidth(120);
        timeCol.getStyleClass().add("event-card-time-col");

        Label coLabel = new Label("CHECK-OUT");
        coLabel.getStyleClass().add("event-card-label");
        Label coTime = new Label(e.checkOut().format(TIME_FMT));
        coTime.getStyleClass().add("event-card-time");
        Label sep = new Label("↓");
        sep.getStyleClass().add("event-card-sep");
        Label ciTime = new Label(e.checkIn().format(TIME_FMT));
        ciTime.getStyleClass().add("event-card-time");
        Label ciLabel = new Label("CHECK-IN");
        ciLabel.getStyleClass().add("event-card-label");

        timeCol.getChildren().addAll(coLabel, coTime, sep, ciTime, ciLabel);

        VBox content = new VBox(4);
        HBox.setHgrow(content, Priority.ALWAYS);
        content.setAlignment(Pos.CENTER_LEFT);

        Label prop = new Label(e.property());
        prop.getStyleClass().add("event-card-property");
        prop.setWrapText(true);

        Label emp = new Label("👤 " + e.employee());
        emp.getStyleClass().add("event-card-employee");

        content.getChildren().addAll(prop, emp);

        Label badge = new Label(e.status().replace('_', ' '));
        badge.getStyleClass().setAll("status-badge", "status-" + e.status().toLowerCase());

        card.getChildren().addAll(timeCol, content, badge);
        card.setOnMouseClicked(ev -> { if (ev.getClickCount() == 2) navCleaningDetail(); });
        return card;
    }

    /* ========================= WEEK VIEW ========================= */
    private Node buildWeekView(LocalDate weekStart, List<CalendarCleaningItem> all) {
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

            List<CalendarCleaningItem> onDay = all.stream()
                    .filter(e -> e.date().equals(day))
                    .sorted(Comparator.comparing(CalendarCleaningItem::checkOut))
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
                for (CalendarCleaningItem e : onDay) eventsList.getChildren().add(buildSmallEventCard(e));
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

    private Node buildSmallEventCard(CalendarCleaningItem e) {
        VBox card = new VBox(3);
        card.getStyleClass().setAll("cal-small-event", "event-" + e.status().toLowerCase());
        card.setPadding(new Insets(8, 10, 8, 10));
        card.setMaxWidth(Double.MAX_VALUE);

        Label time = new Label(e.checkOut().format(TIME_FMT) + " → " + e.checkIn().format(TIME_FMT));
        time.getStyleClass().add("cal-small-time");

        Label prop = new Label(e.property());
        prop.getStyleClass().add("cal-small-property");
        prop.setWrapText(true);

        Label emp = new Label(e.employee());
        emp.getStyleClass().add("cal-small-employee");
        emp.setWrapText(true);

        card.getChildren().addAll(time, prop, emp);
        card.setOnMouseClicked(ev -> { if (ev.getClickCount() == 2) navCleaningDetail(); });
        return card;
    }

    /* ========================= MONTH VIEW ========================= */
    private Node buildMonthView(YearMonth ym, List<CalendarCleaningItem> all) {
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
            Label l = new Label(days[c]);
            l.getStyleClass().add("cal-month-weekday");
            l.setMaxWidth(Double.MAX_VALUE);
            l.setAlignment(Pos.CENTER);
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
        int weeks = 6;
        for (int w = 0; w < weeks; w++) {
            RowConstraints rrc = new RowConstraints();
            rrc.setPercentHeight(100.0 / weeks);
            rrc.setVgrow(Priority.ALWAYS);
            rrc.setValignment(VPos.TOP);
            grid.getRowConstraints().add(rrc);
        }

        LocalDate first = ym.atDay(1);
        LocalDate gridStart = first.with(DayOfWeek.MONDAY);
        if (gridStart.isAfter(first)) gridStart = gridStart.minusWeeks(1);

        for (int w = 0; w < weeks; w++) {
            for (int d = 0; d < 7; d++) {
                LocalDate cellDate = gridStart.plusDays(w * 7L + d);
                grid.add(buildMonthCell(cellDate, ym, all), d, w);
            }
        }
        VBox.setVgrow(grid, Priority.ALWAYS);
        root.getChildren().add(grid);
        return root;
    }

    private Node buildMonthCell(LocalDate day, YearMonth currentMonth, List<CalendarCleaningItem> all) {
        VBox cell = new VBox(3);
        cell.getStyleClass().add("cal-month-cell");
        cell.setPadding(new Insets(6, 6, 6, 8));
        if (!YearMonth.from(day).equals(currentMonth)) cell.getStyleClass().add("cal-month-other");

        Label num = new Label(String.valueOf(day.getDayOfMonth()));
        num.getStyleClass().add("cal-month-day-number");
        if (day.equals(LocalDate.now())) num.getStyleClass().add("cal-month-today-num");
        cell.getChildren().add(num);

        List<CalendarCleaningItem> onDay = all.stream()
                .filter(e -> e.date().equals(day))
                .sorted(Comparator.comparing(CalendarCleaningItem::checkOut))
                .toList();

        int shown = Math.min(3, onDay.size());
        for (int i = 0; i < shown; i++) {
            CalendarCleaningItem e = onDay.get(i);
            Label chip = new Label(e.checkOut().format(TIME_FMT) + " " + e.property());
            chip.getStyleClass().setAll("cal-month-event", "event-" + e.status().toLowerCase());
            chip.setMaxWidth(Double.MAX_VALUE);
            chip.setOnMouseClicked(ev -> { if (ev.getClickCount() == 2) navCleaningDetail(); });
            cell.getChildren().add(chip);
        }
        if (onDay.size() > shown) {
            Label more = new Label("+ " + (onDay.size() - shown) + " ďalšie");
            more.getStyleClass().add("cal-month-more");
            cell.getChildren().add(more);
        }
        return cell;
    }

    /* ========================= MODEL ========================= */
    public record CalendarCleaningItem(
            LocalDate date,
            LocalTime checkOut,
            LocalTime checkIn,
            String property,
            String employee,
            String status) {

        public boolean occursOn(LocalDate d) { return d.equals(date); }
    }

    private static List<CalendarCleaningItem> sampleData() {
        LocalDate t = LocalDate.now();
        List<CalendarCleaningItem> list = new ArrayList<>();
        list.add(new CalendarCleaningItem(t,             LocalTime.of(9, 0),  LocalTime.of(11, 30), "Panská 12, BA",         "Anna Nová",      "DONE"));
        list.add(new CalendarCleaningItem(t,             LocalTime.of(10, 30),LocalTime.of(13, 0),  "Hviezdoslavovo nám. 4", "Peter Malý",     "IN_PROGRESS"));
        list.add(new CalendarCleaningItem(t,             LocalTime.of(11, 0), LocalTime.of(14, 0),  "Obchodná 27",           "Peter Malý",     "NEW"));
        list.add(new CalendarCleaningItem(t,             LocalTime.of(13, 0), LocalTime.of(15, 30), "Panenská 8",            "Eva Horváthová", "ASSIGNED"));
        list.add(new CalendarCleaningItem(t,             LocalTime.of(15, 0), LocalTime.of(17, 30), "Laurinská 3",           "Ján Kováč",      "CANCELLED"));
        list.add(new CalendarCleaningItem(t.plusDays(1), LocalTime.of(9, 0),  LocalTime.of(11, 30), "Grösslingova 45",       "Anna Nová",      "ASSIGNED"));
        list.add(new CalendarCleaningItem(t.plusDays(1), LocalTime.of(12, 0), LocalTime.of(14, 30), "Ventúrska 7",           "Peter Malý",     "NEW"));
        list.add(new CalendarCleaningItem(t.plusDays(2), LocalTime.of(10, 0), LocalTime.of(12, 30), "Michalská 22",          "Eva Horváthová", "ASSIGNED"));
        list.add(new CalendarCleaningItem(t.minusDays(1),LocalTime.of(14, 0), LocalTime.of(16, 30), "Sedlárska 5",           "Ján Kováč",      "DONE"));
        list.add(new CalendarCleaningItem(t.plusDays(3), LocalTime.of(9, 0),  LocalTime.of(11, 0),  "Kapitulská 18",         "Anna Nová",      "NEW"));

        // Historical data for statistics/invoicing (last 6 months)
        addHistoricalData(list, t);
        return list;
    }

    /** Generates ~70 past cleanings spread across the last 6 months for invoice/stat testing. */
    private static void addHistoricalData(List<CalendarCleaningItem> list, LocalDate today) {
        // (property, employee) pairings — kept stable so month-to-month assignment looks realistic.
        String[][] assignments = {
                {"Panská 12, BA",         "Anna Nová"},
                {"Hviezdoslavovo nám. 4", "Anna Nová"},
                {"Obchodná 27",           "Peter Malý"},
                {"Panenská 8",            "Peter Malý"},
                {"Laurinská 3",           "Eva Horváthová"},
                {"Grösslingova 45",       "Eva Horváthová"},
                {"Ventúrska 7",           "Ján Kováč"},
                {"Michalská 22",          "Ján Kováč"},
                {"Sedlárska 5",           "Anna Nová"},
                {"Kapitulská 18",         "Peter Malý"}
        };

        // Time slot templates (checkOut, checkIn) — varying durations for realistic stats.
        LocalTime[][] slots = {
                { LocalTime.of(9, 0),  LocalTime.of(11, 30) },
                { LocalTime.of(10, 0), LocalTime.of(13, 0)  },
                { LocalTime.of(11, 30),LocalTime.of(14, 0)  },
                { LocalTime.of(13, 0), LocalTime.of(16, 0)  },
                { LocalTime.of(14, 30),LocalTime.of(17, 0)  }
        };

        // Spread over 6 past months, with varying frequency
        int[] monthsBack    = {6, 5, 4, 3, 2, 1};
        int[] perMonthCount = {8, 10, 12, 11, 14, 13}; // rising trend — nice for the bar chart

        for (int m = 0; m < monthsBack.length; m++) {
            LocalDate monthAnchor = today.minusMonths(monthsBack[m]).withDayOfMonth(1);
            int days = monthAnchor.lengthOfMonth();
            for (int i = 0; i < perMonthCount[m]; i++) {
                int dayOfMonth = 1 + ((i * 7 + m * 3) % days);
                LocalDate d = monthAnchor.withDayOfMonth(dayOfMonth);
                String[] pair  = assignments[(i + m) % assignments.length];
                LocalTime[] s  = slots[(i + m * 2) % slots.length];

                // Most past items are DONE, sprinkle a few CANCELLED for realism
                String status = (i % 11 == 0) ? "CANCELLED" : "DONE";

                list.add(new CalendarCleaningItem(d, s[0], s[1], pair[0], pair[1], status));
            }
        }

        // Some future planned items for upcoming months
        int[] monthsForward = {1, 2};
        for (int m : monthsForward) {
            LocalDate monthAnchor = today.plusMonths(m).withDayOfMonth(1);
            int days = monthAnchor.lengthOfMonth();
            for (int i = 0; i < 6; i++) {
                int dayOfMonth = 1 + ((i * 5 + m * 2) % days);
                LocalDate d = monthAnchor.withDayOfMonth(dayOfMonth);
                String[] pair = assignments[(i + m) % assignments.length];
                LocalTime[] s = slots[(i + m) % slots.length];
                String status = (i % 2 == 0) ? "ASSIGNED" : "NEW";
                list.add(new CalendarCleaningItem(d, s[0], s[1], pair[0], pair[1], status));
            }
        }
    }
}
