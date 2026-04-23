package com.cleanmate.presentation.myschedule;

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

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
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

        greetingLabel.setText("Dobrý deň, Peter");
        dateLabel.setText(LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE d. MMMM yyyy", Locale.of("sk"))));

        ObservableList<MyTaskItem> items = FXCollections.observableArrayList(
                new MyTaskItem(LocalTime.of(9, 0),  "Panská 12, Bratislava",       "Acme Rentals",          "DONE",        7, 7),
                new MyTaskItem(LocalTime.of(10, 30),"Hviezdoslavovo nám. 4",       "City Nest Bratislava",  "IN_PROGRESS", 7, 4),
                new MyTaskItem(LocalTime.of(13, 0), "Panenská 8",                  "Jana Kováčová",         "ASSIGNED",    6, 0),
                new MyTaskItem(LocalTime.of(15, 45),"Ventúrska 7, Bratislava",     "Riverside Apartments",  "ASSIGNED",    8, 0)
        );

        long done = items.stream().filter(i -> "DONE".equals(i.status())).count();
        summaryLabel.setText("Dnes máš " + items.size() + " úloh, " + done + " dokončené");

        list.setItems(items);
        list.setCellFactory(l -> new TaskCell());

        list.setOnMouseClicked(e -> {
            MyTaskItem sel = list.getSelectionModel().getSelectedItem();
            if (sel != null) {
                LOG.info("Open checklist for: " + sel.property() + " @ " + sel.time());
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

            Label custLbl = new Label("Zákazník: " + item.customer());
            custLbl.getStyleClass().add("task-customer");

            ProgressBar pb = new ProgressBar(
                    item.stepsTotal() == 0 ? 0.0 : (double) item.stepsDone() / item.stepsTotal());
            pb.getStyleClass().add("task-progress");
            pb.setPrefWidth(220);

            Label progressLbl = new Label(item.stepsDone() + " / " + item.stepsTotal() + " krokov");
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
