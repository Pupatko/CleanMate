package com.cleanmate.presentation.customer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class CustomerManagementController extends com.cleanmate.presentation.nav.BaseNavController {

    private static final Logger LOG = Logger.getLogger(CustomerManagementController.class.getName());

    @FXML private TextField searchField;
    @FXML private Label searchHintLabel;

    @FXML private TableView<CustomerRow> table;
    @FXML private TableColumn<CustomerRow, String> colName;
    @FXML private TableColumn<CustomerRow, String> colEmail;
    @FXML private TableColumn<CustomerRow, String> colPhone;
    @FXML private TableColumn<CustomerRow, Number> colProperties;

    @FXML private Label detailPlaceholder;
    @FXML private Label detailName;
    @FXML private Label detailEmail;
    @FXML private Label detailPhone;
    @FXML private Label detailProperties;
    @FXML private Label detailNote;

    @FXML private Button editButton;
    @FXML private Button showPropertiesButton;

    private final ObservableList<CustomerRow> data = FXCollections.observableArrayList();
    private FilteredList<CustomerRow> filtered;

    @FXML
    public void initialize() {
        LOG.info("Customer management initialized");

        data.setAll(
                new CustomerRow("Acme Rentals s.r.o.", "info@acme-rentals.sk", "+421 902 111 222", 12, "Platinum klient, zmluva do 2027"),
                new CustomerRow("Jana Kováčová", "jana.kovacova@gmail.com", "+421 905 333 444", 2, "Dva apartmány v Starom Meste"),
                new CustomerRow("Bratislava Stays s.r.o.", "kontakt@bastays.sk", "+421 903 555 666", 28, "Najväčší zákazník, vyžaduje týždenné reporty"),
                new CustomerRow("Martin Novák", "m.novak@seznam.cz", "+421 907 777 888", 1, "Apartmán Panská 12"),
                new CustomerRow("Riverside Apartments", "hello@riverside.sk", "+421 904 999 000", 6, "Nábrežie Dunaja"),
                new CustomerRow("Eva Horáková", "eva.horakova@outlook.com", "+421 910 123 456", 3, "Individuálna klientka"),
                new CustomerRow("City Nest Bratislava", "booking@citynest.sk", "+421 908 234 567", 9, "Boutique apartmány"),
                new CustomerRow("Peter Svoboda", "p.svoboda@gmail.com", "+421 911 345 678", 1, "Nový zákazník od 2026-03")
        );

        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colProperties.setCellValueFactory(new PropertyValueFactory<>("propertyCount"));

        filtered = new FilteredList<>(data, r -> true);
        table.setItems(filtered);

        searchField.textProperty().addListener((obs, o, n) -> applyRegexFilter(n));
        table.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> showDetail(n));

        showDetail(null);
    }

    private void applyRegexFilter(String raw) {
        String input = raw == null ? "" : raw.trim();
        if (input.isEmpty()) {
            filtered.setPredicate(r -> true);
            setHint("Regex vyhladavanie v mene, emaili a telefone.", false);
            return;
        }
        try {
            Pattern p = Pattern.compile(input, Pattern.CASE_INSENSITIVE);
            filtered.setPredicate(r ->
                    p.matcher(r.getName()).find()
                    || p.matcher(r.getEmail()).find()
                    || p.matcher(r.getPhone()).find());
            setHint("Zodpoveda: " + filtered.size() + "  |  regex OK", false);
        } catch (PatternSyntaxException ex) {
            filtered.setPredicate(r -> false);
            setHint("Neplatny regex: " + ex.getDescription(), true);
            LOG.warning("Invalid regex: " + input);
        }
    }

    private void setHint(String text, boolean error) {
        searchHintLabel.setText(text);
        searchHintLabel.getStyleClass().removeAll("error-label", "page-subtitle");
        searchHintLabel.getStyleClass().add(error ? "error-label" : "page-subtitle");
    }

    private void showDetail(CustomerRow r) {
        boolean has = r != null;
        detailPlaceholder.setVisible(!has);
        detailPlaceholder.setManaged(!has);
        detailName.setVisible(has);      detailName.setManaged(has);
        detailEmail.setVisible(has);     detailEmail.setManaged(has);
        detailPhone.setVisible(has);     detailPhone.setManaged(has);
        detailProperties.setVisible(has);detailProperties.setManaged(has);
        detailNote.setVisible(has);      detailNote.setManaged(has);
        editButton.setDisable(!has);
        showPropertiesButton.setDisable(!has);

        if (!has) return;
        detailName.setText(r.getName());
        detailEmail.setText("✉  " + r.getEmail());
        detailPhone.setText("☎  " + r.getPhone());
        detailProperties.setText("🏠  " + r.getPropertyCount() + " apartmánov");
        detailNote.setText(r.getNote());
    }

    @FXML private void onAdd() { LOG.info("Add customer clicked"); }
    @FXML private void onEdit() {
        CustomerRow r = table.getSelectionModel().getSelectedItem();
        if (r != null) LOG.info("Edit customer: " + r.getName());
    }
    @FXML private void onShowProperties() {
        CustomerRow r = table.getSelectionModel().getSelectedItem();
        if (r != null) LOG.info("Show properties of: " + r.getName());
    }
}
