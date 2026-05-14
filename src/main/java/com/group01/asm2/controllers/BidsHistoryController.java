package com.group01.asm2.controllers;

import com.group01.asm2.dtos.BidHistoryDto;
import com.group01.asm2.models.Auction;
import com.group01.asm2.services.AuctionService;
import com.group01.asm2.services.BidService;
import com.group01.asm2.services.NavigationService;
import com.group01.asm2.utils.MoneyFormatter;
import com.group01.asm2.utils.ScrollUtils;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BidsHistoryController extends BaseController {

    private final BidService bidService = new BidService();
    private final AuctionService auctionService = new AuctionService();

    @FXML private ScrollPane BidHistoryScrollPane;
    @FXML private TextField searchField;
    @FXML private VBox bidCardsContainer;

    @FXML private StackPane filterModalOverlay;
    @FXML private ComboBox<String> statusFilterBox;
    @FXML private ComboBox<String> paymentFilterBox;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private final List<BidHistoryDto> bidHistory = new ArrayList<>();

    private String selectedStatusFilter = "All";
    private String selectedPaymentFilter = "All";
    private LocalDate selectedFromDate = null;
    private LocalDate selectedToDate = null;

    @FXML
    public void initialize() {
        BidHistoryScrollPane.setFitToWidth(true);

        ScrollUtils.makeSmooth(
                BidHistoryScrollPane,
                0.003
        );

        setupSearchListener();
        setupFilterForm();
        loadBidHistory();
    }

    private void setupSearchListener() {
        searchField.textProperty().addListener(
                (observable, oldValue, newValue) -> refreshBidCards()
        );
    }

    private void setupFilterForm() {

        statusFilterBox.getItems().setAll(
                "All",
                "LEADING",
                "OUTBID",
                "WON",
                "LOST",
                "CANCELLED"
        );

        paymentFilterBox.getItems().setAll(
                "All",
                "PENDING",
                "COMPLETED",
                "FAILED",
                "CANCELLED"
        );

        statusFilterBox.setValue("All");
        paymentFilterBox.setValue("All");
    }

    private void loadBidHistory() {

        try {

            bidHistory.clear();

            bidHistory.addAll(
                    bidService.readMyBidHistory()
            );

            refreshBidCards();

        } catch (Exception exception) {

            exception.printStackTrace();

            bidCardsContainer.getChildren().clear();

            bidCardsContainer.getChildren().add(
                    createErrorState(
                            "Could not load your bid history."
                    )
            );
        }
    }

    private void refreshBidCards() {

        bidCardsContainer.getChildren().clear();

        String keyword = searchField.getText();

        List<BidHistoryDto> filteredBids =
                getFilteredBidHistory(keyword);

        if (filteredBids.isEmpty()) {

            bidCardsContainer.getChildren().add(
                    createEmptyState()
            );

            return;
        }

        for (BidHistoryDto bid : filteredBids) {

            bidCardsContainer.getChildren().add(
                    createBidCard(bid)
            );
        }
    }

    @FXML
    private void handleOpenFilterForm() {

        filterModalOverlay.setVisible(true);
        filterModalOverlay.setManaged(true);

        statusFilterBox.setValue(selectedStatusFilter);

        paymentFilterBox.setValue(selectedPaymentFilter);

        fromDatePicker.setValue(selectedFromDate);

        toDatePicker.setValue(selectedToDate);
    }

    @FXML
    private void handleCloseFilterForm() {

        filterModalOverlay.setVisible(false);
        filterModalOverlay.setManaged(false);
    }

    @FXML
    private void handleApplyFilters() {

        selectedStatusFilter =
                statusFilterBox.getValue();

        selectedPaymentFilter =
                paymentFilterBox.getValue();

        selectedFromDate =
                fromDatePicker.getValue();

        selectedToDate =
                toDatePicker.getValue();

        refreshBidCards();

        handleCloseFilterForm();
    }

    @FXML
    private void handleResetFilters() {

        selectedStatusFilter = "All";
        selectedPaymentFilter = "All";

        selectedFromDate = null;
        selectedToDate = null;

        statusFilterBox.setValue("All");

        paymentFilterBox.setValue("All");

        fromDatePicker.setValue(null);

        toDatePicker.setValue(null);

        refreshBidCards();
    }

    @FXML
    private void handleExportBidHistory() {

        List<BidHistoryDto> filteredBids =
                getFilteredBidHistory(
                        searchField.getText()
                );

        try (
                FileWriter writer =
                        new FileWriter("bid_history_export.csv")
        ) {

            writer.write(
                    "Auction ID,Item Name,Bid Status,Payment Status,My Bid,Current Highest,Bid Date\n"
            );

            for (BidHistoryDto bid : filteredBids) {

                writer.write(
                        bid.getBid().getAuctionId() + "," +
                                escapeCsv(bid.getItemName()) + "," +
                                escapeCsv(bid.getBidStatus()) + "," +
                                escapeCsv(bid.getPaymentStatus()) + "," +
                                escapeCsv(
                                        MoneyFormatter.formatUSD(
                                                bid.getMyBidAmount()
                                        )
                                ) + "," +
                                escapeCsv(
                                        MoneyFormatter.formatUSD(
                                                bid.getCurrentHighestBidAmount()
                                        )
                                ) + "," +
                                escapeCsv(
                                        bid.getBidDateTime().format(dateFormatter)
                                ) +
                                "\n"
                );
            }

            showInfo(
                    "Bid history exported to bid_history_export.csv"
            );

        } catch (IOException exception) {

            exception.printStackTrace();

            showError(
                    "Could not export bid history."
            );
        }
    }

    private String escapeCsv(String value) {

        if (value == null) {
            return "";
        }

        return "\"" +
                value.replace("\"", "\"\"") +
                "\"";
    }

    private List<BidHistoryDto> getFilteredBidHistory(
            String keyword
    ) {

        return bidHistory.stream()
                .filter(this::matchesStatusFilter)
                .filter(this::matchesPaymentFilter)
                .filter(this::matchesDateFilter)
                .filter(
                        bid -> matchesKeyword(
                                bid,
                                keyword
                        )
                )
                .toList();
    }

    private boolean matchesStatusFilter(
            BidHistoryDto bid
    ) {

        if (selectedStatusFilter == null
                || selectedStatusFilter.isBlank()
                || "All".equalsIgnoreCase(selectedStatusFilter)) {

            return true;
        }

        return selectedStatusFilter.equalsIgnoreCase(
                bid.getBidStatus()
        );
    }

    private boolean matchesPaymentFilter(
            BidHistoryDto bid
    ) {

        if (selectedPaymentFilter == null
                || selectedPaymentFilter.isBlank()
                || "All".equalsIgnoreCase(selectedPaymentFilter)) {

            return true;
        }

        return selectedPaymentFilter.equalsIgnoreCase(
                bid.getPaymentStatus()
        );
    }

    private boolean matchesDateFilter(
            BidHistoryDto bid
    ) {

        if (bid.getBidDateTime() == null) {
            return true;
        }

        LocalDate bidDate =
                bid.getBidDateTime().toLocalDate();

        if (selectedFromDate != null
                && bidDate.isBefore(selectedFromDate)) {

            return false;
        }

        if (selectedToDate != null
                && bidDate.isAfter(selectedToDate)) {

            return false;
        }

        return true;
    }

    private boolean matchesKeyword(
            BidHistoryDto bid,
            String keyword
    ) {

        if (keyword == null || keyword.isBlank()) {
            return true;
        }

        String lowerKeyword =
                keyword.toLowerCase(Locale.ROOT);

        String auctionId =
                String.valueOf(
                        bid.getBid().getAuctionId()
                );

        String myBidAmount =
                MoneyFormatter.formatUSD(
                        bid.getMyBidAmount()
                );

        String highestAmount =
                MoneyFormatter.formatUSD(
                        bid.getCurrentHighestBidAmount()
                );

        return safeLower(
                bid.getItemName()
        ).contains(lowerKeyword)

                || auctionId.contains(lowerKeyword)

                || safeLower(
                bid.getBidStatus()
        ).contains(lowerKeyword)

                || safeLower(
                bid.getPaymentStatus()
        ).contains(lowerKeyword)

                || myBidAmount.toLowerCase(
                Locale.ROOT
        ).contains(lowerKeyword)

                || highestAmount.toLowerCase(
                Locale.ROOT
        ).contains(lowerKeyword);
    }

    private String safeLower(String value) {

        return value == null
                ? ""
                : value.toLowerCase(Locale.ROOT);
    }

    private VBox createBidCard(
            BidHistoryDto bid
    ) {

        VBox card = new VBox(16);

        card.getStyleClass().add("bid-card");

        card.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(
                createCardHeader(bid),
                createCardBody(bid),
                createCardFooter(bid)
        );

        return card;
    }

    private HBox createCardHeader(
            BidHistoryDto bid
    ) {

        HBox header = new HBox(12);

        header.setAlignment(Pos.CENTER_LEFT);

        header.getStyleClass().add("card-header");

        VBox sellerInfo = new VBox(4);

        Label sellerName =
                new Label("BidBlitz Auction Item");

        sellerName.getStyleClass().add("seller-name");

        Label auctionId = new Label(
                "Auction ID: #"
                        + bid.getBid().getAuctionId()
        );

        auctionId.getStyleClass().add("auction-id");

        sellerInfo.getChildren().addAll(
                sellerName,
                auctionId
        );

        Region spacer = new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Label bidStatusBadge =
                new Label(
                        safeUpper(
                                bid.getBidStatus()
                        )
                );

        bidStatusBadge.getStyleClass().addAll(
                "status-badge",
                getBidStatusStyleClass(
                        bid.getBidStatus()
                )
        );

        Label paymentBadge =
                new Label(
                        safeUpper(
                                bid.getPaymentStatus()
                        )
                );

        paymentBadge.getStyleClass().addAll(
                "payment-badge",
                getPaymentStatusStyleClass(
                        bid.getPaymentStatus()
                )
        );

        header.getChildren().addAll(
                sellerInfo,
                spacer,
                bidStatusBadge,
                paymentBadge
        );

        return header;
    }

    private String safeUpper(String value) {

        return value == null
                ? "N/A"
                : value.toUpperCase();
    }

    private HBox createCardBody(
            BidHistoryDto bid
    ) {

        HBox body = new HBox(18);

        body.setAlignment(Pos.CENTER_LEFT);

        Region itemImage = new Region();

        itemImage.getStyleClass().add(
                "bid-item-image"
        );

        if (bid.getMainBgClass() != null
                && !bid.getMainBgClass().isBlank()) {

            itemImage.getStyleClass().add(
                    bid.getMainBgClass()
            );

        } else {

            itemImage.getStyleClass().add(
                    "item-image-placeholder"
            );
        }

        VBox itemInfo = new VBox(8);

        HBox.setHgrow(
                itemInfo,
                Priority.ALWAYS
        );

        Label itemName =
                new Label(
                        bid.getItemName()
                );

        itemName.getStyleClass().add(
                "item-title"
        );

        itemName.setWrapText(true);

        Label date = new Label(
                "Bid date: "
                        + bid.getBidDateTime().format(dateFormatter)
        );

        date.getStyleClass().add("item-meta");

        Label result = new Label(
                "Result: "
                        + bid.getFinalResultText()
        );

        result.getStyleClass().add("item-meta");

        result.setWrapText(true);

        itemInfo.getChildren().addAll(
                itemName,
                date,
                result
        );

        VBox priceBox = new VBox(7);

        priceBox.setAlignment(Pos.CENTER_RIGHT);

        Label myBidLabel = new Label("My Bid");

        myBidLabel.getStyleClass().add("price-label");

        Label myBidValue = new Label(
                MoneyFormatter.formatUSD(
                        bid.getMyBidAmount()
                )
        );

        myBidValue.getStyleClass().add("price-value");

        Label highestLabel =
                new Label("Current Highest");

        highestLabel.getStyleClass().add("price-label");

        Label highestValue = new Label(
                MoneyFormatter.formatUSD(
                        bid.getCurrentHighestBidAmount()
                )
        );

        highestValue.getStyleClass().add(
                "price-highlight"
        );

        priceBox.getChildren().addAll(
                myBidLabel,
                myBidValue,
                highestLabel,
                highestValue
        );

        body.getChildren().addAll(
                itemImage,
                itemInfo,
                priceBox
        );

        return body;
    }

    private HBox createCardFooter(
            BidHistoryDto bid
    ) {

        HBox footer = new HBox(12);

        footer.setAlignment(Pos.CENTER_RIGHT);

        footer.getStyleClass().add("card-footer");

        footer.getChildren().addAll(
                createActionButtons(bid)
        );

        return footer;
    }

    private List<Button> createActionButtons(
            BidHistoryDto bid
    ) {

        String bidStatus =
                bid.getBidStatus();

        String paymentStatus =
                bid.getPaymentStatus();

        if ("LEADING".equalsIgnoreCase(bidStatus)) {

            return List.of(
                    createSecondaryButton(
                            "View Auction",
                            bid
                    ),

                    createPrimaryButton(
                            "Increase Bid",
                            bid
                    )
            );
        }

        if ("OUTBID".equalsIgnoreCase(bidStatus)) {

            return List.of(
                    createSecondaryButton(
                            "View Auction",
                            bid
                    ),

                    createPrimaryButton(
                            "Bid Again",
                            bid
                    )
            );
        }

        if ("WON".equalsIgnoreCase(bidStatus)
                && "PENDING".equalsIgnoreCase(paymentStatus)) {

            return List.of(
                    createSecondaryButton(
                            "View Details",
                            bid
                    ),

                    createPrimaryButton(
                            "Pay Now",
                            bid
                    )
            );
        }

        return List.of(
                createSecondaryButton(
                        "View Auction",
                        bid
                )
        );
    }

    private Button createPrimaryButton(
            String text,
            BidHistoryDto bid
    ) {

        Button button = new Button(text);

        button.getStyleClass().add(
                "primary-action-button"
        );

        bindAction(button, event -> {

            event.consume();

            if ("Pay Now".equalsIgnoreCase(text)) {

                showInfo(
                        "Payment processing will be connected after PaymentService is implemented."
                );

                return;
            }

            goToAuctionDetails(bid);
        });

        return button;
    }

    private Button createSecondaryButton(
            String text,
            BidHistoryDto bid
    ) {

        Button button = new Button(text);

        button.getStyleClass().add(
                "secondary-action-button"
        );

        bindAction(button, event -> {

            event.consume();

            goToAuctionDetails(bid);
        });

        return button;
    }

    private void goToAuctionDetails(
            BidHistoryDto bid
    ) {

        Auction auction =
                auctionService.readAuction(
                        bid.getBid().getAuctionId()
                );

        if (auction == null) {

            showError("Auction not found.");

            return;
        }

        Pane contentArea =
                (Pane) bidCardsContainer
                        .getScene()
                        .lookup("#contentArea");

        if (contentArea == null) {

            showError(
                    "Could not open auction details."
            );

            return;
        }

        NavigationService.goToAuctionDetails(
                contentArea,
                auction
        );
    }

    private VBox createEmptyState() {

        VBox emptyState = new VBox(8);

        emptyState.getStyleClass().add("empty-state");

        emptyState.setAlignment(Pos.CENTER);

        emptyState.setMaxWidth(Double.MAX_VALUE);

        Label title =
                new Label("No bids found.");

        title.getStyleClass().add("empty-title");

        Label subtitle = new Label(
                "Try changing the filter or search keyword."
        );

        subtitle.getStyleClass().add(
                "empty-subtitle"
        );

        emptyState.getChildren().addAll(
                title,
                subtitle
        );

        return emptyState;
    }

    private VBox createErrorState(
            String message
    ) {

        VBox errorState = new VBox(8);

        errorState.getStyleClass().add("empty-state");

        errorState.setAlignment(Pos.CENTER);

        errorState.setMaxWidth(Double.MAX_VALUE);

        Label title = new Label(message);

        title.getStyleClass().add("empty-title");

        Label subtitle = new Label(
                "Please try again later."
        );

        subtitle.getStyleClass().add(
                "empty-subtitle"
        );

        errorState.getChildren().addAll(
                title,
                subtitle
        );

        return errorState;
    }

    private String getBidStatusStyleClass(
            String status
    ) {

        if (status == null) {
            return "status-lost";
        }

        return switch (status.toUpperCase()) {

            case "LEADING" -> "status-winning";

            case "OUTBID" -> "status-outbid";

            case "WON" -> "status-won";

            case "LOST" -> "status-lost";

            case "CANCELLED" -> "status-cancelled";

            default -> "status-lost";
        };
    }

    private String getPaymentStatusStyleClass(
            String status
    ) {

        if (status == null) {
            return "payment-failed";
        }

        return switch (status.toUpperCase()) {

            case "PENDING" -> "payment-pending";

            case "COMPLETED" -> "payment-paid";

            case "FAILED" -> "payment-failed";

            case "CANCELLED" -> "payment-cancelled";

            default -> "payment-failed";
        };
    }
}