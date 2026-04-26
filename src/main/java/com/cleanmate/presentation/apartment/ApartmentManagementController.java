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

import com.cleanmate.presentation.nav.LanguageManager;

import java.util.logging.Logger;

public class ApartmentManagementController extends com.cleanmate.presentation.nav.BaseNavController {

    private static final Logger LOG = Logger.getLogger(ApartmentManagementController.class.getName());

    /** Set before navigating here to pre-filter by customer name. */
    public static String filterCustomer = null;

    private static final ObservableList<ApartmentItem> DATA = FXCollections.observableArrayList();
    static { DATA.addAll(sampleData()); }

    public static void addApartment(ApartmentItem a)    { DATA.add(a); }
    public static void removeApartment(ApartmentItem a) { DATA.remove(a); }
    public static ObservableList<ApartmentItem> data()  { return DATA; }

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

        filtered = new FilteredList<>(DATA, r -> true);
        table.setItems(filtered);

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

    private static ObservableList<ApartmentItem> sampleData() {
        ObservableList<ApartmentItem> list = FXCollections.observableArrayList();
        list.add(new ApartmentItem("Panská 12, BA",         "Acme Rentals s.r.o.",     2, 65.0, "Centrum, wifi, parkovanie",
                new ApartmentTask("Vysávanie",        "Vysávanie celého bytu",  ApartmentTask.Type.BASIC),
                new ApartmentTask("Výmena bielizne",  "Kompletná výmena",       ApartmentTask.Type.BASIC),
                new ApartmentTask("Kúpeľňa",          "Dezinfekcia + foto",     ApartmentTask.Type.PHOTO),
                new ApartmentTask("Kuchyňa",          "Umyť riad a povrchy",    ApartmentTask.Type.BASIC),
                new ApartmentTask("Kontrola",         "Finálna kontrola stavu", ApartmentTask.Type.CHECKLIST)));
        list.add(new ApartmentItem("Hviezdoslavovo nám. 4", "Acme Rentals s.r.o.",     1, 48.0, "Historické centrum",
                new ApartmentTask("Vysávanie", "", ApartmentTask.Type.BASIC),
                new ApartmentTask("Kúpeľňa",    "", ApartmentTask.Type.PHOTO)));
        list.add(new ApartmentItem("Obchodná 27",            "Jana Kováčová",            3, 82.0, "3-izbový byt",
                new ApartmentTask("Vysávanie", "", ApartmentTask.Type.BASIC),
                new ApartmentTask("Spálne",    "", ApartmentTask.Type.BASIC),
                new ApartmentTask("Kuchyňa",   "", ApartmentTask.Type.BASIC)));
        list.add(new ApartmentItem("Panenská 8",             "Jana Kováčová",            2, 60.0, "",
                new ApartmentTask("Vysávanie", "", ApartmentTask.Type.BASIC)));
        list.add(new ApartmentItem("Laurinská 3",            "Bratislava Stays s.r.o.", 1, 42.0, "Ateliér"));
        list.add(new ApartmentItem("Grösslingova 45",        "Bratislava Stays s.r.o.", 2, 58.0, "",
                new ApartmentTask("Vysávanie",       "", ApartmentTask.Type.BASIC),
                new ApartmentTask("Balkón",          "", ApartmentTask.Type.CHECKLIST)));
        list.add(new ApartmentItem("Ventúrska 7",            "Bratislava Stays s.r.o.", 3, 95.0, "Luxusný apartmán",
                new ApartmentTask("Minibar",         "Kontrola a doplnenie",   ApartmentTask.Type.CHECKLIST),
                new ApartmentTask("Foto pre hostí",  "Foto každej izby",       ApartmentTask.Type.PHOTO)));
        list.add(new ApartmentItem("Michalská 22",           "Riverside Apartments",    1, 38.0, ""));
        list.add(new ApartmentItem("Sedlárska 5",            "Riverside Apartments",    2, 55.0, "Výhľad na Michalskú vežu"));
        list.add(new ApartmentItem("Kapitulská 18",          "Martin Novák",            2, 70.0, "Blízko hradu"));
        return list;
    }
}
