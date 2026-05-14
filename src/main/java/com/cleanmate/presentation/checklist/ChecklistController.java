package com.cleanmate.presentation.checklist;

import com.cleanmate.model.Cleaning;
import com.cleanmate.presentation.detail.ChecklistStep;
import com.cleanmate.presentation.nav.LanguageManager;
import com.cleanmate.presentation.util.EmptyState;
import com.cleanmate.service.ServiceLocator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

public class ChecklistController extends com.cleanmate.presentation.nav.BaseNavController {

    private static final Logger LOG = Logger.getLogger(ChecklistController.class.getName());
    private static final int THUMB = 120;
    private static final int COLS  = 5;

    /** Set before navigating here. */
    public static Cleaning currentCleaning = null;

    @FXML private Label       propertyLabel;
    @FXML private Label       metaLabel;
    @FXML private Label       progressLabel;
    @FXML private ProgressBar progressBar;
    @FXML private ListView<ChecklistStep> checklist;
    @FXML private GridPane    photoGrid;
    @FXML private ScrollPane  photoScroll;
    @FXML private Button      addPhotoButton;
    @FXML private Button      completeButton;
    @FXML private Label       statusBanner;

    private final ObservableList<ChecklistStep> steps  = FXCollections.observableArrayList();
    private final ObservableList<File>          photos = FXCollections.observableArrayList();

    private Cleaning cleaning;

    @FXML
    public void initialize() {
        LOG.info("Checklist view initialized");

        cleaning = currentCleaning;
        currentCleaning = null;

        if (cleaning != null) {
            propertyLabel.setText(cleaning.property());

            String time = cleaning.checkOut().format(DateTimeFormatter.ofPattern("HH:mm"));
            metaLabel.setText("Zákazník: " + cleaning.customer()
                    + "  •  Čas: " + time
                    + "  •  Stav: " + cleaning.status());

            List<String> taskNames = ServiceLocator.apartments().getAll().stream()
                    .filter(a -> a.getAddress().equals(cleaning.property()))
                    .findFirst()
                    .map(a -> a.getTaskNames())
                    .orElse(List.of());

            if (taskNames.isEmpty()) {
                steps.add(new ChecklistStep("Upratanie priestoru", false));
            } else {
                taskNames.forEach(name -> steps.add(new ChecklistStep(name, false)));
            }
        } else {
            propertyLabel.setText("—");
            metaLabel.setText("—");
        }

        checklist.setItems(steps);
        checklist.setPlaceholder(EmptyState.build("✅", "empty.checklist"));
        checklist.setCellFactory(CheckBoxListCell.forListView(ChecklistStep::doneProperty));

        for (ChecklistStep s : steps) {
            s.doneProperty().addListener((obs, o, n) -> updateProgress());
        }

        photos.addListener((javafx.collections.ListChangeListener<File>) c -> renderPhotos());
        renderPhotos();
        updateProgress();

        statusBanner.setVisible(false);
        statusBanner.setManaged(false);

        if (cleaning != null) {
            boolean pastDay = cleaning.date().isBefore(LocalDate.now());
            boolean done    = "DONE".equals(cleaning.status());

            if (pastDay) {
                applyLock();
            } else if (done) {
                showSuccess();
            }
        }
    }

    private void updateProgress() {
        int  total = steps.size();
        long done  = steps.stream().filter(s -> s.doneProperty().get()).count();
        double ratio = total == 0 ? 0.0 : (double) done / total;
        progressBar.setProgress(ratio);
        progressLabel.setText(done + " / " + total + "  (" + (int)(ratio * 100) + " %)");
        completeButton.setDisable(ratio < 1.0);
    }

    @FXML
    private void onAddPhoto() {
        FileChooser fc = new FileChooser();
        fc.setTitle(LanguageManager.getBundle().getString("checklist.photo.dialog"));
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        LanguageManager.getBundle().getString("checklist.photo.filter"),
                        "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File f = fc.showOpenDialog(addPhotoButton.getScene().getWindow());
        if (f != null) {
            photos.add(f);
            LOG.info("Photo added: " + f.getName());
        }
    }

    @FXML
    private void onComplete() {
        if (cleaning != null) {
            cleaning = cleaning.withStatus("DONE");
            ServiceLocator.cleanings().save(cleaning);
            LOG.info("Cleaning marked DONE: " + cleaning.id());
        }
        showSuccess();
    }

    private void showSuccess() {
        completeButton.setDisable(true);
        completeButton.setText(LanguageManager.getBundle().getString("checklist.complete.done"));
        statusBanner.setText("✓  Upratovanie bolo úspešne dokončené. Dnes ho môžeš ešte upraviť.");
        statusBanner.getStyleClass().removeAll("banner-lock");
        statusBanner.getStyleClass().add("banner-done");
        statusBanner.setVisible(true);
        statusBanner.setManaged(true);
    }

    private void applyLock() {
        completeButton.setDisable(true);
        completeButton.setVisible(false);
        completeButton.setManaged(false);
        addPhotoButton.setDisable(true);
        checklist.setDisable(true);
        statusBanner.setText("🔒  Tento záznam je uzamknutý. Zmeny môže vykonať iba administrátor.");
        statusBanner.getStyleClass().removeAll("banner-done");
        statusBanner.getStyleClass().add("banner-lock");
        statusBanner.setVisible(true);
        statusBanner.setManaged(true);
    }

    @FXML
    private void onBack() {
        navMySchedule();
    }

    private void renderPhotos() {
        photoGrid.getChildren().clear();
        if (photos.isEmpty()) {
            Label hint = new Label(LanguageManager.getBundle().getString("checklist.photo.hint"));
            hint.getStyleClass().add("empty-hint");
            photoGrid.add(hint, 0, 0);
            return;
        }
        for (int i = 0; i < photos.size(); i++) {
            photoGrid.add(buildThumb(photos.get(i)), i % COLS, i / COLS);
        }
    }

    private StackPane buildThumb(File f) {
        StackPane tile = new StackPane();
        tile.getStyleClass().add("photo-tile");
        tile.setPrefSize(THUMB, THUMB);
        tile.setMinSize(THUMB, THUMB);
        try {
            Image img = new Image(f.toURI().toString(), THUMB, THUMB, true, true);
            ImageView iv = new ImageView(img);
            iv.setFitWidth(THUMB);
            iv.setFitHeight(THUMB);
            iv.setPreserveRatio(true);
            tile.getChildren().add(iv);
        } catch (Exception ex) {
            Rectangle r = new Rectangle(THUMB, THUMB, Color.web("#E2E8F0"));
            tile.getChildren().addAll(r, new Label(f.getName()));
        }
        return tile;
    }
}
