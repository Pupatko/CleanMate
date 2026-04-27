package com.cleanmate.presentation.portal;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import com.cleanmate.presentation.nav.LanguageManager;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

public class CustomerPortalController extends com.cleanmate.presentation.nav.BaseNavController {

    private static final Logger LOG = Logger.getLogger(CustomerPortalController.class.getName());
    private static final String ALL = "— všetky apartmány —";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @FXML private ComboBox<String> propertyFilter;
    @FXML private Label countLabel;

    @FXML private TableView<HistoryRow> table;
    @FXML private TableColumn<HistoryRow, LocalDate> colDate;
    @FXML private TableColumn<HistoryRow, String> colProperty;
    @FXML private TableColumn<HistoryRow, String> colEmployee;
    @FXML private TableColumn<HistoryRow, String> colStatus;
    @FXML private TableColumn<HistoryRow, Number> colPhotos;
    @FXML private TableColumn<HistoryRow, Number> colRating;

    @FXML private Label qcPlaceholder;
    @FXML private Label qcTitle;
    @FXML private Label qcMeta;
    @FXML private Label qcRating;
    @FXML private Label qcNote;
    @FXML private FlowPane qcPhotos;

    private final ObservableList<HistoryRow> data = FXCollections.observableArrayList();
    private FilteredList<HistoryRow> filtered;

    @FXML
    public void initialize() {
        LOG.info("Customer portal initialized");

        LocalDate today = LocalDate.now();
        data.setAll(
                new HistoryRow(today.minusDays(0), "Panská 12, Bratislava",     "Anna Nová",        "DONE", 8, 5, "Perfektné, žiadne poznámky."),
                new HistoryRow(today.minusDays(1), "Panská 12, Bratislava",     "Peter Malý",       "DONE", 6, 4, "Drobné odtlačky na zrkadle v kúpeľni."),
                new HistoryRow(today.minusDays(3), "Hviezdoslavovo nám. 4",     "Eva Horváthová",   "DONE", 10, 5, "Skvelá práca, host spokojný."),
                new HistoryRow(today.minusDays(5), "Panská 12, Bratislava",     "Ján Kováč",        "DONE", 5, 4, ""),
                new HistoryRow(today.minusDays(7), "Hviezdoslavovo nám. 4",     "Anna Nová",        "DONE", 7, 5, "Doplnené všetky kozmetické prostriedky."),
                new HistoryRow(today.minusDays(10),"Obchodná 27",               "Peter Malý",       "DONE", 4, 3, "Odpad z kuchyne ostal pod drezom."),
                new HistoryRow(today.minusDays(14),"Panská 12, Bratislava",     "Katarína Veselá",  "DONE", 9, 5, "")
        );

        ObservableList<String> props = FXCollections.observableArrayList(ALL);
        data.stream().map(HistoryRow::getProperty).distinct().sorted().forEach(props::add);
        propertyFilter.setItems(props);
        propertyFilter.getSelectionModel().selectFirst();

        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDate.setCellFactory(dateCellFactory());
        colProperty.setCellValueFactory(new PropertyValueFactory<>("property"));
        colEmployee.setCellValueFactory(new PropertyValueFactory<>("employee"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(v);
                badge.getStyleClass().setAll("status-badge", "status-" + v.toLowerCase());
                setGraphic(badge);
                setText(null);
            }
        });
        colPhotos.setCellValueFactory(new PropertyValueFactory<>("photoCount"));
        colRating.setCellValueFactory(new PropertyValueFactory<>("rating"));
        colRating.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Number v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); return; }
                int r = v.intValue();
                setText("★".repeat(r) + "☆".repeat(5 - r));
                setStyle("-fx-text-fill: #F59E0B;");
            }
        });

        filtered = new FilteredList<>(data, r -> true);
        table.setItems(filtered);

        propertyFilter.valueProperty().addListener((obs, o, n) -> applyFilter());
        table.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> showQC(n));

        applyFilter();
        showQC(null);
    }

    private Callback<TableColumn<HistoryRow, LocalDate>, TableCell<HistoryRow, LocalDate>> dateCellFactory() {
        return c -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v.format(DATE_FMT));
            }
        };
    }

    private void applyFilter() {
        String sel = propertyFilter.getValue();
        filtered.setPredicate(r -> sel == null || ALL.equals(sel) || sel.equals(r.getProperty()));
        countLabel.setText(LanguageManager.getBundle().getString("portal.count.prefix") + " " + filtered.size() + " / " + data.size());
    }

    private void showQC(HistoryRow r) {
        boolean has = r != null;
        qcPlaceholder.setVisible(!has); qcPlaceholder.setManaged(!has);
        qcTitle.setVisible(has);        qcTitle.setManaged(has);
        qcMeta.setVisible(has);         qcMeta.setManaged(has);
        qcRating.setVisible(has);       qcRating.setManaged(has);
        qcNote.setVisible(has);         qcNote.setManaged(has);
        qcPhotos.setVisible(has);       qcPhotos.setManaged(has);

        if (!has) { qcPhotos.getChildren().clear(); return; }

        qcTitle.setText(r.getProperty());
        qcMeta.setText(r.getDate().format(DATE_FMT) + "  •  " + r.getEmployee() + "  •  " + r.getStatus());
        qcRating.setText(LanguageManager.getBundle().getString("portal.qc.rating.prefix") + " "
                + "★".repeat(r.getRating()) + "☆".repeat(5 - r.getRating()));
        qcNote.setText(r.getNote() == null || r.getNote().isBlank()
                ? LanguageManager.getBundle().getString("portal.qc.no.note")
                : "„" + r.getNote() + "\"");

        qcPhotos.getChildren().clear();
        for (int i = 0; i < r.getPhotoCount(); i++) {
            StackPane tile = new StackPane();
            tile.getStyleClass().add("photo-tile");
            tile.setPrefSize(100, 100);
            tile.setMinSize(100, 100);
            Rectangle rect = new Rectangle(100, 100, Color.web("#DBEAFE"));
            Label n = new Label("#" + (i + 1));
            n.setStyle("-fx-text-fill: #2563EB; -fx-font-weight: bold;");
            tile.getChildren().addAll(rect, n);
            qcPhotos.getChildren().add(tile);
        }
    }

    @FXML
    private void onExportXml() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Uložiť XML report");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML", "*.xml"));
        fc.setInitialFileName("cleanmate-report-" + LocalDate.now() + ".xml");
        File out = fc.showSaveDialog(table.getScene().getWindow());
        if (out == null) return;

        String xml = buildXml();
        try {
            Files.writeString(out.toPath(), xml, StandardCharsets.UTF_8);
            LOG.info("Report exported to " + out.getAbsolutePath() + " (" + filtered.size() + " rows)");
            String msg = java.text.MessageFormat.format(
                    LanguageManager.getBundle().getString("toast.export.ok"), filtered.size());
            toast(msg, com.cleanmate.presentation.util.ToastManager.Type.SUCCESS);
        } catch (IOException ex) {
            LOG.warning("Export failed: " + ex.getMessage());
            toast(LanguageManager.getBundle().getString("toast.export.fail"), com.cleanmate.presentation.util.ToastManager.Type.ERROR);
        }
    }

    private String buildXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<cleaningReport generated=\"").append(LocalDate.now()).append("\" ")
          .append("filter=\"").append(escape(propertyFilter.getValue())).append("\" ")
          .append("count=\"").append(filtered.size()).append("\">\n");
        for (HistoryRow r : filtered) {
            sb.append("  <cleaning>\n")
              .append("    <date>").append(r.getDate()).append("</date>\n")
              .append("    <property>").append(escape(r.getProperty())).append("</property>\n")
              .append("    <employee>").append(escape(r.getEmployee())).append("</employee>\n")
              .append("    <status>").append(r.getStatus()).append("</status>\n")
              .append("    <photoCount>").append(r.getPhotoCount()).append("</photoCount>\n")
              .append("    <rating>").append(r.getRating()).append("</rating>\n")
              .append("    <note>").append(escape(r.getNote())).append("</note>\n")
              .append("  </cleaning>\n");
        }
        sb.append("</cleaningReport>\n");
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
