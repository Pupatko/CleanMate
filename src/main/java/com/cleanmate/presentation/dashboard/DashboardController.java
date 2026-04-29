package com.cleanmate.presentation.dashboard;

import com.cleanmate.model.Cleaning;
import com.cleanmate.presentation.detail.CleaningDetailController;
import com.cleanmate.presentation.nav.BaseNavController;
import com.cleanmate.presentation.util.EmptyState;
import com.cleanmate.service.ServiceLocator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

public class DashboardController extends BaseNavController {

    private static final Logger LOG = Logger.getLogger(DashboardController.class.getName());

    @FXML private Label todayCountLabel;
    @FXML private Label unassignedCountLabel;
    @FXML private Label onDutyCountLabel;
    @FXML private Label doneCountLabel;

    @FXML private TableView<CleaningRow> recentTable;
    @FXML private TableColumn<CleaningRow, String> colTime;
    @FXML private TableColumn<CleaningRow, String> colProperty;
    @FXML private TableColumn<CleaningRow, String> colEmployee;
    @FXML private TableColumn<CleaningRow, String> colStatus;

    @FXML
    public void initialize() {
        LOG.info("Dashboard initialized");

        List<Cleaning> todayList = ServiceLocator.cleanings().getByDate(LocalDate.now());
        long unassigned = todayList.stream().filter(c -> "NEW".equals(c.status())).count();
        long onDuty     = todayList.stream().filter(c -> "IN_PROGRESS".equals(c.status())).count();
        long done       = todayList.stream().filter(c -> "DONE".equals(c.status())).count();

        todayCountLabel.setText(String.valueOf(todayList.size()));
        unassignedCountLabel.setText(String.valueOf(unassigned));
        onDutyCountLabel.setText(String.valueOf(onDuty));
        doneCountLabel.setText(String.valueOf(done));

        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colProperty.setCellValueFactory(new PropertyValueFactory<>("property"));
        colEmployee.setCellValueFactory(new PropertyValueFactory<>("employee"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colStatus.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Label badge = new Label(status);
                badge.getStyleClass().setAll("status-badge", "status-" + status.toLowerCase());
                setGraphic(badge);
                setText(null);
            }
        });

        recentTable.setItems(buildRows(todayList));
        recentTable.setPlaceholder(EmptyState.build("📋", "empty.dashboard"));

        recentTable.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null && n.getSource() != null) {
                CleaningDetailController.selected = n.getSource();
                navCleaningDetail();
            }
        });
    }

    private ObservableList<CleaningRow> buildRows(List<Cleaning> list) {
        var fmt = DateTimeFormatter.ofPattern("HH:mm");
        ObservableList<CleaningRow> rows = FXCollections.observableArrayList();
        list.stream()
            .sorted((a, b) -> a.checkOut().compareTo(b.checkOut()))
            .forEach(c -> rows.add(new CleaningRow(
                    c.checkOut().format(fmt), c.property(), c.employee(), c.status(), c)));
        return rows;
    }

}
