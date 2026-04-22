package com.cleanmate.presentation.plan;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.logging.Logger;

public class CleaningPlanBuilderController {

    private static final Logger LOG = Logger.getLogger(CleaningPlanBuilderController.class.getName());

    @FXML private TextField planNameField;
    @FXML private ComboBox<String> propertyCombo;

    @FXML private TextField stepNameField;
    @FXML private ComboBox<String> stepTypeCombo;
    @FXML private Button addStepButton;

    @FXML private ListView<PlanStep> stepList;
    @FXML private Label countLabel;
    @FXML private Label errorLabel;

    private final ObservableList<PlanStep> steps = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        LOG.info("Plan builder initialized");

        propertyCombo.setItems(FXCollections.observableArrayList(
                "Panská 12, Bratislava",
                "Hviezdoslavovo nám. 4",
                "Obchodná 27",
                "Panenská 8",
                "Laurinská 3"));
        propertyCombo.getSelectionModel().selectFirst();

        stepTypeCombo.setItems(FXCollections.observableArrayList(
                "CLEANING", "LAUNDRY", "CHECK", "RESTOCK", "PHOTO"));
        stepTypeCombo.getSelectionModel().selectFirst();

        steps.addAll(
                new PlanStep("Vysávanie obývačky", "CLEANING"),
                new PlanStep("Výmena posteľnej bielizne", "LAUNDRY"),
                new PlanStep("Čistenie kúpeľne", "CLEANING"),
                new PlanStep("Doplnenie toaletných potrieb", "RESTOCK"),
                new PlanStep("Foto finálneho stavu", "PHOTO"));

        stepList.setItems(steps);
        stepList.setCellFactory(lv -> new StepCell(steps));

        steps.addListener((javafx.collections.ListChangeListener<PlanStep>) c -> updateCount());
        updateCount();
        hideError();
    }

    private void updateCount() {
        countLabel.setText("Krokov: " + steps.size());
    }

    @FXML
    private void onAddStep() {
        String name = stepNameField.getText() == null ? "" : stepNameField.getText().trim();
        String type = stepTypeCombo.getValue();
        if (name.isEmpty()) {
            showError("Zadajte nazov kroku.");
            return;
        }
        if (type == null) {
            showError("Vyberte typ kroku.");
            return;
        }
        steps.add(new PlanStep(name, type));
        stepNameField.clear();
        hideError();
        LOG.info("Step added: " + name + " [" + type + "]");
    }

    @FXML
    private void onSave() {
        String planName = planNameField.getText() == null ? "" : planNameField.getText().trim();
        if (planName.isEmpty()) {
            showError("Zadajte nazov sablony.");
            return;
        }
        if (propertyCombo.getValue() == null) {
            showError("Vyberte apartman.");
            return;
        }
        if (steps.isEmpty()) {
            showError("Sablona musi obsahovat aspon jeden krok.");
            return;
        }
        hideError();
        LOG.info("Plan saved: '" + planName + "' for " + propertyCombo.getValue() + " (" + steps.size() + " steps)");
    }

    @FXML
    private void onClear() {
        steps.clear();
        stepNameField.clear();
        planNameField.clear();
        hideError();
        LOG.info("Builder cleared");
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private static final class StepCell extends ListCell<PlanStep> {
        private static final String MIME = "application/x-cleanmate-step-index";
        private final ObservableList<PlanStep> backing;

        StepCell(ObservableList<PlanStep> backing) {
            this.backing = backing;
            setOnDragDetected(this::onDragDetected);
            setOnDragOver(this::onDragOver);
            setOnDragDropped(this::onDragDropped);
            setOnDragDone(e -> pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("drop-target"), false));
            setOnDragExited(e -> pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("drop-target"), false));
        }

        private void onDragDetected(MouseEvent e) {
            if (isEmpty() || getItem() == null) return;
            Dragboard db = startDragAndDrop(TransferMode.MOVE);
            ClipboardContent cc = new ClipboardContent();
            cc.put(javafx.scene.input.DataFormat.lookupMimeType(MIME) != null
                    ? javafx.scene.input.DataFormat.lookupMimeType(MIME)
                    : new javafx.scene.input.DataFormat(MIME), getIndex());
            db.setContent(cc);
            e.consume();
        }

        private void onDragOver(DragEvent e) {
            if (e.getGestureSource() != this && e.getDragboard().hasContent(fmt())) {
                e.acceptTransferModes(TransferMode.MOVE);
                pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("drop-target"), true);
            }
            e.consume();
        }

        private void onDragDropped(DragEvent e) {
            Dragboard db = e.getDragboard();
            boolean ok = false;
            if (db.hasContent(fmt())) {
                int from = (Integer) db.getContent(fmt());
                int to = isEmpty() ? backing.size() - 1 : getIndex();
                if (from != to && from >= 0 && from < backing.size() && to >= 0 && to < backing.size()) {
                    PlanStep moved = backing.remove(from);
                    backing.add(to, moved);
                    ok = true;
                }
            }
            e.setDropCompleted(ok);
            e.consume();
        }

        private static javafx.scene.input.DataFormat fmt() {
            javafx.scene.input.DataFormat existing = javafx.scene.input.DataFormat.lookupMimeType(MIME);
            return existing != null ? existing : new javafx.scene.input.DataFormat(MIME);
        }

        @Override
        protected void updateItem(PlanStep item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
                return;
            }
            Label handle = new Label("☰");
            handle.getStyleClass().add("drag-handle");

            Label order = new Label(String.valueOf(getIndex() + 1) + ".");
            order.getStyleClass().add("step-order");

            Label name = new Label(item.getName());
            name.getStyleClass().add("step-name");

            Label type = new Label(item.getType());
            type.getStyleClass().setAll("type-chip", "type-" + item.getType().toLowerCase());

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button remove = new Button("×");
            remove.getStyleClass().add("icon-button");
            remove.setOnAction(e -> backing.remove(getItem()));

            HBox row = new HBox(10, handle, order, name, spacer, type, remove);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("step-row");

            setGraphic(row);
            setText(null);
        }
    }
}
