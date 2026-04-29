package com.cleanmate.presentation.properties;

import com.cleanmate.presentation.nav.BaseNavController;
import com.cleanmate.presentation.util.EmptyState;
import com.cleanmate.service.ServiceLocator;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.logging.Logger;

public class MyPropertiesController extends BaseNavController {

    private static final Logger LOG = Logger.getLogger(MyPropertiesController.class.getName());

    /** Set before navigating here to filter by customer name. */
    public static String customerName = null;

    @FXML private Label countLabel;
    @FXML private ListView<Property> list;

    @FXML
    public void initialize() {
        LOG.info("My properties initialized");

        String cust = customerName;
        customerName = null;

        java.time.LocalDate now = java.time.LocalDate.now();
        ObservableList<Property> data = FXCollections.observableArrayList();
        ServiceLocator.apartments().getAll().stream()
                .filter(a -> cust == null || cust.equals(a.getCustomerName()))
                .forEach(a -> {
                    long monthly = ServiceLocator.cleanings().getAll().stream()
                            .filter(c -> c.property().equals(a.getAddress())
                                    && c.date().getMonth() == now.getMonth()
                                    && c.date().getYear()  == now.getYear())
                            .count();
                    String type = a.getRooms() <= 1 ? "Štúdio"
                                : a.getRooms() + "-izbový";
                    data.add(new Property(a.getAddress(), a.getCustomerName(), type, (int) monthly, "ACTIVE"));
                });

        countLabel.setText(data.size() + " apartmánov");
        list.setItems(data);
        list.setPlaceholder(EmptyState.build("🏠", "empty.properties"));
        list.setCellFactory(l -> new PropertyCell());
    }

    public static class Property {
        private final SimpleStringProperty name = new SimpleStringProperty();
        private final SimpleStringProperty district = new SimpleStringProperty();
        private final SimpleStringProperty type = new SimpleStringProperty();
        private final SimpleIntegerProperty monthlyCleanings = new SimpleIntegerProperty();
        private final SimpleStringProperty status = new SimpleStringProperty();

        public Property(String name, String district, String type, int monthlyCleanings, String status) {
            this.name.set(name); this.district.set(district); this.type.set(type);
            this.monthlyCleanings.set(monthlyCleanings); this.status.set(status);
        }
        public String getName() { return name.get(); }
        public String getDistrict() { return district.get(); }
        public String getType() { return type.get(); }
        public int getMonthlyCleanings() { return monthlyCleanings.get(); }
        public String getStatus() { return status.get(); }
    }

    private static final class PropertyCell extends ListCell<Property> {
        @Override
        protected void updateItem(Property p, boolean empty) {
            super.updateItem(p, empty);
            if (empty || p == null) { setGraphic(null); setText(null); return; }

            Label icon = new Label("🏠");
            icon.getStyleClass().add("prop-icon");

            Label name = new Label(p.getName());
            name.getStyleClass().add("detail-name");

            Label district = new Label(p.getDistrict() + "  •  " + p.getType());
            district.getStyleClass().add("task-customer");

            Label stats = new Label("📅 " + p.getMonthlyCleanings() + " upratovaní / mesiac");
            stats.getStyleClass().add("task-customer");

            VBox textBox = new VBox(4, name, district, stats);
            textBox.setAlignment(Pos.CENTER_LEFT);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label badge = new Label(p.getStatus());
            badge.getStyleClass().setAll("status-badge",
                    "ACTIVE".equals(p.getStatus()) ? "avail-available" : "avail-off_duty");

            HBox row = new HBox(16, icon, textBox, spacer, badge);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("task-row");

            setGraphic(row);
            setText(null);
        }
    }
}
