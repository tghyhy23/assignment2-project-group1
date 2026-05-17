package com.group01.asm2.controllers;

import com.group01.asm2.models.Item;
import com.group01.asm2.enums.ItemStatus;
import com.group01.asm2.utils.ScrollUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

public class SystemItemsController {
    @FXML private ScrollPane mainScrollPane;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private TableView<Item> itemsTable;
    @FXML private TableColumn<Item, Void> actionCol;
    @FXML private StackPane modalOverlay;
    @FXML private Label modalTitle;
    @FXML private ComboBox<String> statusInput;

    private ObservableList<Item> itemList = FXCollections.observableArrayList();
    private Item editingItem = null;

    @FXML
    public void initialize() {
        if (mainScrollPane != null) ScrollUtils.makeSmooth(mainScrollPane);

        statusFilterCombo.setItems(FXCollections.observableArrayList("All", "ACTIVE", "INACTIVE"));
        statusFilterCombo.setValue("All");
        statusInput.setItems(FXCollections.observableArrayList("ACTIVE", "INACTIVE"));

        // Ghi đè lấy title thay vì name, categoryId thay vì category
        ((TableColumn<Item, String>) itemsTable.getColumns().get(1)).setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        ((TableColumn<Item, String>) itemsTable.getColumns().get(2)).setCellValueFactory(data -> new SimpleStringProperty("Cat #" + data.getValue().getCategoryId()));
        ((TableColumn<Item, String>) itemsTable.getColumns().get(3)).setCellValueFactory(data -> new SimpleStringProperty("Seller #" + data.getValue().getSellerId()));
        ((TableColumn<Item, String>) itemsTable.getColumns().get(4)).setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().name()));

        setupActionColumn();
        setupFilters();
    }

    private void setupFilters() {
        FilteredList<Item> filteredData = new FilteredList<>(itemList, b -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter(filteredData));
        statusFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter(filteredData));
        SortedList<Item> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(itemsTable.comparatorProperty());
        itemsTable.setItems(sortedData);
    }

    private void applyFilter(FilteredList<Item> filteredData) {
        String search = searchField.getText().toLowerCase();
        String status = statusFilterCombo.getValue();
        filteredData.setPredicate(item -> {
            boolean matchesSearch = search.isEmpty() || item.getTitle().toLowerCase().contains(search);
            boolean matchesStatus = status.equals("All") || item.getStatus().name().equals(status);
            return matchesSearch && matchesStatus;
        });
    }

    private void setupActionColumn() {
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Moderate");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(8, editBtn, deleteBtn);
            {
                pane.setAlignment(Pos.CENTER);
                editBtn.getStyleClass().add("action-btn");
                deleteBtn.getStyleClass().addAll("action-btn", "btn-delete");
                editBtn.setOnAction(e -> openModerateModal(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> itemList.remove(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void openModerateModal(Item item) {
        editingItem = item; modalTitle.setText("Moderate Item #" + item.getId());
        statusInput.setValue(item.getStatus().name()); modalOverlay.setVisible(true);
    }

    @FXML private void closeModal() { modalOverlay.setVisible(false); }

    @FXML private void saveItem() {
        if (editingItem != null) {
            editingItem.setStatus(ItemStatus.valueOf(statusInput.getValue()));
            itemsTable.refresh();
        }
        closeModal();
    }
}