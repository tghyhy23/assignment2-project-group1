package com.group01.asm2.controllers;

import com.group01.asm2.models.Auction;
import com.group01.asm2.models.Item;
import com.group01.asm2.services.AuctionService;
import com.group01.asm2.services.ItemService;
import com.group01.asm2.services.NavigationService;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuctionsController {

    @FXML private TableView<Auction> auctionsTable;
    @FXML private TableColumn<Auction, String> auctionDateColumn;
    @FXML private TableColumn<Auction, String> auctionTypeColumn;
    @FXML private TableColumn<Auction, String> auctionItemColumn;
    @FXML private TableColumn<Auction, String> auctionAmountColumn;
    @FXML private TableColumn<Auction, String> auctionStatusColumn;

    private final ObservableList<Auction> auctionsList = FXCollections.observableArrayList();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @FXML
    public void initialize() {
        // 1. Gọi Service lấy Data thay vì tự tạo mock
        auctionsList.addAll(AuctionService.getAll());

        // 2. Setup giao diện bảng
        setupAuctionTable();
        makeTableResponsive(auctionsTable);

        // 3. Lắng nghe sự kiện click
        auctionsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1 && auctionsTable.getSelectionModel().getSelectedItem() != null) {
                Auction selectedAuction = auctionsTable.getSelectionModel().getSelectedItem();

                // GỌI HÀM TỪ NAVIGATION SERVICE
                Pane contentArea = (Pane) auctionsTable.getScene().lookup("#contentArea");
                NavigationService.goToAuctionDetails(contentArea, selectedAuction);
            }
        });
    }

    private void setupAuctionTable() {
        auctionDateColumn.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getEndDateTime();
            return new SimpleStringProperty(date != null ? date.format(formatter) : "N/A");
        });

        auctionTypeColumn.setCellValueFactory(cellData -> {
            boolean hasWinner = cellData.getValue().hasWinner();
            return new SimpleStringProperty(hasWinner ? "Purchase" : "Sale");
        });

        // ĐIỂM SÁNG: Dùng ItemService để tra cứu tên sản phẩm theo Item ID
        auctionItemColumn.setCellValueFactory(cellData -> {
            Integer itemId = cellData.getValue().getItemId();
            Item item = ItemService.getItemById(itemId);
            String itemName = (item != null) ? item.getTitle() : "Unknown Item";
            return new SimpleStringProperty(itemName);
        });

        auctionAmountColumn.setCellValueFactory(cellData -> {
            BigDecimal price = cellData.getValue().getFinalSalePrice();
            return new SimpleStringProperty(price != null ? "$" + price.toString() : "N/A");
        });

        auctionStatusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus().name())
        );

        auctionsTable.setItems(auctionsList);
    }

    private void makeTableResponsive(TableView<?> table) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No history found."));
        table.setFixedCellSize(48);
        table.prefHeightProperty().bind(Bindings.size(table.getItems()).multiply(table.getFixedCellSize()).add(52));
        table.setMinHeight(Region.USE_PREF_SIZE);
        table.setMaxHeight(Region.USE_PREF_SIZE);
    }
}