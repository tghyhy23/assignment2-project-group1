package com.group01.asm2.controllers;

/**
 * @author Group 01
 */

import com.group01.asm2.dtos.BidHistoryDto;
import com.group01.asm2.models.Auction;
import com.group01.asm2.services.AuctionService;
import com.group01.asm2.services.BidService;
import com.group01.asm2.services.NavigationService;
import com.group01.asm2.utils.MoneyFormatter;
import com.group01.asm2.utils.ScrollUtils;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BidsHistoryController extends BaseController {

    private final BidService bidService = new BidService();
    private final AuctionService auctionService = new AuctionService();

    @FXML
    private HBox statusTabsBox;

    @FXML
    private TextField searchField;

    @FXML
    private ScrollPane BidHistoryScrollPane;

    @FXML
    private VBox bidCardsContainer;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private final List<BidHistoryDto> bidHistory = new ArrayList<>();

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
        loadBidHistory();
    }

    private void loadBidHistory() {
        try {
            bidHistory.clear();
            bidHistory.addAll(bidService.readMyBidHistory());
            refreshBidCards();
        } catch (Exception exception) {
            exception.printStackTrace();
            bidCardsContainer.getChildren().clear();
            bidCardsContainer.getChildren().add(createErrorState("Could not load your bid history."));
        }
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

        List<BidHistoryDto> filteredBids = getFilteredBidHistory(selectedCategory, keyword);

        if (filteredBids.isEmpty()) {
            bidCardsContainer.getChildren().add(createEmptyState());
            return;
        }

        for (BidHistoryDto bid : filteredBids) {
            bidCardsContainer.getChildren().add(createBidCard(bid));
        }
    }

    private VBox createBidCard(BidHistoryDto bid) {
        VBox card = new VBox(16);
        card.getStyleClass().add("bid-card");
        card.setMaxWidth(Double.MAX_VALUE);

        HBox header = createCardHeader(bid);
        HBox body = createCardBody(bid);
        HBox footer = createCardFooter(bid);

        card.getChildren().addAll(header, body, footer);

        return card;
    }

    private HBox createCardHeader(BidHistoryDto bid) {
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

    private HBox createCardBody(BidHistoryDto bid) {
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

    private HBox createCardFooter(BidHistoryDto bid) {
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.getStyleClass().add("card-footer");

        List<Button> buttons = createActionButtons(bid);
        footer.getChildren().addAll(buttons);

        return footer;
    }

    private List<Button> createActionButtons(BidHistoryDto bid) {
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

        if ("Won".equalsIgnoreCase(bidStatus)
            && "Paid".equalsIgnoreCase(paymentStatus)) {
            return List.of(
                createSecondaryButton("View Auction", bid)
            );
        }

        if ("Lost".equalsIgnoreCase(bidStatus)) {
            return List.of(
                createSecondaryButton("View Auction", bid)
            );
        }

        if ("Cancelled".equalsIgnoreCase(bidStatus)) {
            return List.of(
                createSecondaryButton("View Details", bid)
            );
        }

        return List.of(
            createSecondaryButton("View Details", bid)
        );
    }

    private Button createPrimaryButton(String text, BidHistoryDto bid) {
        Button button = new Button(text);
        button.getStyleClass().add("primary-action-button");

        bindAction(button, event -> {
            event.consume();

            if ("Pay Now".equalsIgnoreCase(text)) {
                showInfo("Payment processing will be connected after PaymentService is implemented.");
                return;
            }

            goToAuctionDetails(bid);
        });

        return button;
    }

    private Button createSecondaryButton(String text, BidHistoryDto bid) {
        Button button = new Button(text);
        button.getStyleClass().add("secondary-action-button");

        bindAction(button, event -> {
            event.consume();
            goToAuctionDetails(bid);
        });

        return button;
    }

    private void goToAuctionDetails(BidHistoryDto bid) {
        Auction auction = auctionService.readAuction(bid.getBid().getAuctionId());

        if (auction == null) {
            showError("Auction not found.");
            return;
        }

        Pane contentArea = (Pane) bidCardsContainer.getScene().lookup("#contentArea");

        if (contentArea == null) {
            showError("Could not open auction details.");
            return;
        }

        NavigationService.goToAuctionDetails(contentArea, auction);
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

    private VBox createErrorState(String message) {
        VBox errorState = new VBox(8);
        errorState.getStyleClass().add("empty-state");
        errorState.setAlignment(Pos.CENTER);
        errorState.setMaxWidth(Double.MAX_VALUE);

        Label title = new Label(message);
        title.getStyleClass().add("empty-title");

        Label subtitle = new Label("Please try again later.");
        subtitle.getStyleClass().add("empty-subtitle");

        errorState.getChildren().addAll(title, subtitle);

        return errorState;
    }

    private List<BidHistoryDto> getFilteredBidHistory(String category, String keyword) {
        return bidHistory.stream()
            .filter(bid -> matchesCategory(bid, category))
            .filter(bid -> matchesKeyword(bid, keyword))
            .toList();
    }

    private boolean matchesCategory(BidHistoryDto bid, String category) {
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

    private boolean matchesKeyword(BidHistoryDto bid, String keyword) {
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

    private String getBidStatusStyleClass(String status) {
        if (status == null) {
            return "status-lost";
        }

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
        if (status == null) {
            return "payment-not-required";
        }

        return switch (status.toLowerCase()) {
            case "pending payment" -> "payment-pending";
            case "paid" -> "payment-paid";
            case "cancelled" -> "payment-cancelled";
            default -> "payment-not-required";
        };
    }
}