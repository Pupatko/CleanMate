package com.cleanmate.presentation.detail;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.logging.Logger;

public class CleaningDetailController extends com.cleanmate.presentation.nav.BaseNavController {

    private static final Logger LOG = Logger.getLogger(CleaningDetailController.class.getName());
    private static final int THUMB_SIZE = 140;
    private static final int GRID_COLS = 4;

    @FXML private TextField propertyField;
    @FXML private TextField customerField;
    @FXML private ComboBox<String> employeeCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextField dateField;
    @FXML private TextField checkOutTimeField;
    @FXML private TextField checkInTimeField;
    @FXML private Label statusBadge;

    @FXML private ListView<ChecklistStep> checklistView;
    @FXML private Label progressLabel;

    @FXML private GridPane photoGrid;

    @FXML private Button editButton;
    @FXML private Button cancelEditButton;
    @FXML private Button confirmButton;

    private final ObservableList<ChecklistStep> steps = FXCollections.observableArrayList();
    private final ObservableList<File> photos = FXCollections.observableArrayList();

    // Saved values for cancel
    private String savedEmployee;
    private String savedStatus;
    private String savedDate;
    private String savedCheckOut;
    private String savedCheckIn;

    @FXML
    public void initialize() {
        LOG.info("Cleaning detail initialized");

        propertyField.setText("Panská 12, Bratislava");
        customerField.setText("Acme Rentals s.r.o.");
        dateField.setText("22.04.2026");
        checkOutTimeField.setText("10:30");
        checkInTimeField.setText("12:00");

        employeeCombo.setItems(FXCollections.observableArrayList(
                "— nepriradený —", "Anna Nová", "Peter Malý", "Eva Horváthová", "Ján Kováč"));
        employeeCombo.getSelectionModel().select("Peter Malý");

        statusCombo.setItems(FXCollections.observableArrayList(
                "NEW", "ASSIGNED", "IN_PROGRESS", "DONE", "CANCELLED"));
        statusCombo.getSelectionModel().select("IN_PROGRESS");
        statusCombo.valueProperty().addListener((obs, o, n) -> updateStatusBadge(n));
        updateStatusBadge(statusCombo.getValue());

        steps.setAll(
                new ChecklistStep("Vysávanie obývačky", true),
                new ChecklistStep("Výmena posteľnej bielizne", true),
                new ChecklistStep("Čistenie kúpeľne", false),
                new ChecklistStep("Doplnenie toaletných potrieb", false),
                new ChecklistStep("Kontrola chladničky", false),
                new ChecklistStep("Vyhodenie odpadu", false),
                new ChecklistStep("Finálna kontrola", false)
        );
        checklistView.setItems(steps);
        checklistView.setCellFactory(CheckBoxListCell.forListView(ChecklistStep::doneProperty));
        for (ChecklistStep s : steps) {
            s.doneProperty().addListener((obs, o, n) -> updateProgress());
        }
        updateProgress();

        photos.addListener((javafx.collections.ListChangeListener<File>) c -> renderPhotos());
        renderPhotos();

        setEditMode(false);
    }

    private void setEditMode(boolean editing) {
        propertyField.setEditable(editing);
        customerField.setEditable(editing);
        dateField.setEditable(editing);
        checkOutTimeField.setEditable(editing);
        checkInTimeField.setEditable(editing);
        employeeCombo.setDisable(!editing);
        statusCombo.setDisable(!editing);

        editButton.setVisible(!editing);
        editButton.setManaged(!editing);
        cancelEditButton.setVisible(editing);
        cancelEditButton.setManaged(editing);
        confirmButton.setVisible(editing);
        confirmButton.setManaged(editing);

        String styleOn  = "input-edit";
        String styleOff = "input-readonly";
        for (TextField f : new TextField[]{propertyField, customerField, dateField, checkOutTimeField, checkInTimeField}) {
            f.getStyleClass().removeAll(styleOn, styleOff);
            f.getStyleClass().add(editing ? styleOn : styleOff);
        }
    }

    @FXML
    private void onEdit() {
        savedEmployee = employeeCombo.getValue();
        savedStatus   = statusCombo.getValue();
        savedDate     = dateField.getText();
        savedCheckOut = checkOutTimeField.getText();
        savedCheckIn  = checkInTimeField.getText();
        setEditMode(true);
    }

    @FXML
    private void onCancelEdit() {
        employeeCombo.setValue(savedEmployee);
        statusCombo.setValue(savedStatus);
        dateField.setText(savedDate);
        checkOutTimeField.setText(savedCheckOut);
        checkInTimeField.setText(savedCheckIn);
        setEditMode(false);
    }

    @FXML
    private void onConfirmEdit() {
        LOG.info("Changes confirmed — employee=" + employeeCombo.getValue()
                + ", status=" + statusCombo.getValue()
                + ", date=" + dateField.getText()
                + ", checkOut=" + checkOutTimeField.getText()
                + ", checkIn=" + checkInTimeField.getText());
        setEditMode(false);
    }

    private void updateStatusBadge(String status) {
        if (status == null) return;
        statusBadge.setText(status);
        statusBadge.getStyleClass().setAll("status-badge", "status-" + status.toLowerCase());
    }

    private void updateProgress() {
        long done = steps.stream().filter(s -> s.doneProperty().get()).count();
        progressLabel.setText("Dokoncene: " + done + " / " + steps.size());
    }

    @FXML
    private void onAddPhoto() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Vyberte fotografiu");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Obrazky", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File f = fc.showOpenDialog(photoGrid.getScene().getWindow());
        if (f != null) {
            photos.add(f);
            LOG.info("Photo added: " + f.getName());
        }
    }

    private void renderPhotos() {
        photoGrid.getChildren().clear();
        for (int i = 0; i < photos.size(); i++) {
            photoGrid.add(buildThumb(photos.get(i)), i % GRID_COLS, i / GRID_COLS);
        }
        if (photos.isEmpty()) {
            Label empty = new Label("Zatial ziadne fotografie. Kliknite na \"Pridat fotku\".");
            empty.getStyleClass().add("empty-hint");
            photoGrid.add(empty, 0, 0);
        }
    }

    private StackPane buildThumb(File f) {
        StackPane tile = new StackPane();
        tile.getStyleClass().add("photo-tile");
        try {
            Image img = new Image(f.toURI().toString(), THUMB_SIZE, THUMB_SIZE, true, true);
            ImageView iv = new ImageView(img);
            iv.setFitWidth(THUMB_SIZE);
            iv.setFitHeight(THUMB_SIZE);
            iv.setPreserveRatio(true);
            tile.getChildren().add(iv);
        } catch (Exception ex) {
            Rectangle r = new Rectangle(THUMB_SIZE, THUMB_SIZE, Color.web("#E2E8F0"));
            tile.getChildren().addAll(r, new Label(f.getName()));
        }
        return tile;
    }

    @FXML
    private void onCancel() {
        statusCombo.setValue("CANCELLED");
        LOG.info("Cleaning cancelled");
    }
}
