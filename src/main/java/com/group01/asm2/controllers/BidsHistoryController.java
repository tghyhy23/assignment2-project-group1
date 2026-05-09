package com.group01.asm2.controllers;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Region;

public class BidsHistoryController {

    @FXML private TableView<Bid> bidsTable;
    @FXML private TableColumn<Bid, String> bidItemNameColumn;
    @FXML private TableColumn<Bid, String> bidMyAmountColumn;
    @FXML private TableColumn<Bid, String> bidHighestAmountColumn;
    @FXML private TableColumn<Bid, String> bidStatusColumn;
    @FXML private TableColumn<Bid, String> bidDateColumn;

    private final ObservableList<Bid> bids = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadMockData();
        setupBidsTable();
        makeTableResponsive(bidsTable);
    }

    private void loadMockData() {
        bids.addAll(
                new Bid("Vintage Camera", "$300.00", "$310.00", "Outbid", "06 May 2026"),
                new Bid("Gaming Monitor", "$260.00", "$260.00", "Winning", "06 May 2026"),
                new Bid("Rare Sneaker Pair", "$190.00", "$220.00", "Lost", "01 May 2026"),
                new Bid("Smart Watch", "$180.00", "$180.00", "Won", "29 Apr 2026")
        );
    }

    private void setupBidsTable() {
        bidItemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        bidMyAmountColumn.setCellValueFactory(new PropertyValueFactory<>("myBidAmount"));
        bidHighestAmountColumn.setCellValueFactory(new PropertyValueFactory<>("currentHighestBid"));
        bidStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        bidDateColumn.setCellValueFactory(new PropertyValueFactory<>("bidDate"));

        bidsTable.setItems(bids);
    }

    private void makeTableResponsive(TableView<?> table) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No bids found."));
        table.setFixedCellSize(48);

        table.prefHeightProperty().bind(
                Bindings.size(table.getItems())
                        .multiply(table.getFixedCellSize())
                        .add(52)
        );

        table.setMinHeight(Region.USE_PREF_SIZE);
        table.setMaxHeight(Region.USE_PREF_SIZE);
    }

    // Model Class
    public static class Bid {
        private final SimpleStringProperty itemName;
        private final SimpleStringProperty myBidAmount;
        private final SimpleStringProperty currentHighestBid;
        private final SimpleStringProperty status;
        private final SimpleStringProperty bidDate;

        public Bid(String itemName, String myBidAmount, String currentHighestBid, String status, String bidDate) {
            this.itemName = new SimpleStringProperty(itemName);
            this.myBidAmount = new SimpleStringProperty(myBidAmount);
            this.currentHighestBid = new SimpleStringProperty(currentHighestBid);
            this.status = new SimpleStringProperty(status);
            this.bidDate = new SimpleStringProperty(bidDate);
        }

        public String getItemName() { return itemName.get(); }
        public String getMyBidAmount() { return myBidAmount.get(); }
        public String getCurrentHighestBid() { return currentHighestBid.get(); }
        public String getStatus() { return status.get(); }
        public String getBidDate() { return bidDate.get(); }
    }
}