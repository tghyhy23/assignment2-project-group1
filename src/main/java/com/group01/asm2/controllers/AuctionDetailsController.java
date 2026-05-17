package com.group01.asm2.controllers;

/**
 * @author Group 01
 */

import com.group01.asm2.dtos.AuctionDetailDto;
import com.group01.asm2.enums.AuctionStatus;
import com.group01.asm2.models.*;
import com.group01.asm2.services.AuctionService;
import com.group01.asm2.services.NavigationService;
import com.group01.asm2.utils.ScrollUtils;
import com.group01.asm2.utils.UiMessageUtils;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AuctionDetailsController {
    @FXML private ScrollPane detailScrollPane;
    @FXML private ImageView mainItemImageView;
    @FXML private Label headerStatusBadge;
    @FXML private Label auctionTitleLabel;
    @FXML private Label currentBidLabel;
    @FXML private Label startingPriceLabel;
    @FXML private Label minimumNextBidLabel;
    @FXML private Label numberOfBidsLabel;
    @FXML private Label timeRemainingLabel;
    @FXML private Label startDateLabel;
    @FXML private Label endDateLabel;
    @FXML private Label auctionIdLabel;
    @FXML private Label itemIdLabel;
    @FXML private Label auctionWarningLabel;
    @FXML private Label sellerNameLabel;
    @FXML private TextField bidAmountField;
    @FXML private Button placeBidButton;
    @FXML private Label bidMessageLabel;
    @FXML private Button viewSellerButton;

    @FXML private GridPane auctionInfoGrid;
    @FXML private GridPane productDetailsGrid;
    @FXML private Label sellerSectionNameLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label bidHistoryCountLabel;

    @FXML private VBox bidHistoryList;
    @FXML private VBox relatedAuctionsList;

    private final AuctionService auctionService = new AuctionService();

    private Pane contentArea;
    private AuctionDetailDto currentDetail;
    private Auction currentAuction;
    private Item currentItem;
    private String sellerName;

    private BigDecimal startingPrice;
    private BigDecimal currentHighestBid;

    private final List<AuctionBidEntry> bidHistory = new ArrayList<>();
    private final DateTimeFormatter dateTimeFormatter =
        DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    @FXML
    public void initialize() {
        ScrollUtils.makeSmooth(detailScrollPane);
    }

    /**
     * Called by NavigationService when user opens the auction detail page.
     */
    public void loadAuctionDetails(Auction auction) {
        if (auction == null || auction.getId() == null) {
            showFatalPageError("Auction data is not available.");
            return;
        }

        loadAuctionDetailsById(auction.getId());
    }

    /**
     * Loads full auction detail from database through AuctionDetailDto.
     */
    public void loadAuctionDetailsById(Integer auctionId) {
        try {
            currentDetail = auctionService.readAuction(auctionId);

            if (currentDetail == null || currentDetail.getAuction() == null) {
                showFatalPageError("Auction data is not available.");
                return;
            }

            currentAuction = currentDetail.getAuction();
            currentItem = currentDetail.getItem();

            if (currentItem == null) {
                showFatalPageError("Item data is not available.");
                return;
            }

            sellerName = resolveSellerName(currentDetail);
            startingPrice = getStartingPrice(currentItem);
            currentHighestBid = currentDetail.getCurrentBidAmount();

            bidHistory.clear();
            bidHistory.addAll(mapRecentBidsToUiEntries(currentDetail));

            renderAuctionDetails();
        } catch (Exception exception) {
            showFatalPageError(exception.getMessage());
        }
    }

    private void renderAuctionDetails() {
        auctionTitleLabel.setText(valueOrNA(currentItem.getTitle()));

        sellerNameLabel.setText(valueOrNA(sellerName));
        sellerSectionNameLabel.setText(valueOrNA(sellerName));

        currentBidLabel.setText(formatCurrentBid(currentHighestBid));
        startingPriceLabel.setText(formatMoney(startingPrice));

        minimumNextBidLabel.setText(formatMoney(
            getMinimumNextBid(currentHighestBid, startingPrice)
        ));

        numberOfBidsLabel.setText(String.valueOf(currentDetail.getBidCount()));
        bidHistoryCountLabel.setText(currentDetail.getBidCount() + " bids");

        startDateLabel.setText(formatDateTime(currentAuction.getStartDateTime()));
        endDateLabel.setText(formatDateTime(currentAuction.getEndDateTime()));

        auctionIdLabel.setText(valueOrNA(currentAuction.getId()));
        itemIdLabel.setText(valueOrNA(currentAuction.getItemId()));

        loadPrimaryImage();

        updateStatusBadge(currentAuction);
        updateTimeRemaining(currentAuction);
        updateBidAvailability(currentAuction);

        populateAuctionInfoGrid(currentAuction);
        populateProductDetailsGrid();
        populateDescription();
        populateBidHistory();
        populateRelatedAuctions();
    }

    private void updateViewSellerButton() {
        if (viewSellerButton == null) {
            return;
        }

        boolean hasSeller = currentDetail != null
            && currentDetail.getSeller() != null
            && currentDetail.getSeller().getId() != null;

        viewSellerButton.setDisable(!hasSeller);
    }

    private void loadPrimaryImage() {
        if (mainItemImageView == null) {
            return;
        }

        String imageUrl = currentDetail.getPrimaryImageUrl();

        if (imageUrl == null || imageUrl.isBlank()) {
            mainItemImageView.setImage(null);
            return;
        }

        try {
            Image image = new Image(imageUrl, true);
            mainItemImageView.setImage(image);
        } catch (Exception exception) {
            mainItemImageView.setImage(null);
        }
    }

    @FXML
    public void handlePlaceBid() {
        UiMessageUtils.showInfo(
            bidMessageLabel,
            "Bidding service is not connected yet."
        );
    }

    private void refreshBidSection() {
        currentBidLabel.setText(formatCurrentBid(currentHighestBid));

        minimumNextBidLabel.setText(formatMoney(
            getMinimumNextBid(currentHighestBid, startingPrice)
        ));

        numberOfBidsLabel.setText(String.valueOf(currentDetail.getBidCount()));
        bidHistoryCountLabel.setText(currentDetail.getBidCount() + " bids");

        populateAuctionInfoGrid(currentAuction);
        populateBidHistory();
    }

    @FXML
    public void handleAddToWatchlist() {
        UiMessageUtils.showInfo(
            bidMessageLabel,
            "This auction has been added to your watchlist."
        );
    }

    @FXML
    public void handleContactSeller() {
        UiMessageUtils.showInfo(
            bidMessageLabel,
            "Contact seller feature is ready to connect with your messaging service."
        );
    }

    @FXML
    public void handleViewSeller() {
        if (currentDetail == null || currentDetail.getSeller() == null) {
            UiMessageUtils.showInfo(
                bidMessageLabel,
                "Seller information is not available."
            );
            return;
        }

        Integer sellerId = currentDetail.getSeller().getId();

        if (sellerId == null) {
            UiMessageUtils.showInfo(
                bidMessageLabel,
                "Seller ID is not available."
            );
            return;
        }

        if (contentArea == null) {
            UiMessageUtils.showInfo(
                bidMessageLabel,
                "Cannot open seller profile because the navigation area is not available."
            );
            return;
        }

        NavigationService.goToProfilePage(contentArea, sellerId);
    }

    public String formatMoney(BigDecimal amount) {
        if (amount == null) return "N/A";
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        return currencyFormat.format(amount);
    }

    private String formatCurrentBid(BigDecimal amount) {
        if (amount == null) {
            return "No bids yet";
        }

        return formatMoney(amount);
    }

    public String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(dateTimeFormatter);
    }

    public void updateStatusBadge(Auction auction) {
        if (auction == null || auction.getStatus() == null) {
            setStatusBadge("UNKNOWN", "pending-status");
            return;
        }

        AuctionStatus status = auction.getStatus();
        String statusText = status.name();

        switch (status) {
            case ACTIVE -> setStatusBadge(statusText, "active-status");
            case ENDED, UNSOLD -> setStatusBadge(statusText, "ended-status");
            case SOLD -> setStatusBadge(statusText, "sold-status");
            case CANCELLED -> setStatusBadge(statusText, "cancelled-status");
            default -> setStatusBadge(statusText, "pending-status");
        }
    }

    private void setStatusBadge(String text, String statusClass) {
        headerStatusBadge.setText(text);
        headerStatusBadge.getStyleClass().removeAll(
            "active-status",
            "ended-status",
            "sold-status",
            "cancelled-status",
            "pending-status"
        );

        if (!headerStatusBadge.getStyleClass().contains("status-badge")) {
            headerStatusBadge.getStyleClass().add("status-badge");
        }

        headerStatusBadge.getStyleClass().add(statusClass);
    }

    public void setContentArea(Pane contentArea) {
        this.contentArea = contentArea;
    }

    public void updateTimeRemaining(Auction auction) {
        if (auction == null || auction.getEndDateTime() == null) {
            timeRemainingLabel.setText("N/A");
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        if (!auction.getEndDateTime().isAfter(now)) {
            timeRemainingLabel.setText("Ended");
            return;
        }

        Duration duration = Duration.between(now, auction.getEndDateTime());
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();

        if (days > 0) {
            timeRemainingLabel.setText(days + "d " + hours + "h " + minutes + "m");
        } else if (hours > 0) {
            timeRemainingLabel.setText(hours + "h " + minutes + "m");
        } else {
            timeRemainingLabel.setText(minutes + "m");
        }
    }

    private void updateBidAvailability(Auction auction) {
        boolean canBid = currentDetail != null
            && currentDetail.canBid()
            && auction != null
            && auction.isActive()
            && !auction.isDue(LocalDateTime.now());

        bidAmountField.setDisable(!canBid);
        placeBidButton.setDisable(!canBid);

        if (!canBid) {
            auctionWarningLabel.setVisible(true);
            auctionWarningLabel.setManaged(true);

            if (auction != null && auction.getStatus() == AuctionStatus.CANCELLED) {
                auctionWarningLabel.setText("This auction has been cancelled. Bidding is no longer available.");
            } else if (currentDetail != null && currentDetail.isOwner()) {
                auctionWarningLabel.setText("You own this auction, so you cannot place a bid on it.");
            } else {
                auctionWarningLabel.setText("This auction is not available for bidding.");
            }
        } else {
            auctionWarningLabel.setVisible(false);
            auctionWarningLabel.setManaged(false);
            auctionWarningLabel.setText("");
        }
    }

    private void populateAuctionInfoGrid(Auction auction) {
        auctionInfoGrid.getChildren().clear();

        addDetailRow(auctionInfoGrid, 0, "Auction ID", valueOrNA(auction.getId()));
        addDetailRow(auctionInfoGrid, 1, "Item ID", valueOrNA(auction.getItemId()));
        addDetailRow(auctionInfoGrid, 2, "Current Highest Bid ID", valueOrNA(currentDetail.getCurrentHighestBidId()));
        addDetailRow(auctionInfoGrid, 3, "Highest Bidder", valueOrNA(currentDetail.getCurrentHighestBidderUsername()));
        addDetailRow(auctionInfoGrid, 4, "Winner ID", valueOrNA(auction.getWinnerId()));
        addDetailRow(auctionInfoGrid, 5, "Current Bid", formatCurrentBid(currentDetail.getCurrentBidAmount()));
        addDetailRow(auctionInfoGrid, 6, "Final Sale Price", formatMoney(auction.getFinalSalePrice()));
        addDetailRow(auctionInfoGrid, 7, "Start Date", formatDateTime(auction.getStartDateTime()));
        addDetailRow(auctionInfoGrid, 8, "End Date", formatDateTime(auction.getEndDateTime()));
        addDetailRow(auctionInfoGrid, 9, "Created At", formatDateTime(auction.getCreatedAt()));
        addDetailRow(auctionInfoGrid, 10, "Updated At", formatDateTime(auction.getUpdatedAt()));
        addDetailRow(auctionInfoGrid, 11, "Status", auction.getStatus() == null ? "N/A" : auction.getStatus().name());
    }

    private void populateProductDetailsGrid() {
        productDetailsGrid.getChildren().clear();

        String categoryName = currentDetail.getCategory() == null
            ? "N/A"
            : currentDetail.getCategory().getName();

        addDetailRow(productDetailsGrid, 0, "Item Name", valueOrNA(currentItem.getTitle()));
        addDetailRow(productDetailsGrid, 1, "Category", valueOrNA(categoryName));
        addDetailRow(productDetailsGrid, 2, "Category ID", valueOrNA(currentItem.getCategoryId()));
        addDetailRow(productDetailsGrid, 3, "Condition", valueOrNA(currentItem.getCondition()));
        addDetailRow(productDetailsGrid, 4, "Starting Price", formatMoney(currentItem.getStartingPrice()));
        addDetailRow(productDetailsGrid, 5, "Reserve Price", formatMoney(currentItem.getReservePrice()));
        addDetailRow(productDetailsGrid, 6, "Seller", valueOrNA(sellerName));
    }

    private void populateDescription() {
        String description = currentItem.getDescription();

        descriptionLabel.setText(
            description == null || description.isBlank()
                ? "No description provided."
                : description
        );
    }

    private void populateBidHistory() {
        bidHistoryList.getChildren().clear();
        bidHistoryCountLabel.setText(currentDetail.getBidCount() + " bids");

        if (bidHistory.isEmpty()) {
            Label emptyLabel = new Label("No bids have been placed yet.");
            emptyLabel.getStyleClass().add("muted-text");
            bidHistoryList.getChildren().add(emptyLabel);
            return;
        }

        for (AuctionBidEntry entry : bidHistory) {
            bidHistoryList.getChildren().add(createBidHistoryCard(entry));
        }
    }

    private HBox createBidHistoryCard(AuctionBidEntry entry) {
        HBox card = new HBox(14);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("bid-history-card");

        VBox bidderBox = new VBox(4);

        Label bidderNameLabel = new Label(entry.bidderName());
        bidderNameLabel.getStyleClass().add("bidder-name");

        Label bidTimeLabel = new Label(formatDateTime(entry.bidTime()));
        bidTimeLabel.getStyleClass().add("bid-time");

        bidderBox.getChildren().addAll(bidderNameLabel, bidTimeLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label amountLabel = new Label(formatMoney(entry.amount()));
        amountLabel.getStyleClass().add("bid-amount");

        Label statusBadgeLabel = new Label(entry.status());

        switch (entry.status().toLowerCase()) {
            case "highest" -> statusBadgeLabel.getStyleClass().add("highest-bid-badge");
            case "outbid" -> statusBadgeLabel.getStyleClass().add("outbid-badge");
            default -> statusBadgeLabel.getStyleClass().add("valid-badge");
        }

        card.getChildren().addAll(bidderBox, spacer, amountLabel, statusBadgeLabel);
        return card;
    }

    private void populateRelatedAuctions() {
        relatedAuctionsList.getChildren().clear();

        Label emptyLabel = new Label("Related auctions are not connected yet.");
        emptyLabel.getStyleClass().add("muted-text");

        relatedAuctionsList.getChildren().add(emptyLabel);
    }

    private VBox createRelatedAuctionCard(String title, BigDecimal price, String meta) {
        VBox card = new VBox(6);
        card.getStyleClass().add("related-card");

        Label titleLabel = new Label(title);
        titleLabel.setWrapText(true);
        titleLabel.getStyleClass().add("related-title");

        Label priceLabel = new Label(formatMoney(price));
        priceLabel.getStyleClass().add("related-price");

        Label metaLabel = new Label(meta);
        metaLabel.getStyleClass().add("related-meta");

        card.getChildren().addAll(titleLabel, priceLabel, metaLabel);
        return card;
    }

    private void addDetailRow(GridPane grid, int rowIndex, String key, String value) {
        Label keyLabel = new Label(key);
        keyLabel.getStyleClass().add("detail-key");

        Label valueLabel = new Label(value == null || value.isBlank() ? "N/A" : value);
        valueLabel.setWrapText(true);
        valueLabel.getStyleClass().add("detail-value");

        grid.add(keyLabel, 0, rowIndex);
        grid.add(valueLabel, 1, rowIndex);
    }

    private List<AuctionBidEntry> mapRecentBidsToUiEntries(AuctionDetailDto detail) {
        List<AuctionBidEntry> entries = new ArrayList<>();

        if (detail == null || detail.getRecentBids() == null) {
            return entries;
        }

        for (Bid bid : detail.getRecentBids()) {
            String bidderName = resolveBidderName(detail, bid);
            String status = bid.getId() != null
                && bid.getId().equals(detail.getCurrentHighestBidId())
                ? "Highest"
                : "Outbid";

            entries.add(new AuctionBidEntry(
                bidderName,
                bid.getAmount(),
                bid.getBidDateTime(),
                status
            ));
        }

        return entries;
    }

    private String resolveBidderName(AuctionDetailDto detail, Bid bid) {
        if (detail == null || bid == null || bid.getBidderId() == null) {
            return "Unknown Bidder";
        }

        if (bid.getBidderId().equals(detail.getCurrentHighestBidderId())
            && detail.getCurrentHighestBidderUsername() != null
            && !detail.getCurrentHighestBidderUsername().isBlank()) {
            return detail.getCurrentHighestBidderUsername();
        }

        return "Bidder #" + bid.getBidderId();
    }

    private String resolveSellerName(AuctionDetailDto detail) {
        if (detail == null || detail.getSeller() == null) {
            return "Unknown Seller";
        }

        String username = detail.getSeller().getUsername();

        if (username == null || username.isBlank()) {
            return "Seller #" + detail.getSeller().getId();
        }

        return username;
    }

    private String valueOrNA(Object value) {
        return value == null ? "N/A" : String.valueOf(value);
    }

    private BigDecimal getStartingPrice(Item item) {
        if (item == null || item.getStartingPrice() == null) {
            return BigDecimal.ZERO;
        }

        return item.getStartingPrice();
    }

    private BigDecimal getMinimumNextBid(
        BigDecimal currentHighestBid,
        BigDecimal startingPrice
    ) {
        BigDecimal minimumBidIncrement = new BigDecimal("5.00");

        if (currentHighestBid != null && currentHighestBid.compareTo(BigDecimal.ZERO) > 0) {
            return currentHighestBid.add(minimumBidIncrement);
        }

        if (startingPrice != null) {
            return startingPrice;
        }

        return BigDecimal.ZERO;
    }

    private void showFatalPageError(String message) {
        String safeMessage = message == null || message.isBlank()
            ? "Could not load auction details."
            : message;

        if (auctionTitleLabel != null) {
            auctionTitleLabel.setText("Auction unavailable");
        }

        if (auctionWarningLabel != null) {
            auctionWarningLabel.setVisible(true);
            auctionWarningLabel.setManaged(true);
            auctionWarningLabel.setText(safeMessage);
        }

        if (bidAmountField != null) {
            bidAmountField.setDisable(true);
        }

        if (placeBidButton != null) {
            placeBidButton.setDisable(true);
        }
    }
}