package com.cleanmate.presentation.detail;

import com.cleanmate.presentation.calendar.CleaningCalendarController.CalendarCleaningItem;
import com.cleanmate.presentation.nav.LanguageManager;
import com.cleanmate.presentation.util.ConfirmDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class CleaningDetailController extends com.cleanmate.presentation.nav.BaseNavController {

    private static final Logger LOG = Logger.getLogger(CleaningDetailController.class.getName());
    private static final int THUMB_SIZE = 140;
    private static final int GRID_COLS  = 4;

    // ── Info fields ──────────────────────────────────────────────────────────
    @FXML private TextField propertyField;
    @FXML private TextField customerField;
    @FXML private ComboBox<String> employeeCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextField dateField;
    @FXML private TextField checkOutTimeField;
    @FXML private TextField checkInTimeField;
    @FXML private Label statusBadge;

    // ── Checklist tab ────────────────────────────────────────────────────────
    @FXML private ListView<ChecklistStep> checklistView;
    @FXML private Label progressLabel;

    // ── Photos tab ───────────────────────────────────────────────────────────
    @FXML private GridPane photoGrid;

    // ── QC tab ───────────────────────────────────────────────────────────────
    @FXML private Label qcLockedLabel;
    @FXML private VBox  qcForm;
    @FXML private HBox  starBox;
    @FXML private TextArea qcNoteArea;
    @FXML private Label qcSavedLabel;

    // ── Status stepper ───────────────────────────────────────────────────────
    @FXML private HBox stepperBox;

    // ── Edit buttons ─────────────────────────────────────────────────────────
    @FXML private Button editButton;
    @FXML private Button cancelEditButton;
    @FXML private Button confirmButton;

    private final ObservableList<ChecklistStep> steps  = FXCollections.observableArrayList();
    private final ObservableList<File>          photos = FXCollections.observableArrayList();

    // Saved values for cancel-edit
    private String savedEmployee;
    private String savedStatus;
    private String savedDate;
    private String savedCheckOut;
    private String savedCheckIn;

    public static CalendarCleaningItem selected = null;

    // QC state
    private int currentRating = 0;
    private final List<Label> starLabels = new ArrayList<>();

    // ── Lifecycle ────────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        LOG.info("Cleaning detail initialized");

        employeeCombo.setItems(FXCollections.observableArrayList(
                "— nepriradený —", "Anna Nová", "Peter Malý", "Eva Horváthová", "Ján Kováč"));

        statusCombo.setItems(FXCollections.observableArrayList(
                "NEW", "ASSIGNED", "IN_PROGRESS", "DONE", "CANCELLED"));

        if (selected != null) {
            java.time.format.DateTimeFormatter dateFmt = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy");
            java.time.format.DateTimeFormatter timeFmt = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
            propertyField.setText(selected.property());
            customerField.setText(selected.customer());
            dateField.setText(selected.date().format(dateFmt));
            checkOutTimeField.setText(selected.checkOut().format(timeFmt));
            checkInTimeField.setText(selected.checkIn().format(timeFmt));
            String emp = selected.employee();
            if (!employeeCombo.getItems().contains(emp)) employeeCombo.getItems().add(emp);
            employeeCombo.getSelectionModel().select(emp);
            statusCombo.getSelectionModel().select(selected.status());
            selected = null;
        } else {
            propertyField.setText("Panská 12, Bratislava");
            customerField.setText("Acme Rentals s.r.o.");
            dateField.setText("22.04.2026");
            checkOutTimeField.setText("10:30");
            checkInTimeField.setText("12:00");
            employeeCombo.getSelectionModel().select("Peter Malý");
            statusCombo.getSelectionModel().select("IN_PROGRESS");
        }
        statusCombo.valueProperty().addListener((obs, o, n) -> {
            updateStatusBadge(n);
            updateQcVisibility(n);
            buildStepper(n);
        });
        updateStatusBadge(statusCombo.getValue());
        buildStepper(statusCombo.getValue());

        steps.setAll(
                new ChecklistStep("Vysávanie obývačky",          true),
                new ChecklistStep("Výmena posteľnej bielizne",   true),
                new ChecklistStep("Čistenie kúpeľne",            false),
                new ChecklistStep("Doplnenie toaletných potrieb", false),
                new ChecklistStep("Kontrola chladničky",         false),
                new ChecklistStep("Vyhodenie odpadu",            false),
                new ChecklistStep("Finálna kontrola",            false)
        );
        checklistView.setItems(steps);
        checklistView.setPlaceholder(com.cleanmate.presentation.util.EmptyState.build("✅", "empty.checklist"));
        checklistView.setCellFactory(CheckBoxListCell.forListView(ChecklistStep::doneProperty));
        for (ChecklistStep s : steps) s.doneProperty().addListener((obs, o, n) -> updateProgress());
        updateProgress();

        photos.addListener((javafx.collections.ListChangeListener<File>) c -> renderPhotos());
        renderPhotos();

        buildStarRow();
        updateQcVisibility(statusCombo.getValue());

        setEditMode(false);
    }

    // ── Edit mode ────────────────────────────────────────────────────────────

    private void setEditMode(boolean editing) {
        propertyField.setEditable(editing);
        customerField.setEditable(editing);
        dateField.setEditable(editing);
        checkOutTimeField.setEditable(editing);
        checkInTimeField.setEditable(editing);
        employeeCombo.setDisable(!editing);
        statusCombo.setDisable(!editing);

        editButton.setVisible(!editing);       editButton.setManaged(!editing);
        cancelEditButton.setVisible(editing);  cancelEditButton.setManaged(editing);
        confirmButton.setVisible(editing);     confirmButton.setManaged(editing);

        String on = "input-edit", off = "input-readonly";
        for (TextField f : new TextField[]{propertyField, customerField, dateField,
                                           checkOutTimeField, checkInTimeField}) {
            f.getStyleClass().removeAll(on, off);
            f.getStyleClass().add(editing ? on : off);
        }
    }

    @FXML private void onEdit() {
        savedEmployee = employeeCombo.getValue();
        savedStatus   = statusCombo.getValue();
        savedDate     = dateField.getText();
        savedCheckOut = checkOutTimeField.getText();
        savedCheckIn  = checkInTimeField.getText();
        setEditMode(true);
    }

    @FXML private void onCancelEdit() {
        employeeCombo.setValue(savedEmployee);
        statusCombo.setValue(savedStatus);
        dateField.setText(savedDate);
        checkOutTimeField.setText(savedCheckOut);
        checkInTimeField.setText(savedCheckIn);
        setEditMode(false);
    }

    @FXML private void onConfirmEdit() {
        com.cleanmate.presentation.util.ChangeSummary diff =
                new com.cleanmate.presentation.util.ChangeSummary()
                .add("Zamestnanec", savedEmployee, employeeCombo.getValue())
                .add("Stav",        savedStatus,   statusCombo.getValue())
                .add("Dátum",       savedDate,     dateField.getText())
                .add("CHECK-OUT",   savedCheckOut, checkOutTimeField.getText())
                .add("CHECK-IN",    savedCheckIn,  checkInTimeField.getText());
        LOG.info("Changes confirmed");
        setEditMode(false);
        toast(LanguageManager.getBundle().getString("toast.changes.saved"), com.cleanmate.presentation.util.ToastManager.Type.SUCCESS);
        diff.show("Úpravy upratovania");
    }

    @FXML private void onCancel() {
        if (!ConfirmDialog.show("confirm.cancel.cleaning.header",
                LanguageManager.getBundle().getString("confirm.cancel.cleaning.content"))) return;
        statusCombo.setValue("CANCELLED");
        LOG.info("Cleaning cancelled");
        toast(LanguageManager.getBundle().getString("toast.cleaning.cancelled"), com.cleanmate.presentation.util.ToastManager.Type.INFO);
    }

    // ── Checklist ────────────────────────────────────────────────────────────

    private void updateProgress() {
        long done = steps.stream().filter(s -> s.doneProperty().get()).count();
        progressLabel.setText(LanguageManager.getBundle().getString("detail.progress.prefix")
                + " " + done + " / " + steps.size());
    }

    private void updateStatusBadge(String status) {
        if (status == null) return;
        statusBadge.setText(status);
        statusBadge.getStyleClass().setAll("status-badge", "status-" + status.toLowerCase());
    }

    private void buildStepper(String status) {
        if (stepperBox == null || status == null) return;
        stepperBox.getChildren().clear();

        var b = LanguageManager.getBundle();
        String[] keys    = {"stepper.new", "stepper.assigned", "stepper.in_progress", "stepper.done"};
        String[] statuses = {"NEW", "ASSIGNED", "IN_PROGRESS", "DONE"};
        String[] icons    = {"1", "2", "3", "4"};

        boolean cancelled = "CANCELLED".equals(status);
        int currentIdx = -1;
        if (!cancelled) {
            for (int i = 0; i < statuses.length; i++) {
                if (statuses[i].equals(status)) { currentIdx = i; break; }
            }
        }

        for (int i = 0; i < statuses.length; i++) {
            // connecting line before each dot (except the first)
            if (i > 0) {
                Region line = new Region();
                line.getStyleClass().add("stepper-line");
                line.getStyleClass().add(cancelled || i <= currentIdx ? "stepper-line-done" : "stepper-line-future");
                HBox.setHgrow(line, Priority.ALWAYS);
                stepperBox.getChildren().add(line);
            }

            String dotStyle;
            String labelStyle = "stepper-label";
            if (cancelled) {
                dotStyle = "stepper-dot-future";
            } else if (i < currentIdx) {
                dotStyle = "stepper-dot-done";
                labelStyle = "stepper-label stepper-label-done";
            } else if (i == currentIdx) {
                dotStyle = "stepper-dot-current";
                labelStyle = "stepper-label stepper-label-current";
            } else {
                dotStyle = "stepper-dot-future";
            }

            Label dot = new Label(i < currentIdx ? "✓" : icons[i]);
            dot.getStyleClass().addAll("stepper-dot", dotStyle);
            dot.setAlignment(Pos.CENTER);

            Label lbl = new Label(b.getString(keys[i]));
            lbl.getStyleClass().setAll(labelStyle.split(" "));
            lbl.setAlignment(Pos.CENTER);

            VBox step = new VBox(4, dot, lbl);
            step.setAlignment(Pos.CENTER);
            stepperBox.getChildren().add(step);
        }

        if (cancelled) {
            Region spacer = new Region();
            spacer.setMinWidth(12);
            stepperBox.getChildren().add(spacer);

            Label badge = new Label("✕ CANCELLED");
            badge.getStyleClass().setAll("status-badge", "status-cancelled");
            stepperBox.getChildren().add(badge);
        }
    }

    // ── Photos ───────────────────────────────────────────────────────────────

    @FXML private void onAddPhoto() {
        FileChooser fc = new FileChooser();
        fc.setTitle(LanguageManager.getBundle().getString("checklist.photo.dialog"));
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                LanguageManager.getBundle().getString("checklist.photo.filter"),
                "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File f = fc.showOpenDialog(photoGrid.getScene().getWindow());
        if (f != null) { photos.add(f); LOG.info("Photo added: " + f.getName()); }
    }

    private void renderPhotos() {
        photoGrid.getChildren().clear();
        if (photos.isEmpty()) {
            Label empty = new Label(LanguageManager.getBundle().getString("checklist.photo.hint"));
            empty.getStyleClass().add("empty-hint");
            photoGrid.add(empty, 0, 0);
            return;
        }
        for (int i = 0; i < photos.size(); i++) {
            photoGrid.add(buildThumb(photos.get(i)), i % GRID_COLS, i / GRID_COLS);
        }
    }

    private StackPane buildThumb(File f) {
        StackPane tile = new StackPane();
        tile.getStyleClass().add("photo-tile");
        try {
            Image img = new Image(f.toURI().toString(), THUMB_SIZE, THUMB_SIZE, true, true);
            ImageView iv = new ImageView(img);
            iv.setFitWidth(THUMB_SIZE); iv.setFitHeight(THUMB_SIZE); iv.setPreserveRatio(true);
            tile.getChildren().add(iv);
        } catch (Exception ex) {
            Rectangle r = new Rectangle(THUMB_SIZE, THUMB_SIZE, Color.web("#E2E8F0"));
            tile.getChildren().addAll(r, new Label(f.getName()));
        }
        return tile;
    }

    // ── QC Rating ────────────────────────────────────────────────────────────

    private void buildStarRow() {
        starBox.getChildren().clear();
        starLabels.clear();
        for (int i = 1; i <= 5; i++) {
            Label star = new Label("☆");
            star.getStyleClass().add("star-btn");
            final int rating = i;
            star.setOnMouseClicked(e -> selectRating(rating));
            star.setOnMouseEntered(e -> renderStarHover(rating));
            star.setOnMouseExited(e  -> renderStars(currentRating));
            starLabels.add(star);
        }
        starBox.getChildren().addAll(starLabels);
        renderStars(currentRating);
    }

    private void selectRating(int r) {
        currentRating = r;
        renderStars(r);
        qcSavedLabel.setText("");
    }

    private void renderStars(int filled) {
        for (int i = 0; i < starLabels.size(); i++) {
            Label s = starLabels.get(i);
            if (i < filled) {
                s.setText("★");
                s.getStyleClass().setAll("star-btn-filled");
            } else {
                s.setText("☆");
                s.getStyleClass().setAll("star-btn");
            }
        }
    }

    private void renderStarHover(int upTo) {
        for (int i = 0; i < starLabels.size(); i++) {
            Label s = starLabels.get(i);
            if (i < upTo) {
                s.setText("★");
                s.getStyleClass().setAll("star-btn-filled");
            } else {
                s.setText("☆");
                s.getStyleClass().setAll("star-btn");
            }
        }
    }

    private void updateQcVisibility(String status) {
        boolean done = "DONE".equals(status);
        qcLockedLabel.setVisible(!done);  qcLockedLabel.setManaged(!done);
        qcForm.setVisible(done);          qcForm.setManaged(done);
        if (done) qcSavedLabel.setText("");
    }

    @FXML private void onSaveQc() {
        if (currentRating == 0) {
            qcSavedLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 12px;");
            qcSavedLabel.setText(LanguageManager.getBundle().getString("detail.qc.no.rating"));
            toast(LanguageManager.getBundle().getString("toast.qc.no.rating"), com.cleanmate.presentation.util.ToastManager.Type.ERROR);
            return;
        }
        String note = qcNoteArea.getText() == null ? "" : qcNoteArea.getText().trim();
        LOG.info("QC saved: rating=" + currentRating + "/5, note='" + note + "'");
        qcSavedLabel.setStyle("-fx-text-fill: #10B981; -fx-font-size: 12px; -fx-font-weight: bold;");
        qcSavedLabel.setText(LanguageManager.getBundle().getString("detail.qc.saved"));
        toast(LanguageManager.getBundle().getString("toast.qc.saved"), com.cleanmate.presentation.util.ToastManager.Type.SUCCESS);
    }
}
