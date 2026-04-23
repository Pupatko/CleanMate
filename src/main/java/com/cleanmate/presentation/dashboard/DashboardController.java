package com.cleanmate.presentation.dashboard;

import com.cleanmate.presentation.nav.BaseNavController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

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

        todayCountLabel.setText("12");
        unassignedCountLabel.setText("3");
        onDutyCountLabel.setText("5");
        doneCountLabel.setText("7");

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

        recentTable.setItems(sampleRows());

        recentTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, o, n) -> { if (n != null) navCleaningDetail(); });
    }

    private ObservableList<CleaningRow> sampleRows() {
        return FXCollections.observableArrayList(
                new CleaningRow("09:00", "Panská 12, BA", "Anna Nová", "DONE"),
                new CleaningRow("10:30", "Hviezdoslavovo nám. 4", "Peter Malý", "IN_PROGRESS"),
                new CleaningRow("11:15", "Obchodná 27", "—", "NEW"),
                new CleaningRow("13:00", "Panenská 8", "Eva Horváthová", "ASSIGNED"),
                new CleaningRow("15:45", "Laurinská 3", "Ján Kováč", "CANCELLED")
        );
    }

}
