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
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class AuctionsController {

    @FXML private TableView<Auction> auctionsTable;
    @FXML private TableColumn<Auction, String> auctionDateColumn;
    @FXML private TableColumn<Auction, String> auctionTypeColumn;
    @FXML private FlowPane allAuctionsContainer;
    @FXML private TableColumn<Auction, String> auctionItemColumn;
    @FXML private TableColumn<Auction, String> auctionAmountColumn;
    @FXML private TableColumn<Auction, String> auctionStatusColumn;

    private final ObservableList<Auction> auctionsList = FXCollections.observableArrayList();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @FXML
    public void initialize() {

        auctionsList.addAll(AuctionService.getAll());

        // PAGE TABLE AUCTIONS
        if (auctionsTable != null) {

            setupAuctionTable();
            makeTableResponsive(auctionsTable);
        // Handle hover imgs
            auctionsTable.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1
                        && auctionsTable.getSelectionModel().getSelectedItem() != null) {

                    Auction selectedAuction =
                            auctionsTable.getSelectionModel().getSelectedItem();

                    Pane contentArea =
                            (Pane) auctionsTable.getScene().lookup("#contentArea");

                    NavigationService.goToAuctionDetails(
                            contentArea,
                            selectedAuction
                    );
                }
            });
        }

        // EXPLORE PAGE
        if (allAuctionsContainer != null) {
            loadAllAuctionsToExplore();
        }
    }

    private void loadAllAuctionsToExplore() {
        allAuctionsContainer.getChildren().clear();

        for (Auction auction : AuctionService.getAll()) {
            VBox card = createAuctionCard(auction);
            allAuctionsContainer.getChildren().add(card);
        }
    }

    //    Create AuctionCard
    private VBox createAuctionCard(Auction auction) {

        Item item = ItemService.getItemById(auction.getItemId());

        String itemName = item != null
                ? item.getTitle()
                : "Unknown Item";

        String price = auction.getFinalSalePrice() != null
                ? "$" + auction.getFinalSalePrice().toPlainString()
                : "N/A";

        String endDate = auction.getEndDateTime() != null
                ? auction.getEndDateTime().format(formatter)
                : "N/A";

        Label imageBox = new Label("IMAGE");
        imageBox.getStyleClass().add("auction-image-placeholder");
        imageBox.setMaxWidth(Double.MAX_VALUE);

        Label nameLabel = new Label(itemName);
        nameLabel.getStyleClass().add("auction-name");
        nameLabel.setWrapText(true);
        nameLabel.setMinHeight(38);
        nameLabel.setPrefHeight(38);
        nameLabel.setMaxHeight(38);

        Label currentBidLabel = new Label("Current bid: ");
        currentBidLabel.getStyleClass().add("auction-price-title");

        Label priceLabel = new Label(price);
        priceLabel.getStyleClass().add("auction-price");

        Label statusLabel = new Label(auction.getStatus().name());
        statusLabel.getStyleClass().add("auction-status");

        switch (auction.getStatus()) {
            case ACTIVE:
                statusLabel.getStyleClass().add("status-active");
                break;
            case SOLD:
                statusLabel.getStyleClass().add("status-sold");
                break;
            case ENDED:
                statusLabel.getStyleClass().add("status-ended");
                break;
            default:
                statusLabel.getStyleClass().add("status-ended");
                break;
        }

        Label dateLabel = new Label("End: " + endDate);
        dateLabel.getStyleClass().add("auction-date");

        VBox bidSection = new VBox(
                currentBidLabel,
                priceLabel
        );

        bidSection.setSpacing(2);

        VBox card = new VBox(
                imageBox,
                nameLabel,
                bidSection,
                statusLabel,
                dateLabel
        );

        card.setSpacing(8);
        card.setFillWidth(true);
        card.getStyleClass().add("auction-card");

        ScaleTransition imageScale = new ScaleTransition(Duration.seconds(0.18), imageBox);
        TranslateTransition cardMove = new TranslateTransition(Duration.seconds(0.18), card);
        TranslateTransition contentMove = new TranslateTransition(Duration.seconds(0.18), nameLabel);

        card.setOnMouseEntered(event -> {

            imageScale.setToX(1.04);
            imageScale.setToY(1.04);
            imageScale.playFromStart();

            cardMove.setToY(-5);
            cardMove.playFromStart();

        });

        card.setOnMouseExited(event -> {

            imageScale.setToX(1.0);
            imageScale.setToY(1.0);
            imageScale.playFromStart();

            cardMove.setToY(0);
            cardMove.playFromStart();

        });

        card.setOnMouseClicked(event -> {
            Pane contentArea = (Pane) card.getScene().lookup("#contentArea");
            NavigationService.goToAuctionDetails(contentArea, auction);
        });

        return card;
    }

    //    For Featured Auctions
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

    //    For Featured Auctions
    private void makeTableResponsive(TableView<?> table) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No history found."));
        table.setFixedCellSize(48);
        table.prefHeightProperty().bind(Bindings.size(table.getItems()).multiply(table.getFixedCellSize()).add(52));
        table.setMinHeight(Region.USE_PREF_SIZE);
        table.setMaxHeight(Region.USE_PREF_SIZE);
    }
}