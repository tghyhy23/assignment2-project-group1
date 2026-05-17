package com.group01.asm2.controllers;

import com.group01.asm2.models.Bid;
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

public class SystemBidsController {
    @FXML private ScrollPane mainScrollPane;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private TableView<Bid> bidsTable;
    @FXML private TableColumn<Bid, Void> actionCol;
    @FXML private StackPane modalOverlay;
    @FXML private Label modalTitle;
    @FXML private ComboBox<String> statusInput;

    private ObservableList<Bid> bidList = FXCollections.observableArrayList();
    private Bid editingBid = null;

    @FXML
    public void initialize() {
        if (mainScrollPane != null) ScrollUtils.makeSmooth(mainScrollPane);

        statusFilterCombo.setItems(FXCollections.observableArrayList("All", "ACTIVE"));
        statusFilterCombo.setValue("All");
        statusInput.setItems(FXCollections.observableArrayList("ACTIVE"));

        // Ghi đè lấy dữ liệu từ Model Bid
        ((TableColumn<Bid, String>) bidsTable.getColumns().get(0)).setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        ((TableColumn<Bid, String>) bidsTable.getColumns().get(2)).setCellValueFactory(data -> new SimpleStringProperty("User #" + data.getValue().getBidderId()));
        ((TableColumn<Bid, String>) bidsTable.getColumns().get(4)).setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBidDateTime() != null ? data.getValue().getBidDateTime().toString() : ""));
        ((TableColumn<Bid, String>) bidsTable.getColumns().get(5)).setCellValueFactory(data -> new SimpleStringProperty("ACTIVE"));

        setupActionColumn();
        setupFilters();
    }

    private void setupFilters() {
        FilteredList<Bid> filteredData = new FilteredList<>(bidList, b -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter(filteredData));
        SortedList<Bid> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(bidsTable.comparatorProperty());
        bidsTable.setItems(sortedData);
    }

    private void applyFilter(FilteredList<Bid> filteredData) {
        String search = searchField.getText();
        filteredData.setPredicate(bid -> search.isEmpty() || String.valueOf(bid.getBidderId()).contains(search) || String.valueOf(bid.getAuctionId()).contains(search));
    }

    private void setupActionColumn() {
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(deleteBtn);
            {
                pane.setAlignment(Pos.CENTER);
                deleteBtn.getStyleClass().addAll("action-btn", "btn-delete");
                deleteBtn.setOnAction(e -> bidList.remove(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    @FXML private void closeModal() { modalOverlay.setVisible(false); }
    @FXML private void saveBid() { closeModal(); }
}