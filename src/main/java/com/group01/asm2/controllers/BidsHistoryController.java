package com.group01.asm2.controllers;

import com.group01.asm2.models.Bid;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Region;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BidsHistoryController {

    @FXML private TableView<Bid> bidsTable;
    @FXML private TableColumn<Bid, String> bidItemNameColumn;
    @FXML private TableColumn<Bid, String> bidMyAmountColumn;
    @FXML private TableColumn<Bid, String> bidHighestAmountColumn;
    @FXML private TableColumn<Bid, String> bidStatusColumn;
    @FXML private TableColumn<Bid, String> bidDateColumn;

    private final ObservableList<Bid> bidsList = FXCollections.observableArrayList();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    @FXML
    public void initialize() {
        loadMockData();
        setupBidsTable();
        makeTableResponsive(bidsTable);
    }

    private void loadMockData() {
        bidsList.addAll(
                new Bid(1, 101, 1, 99, new BigDecimal("300.00"), LocalDateTime.now().minusDays(1)),
                new Bid(2, 102, 2, 99, new BigDecimal("260.00"), LocalDateTime.now().minusDays(2)),
                new Bid(3, 103, 3, 99, new BigDecimal("190.00"), LocalDateTime.now().minusDays(5)),
                new Bid(4, 104, 4, 99, new BigDecimal("180.00"), LocalDateTime.now().minusDays(10))
        );
    }

    private void setupBidsTable() {
        // Cột Item Name: Giả lập lấy tên sản phẩm dựa vào Item ID
        bidItemNameColumn.setCellValueFactory(cellData -> {
            Integer itemId = cellData.getValue().getItemId();
            String itemName = "Unknown Item";

            if (itemId == 1) itemName = "Vintage Camera";
            else if (itemId == 2) itemName = "Gaming Monitor";
            else if (itemId == 3) itemName = "Rare Sneaker Pair";
            else if (itemId == 4) itemName = "Smart Watch";

            return new SimpleStringProperty(itemName);
        });

        // Cột My Bid Amount: Lấy giá trị lượng tiền đã bid
        bidMyAmountColumn.setCellValueFactory(cellData -> {
            BigDecimal amount = cellData.getValue().getAmount();
            return new SimpleStringProperty(amount != null ? "$" + amount.toString() : "N/A");
        });

        // Cột Highest Amount: Giả lập logic kiểm tra giá cao nhất hiện tại của Auction
        bidHighestAmountColumn.setCellValueFactory(cellData -> {
            Integer bidId = cellData.getValue().getId();
            BigDecimal myAmount = cellData.getValue().getAmount();
            BigDecimal highest = myAmount;

            // Giả lập: Nếu ID bid là lẻ thì hiện tại đã có người khác trả cao hơn $20
            if (bidId % 2 != 0) {
                highest = myAmount.add(new BigDecimal("20.00"));
            }
            return new SimpleStringProperty("$" + highest.toString());
        });

        // Cột Status: Giả lập trạng thái tương ứng với cái Highest Amount ở trên
        bidStatusColumn.setCellValueFactory(cellData -> {
            Integer bidId = cellData.getValue().getId();

            if (bidId == 1) return new SimpleStringProperty("Outbid");
            if (bidId == 2) return new SimpleStringProperty("Winning");
            if (bidId == 3) return new SimpleStringProperty("Lost");
            return new SimpleStringProperty("Won");
        });

        // Cột Date: Format LocalDateTime ra chuỗi hiển thị
        bidDateColumn.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getBidDateTime();
            return new SimpleStringProperty(date != null ? date.format(formatter) : "N/A");
        });

        bidsTable.setItems(bidsList);
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
}