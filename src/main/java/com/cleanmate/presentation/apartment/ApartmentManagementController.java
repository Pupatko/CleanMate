package com.cleanmate.presentation.apartment;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import com.cleanmate.model.Apartment;
import com.cleanmate.presentation.nav.LanguageManager;
import com.cleanmate.presentation.util.EmptyState;
import com.cleanmate.service.ServiceLocator;

import java.util.logging.Logger;

public class ApartmentManagementController extends com.cleanmate.presentation.nav.BaseNavController {

    private static final Logger LOG = Logger.getLogger(ApartmentManagementController.class.getName());

    /** Set before navigating here to pre-filter by customer name. */
    public static String filterCustomer = null;

    private final ObservableList<ApartmentItem> DATA = FXCollections.observableArrayList();

    private void loadFromService() {
        DATA.clear();
        for (Apartment a : ServiceLocator.apartments().getAll()) {
            ApartmentItem item = new ApartmentItem(a.getAddress(), a.getCustomerName(),
                    a.getRooms(), a.getArea(), a.getNote());
            for (String name : a.getTaskNames())
                item.getTasks().add(new ApartmentTask(name));
            DATA.add(item);
        }
    }

    @FXML private TextField searchField;
    @FXML private Label countLabel;

    @FXML private TableView<ApartmentItem> table;
    @FXML private TableColumn<ApartmentItem, String> colAddress;
    @FXML private TableColumn<ApartmentItem, String> colCustomer;
    @FXML private TableColumn<ApartmentItem, Integer> colRooms;
    @FXML private TableColumn<ApartmentItem, Double> colArea;
    @FXML private TableColumn<ApartmentItem, Integer> colSteps;

    private FilteredList<ApartmentItem> filtered;

    @FXML
    public void initialize() {
        LOG.info("Apartment management initialized");

        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customer"));
        colRooms.setCellValueFactory(new PropertyValueFactory<>("rooms"));
        colArea.setCellValueFactory(new PropertyValueFactory<>("area"));
        colSteps.setCellValueFactory(new PropertyValueFactory<>("stepCount"));

        loadFromService();
        filtered = new FilteredList<>(DATA, r -> true);
        table.setItems(filtered);
        table.setPlaceholder(EmptyState.build("🏢", "empty.apartments"));

        // Click row to open edit screen
        table.setRowFactory(tv -> {
            TableRow<ApartmentItem> row = new TableRow<>();
            row.setOnMouseClicked(ev -> {
                if (!row.isEmpty() && ev.getClickCount() == 2) {
                    EditApartmentController.editTarget = row.getItem();
                    navEditApartment();
                }
            });
            return row;
        });

        searchField.textProperty().addListener((obs, o, n) -> applyFilter());

        if (filterCustomer != null) {
            searchField.setText(filterCustomer);
            filterCustomer = null;
        }

        applyFilter();
    }

    private void applyFilter() {
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        filtered.setPredicate(r -> {
            if (q.isEmpty()) return true;
            return r.getAddress().toLowerCase().contains(q)
                || r.getCustomer().toLowerCase().contains(q);
        });
        countLabel.setText(LanguageManager.getBundle().getString("apartments.count.prefix") + " " + filtered.size());
    }

    @FXML
    private void onAdd() {
        EditApartmentController.editTarget = null;
        navEditApartment();
    }

}
