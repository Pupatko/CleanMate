package com.cleanmate.presentation.checklist;

import com.cleanmate.presentation.detail.ChecklistStep;
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

import com.cleanmate.presentation.nav.LanguageManager;
import java.io.File;
import java.util.logging.Logger;

public class ChecklistController extends com.cleanmate.presentation.nav.BaseNavController {

    private static final Logger LOG = Logger.getLogger(ChecklistController.class.getName());
    private static final int THUMB = 120;
    private static final int COLS = 5;

    @FXML private Label propertyLabel;
    @FXML private Label metaLabel;
    @FXML private Label progressLabel;
    @FXML private ProgressBar progressBar;
    @FXML private ListView<ChecklistStep> checklist;
    @FXML private GridPane photoGrid;
    @FXML private ScrollPane photoScroll;
    @FXML private Button addPhotoButton;
    @FXML private Button completeButton;

    private final ObservableList<ChecklistStep> steps = FXCollections.observableArrayList();
    private final ObservableList<File> photos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        LOG.info("Checklist view initialized");

        propertyLabel.setText("Hviezdoslavovo nám. 4, Bratislava");
        metaLabel.setText("Zákazník: City Nest Bratislava  •  Čas: 10:30  •  Stav: IN_PROGRESS");

        steps.setAll(
                new ChecklistStep("Vysávanie obývačky", true),
                new ChecklistStep("Vysávanie spálne", true),
                new ChecklistStep("Výmena posteľnej bielizne", true),
                new ChecklistStep("Čistenie kúpeľne", false),
                new ChecklistStep("Doplnenie toaletných potrieb", false),
                new ChecklistStep("Kontrola chladničky", false),
                new ChecklistStep("Vyhodenie odpadu", false)
        );
        checklist.setItems(steps);
        checklist.setPlaceholder(com.cleanmate.presentation.util.EmptyState.build("✅", "empty.checklist"));
        checklist.setCellFactory(CheckBoxListCell.forListView(ChecklistStep::doneProperty));

        for (ChecklistStep s : steps) {
            s.doneProperty().addListener((obs, o, n) -> updateProgress());
        }

        photos.addListener((javafx.collections.ListChangeListener<File>) c -> renderPhotos());
        renderPhotos();
        updateProgress();
    }

    private void updateProgress() {
        int total = steps.size();
        long done = steps.stream().filter(s -> s.doneProperty().get()).count();
        double ratio = total == 0 ? 0.0 : (double) done / total;
        progressBar.setProgress(ratio);
        progressLabel.setText(done + " / " + total + "  (" + (int) (ratio * 100) + " %)");
        completeButton.setDisable(ratio < 1.0);
    }

    @FXML
    private void onAddPhoto() {
        FileChooser fc = new FileChooser();
        fc.setTitle(LanguageManager.getBundle().getString("checklist.photo.dialog"));
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(LanguageManager.getBundle().getString("checklist.photo.filter"), "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File f = fc.showOpenDialog(addPhotoButton.getScene().getWindow());
        if (f != null) {
            photos.add(f);
            LOG.info("Photo added: " + f.getName() + " (total=" + photos.size() + ")");
        }
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

    @FXML
    private void onComplete() {
        LOG.info("Cleaning completed — photos=" + photos.size() + ", steps=" + steps.size() + "/100%");
        completeButton.setDisable(true);
        completeButton.setText(LanguageManager.getBundle().getString("checklist.complete.done"));
    }

    @FXML
    private void onBack() {
        LOG.info("Back to schedule");
        navMySchedule();
    }
}
