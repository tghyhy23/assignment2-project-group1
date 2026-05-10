package com.group01.asm2.controllers;

import com.group01.asm2.enums.AuctionStatus;
import com.group01.asm2.models.Auction;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AuctionDetailsController {

    @FXML private VBox pageWrapper;

    @FXML private Label headerStatusBadge;

    @FXML private ImageView mainItemImageView;

    @FXML private Label auctionTitleLabel;
    @FXML private Label sellerNameLabel;
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

    @FXML private TextField bidAmountField;
    @FXML private Button placeBidButton;
    @FXML private Label bidMessageLabel;

    @FXML private Label sellerSectionNameLabel;

    @FXML private GridPane auctionInfoGrid;
    @FXML private GridPane productDetailsGrid;

    @FXML private Label descriptionLabel;

    @FXML private VBox bidHistoryList;
    @FXML private Label bidHistoryCountLabel;

    @FXML private VBox relatedAuctionsList;

    private Auction currentAuction;

    private BigDecimal startingPrice;
    private BigDecimal currentHighestBid;
    private BigDecimal minimumBidIncrement;

    private String mockItemName;
    private String mockCategory;
    private String mockCondition;
    private String mockBrand;
    private String mockLocation;
    private String mockDescription;
    private String mockSellerName;

    private final List<BidHistoryEntry> bidHistory = new ArrayList<>();

    private final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    @FXML
    public void initialize() {
        setupMockData();

        Auction mockAuction = new Auction(
                1001,
                501,
                AuctionStatus.ACTIVE,
                3004,
                null,
                null,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(2).plusHours(5).plusMinutes(30),
                LocalDateTime.now().minusDays(3),
                LocalDateTime.now().minusHours(2)
        );

        loadAuctionDetails(mockAuction);
    }

    private void setupMockData() {
        mockItemName = "Premium Mechanical Keyboard - Limited Edition";
        mockCategory = "Electronics / Computer Accessories";
        mockCondition = "Like New";
        mockBrand = "Keychron";
        mockLocation = "Ho Chi Minh City, Vietnam";
        mockSellerName = "Tech Auction Store";

        startingPrice = new BigDecimal("120.00");
        currentHighestBid = new BigDecimal("185.00");
        minimumBidIncrement = new BigDecimal("5.00");

        mockDescription = """
                This auction is for a premium mechanical keyboard in excellent condition.
                
                The item has been carefully checked before listing and is suitable for collectors, programmers, students, and office users who enjoy a high-quality typing experience.
                
                The keyboard includes a solid body, responsive switches, clean keycaps, and a modern layout. Minor signs of use may be present, but the overall condition remains very good.
                
                Please review the auction details carefully before placing your bid. Once a bid is submitted successfully, it will be added to the bid history and may become the current highest bid.
                """;

        bidHistory.clear();
        bidHistory.add(new BidHistoryEntry("Minh T.", new BigDecimal("185.00"), LocalDateTime.now().minusHours(1), "Highest"));
        bidHistory.add(new BidHistoryEntry("Alex N.", new BigDecimal("175.00"), LocalDateTime.now().minusHours(3), "Outbid"));
        bidHistory.add(new BidHistoryEntry("Sarah L.", new BigDecimal("160.00"), LocalDateTime.now().minusHours(5), "Outbid"));
        bidHistory.add(new BidHistoryEntry("David P.", new BigDecimal("145.00"), LocalDateTime.now().minusHours(8), "Valid"));
    }

    public void loadAuctionDetails(Auction auction) {
        this.currentAuction = auction;

        auctionTitleLabel.setText(mockItemName);
        sellerNameLabel.setText(mockSellerName);
        sellerSectionNameLabel.setText(mockSellerName);

        currentBidLabel.setText(formatMoney(currentHighestBid));
        startingPriceLabel.setText(formatMoney(startingPrice));
        minimumNextBidLabel.setText(formatMoney(getMinimumNextBid()));
        numberOfBidsLabel.setText(String.valueOf(bidHistory.size()));

        startDateLabel.setText(formatDateTime(auction.getStartDateTime()));
        endDateLabel.setText(formatDateTime(auction.getEndDateTime()));
        auctionIdLabel.setText(valueOrNA(auction.getId()));
        itemIdLabel.setText(valueOrNA(auction.getItemId()));

        updateStatusBadge(auction);
        updateTimeRemaining(auction);
        updateBidAvailability(auction);

        populateAuctionInfoGrid(auction);
        populateProductDetailsGrid();
        populateDescription();
        populateBidHistory();
        populateRelatedAuctions();
    }

    @FXML
    public void handlePlaceBid() {
        clearBidMessage();

        if (currentAuction == null) {
            showBidError("Auction data is not available.");
            return;
        }

        String rawAmount = bidAmountField.getText();

        if (rawAmount == null || rawAmount.trim().isEmpty()) {
            showBidError("Please enter a bid amount.");
            return;
        }

        BigDecimal bidAmount;

        try {
            bidAmount = new BigDecimal(rawAmount.trim()).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException ex) {
            showBidError("Bid amount must be a valid number.");
            return;
        }

        String validationMessage = validateBid(bidAmount);

        if (validationMessage != null) {
            showBidError(validationMessage);
            return;
        }

        currentHighestBid = bidAmount;
        currentAuction.setCurrentHighestBidId(generateMockBidId());

        bidHistory.add(0, new BidHistoryEntry(
                "You",
                bidAmount,
                LocalDateTime.now(),
                "Highest"
        ));

        for (int i = 1; i < bidHistory.size(); i++) {
            if ("Highest".equalsIgnoreCase(bidHistory.get(i).status())) {
                bidHistory.set(i, new BidHistoryEntry(
                        bidHistory.get(i).bidderName(),
                        bidHistory.get(i).amount(),
                        bidHistory.get(i).bidTime(),
                        "Outbid"
                ));
            }
        }

        bidAmountField.clear();

        currentBidLabel.setText(formatMoney(currentHighestBid));
        minimumNextBidLabel.setText(formatMoney(getMinimumNextBid()));
        numberOfBidsLabel.setText(String.valueOf(bidHistory.size()));
        bidHistoryCountLabel.setText(bidHistory.size() + " bids");

        populateAuctionInfoGrid(currentAuction);
        populateBidHistory();

        showBidSuccess("Your bid has been placed successfully.");
    }

    @FXML
    public void handleAddToWatchlist() {
        showBidInfo("This auction has been added to your watchlist.");
    }

    @FXML
    public void handleContactSeller() {
        showBidInfo("Contact seller feature is ready to connect with your messaging service.");
    }

    public String validateBid(BigDecimal bidAmount) {
        if (bidAmount == null) {
            return "Please enter a bid amount.";
        }

        if (bidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return "Bid amount must be greater than 0.";
        }

        if (!currentAuction.isActive()) {
            return "You cannot bid because this auction is not active.";
        }

        if (currentAuction.isDue(LocalDateTime.now())) {
            return "You cannot bid because this auction is already due.";
        }

        BigDecimal minimumRequiredBid = getMinimumNextBid();

        if (bidAmount.compareTo(minimumRequiredBid) < 0) {
            return "Your bid must be at least " + formatMoney(minimumRequiredBid) + ".";
        }

        return null;
    }

    public String formatMoney(BigDecimal amount) {
        if (amount == null) {
            return "N/A";
        }

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        return currencyFormat.format(amount);
    }

    public String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }

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
        boolean canBid = auction != null
                && auction.isActive()
                && !auction.isDue(LocalDateTime.now());

        bidAmountField.setDisable(!canBid);
        placeBidButton.setDisable(!canBid);

        if (!canBid) {
            auctionWarningLabel.setVisible(true);
            auctionWarningLabel.setManaged(true);

            if (auction != null && auction.getStatus() == AuctionStatus.CANCELLED) {
                auctionWarningLabel.setText("This auction has been cancelled. Bidding is no longer available.");
            } else {
                auctionWarningLabel.setText("This auction has ended. You can review the result but cannot place a new bid.");
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
        addDetailRow(auctionInfoGrid, 2, "Current Highest Bid ID", valueOrNA(auction.getCurrentHighestBidId()));
        addDetailRow(auctionInfoGrid, 3, "Winner ID", valueOrNA(auction.getWinnerId()));
        addDetailRow(auctionInfoGrid, 4, "Final Sale Price", formatMoney(auction.getFinalSalePrice()));
        addDetailRow(auctionInfoGrid, 5, "Start Date Time", formatDateTime(auction.getStartDateTime()));
        addDetailRow(auctionInfoGrid, 6, "End Date Time", formatDateTime(auction.getEndDateTime()));
        addDetailRow(auctionInfoGrid, 7, "Created At", formatDateTime(auction.getCreatedAt()));
        addDetailRow(auctionInfoGrid, 8, "Updated At", formatDateTime(auction.getUpdatedAt()));
        addDetailRow(auctionInfoGrid, 9, "Status", auction.getStatus() == null ? "N/A" : auction.getStatus().name());
    }

    private void populateProductDetailsGrid() {
        productDetailsGrid.getChildren().clear();

        addDetailRow(productDetailsGrid, 0, "Item Name", mockItemName);
        addDetailRow(productDetailsGrid, 1, "Category", mockCategory);
        addDetailRow(productDetailsGrid, 2, "Condition", mockCondition);
        addDetailRow(productDetailsGrid, 3, "Brand", mockBrand);
        addDetailRow(productDetailsGrid, 4, "Location", mockLocation);
        addDetailRow(productDetailsGrid, 5, "Seller", mockSellerName);
    }

    private void populateDescription() {
        descriptionLabel.setText(mockDescription);
    }

    private void populateBidHistory() {
        bidHistoryList.getChildren().clear();
        bidHistoryCountLabel.setText(bidHistory.size() + " bids");

        for (BidHistoryEntry entry : bidHistory) {
            bidHistoryList.getChildren().add(createBidHistoryCard(entry));
        }
    }

    private HBox createBidHistoryCard(BidHistoryEntry entry) {
        HBox card = new HBox(14);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("bid-history-card");

        VBox bidderBox = new VBox(4);

        Label bidderName = new Label(entry.bidderName());
        bidderName.getStyleClass().add("bidder-name");

        Label bidTime = new Label(formatDateTime(entry.bidTime()));
        bidTime.getStyleClass().add("bid-time");

        bidderBox.getChildren().addAll(bidderName, bidTime);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label amount = new Label(formatMoney(entry.amount()));
        amount.getStyleClass().add("bid-amount");

        Label statusBadge = new Label(entry.status());

        switch (entry.status().toLowerCase()) {
            case "highest" -> statusBadge.getStyleClass().add("highest-bid-badge");
            case "outbid" -> statusBadge.getStyleClass().add("outbid-badge");
            default -> statusBadge.getStyleClass().add("valid-badge");
        }

        card.getChildren().addAll(bidderBox, spacer, amount, statusBadge);

        return card;
    }

    private void populateRelatedAuctions() {
        relatedAuctionsList.getChildren().clear();

        relatedAuctionsList.getChildren().add(createRelatedAuctionCard(
                "Gaming Mouse Wireless",
                new BigDecimal("45.00"),
                "Ends in 8h"
        ));

        relatedAuctionsList.getChildren().add(createRelatedAuctionCard(
                "USB-C Docking Station",
                new BigDecimal("78.00"),
                "Ends in 1d 4h"
        ));

        relatedAuctionsList.getChildren().add(createRelatedAuctionCard(
                "Mechanical Keycap Set",
                new BigDecimal("32.00"),
                "Ends in 3d"
        ));
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

    private BigDecimal getMinimumNextBid() {
        BigDecimal baseAmount = currentHighestBid != null ? currentHighestBid : startingPrice;

        if (baseAmount == null) {
            baseAmount = BigDecimal.ZERO;
        }

        return baseAmount.add(minimumBidIncrement);
    }

    private int generateMockBidId() {
        return 3000 + bidHistory.size() + 1;
    }

    private String valueOrNA(Object value) {
        return value == null ? "N/A" : String.valueOf(value);
    }

    private void clearBidMessage() {
        bidMessageLabel.setText("");
        bidMessageLabel.getStyleClass().removeAll("error-message", "success-message", "info-message");
    }

    private void showBidError(String message) {
        bidMessageLabel.setText(message);
        bidMessageLabel.getStyleClass().removeAll("success-message", "info-message");
        if (!bidMessageLabel.getStyleClass().contains("error-message")) {
            bidMessageLabel.getStyleClass().add("error-message");
        }
    }

    private void showBidSuccess(String message) {
        bidMessageLabel.setText(message);
        bidMessageLabel.getStyleClass().removeAll("error-message", "info-message");
        if (!bidMessageLabel.getStyleClass().contains("success-message")) {
            bidMessageLabel.getStyleClass().add("success-message");
        }
    }

    private void showBidInfo(String message) {
        bidMessageLabel.setText(message);
        bidMessageLabel.getStyleClass().removeAll("error-message", "success-message");
        if (!bidMessageLabel.getStyleClass().contains("info-message")) {
            bidMessageLabel.getStyleClass().add("info-message");
        }
    }

    private record BidHistoryEntry(
            String bidderName,
            BigDecimal amount,
            LocalDateTime bidTime,
            String status
    ) {}
}