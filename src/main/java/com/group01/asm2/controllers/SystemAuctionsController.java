package com.group01.asm2.controllers;

import com.group01.asm2.models.Auction;
import com.group01.asm2.enums.AuctionStatus;
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

public class SystemAuctionsController {
    @FXML private ScrollPane mainScrollPane;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private TableView<Auction> auctionsTable;
    @FXML private TableColumn<Auction, Void> actionCol;
    @FXML private StackPane modalOverlay;
    @FXML private Label modalTitle;
    @FXML private ComboBox<String> statusInput;

    private ObservableList<Auction> auctionList = FXCollections.observableArrayList();
    private Auction editingAuction = null;

    @FXML
    public void initialize() {
        if (mainScrollPane != null) ScrollUtils.makeSmooth(mainScrollPane);

        statusFilterCombo.setItems(FXCollections.observableArrayList("All", "ACTIVE", "ENDED", "CANCELLED"));
        statusFilterCombo.setValue("All");
        statusInput.setItems(FXCollections.observableArrayList("ACTIVE", "ENDED", "CANCELLED"));

        // Map ItemId sang FXML itemName, Enum sang String
        ((TableColumn<Auction, String>) auctionsTable.getColumns().get(1)).setCellValueFactory(data -> new SimpleStringProperty("Item #" + data.getValue().getItemId()));
        ((TableColumn<Auction, String>) auctionsTable.getColumns().get(2)).setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFinalSalePrice() != null ? "$" + data.getValue().getFinalSalePrice() : "N/A"));
        ((TableColumn<Auction, String>) auctionsTable.getColumns().get(3)).setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEndDateTime() != null ? data.getValue().getEndDateTime().toString() : "N/A"));
        ((TableColumn<Auction, String>) auctionsTable.getColumns().get(4)).setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus() != null ? data.getValue().getStatus().name() : "N/A"));

        setupActionColumn();
        setupFilters();
    }

    private void setupFilters() {
        FilteredList<Auction> filteredData = new FilteredList<>(auctionList, b -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter(filteredData));
        statusFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter(filteredData));
        SortedList<Auction> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(auctionsTable.comparatorProperty());
        auctionsTable.setItems(sortedData);
    }

    private void applyFilter(FilteredList<Auction> filteredData) {
        String search = searchField.getText();
        String status = statusFilterCombo.getValue();
        filteredData.setPredicate(auc -> {
            boolean matchesSearch = search.isEmpty() || String.valueOf(auc.getItemId()).contains(search);
            boolean matchesStatus = status.equals("All") || auc.getStatus().name().equals(status);
            return matchesSearch && matchesStatus;
        });
    }

    private void setupActionColumn() {
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit Status");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(8, editBtn, deleteBtn);
            {
                pane.setAlignment(Pos.CENTER);
                editBtn.getStyleClass().add("action-btn");
                deleteBtn.getStyleClass().addAll("action-btn", "btn-delete");
                editBtn.setOnAction(e -> openEditModal(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> auctionList.remove(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void openEditModal(Auction auc) {
        editingAuction = auc; modalTitle.setText("Update Auction #" + auc.getId());
        statusInput.setValue(auc.getStatus().name()); modalOverlay.setVisible(true);
    }

    @FXML private void closeModal() { modalOverlay.setVisible(false); }

    @FXML private void saveStatus() {
        if (editingAuction != null) {
            editingAuction.setStatus(AuctionStatus.valueOf(statusInput.getValue()));
            auctionsTable.refresh();
        }
        closeModal();
    }
}