package com.group01.asm2.controllers;

import com.group01.asm2.models.BidHistoryViewModel;
import com.group01.asm2.models.Bid;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Locale;
import com.group01.asm2.utils.MoneyFormatter;
import com.group01.asm2.utils.ScrollUtils;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class BidsHistoryController {

    @FXML private HBox statusTabsBox;
    @FXML private TextField searchField;
    @FXML private ScrollPane BidHistoryScrollPane;
    @FXML private VBox bidCardsContainer;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private String selectedCategory = "All";

    private final List<String> categories = List.of(
            "All",
            "Winning",
            "Outbid",
            "Won",
            "Lost",
            "Pending Payment",
            "Paid",
            "Cancelled"
    );

    @FXML
    public void initialize() {
        BidHistoryScrollPane.setFitToWidth(true);

        ScrollUtils.makeSmooth(BidHistoryScrollPane, 0.003);

        renderStatusTabs();
        setupSearchListener();
        refreshBidCards();
    }

    private void renderStatusTabs() {
        statusTabsBox.getChildren().clear();

        for (String category : categories) {
            Label tab = new Label(category);
            tab.getStyleClass().add("status-tab");

            if (category.equals(selectedCategory)) {
                tab.getStyleClass().add("status-tab-active");
            }

            tab.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(tab, Priority.ALWAYS);
            tab.setAlignment(Pos.CENTER);

            tab.setOnMouseClicked(event -> {
                selectedCategory = category;
                renderStatusTabs();
                refreshBidCards();
            });

            statusTabsBox.getChildren().add(tab);
        }
    }

    private void setupSearchListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> refreshBidCards());
    }

    private void refreshBidCards() {
        bidCardsContainer.getChildren().clear();

        String keyword = searchField.getText();

        List<BidHistoryViewModel> filteredBids =
                getFilteredBidHistory(selectedCategory, keyword);

        if (filteredBids.isEmpty()) {
            bidCardsContainer.getChildren().add(createEmptyState());
            return;
        }

        for (BidHistoryViewModel bid : filteredBids) {
            bidCardsContainer.getChildren().add(createBidCard(bid));
        }
    }

    private VBox createBidCard(BidHistoryViewModel bid) {
        VBox card = new VBox(16);
        card.getStyleClass().add("bid-card");
        card.setMaxWidth(Double.MAX_VALUE);

        HBox header = createCardHeader(bid);
        HBox body = createCardBody(bid);
        HBox footer = createCardFooter(bid);

        card.getChildren().addAll(header, body, footer);

        return card;
    }

    private HBox createCardHeader(BidHistoryViewModel bid) {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("card-header");

        VBox sellerInfo = new VBox(4);

        Label sellerName = new Label("BidBlitz Auction Item");
        sellerName.getStyleClass().add("seller-name");

        Label auctionId = new Label("Auction ID: #" + bid.getBid().getAuctionId());
        auctionId.getStyleClass().add("auction-id");

        sellerInfo.getChildren().addAll(sellerName, auctionId);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label bidStatusBadge = new Label(bid.getBidStatus().toUpperCase());
        bidStatusBadge.getStyleClass().addAll("status-badge", getBidStatusStyleClass(bid.getBidStatus()));

        Label paymentBadge = new Label(bid.getPaymentStatus().toUpperCase());
        paymentBadge.getStyleClass().addAll("payment-badge", getPaymentStatusStyleClass(bid.getPaymentStatus()));

        header.getChildren().addAll(sellerInfo, spacer, bidStatusBadge, paymentBadge);

        return header;
    }

    private HBox createCardBody(BidHistoryViewModel bid) {
        HBox body = new HBox(18);
        body.setAlignment(Pos.CENTER_LEFT);

        Region itemImage = new Region();
        itemImage.getStyleClass().add("bid-item-image");

        if (bid.getMainBgClass() != null && !bid.getMainBgClass().isBlank()) {
            itemImage.getStyleClass().add(bid.getMainBgClass());
        } else {
            itemImage.getStyleClass().add("item-image-placeholder");
        }

        VBox itemInfo = new VBox(8);
        HBox.setHgrow(itemInfo, Priority.ALWAYS);

        Label itemName = new Label(bid.getItemName());
        itemName.getStyleClass().add("item-title");
        itemName.setWrapText(true);

        Label date = new Label("Bid date: " + bid.getBidDateTime().format(dateFormatter));
        date.getStyleClass().add("item-meta");

        Label result = new Label("Result: " + bid.getFinalResultText());
        result.getStyleClass().add("item-meta");
        result.setWrapText(true);

        itemInfo.getChildren().addAll(itemName, date, result);

        VBox priceBox = new VBox(7);
        priceBox.setAlignment(Pos.CENTER_RIGHT);

        Label myBidLabel = new Label("My Bid");
        myBidLabel.getStyleClass().add("price-label");

        Label myBidValue = new Label(MoneyFormatter.formatUSD(bid.getMyBidAmount()));
        myBidValue.getStyleClass().add("price-value");

        Label highestLabel = new Label("Current Highest");
        highestLabel.getStyleClass().add("price-label");

        Label highestValue = new Label(MoneyFormatter.formatUSD(bid.getCurrentHighestBidAmount()));
        highestValue.getStyleClass().add("price-highlight");

        priceBox.getChildren().addAll(
                myBidLabel,
                myBidValue,
                highestLabel,
                highestValue
        );

        body.getChildren().addAll(itemImage, itemInfo, priceBox);

        return body;
    }

    private HBox createCardFooter(BidHistoryViewModel bid) {
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.getStyleClass().add("card-footer");

        List<Button> buttons = createActionButtons(bid);
        footer.getChildren().addAll(buttons);

        return footer;
    }

    private List<Button> createActionButtons(BidHistoryViewModel bid) {
        String bidStatus = bid.getBidStatus();
        String paymentStatus = bid.getPaymentStatus();

        if ("Winning".equalsIgnoreCase(bidStatus)) {
            return List.of(
                    createSecondaryButton("View Auction", bid),
                    createPrimaryButton("Increase Bid", bid)
            );
        }

        if ("Outbid".equalsIgnoreCase(bidStatus)) {
            return List.of(
                    createSecondaryButton("View Auction", bid),
                    createPrimaryButton("Bid Again", bid)
            );
        }

        if ("Won".equalsIgnoreCase(bidStatus)
                && "Pending Payment".equalsIgnoreCase(paymentStatus)) {
            return List.of(
                    createSecondaryButton("View Details", bid),
                    createPrimaryButton("Pay Now", bid)
            );
        }

        // Won + Paid: no action button
        if ("Won".equalsIgnoreCase(bidStatus)
                && "Paid".equalsIgnoreCase(paymentStatus)) {
            return List.of();
        }

        if ("Lost".equalsIgnoreCase(bidStatus)) {
            return List.of(
                    createSecondaryButton("View Auction", bid)
            );
        }

        if ("Cancelled".equalsIgnoreCase(bidStatus)) {
            return List.of(
                    createSecondaryButton("View Details", bid));
        }

        return List.of(
                createSecondaryButton("View Details", bid)
        );
    }

    private Button createPrimaryButton(String text, BidHistoryViewModel bid) {
        Button button = new Button(text);
        button.getStyleClass().add("primary-action-button");
        button.setOnAction(event ->
                System.out.println(text + " clicked for bid ID: " + bid.getBid().getId())
        );
        return button;
    }

    private Button createSecondaryButton(String text, BidHistoryViewModel bid) {
        Button button = new Button(text);
        button.getStyleClass().add("secondary-action-button");
        button.setOnAction(event ->
                System.out.println(text + " clicked for bid ID: " + bid.getBid().getId())
        );
        return button;
    }

    private VBox createEmptyState() {
        VBox emptyState = new VBox(8);
        emptyState.getStyleClass().add("empty-state");
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setMaxWidth(Double.MAX_VALUE);

        Label title = new Label("No bids found.");
        title.getStyleClass().add("empty-title");

        Label subtitle = new Label("Try changing the filter or search keyword.");
        subtitle.getStyleClass().add("empty-subtitle");

        emptyState.getChildren().addAll(title, subtitle);

        return emptyState;
    }

    private String getBidStatusStyleClass(String status) {
        if (status == null) return "status-lost";

        return switch (status.toLowerCase()) {
            case "winning" -> "status-winning";
            case "outbid" -> "status-outbid";
            case "won" -> "status-won";
            case "lost" -> "status-lost";
            case "cancelled" -> "status-cancelled";
            default -> "status-lost";
        };
    }

    private String getPaymentStatusStyleClass(String status) {
        if (status == null) return "payment-not-required";

        return switch (status.toLowerCase()) {
            case "pending payment" -> "payment-pending";
            case "paid" -> "payment-paid";
            case "cancelled" -> "payment-cancelled";
            default -> "payment-not-required";
        };
    }

    private List<BidHistoryViewModel> getAllBidHistory() {
        List<Bid> bids = getMockBids();
        List<BidHistoryViewModel> viewModels = new ArrayList<>();

        for (Bid bid : bids) {
            viewModels.add(mapToViewModel(bid));
        }

        return viewModels;
    }

    private List<BidHistoryViewModel> getFilteredBidHistory(String category, String keyword) {
        List<BidHistoryViewModel> allBids = getAllBidHistory();

        return allBids.stream()
                .filter(bid -> matchesCategory(bid, category))
                .filter(bid -> matchesKeyword(bid, keyword))
                .toList();
    }

    private List<Bid> getMockBids() {
        List<Bid> bids = new ArrayList<>();

        bids.add(new Bid(
                1,
                101,
                1,
                99,
                new BigDecimal("150000000.00"),
                LocalDateTime.now().minusHours(4)
        ));

        bids.add(new Bid(
                2,
                102,
                2,
                99,
                new BigDecimal("45000000.00"),
                LocalDateTime.now().minusDays(1)
        ));

        bids.add(new Bid(
                3,
                103,
                3,
                99,
                new BigDecimal("800000000.00"),
                LocalDateTime.now().minusDays(2)
        ));

        bids.add(new Bid(
                4,
                104,
                1,
                99,
                new BigDecimal("152000000.00"),
                LocalDateTime.now().minusDays(4)
        ));

        bids.add(new Bid(
                5,
                105,
                2,
                99,
                new BigDecimal("47000000.00"),
                LocalDateTime.now().minusDays(7)
        ));

        bids.add(new Bid(
                6,
                106,
                3,
                99,
                new BigDecimal("790000000.00"),
                LocalDateTime.now().minusDays(10)
        ));

        return bids;
    }

    private BidHistoryViewModel mapToViewModel(Bid bid) {
        String itemName = getItemTitleById(bid.getItemId());
        String mainBgClass = getMainBgClassByItemId(bid.getItemId());

        BigDecimal currentHighest = calculateCurrentHighestBid(bid);
        String bidStatus = calculateBidStatus(bid);
        String paymentStatus = calculatePaymentStatus(bidStatus, bid);
        String finalResultText = createFinalResultText(bidStatus, paymentStatus, bid, currentHighest);

        return new BidHistoryViewModel(
                bid,
                itemName,
                mainBgClass,
                bidStatus,
                paymentStatus,
                bid.getAmount(),
                currentHighest,
                bid.getBidDateTime(),
                finalResultText
        );
    }

    private String getItemTitleById(Integer itemId) {
        if (itemId == null) {
            return "Unknown Item";
        }

        return switch (itemId) {
            case 1 -> "Đồng hồ Rolex Submariner 2020";
            case 2 -> "Bức tranh sơn dầu thế kỷ 19";
            case 3 -> "Siêu xe Ford Mustang 1969 Classic";
            default -> "Unknown Item";
        };
    }

    private String getMainBgClassByItemId(Integer itemId) {
        if (itemId == null) {
            return "item-image-placeholder";
        }

        return switch (itemId) {
            case 1 -> "watch-bg";
            case 2 -> "painting-bg";
            case 3 -> "car-bg";
            default -> "item-image-placeholder";
        };
    }

    private BigDecimal calculateCurrentHighestBid(Bid bid) {
        if (bid.getAmount() == null) {
            return BigDecimal.ZERO;
        }

        Integer bidId = bid.getId();

        if (bidId == null) {
            return bid.getAmount();
        }

        // Mock logic only:
        // Some bids are outbid by another user.
        if (bidId == 2 || bidId == 5) {
            return bid.getAmount().add(new BigDecimal("5000000.00"));
        }

        return bid.getAmount();
    }

    private String calculateBidStatus(Bid bid) {
        Integer bidId = bid.getId();

        if (bidId == null) {
            return "Lost";
        }

        // Mock status logic for UI testing.
        return switch (bidId) {
            case 1 -> "Winning";
            case 2 -> "Outbid";
            case 3 -> "Won";
            case 4 -> "Won";
            case 5 -> "Lost";
            case 6 -> "Cancelled";
            default -> "Lost";
        };
    }

    private String calculatePaymentStatus(String bidStatus, Bid bid) {
        Integer bidId = bid.getId();

        if ("Cancelled".equalsIgnoreCase(bidStatus)) {
            return "Cancelled";
        }

        if ("Won".equalsIgnoreCase(bidStatus)) {
            if (bidId != null && bidId == 3) {
                return "Pending Payment";
            }

            return "Paid";
        }

        return "Not Required";
    }

    private String createFinalResultText(
            String bidStatus,
            String paymentStatus,
            Bid bid,
            BigDecimal currentHighest
    ) {
        if ("Winning".equalsIgnoreCase(bidStatus)) {
            return "You are currently the highest bidder.";
        }

        if ("Outbid".equalsIgnoreCase(bidStatus)) {
            return "Another bidder has placed a higher bid.";
        }

        if ("Won".equalsIgnoreCase(bidStatus)
                && "Pending Payment".equalsIgnoreCase(paymentStatus)) {
            return "You won this auction. Payment is required.";
        }

        if ("Won".equalsIgnoreCase(bidStatus)
                && "Paid".equalsIgnoreCase(paymentStatus)) {
            return "You won this auction and payment has been completed.";
        }

        if ("Lost".equalsIgnoreCase(bidStatus)) {
            return "The auction ended with another winner.";
        }

        if ("Cancelled".equalsIgnoreCase(bidStatus)) {
            return "This bid or auction was cancelled.";
        }

        return "Current highest bid: " + MoneyFormatter.formatUSD(currentHighest);
    }

    private boolean matchesCategory(BidHistoryViewModel bid, String category) {
        if (category == null || category.isBlank() || "All".equalsIgnoreCase(category)) {
            return true;
        }

        if ("Pending Payment".equalsIgnoreCase(category)) {
            return "Pending Payment".equalsIgnoreCase(bid.getPaymentStatus());
        }

        if ("Paid".equalsIgnoreCase(category)) {
            return "Paid".equalsIgnoreCase(bid.getPaymentStatus());
        }

        return category.equalsIgnoreCase(bid.getBidStatus());
    }

    private boolean matchesKeyword(BidHistoryViewModel bid, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }

        String lowerKeyword = keyword.toLowerCase(Locale.ROOT);

        String auctionId = String.valueOf(bid.getBid().getAuctionId());
        String myBidAmount = MoneyFormatter.formatUSD(bid.getMyBidAmount());
        String highestAmount = MoneyFormatter.formatUSD(bid.getCurrentHighestBidAmount());

        return bid.getItemName().toLowerCase(Locale.ROOT).contains(lowerKeyword)
                || auctionId.toLowerCase(Locale.ROOT).contains(lowerKeyword)
                || bid.getBidStatus().toLowerCase(Locale.ROOT).contains(lowerKeyword)
                || bid.getPaymentStatus().toLowerCase(Locale.ROOT).contains(lowerKeyword)
                || myBidAmount.toLowerCase(Locale.ROOT).contains(lowerKeyword)
                || highestAmount.toLowerCase(Locale.ROOT).contains(lowerKeyword);
    }
}