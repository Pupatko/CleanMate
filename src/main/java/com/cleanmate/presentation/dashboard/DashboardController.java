package com.cleanmate.presentation.dashboard;

import com.cleanmate.model.Cleaning;
import com.cleanmate.presentation.detail.CleaningDetailController;
import com.cleanmate.presentation.nav.BaseNavController;
import com.cleanmate.presentation.util.EmptyState;
import com.cleanmate.presentation.util.ToastManager;
import com.cleanmate.service.ServiceLocator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
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

    // ── XML Import ────────────────────────────────────────────────────────────

    @FXML
    private void onImportXml() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Vybrať XML súbor na import");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML súbory", "*.xml"));
        File file = fc.showOpenDialog(recentTable.getScene().getWindow());
        if (file == null) return;

        int imported = 0;
        int skipped  = 0;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);
            doc.getDocumentElement().normalize();

            NodeList nodes = doc.getElementsByTagName("cleaning");
            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                try {
                    LocalDate date     = LocalDate.parse(text(el, "date"), dateFmt);
                    LocalTime checkOut = LocalTime.parse(text(el, "checkOut"), timeFmt);
                    LocalTime checkIn  = LocalTime.parse(text(el, "checkIn"),  timeFmt);
                    String property    = text(el, "property");
                    String customer    = text(el, "customer");
                    String employee    = text(el, "employee");
                    String status      = text(el, "status");

                    if (property.isBlank()) { skipped++; continue; }

                    Cleaning c = new Cleaning(UUID.randomUUID().toString(),
                            date, checkOut, checkIn, property, customer, employee, status, 0, "");
                    ServiceLocator.cleanings().save(c);
                    imported++;
                } catch (Exception ex) {
                    LOG.warning("Skipping malformed <cleaning> #" + i + ": " + ex.getMessage());
                    skipped++;
                }
            }

            toast("Import hotový: " + imported + " importované, " + skipped + " preskočené.", ToastManager.Type.SUCCESS);
            LOG.info("XML import done: imported=" + imported + " skipped=" + skipped);

            List<Cleaning> todayList = ServiceLocator.cleanings().getByDate(LocalDate.now());
            recentTable.setItems(buildRows(todayList));

        } catch (Exception ex) {
            LOG.severe("XML import failed: " + ex.getMessage());
            toast("Import zlyhal: " + ex.getMessage(), ToastManager.Type.ERROR);
        }
    }

    private static String text(Element el, String tag) {
        NodeList nl = el.getElementsByTagName(tag);
        if (nl.getLength() == 0) return "";
        return nl.item(0).getTextContent().trim();
    }

    // ── XML Export ────────────────────────────────────────────────────────────

    @FXML
    private void onExportXml() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Uložiť XML export");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML súbory", "*.xml"));
        fc.setInitialFileName("cleanmate-cleanings-" + LocalDate.now() + ".xml");
        File file = fc.showSaveDialog(recentTable.getScene().getWindow());
        if (file == null) return;

        List<Cleaning> all = ServiceLocator.cleanings().getAll();
        try (PrintWriter pw = new PrintWriter(file, StandardCharsets.UTF_8)) {
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            pw.println("<cleanings generated=\"" + LocalDate.now() + "\" count=\"" + all.size() + "\">");
            for (Cleaning c : all) {
                pw.println("  <cleaning>");
                pw.println("    <date>"     + c.date()         + "</date>");
                pw.println("    <checkOut>" + c.checkOut()     + "</checkOut>");
                pw.println("    <checkIn>"  + c.checkIn()      + "</checkIn>");
                pw.println("    <property>" + esc(c.property()) + "</property>");
                pw.println("    <customer>" + esc(c.customer()) + "</customer>");
                pw.println("    <employee>" + esc(c.employee()) + "</employee>");
                pw.println("    <status>"   + c.status()       + "</status>");
                pw.println("    <rating>"   + c.qcRating()     + "</rating>");
                pw.println("    <note>"     + esc(c.qcNote())  + "</note>");
                pw.println("  </cleaning>");
            }
            pw.println("</cleanings>");

            toast("Export hotový: " + all.size() + " upratovaní uložených.", ToastManager.Type.SUCCESS);
            LOG.info("XML export done: " + all.size() + " cleanings → " + file.getName());

        } catch (Exception ex) {
            LOG.severe("XML export failed: " + ex.getMessage());
            toast("Export zlyhal: " + ex.getMessage(), ToastManager.Type.ERROR);
        }
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

}
