package com.cleanmate.presentation.detail;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
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

public class CleaningDetailController {

    private static final Logger LOG = Logger.getLogger(CleaningDetailController.class.getName());
    private static final int THUMB_SIZE = 140;
    private static final int GRID_COLS = 4;

    @FXML private TextField propertyField;
    @FXML private TextField customerField;
    @FXML private ComboBox<String> employeeCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextField dateField;
    @FXML private Label statusBadge;

    @FXML private ListView<ChecklistStep> checklistView;
    @FXML private Label progressLabel;

    @FXML private GridPane photoGrid;

    private final ObservableList<ChecklistStep> steps = FXCollections.observableArrayList();
    private final ObservableList<File> photos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        LOG.info("Cleaning detail initialized");

        propertyField.setText("Panská 12, Bratislava");
        customerField.setText("Acme Rentals s.r.o.");
        dateField.setText("22.04.2026  10:30");

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
    }

    private void updateStatusBadge(String status) {
        if (status == null) return;
        statusBadge.setText(status);
        statusBadge.getStyleClass().setAll("status-badge", "status-" + status.toLowerCase());
    }

    private void updateProgress() {
        long done = steps.stream().filter(s -> s.doneProperty().get()).count();
        progressLabel.setText("Dokončené: " + done + " / " + steps.size());
    }

    @FXML
    private void onAddPhoto() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Vyberte fotografiu");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Obrázky", "*.png", "*.jpg", "*.jpeg", "*.gif"));
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
    private void onSave() {
        LOG.info("Save clicked — status=" + statusCombo.getValue()
                + ", employee=" + employeeCombo.getValue()
                + ", done=" + steps.stream().filter(s -> s.doneProperty().get()).count()
                + "/" + steps.size()
                + ", photos=" + photos.size());
    }

    @FXML
    private void onAssign() {
        String emp = employeeCombo.getValue();
        if (emp == null || emp.startsWith("—")) {
            LOG.warning("Assign failed: no employee selected");
            return;
        }
        statusCombo.setValue("ASSIGNED");
        LOG.info("Assigned to " + emp);
    }

    @FXML
    private void onCancel() {
        statusCombo.setValue("CANCELLED");
        LOG.info("Cleaning cancelled");
    }
}
