package com.cleanmate.presentation.apartment;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.util.logging.Logger;

public class ApartmentManagementController extends com.cleanmate.presentation.nav.BaseNavController {

    private static final Logger LOG = Logger.getLogger(ApartmentManagementController.class.getName());

    /** Set before navigating here to pre-filter by customer name. */
    public static String filterCustomer = null;

    @FXML private TextField searchField;
    @FXML private Label countLabel;

    @FXML private TableView<ApartmentItem> table;
    @FXML private TableColumn<ApartmentItem, String> colAddress;
    @FXML private TableColumn<ApartmentItem, String> colCustomer;
    @FXML private TableColumn<ApartmentItem, Integer> colRooms;
    @FXML private TableColumn<ApartmentItem, Double> colArea;
    @FXML private TableColumn<ApartmentItem, Integer> colSteps;

    @FXML private Label detailPlaceholder;
    @FXML private VBox detailContent;
    @FXML private Label detailAddress;
    @FXML private Label detailCustomer;
    @FXML private Label detailRooms;
    @FXML private Label detailNote;

    @FXML private ListView<String> stepsView;
    @FXML private TextField newStepField;
    @FXML private Button editApartmentButton;

    private final ObservableList<ApartmentItem> data = FXCollections.observableArrayList();
    private FilteredList<ApartmentItem> filtered;

    @FXML
    public void initialize() {
        LOG.info("Apartment management initialized");

        data.setAll(
                new ApartmentItem("Panská 12, BA",           "Acme Rentals s.r.o.",     2, 65.0,  "Centrum, wifi, parkovanie", "Vysávanie", "Výmena bielizne", "Kúpeľňa", "Kuchyňa", "Kontrola"),
                new ApartmentItem("Hviezdoslavovo nám. 4",   "Acme Rentals s.r.o.",     1, 48.0,  "Historické centrum",        "Vysávanie", "Výmena bielizne", "Kúpeľňa"),
                new ApartmentItem("Obchodná 27",              "Jana Kováčová",            3, 82.0,  "3-izbový byt",              "Vysávanie", "Kuchyňa", "Kúpeľňa", "Spálne", "Vstupná hala"),
                new ApartmentItem("Panenská 8",               "Jana Kováčová",            2, 60.0,  "",                          "Vysávanie", "Výmena bielizne", "Kúpeľňa"),
                new ApartmentItem("Laurinská 3",              "Bratislava Stays s.r.o.", 1, 42.0,  "Ateliér",                   "Vysávanie", "Kúpeľňa"),
                new ApartmentItem("Grösslingova 45",          "Bratislava Stays s.r.o.", 2, 58.0,  "",                          "Vysávanie", "Výmena bielizne", "Kúpeľňa", "Balkón"),
                new ApartmentItem("Ventúrska 7",              "Bratislava Stays s.r.o.", 3, 95.0,  "Luxusný apartmán",          "Vysávanie", "Výmena bielizne", "Kúpeľňa", "Kuchyňa", "Balkón", "Kontrola minibar"),
                new ApartmentItem("Michalská 22",             "Riverside Apartments",    1, 38.0,  "",                          "Vysávanie", "Kúpeľňa"),
                new ApartmentItem("Sedlárska 5",              "Riverside Apartments",    2, 55.0,  "Výhľad na Michalskú vežu",  "Vysávanie", "Výmena bielizne", "Kúpeľňa"),
                new ApartmentItem("Kapitulská 18",            "Martin Novák",            2, 70.0,  "Blízko hradu",              "Vysávanie", "Výmena bielizne", "Kúpeľňa", "Kuchyňa")
        );

        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customer"));
        colRooms.setCellValueFactory(new PropertyValueFactory<>("rooms"));
        colArea.setCellValueFactory(new PropertyValueFactory<>("area"));
        colSteps.setCellValueFactory(new PropertyValueFactory<>("stepCount"));

        filtered = new FilteredList<>(data, r -> true);
        table.setItems(filtered);

        searchField.textProperty().addListener((obs, o, n) -> applyFilter());
        table.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> showDetail(n));

        // Apply pre-filter from customer navigation
        if (filterCustomer != null) {
            searchField.setText(filterCustomer);
            filterCustomer = null;
        }

        applyFilter();
        showDetail(null);
    }

    private void applyFilter() {
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        filtered.setPredicate(r -> {
            if (q.isEmpty()) return true;
            return r.getAddress().toLowerCase().contains(q)
                || r.getCustomer().toLowerCase().contains(q);
        });
        countLabel.setText("Apartmánov: " + filtered.size());
    }

    private void showDetail(ApartmentItem item) {
        boolean has = item != null;
        detailPlaceholder.setVisible(!has);
        detailPlaceholder.setManaged(!has);
        detailContent.setVisible(has);
        detailContent.setManaged(has);
        editApartmentButton.setDisable(!has);

        if (!has) return;

        detailAddress.setText(item.getAddress());
        detailCustomer.setText("Zákazník: " + item.getCustomer());
        detailRooms.setText("Izby: " + item.getRooms() + "  |  Plocha: " + item.getArea() + " m²");
        detailNote.setText(item.getNote().isEmpty() ? "" : "Poznámka: " + item.getNote());
        stepsView.setItems(item.getSteps());
    }

    @FXML
    private void onAddStep() {
        ApartmentItem selected = table.getSelectionModel().getSelectedItem();
        String step = newStepField.getText() == null ? "" : newStepField.getText().trim();
        if (selected != null && !step.isEmpty()) {
            selected.getSteps().add(step);
            newStepField.clear();
            table.refresh();
            LOG.info("Added step '" + step + "' to " + selected.getAddress());
        }
    }

    @FXML
    private void onRemoveStep() {
        ApartmentItem selected = table.getSelectionModel().getSelectedItem();
        String step = stepsView.getSelectionModel().getSelectedItem();
        if (selected != null && step != null) {
            selected.getSteps().remove(step);
            table.refresh();
            LOG.info("Removed step '" + step + "' from " + selected.getAddress());
        }
    }

    @FXML
    private void onAdd() { LOG.info("Add apartment clicked"); }

    @FXML
    private void onEditApartment() {
        ApartmentItem selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) LOG.info("Edit apartment: " + selected.getAddress());
    }
}
