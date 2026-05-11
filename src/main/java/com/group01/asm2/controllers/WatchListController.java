package com.group01.asm2.controllers;

import com.group01.asm2.models.Auction;
import com.group01.asm2.models.Item;
import com.group01.asm2.services.AuctionService;
import com.group01.asm2.services.ItemService;
import com.group01.asm2.services.NavigationService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class WatchListController {

    @FXML
    private VBox watchListContainer;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private final List<Auction> watchList = new ArrayList<>();

    @FXML
    public void initialize() {
        watchList.addAll(AuctionService.getWatchList());
        renderWatchList();
    }

    private void renderWatchList() {
        watchListContainer.getChildren().clear();
        watchListContainer.getChildren().add(createHeaderRow());

        for (Auction auction : watchList) {
            watchListContainer.getChildren().add(createAuctionRow(auction));
        }
    }

    private HBox createHeaderRow() {
        HBox row = new HBox();
        row.getStyleClass().add("watchlist-header");
        row.setAlignment(Pos.CENTER_LEFT);

        Label remove = createHeaderCell("", 70);
        Label auction = createHeaderCell("Auction", 420);
        Label price = createHeaderCell("Current Bid", 210);
        Label date = createHeaderCell("End Date", 200);
        Label status = createHeaderCell("Status", 170);
        Label action = createHeaderCell("", 170);

        row.getChildren().addAll(remove, auction, price, date, status, action);

        return row;
    }

    private HBox createAuctionRow(Auction auction) {
        HBox row = new HBox();
        row.getStyleClass().add("watchlist-row");
        row.setAlignment(Pos.CENTER_LEFT);

        Button removeButton = createRemoveButton(auction);
        HBox auctionBox = createAuctionInfoBox(auction);
        Label price = createPriceLabel(auction);
        Label date = createDateLabel(auction);
        Label status = createStatusLabel(auction);
        Button bidButton = createBidButton(auction);

        row.getChildren().addAll(
                removeButton,
                auctionBox,
                price,
                date,
                status,
                bidButton
        );

        row.setOnMouseClicked(event -> {
            Pane contentArea = (Pane) watchListContainer.getScene().lookup("#contentArea");
            NavigationService.goToAuctionDetails(contentArea, auction);
        });

        return row;
    }

    private Button createRemoveButton(Auction auction) {
        SVGPath trashIcon = new SVGPath();
        trashIcon.setContent(
                "M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6 " +
                        "M3 6h18 " +
                        "M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"
        );
        trashIcon.getStyleClass().add("trash-icon");

        Button removeButton = new Button();
        removeButton.setGraphic(trashIcon);
        removeButton.getStyleClass().add("remove-button");
        removeButton.setPrefWidth(70);

        removeButton.setOnAction(event -> {
            event.consume();
            AuctionService.removeFromWatchList(auction.getId());
            watchList.remove(auction);
            renderWatchList();
        });

        return removeButton;
    }

    private HBox createAuctionInfoBox(Auction auction) {
        Item item = ItemService.getItemById(auction.getItemId());
        String itemName = item != null ? item.getTitle() : "Unknown Item";

        Label imageBox = new Label("IMAGE");
        imageBox.getStyleClass().add("watchlist-image-placeholder");

        Label productName = new Label(itemName);
        productName.getStyleClass().add("product-name");
        productName.setWrapText(true);

        Label productInfo = new Label("Auction ID: " + auction.getId());
        productInfo.getStyleClass().add("product-info");

        VBox textBox = new VBox(4);
        textBox.setAlignment(Pos.CENTER_LEFT);
        textBox.getChildren().addAll(productName, productInfo);

        HBox auctionBox = new HBox(12);
        auctionBox.setAlignment(Pos.CENTER_LEFT);
        auctionBox.setPrefWidth(420);
        auctionBox.getChildren().addAll(imageBox, textBox);

        return auctionBox;
    }

    private Label createPriceLabel(Auction auction) {
        Label price = new Label(
                auction.getFinalSalePrice() != null
                        ? "$" + auction.getFinalSalePrice().toPlainString()
                        : "N/A"
        );

        price.getStyleClass().add("price-cell");
        price.setPrefWidth(210);

        return price;
    }

    private Label createDateLabel(Auction auction) {
        Label date = new Label(
                auction.getEndDateTime() != null
                        ? auction.getEndDateTime().format(formatter)
                        : "N/A"
        );

        date.getStyleClass().add("normal-cell");
        date.setPrefWidth(200);

        return date;
    }

    private Label createStatusLabel(Auction auction) {
        Label status = new Label(auction.getStatus().name());
        status.getStyleClass().add("status-cell");
        status.setPrefWidth(170);

        switch (auction.getStatus()) {
            case ACTIVE:
                status.getStyleClass().add("status-active");
                break;
            case SOLD:
                status.getStyleClass().add("status-sold");
                break;
            case ENDED:
                status.getStyleClass().add("status-ended");
                break;
            default:
                status.getStyleClass().add("status-ended");
                break;
        }

        return status;
    }

    private Button createBidButton(Auction auction) {
        Button bidButton = new Button("Place Your Bid");
        bidButton.getStyleClass().add("bid-button");
        bidButton.setPrefWidth(170);

        bidButton.setOnAction(event -> {
            event.consume();

            Pane contentArea = (Pane) watchListContainer.getScene().lookup("#contentArea");
            NavigationService.goToAuctionDetails(contentArea, auction);
        });

        return bidButton;
    }

    private Label createHeaderCell(String text, double width) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        label.getStyleClass().add("header-cell");
        return label;
    }
}