package com.cleanmate.presentation.customer;

import com.cleanmate.model.Customer;
import com.cleanmate.presentation.nav.LanguageManager;
import com.cleanmate.presentation.util.EmptyState;
import com.cleanmate.service.ServiceLocator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class CustomerManagementController extends com.cleanmate.presentation.nav.BaseNavController {

    private static final Logger LOG = Logger.getLogger(CustomerManagementController.class.getName());

    private final ObservableList<CustomerRow> DATA = FXCollections.observableArrayList();

    private void loadFromService() {
        DATA.clear();
        for (Customer c : ServiceLocator.customers().getAll()) {
            int aptCount = (int) ServiceLocator.apartments().getAll().stream()
                    .filter(a -> a.getCustomerId().equals(c.getId())).count();
            DATA.add(new CustomerRow(c.getName(), c.getEmail(), c.getPhone(), aptCount, c.getNotes()));
        }
    }

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

    private FilteredList<CustomerRow> filtered;

    @FXML
    public void initialize() {
        LOG.info("Customer management initialized");

        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colProperties.setCellValueFactory(new PropertyValueFactory<>("propertyCount"));

        loadFromService();
        filtered = new FilteredList<>(DATA, r -> true);
        table.setItems(filtered);
        table.setPlaceholder(EmptyState.build("👥", "empty.customers"));

        // Row click → navigate to EditCustomerView
        table.setRowFactory(tv -> {
            TableRow<CustomerRow> row = new TableRow<>();
            row.setOnMouseClicked(ev -> {
                if (!row.isEmpty() && ev.getClickCount() == 2) {
                    EditCustomerController.editTarget = row.getItem();
                    navEditCustomer();
                }
            });
            return row;
        });

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
        detailProperties.setText("🏠  " + r.getPropertyCount() + LanguageManager.getBundle().getString("customers.properties.suffix"));
        detailNote.setText(r.getNote());
    }

    @FXML private void onAdd() {
        EditCustomerController.editTarget = null;
        navEditCustomer();
    }
    @FXML private void onEdit() {
        CustomerRow r = table.getSelectionModel().getSelectedItem();
        if (r != null) {
            EditCustomerController.editTarget = r;
            navEditCustomer();
        }
    }
    @FXML private void onShowProperties() {
        CustomerRow r = table.getSelectionModel().getSelectedItem();
        if (r == null) return;
        com.cleanmate.presentation.apartment.ApartmentManagementController.filterCustomer = r.getName();
        navApartments();
    }
}
