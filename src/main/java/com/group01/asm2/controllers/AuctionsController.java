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

public class AuctionsController {

    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, String> transactionDateColumn;
    @FXML private TableColumn<Transaction, String> transactionTypeColumn;
    @FXML private TableColumn<Transaction, String> transactionItemColumn;
    @FXML private TableColumn<Transaction, String> transactionAmountColumn;
    @FXML private TableColumn<Transaction, String> transactionStatusColumn;

    private final ObservableList<Transaction> transactions = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadMockData();
        setupTransactionsTable();
        makeTableResponsive(transactionsTable);
    }

    private void loadMockData() {
        transactions.addAll(
                new Transaction("06 May 2026", "Purchase", "Smart Watch", "$180.00", "Payment Processed"),
                new Transaction("04 May 2026", "Sale", "Bluetooth Speaker", "$95.00", "Completed"),
                new Transaction("01 May 2026", "Purchase", "Rare Sneaker Pair", "$220.00", "Lost Auction"),
                new Transaction("28 Apr 2026", "Sale", "Coffee Machine", "$160.00", "Completed")
        );
    }

    private void setupTransactionsTable() {
        transactionDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        transactionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        transactionItemColumn.setCellValueFactory(new PropertyValueFactory<>("item"));
        transactionAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        transactionStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        transactionsTable.setItems(transactions);
    }

    private void makeTableResponsive(TableView<?> table) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No transactions found."));
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
    public static class Transaction {
        private final SimpleStringProperty date;
        private final SimpleStringProperty type;
        private final SimpleStringProperty item;
        private final SimpleStringProperty amount;
        private final SimpleStringProperty status;

        public Transaction(String date, String type, String item, String amount, String status) {
            this.date = new SimpleStringProperty(date);
            this.type = new SimpleStringProperty(type);
            this.item = new SimpleStringProperty(item);
            this.amount = new SimpleStringProperty(amount);
            this.status = new SimpleStringProperty(status);
        }

        public String getDate() { return date.get(); }
        public String getType() { return type.get(); }
        public String getItem() { return item.get(); }
        public String getAmount() { return amount.get(); }
        public String getStatus() { return status.get(); }
    }
}