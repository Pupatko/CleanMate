package com.cleanmate.presentation.myschedule;

import com.cleanmate.presentation.checklist.ChecklistController;
import com.cleanmate.presentation.nav.LanguageManager;
import com.cleanmate.presentation.util.EmptyState;
import com.cleanmate.service.ServiceLocator;
import com.cleanmate.service.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

public class MyScheduleController extends com.cleanmate.presentation.nav.BaseNavController {

    private static final Logger LOG = Logger.getLogger(MyScheduleController.class.getName());

    @FXML private Label greetingLabel;
    @FXML private Label dateLabel;
    @FXML private Label summaryLabel;
    @FXML private ListView<MyTaskItem> list;

    @FXML
    public void initialize() {
        LOG.info("My schedule initialized");

        String emp = Session.getRole() == com.cleanmate.service.Session.Role.ZAMESTNANEC
                ? Session.getDisplayName() : null;

        String firstName = emp != null ? emp.split(" ")[0] : "";
        greetingLabel.setText(LanguageManager.getBundle().getString("schedule.greeting")
                + (firstName.isEmpty() ? "" : ", " + firstName));
        dateLabel.setText(LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE d. MMMM yyyy", LanguageManager.getLocale())));

        ObservableList<MyTaskItem> items = FXCollections.observableArrayList();
        ServiceLocator.cleanings().getByDate(LocalDate.now()).stream()
                .filter(c -> emp == null || emp.equals(c.employee()))
                .sorted((a, b) -> a.checkOut().compareTo(b.checkOut()))
                .forEach(c -> {
                    int total = ServiceLocator.apartments().getAll().stream()
                            .filter(a -> a.getAddress().equals(c.property()))
                            .mapToInt(a -> a.getTaskCount()).findFirst().orElse(0);
                    int done  = "DONE".equals(c.status()) ? total : 0;
                    items.add(new MyTaskItem(c.id(), c.checkOut(), c.property(), c.customer(),
                            c.status(), total, done));
                });

        long done = items.stream().filter(i -> "DONE".equals(i.status())).count();
        summaryLabel.setText(MessageFormat.format(LanguageManager.getBundle().getString("schedule.summary"), items.size(), done));

        list.setItems(items);
        list.setPlaceholder(EmptyState.build("📅", "empty.schedule"));
        list.setCellFactory(l -> new TaskCell());

        list.setOnMouseClicked(e -> {
            MyTaskItem sel = list.getSelectionModel().getSelectedItem();
            if (sel != null) {
                LOG.info("Open checklist for: " + sel.property() + " @ " + sel.time());
                ServiceLocator.cleanings().findById(sel.id()).ifPresent(c ->
                        ChecklistController.currentCleaning = c);
                navChecklist();
            }
        });
    }

    private static final class TaskCell extends ListCell<MyTaskItem> {
        private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

        @Override
        protected void updateItem(MyTaskItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
                return;
            }

            Label timeLbl = new Label(item.time().format(TIME));
            timeLbl.getStyleClass().add("task-time");

            VBox timeBox = new VBox(timeLbl);
            timeBox.getStyleClass().add("task-time-box");
            timeBox.setAlignment(Pos.CENTER);
            timeBox.setMinWidth(90);

            Label propLbl = new Label(item.property());
            propLbl.getStyleClass().add("task-property");

            Label custLbl = new Label(LanguageManager.getBundle().getString("schedule.task.customer.prefix") + " " + item.customer());
            custLbl.getStyleClass().add("task-customer");

            ProgressBar pb = new ProgressBar(
                    item.stepsTotal() == 0 ? 0.0 : (double) item.stepsDone() / item.stepsTotal());
            pb.getStyleClass().add("task-progress");
            pb.setPrefWidth(220);

            Label progressLbl = new Label(item.stepsDone() + " / " + item.stepsTotal() + LanguageManager.getBundle().getString("schedule.task.steps.suffix"));
            progressLbl.getStyleClass().add("task-progress-label");

            HBox pbBox = new HBox(10, pb, progressLbl);
            pbBox.setAlignment(Pos.CENTER_LEFT);

            VBox textBox = new VBox(4, propLbl, custLbl, pbBox);
            textBox.setAlignment(Pos.CENTER_LEFT);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label badge = new Label(item.status());
            badge.getStyleClass().setAll("status-badge", "status-" + item.status().toLowerCase());

            Label arrow = new Label("›");
            arrow.getStyleClass().add("task-arrow");

            HBox row = new HBox(16, timeBox, textBox, spacer, badge, arrow);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("task-row");

            setGraphic(row);
            setText(null);
        }
    }
}
