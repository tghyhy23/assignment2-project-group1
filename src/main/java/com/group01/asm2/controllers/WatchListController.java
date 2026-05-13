package com.group01.asm2.controllers;

/**
 * @author Group 01
 */

import com.group01.asm2.dtos.AuctionCardDto;
import com.group01.asm2.models.Auction;
import com.group01.asm2.services.NavigationService;
import com.group01.asm2.services.WatchlistService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class WatchListController extends BaseController {
    private final WatchlistService watchlistService = new WatchlistService();

    @FXML
    private VBox watchListContainer;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private final List<AuctionCardDto> watchList = new ArrayList<>();

    @FXML
    public void initialize() {
        loadWatchList();
    }

    private void loadWatchList() {
        watchList.clear();
        watchList.addAll(watchlistService.readMyWatchlist());
        renderWatchList();
    }

    private void renderWatchList() {
        watchListContainer.getChildren().clear();
        watchListContainer.getChildren().add(createHeaderRow());

        if (watchList.isEmpty()) {
            watchListContainer.getChildren().add(createEmptyRow());
            return;
        }

        for (AuctionCardDto auctionCard : watchList) {
            watchListContainer.getChildren().add(createAuctionRow(auctionCard));
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

    private HBox createAuctionRow(AuctionCardDto auctionCard) {
        HBox row = new HBox();
        row.getStyleClass().add("watchlist-row");
        row.setAlignment(Pos.CENTER_LEFT);

        Button removeButton = createRemoveButton(auctionCard);
        HBox auctionBox = createAuctionInfoBox(auctionCard);
        Label price = createPriceLabel(auctionCard);
        Label date = createDateLabel(auctionCard.getEndDateTime());
        Label status = createStatusLabel(auctionCard);
        Button bidButton = createBidButton(auctionCard);

        row.getChildren().addAll(
            removeButton,
            auctionBox,
            price,
            date,
            status,
            bidButton
        );

        row.setOnMouseClicked(event -> goToAuctionDetails(auctionCard));

        return row;
    }

    private Button createRemoveButton(AuctionCardDto auctionCard) {
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

        bindAction(removeButton, event -> {
            event.consume();

            watchlistService.unwatchAuction(auctionCard.getAuctionId());
            loadWatchList();
        });

        return removeButton;
    }

    private HBox createAuctionInfoBox(AuctionCardDto auctionCard) {
        StackPane imageBox = createImageBox(auctionCard);

        Label productName = new Label(
            auctionCard.getItemTitle() != null
                ? auctionCard.getItemTitle()
                : "Unknown Item"
        );
        productName.getStyleClass().add("product-name");
        productName.setWrapText(true);

        Label productInfo = new Label(
            "Auction ID: " + auctionCard.getAuctionId()
                + " • " + safeText(auctionCard.getCategoryName())
                + " • " + safeText(auctionCard.getItemCondition())
        );
        productInfo.getStyleClass().add("product-info");

        Label sellerInfo = new Label("Seller: " + safeText(auctionCard.getSellerUsername()));
        sellerInfo.getStyleClass().add("product-info");

        VBox textBox = new VBox(4);
        textBox.setAlignment(Pos.CENTER_LEFT);
        textBox.getChildren().addAll(productName, productInfo, sellerInfo);

        HBox auctionBox = new HBox(12);
        auctionBox.setAlignment(Pos.CENTER_LEFT);
        auctionBox.setPrefWidth(420);
        auctionBox.getChildren().addAll(imageBox, textBox);

        return auctionBox;
    }

    private StackPane createImageBox(AuctionCardDto auctionCard) {
        StackPane imageBox = new StackPane();
        imageBox.getStyleClass().add("watchlist-image-placeholder");
        imageBox.setPrefSize(70, 70);
        imageBox.setMaxSize(70, 70);
        imageBox.setMinSize(70, 70);

        if (auctionCard.hasPrimaryImage()) {
            try {
                Image image = new Image(
                    auctionCard.getPrimaryImageUrl(),
                    70,
                    70,
                    true,
                    true,
                    true
                );

                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(70);
                imageView.setFitHeight(70);
                imageView.setPreserveRatio(true);

                imageBox.getChildren().add(imageView);
                return imageBox;

            } catch (Exception ignored) {
                // Fallback below.
            }
        }

        Label placeholder = new Label("IMAGE");
        placeholder.getStyleClass().add("product-info");
        imageBox.getChildren().add(placeholder);

        return imageBox;
    }

    private Label createPriceLabel(AuctionCardDto auctionCard) {
        Label price = new Label(formatMoney(auctionCard.getCurrentBidAmount()));
        price.getStyleClass().add("price-cell");
        price.setPrefWidth(210);

        return price;
    }

    private Label createDateLabel(LocalDateTime endDateTime) {
        Label date = new Label(
            endDateTime != null
                ? endDateTime.format(formatter)
                : "N/A"
        );

        date.getStyleClass().add("normal-cell");
        date.setPrefWidth(200);

        return date;
    }

    private Label createStatusLabel(AuctionCardDto auctionCard) {
        Label status = new Label(
            auctionCard.getStatus() != null
                ? auctionCard.getStatus().name()
                : "UNKNOWN"
        );

        status.getStyleClass().add("status-cell");
        status.setPrefWidth(170);

        if (auctionCard.getStatus() == null) {
            status.getStyleClass().add("status-ended");
            return status;
        }

        switch (auctionCard.getStatus()) {
            case ACTIVE -> status.getStyleClass().add("status-active");
            case SOLD -> status.getStyleClass().add("status-sold");
            case ENDED, UNSOLD, CANCELLED -> status.getStyleClass().add("status-ended");
            default -> status.getStyleClass().add("status-ended");
        }

        return status;
    }

    private Button createBidButton(AuctionCardDto auctionCard) {
        Button bidButton = new Button(
            auctionCard.isActive()
                ? "Place Your Bid"
                : "View Auction"
        );

        bidButton.getStyleClass().add("bid-button");
        bidButton.setPrefWidth(170);

        bindAction(bidButton, event -> {
            event.consume();
            goToAuctionDetails(auctionCard);
        });

        return bidButton;
    }

    private HBox createEmptyRow() {
        HBox row = new HBox();
        row.getStyleClass().add("watchlist-row");
        row.setAlignment(Pos.CENTER);
        row.setPrefHeight(100);

        Label emptyLabel = new Label("Your watchlist is empty.");
        emptyLabel.getStyleClass().add("normal-cell");

        row.getChildren().add(emptyLabel);

        return row;
    }

    private void goToAuctionDetails(AuctionCardDto auctionCard) {
        Auction auction = auctionCard.getAuction();

        if (auction == null) {
            return;
        }

        Pane contentArea = (Pane) watchListContainer.getScene().lookup("#contentArea");
        NavigationService.goToAuctionDetails(contentArea, auction);
    }

    private Label createHeaderCell(String text, double width) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        label.getStyleClass().add("header-cell");
        return label;
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) {
            return "N/A";
        }

        return "$" + amount.toPlainString();
    }

    private String safeText(String value) {
        return value != null && !value.isBlank() ? value : "N/A";
    }
}