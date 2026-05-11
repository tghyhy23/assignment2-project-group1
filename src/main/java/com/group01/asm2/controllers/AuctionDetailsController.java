package com.group01.asm2.controllers;

import com.group01.asm2.enums.AuctionStatus;
import com.group01.asm2.models.Auction;
import com.group01.asm2.models.Item;
import com.group01.asm2.utils.ScrollUtils;
import javafx.scene.control.ScrollPane;
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
    @FXML private ScrollPane detailScrollPane;

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

    // --- CÁC BIẾN MODEL CHÍNH ---
    private Auction currentAuction;
    private Item currentItem;
    private String sellerName; // Mock tên người bán (tương lai thay bằng model User)

    private BigDecimal startingPrice;
    private BigDecimal currentHighestBid;
    private final BigDecimal minimumBidIncrement = new BigDecimal("5.00");

    private final List<BidHistoryEntry> bidHistory = new ArrayList<>();

    private final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    @FXML
    public void initialize() {
        ScrollUtils.makeSmooth(detailScrollPane);
    }

    /**
     * Hàm nhận data từ màn hình danh sách và bắt đầu render giao diện
     */
    public void loadAuctionDetails(Auction auction) {
        if (auction == null) return;
        this.currentAuction = auction;

        // 1. Lấy thông tin sản phẩm (Item) dựa trên itemId
        fetchItemDetailsMock(auction.getItemId());

        // 2. Xử lý giá tiền (Lấy từ Auction)
        currentHighestBid = auction.getFinalSalePrice() != null ? auction.getFinalSalePrice() : BigDecimal.ZERO;
        startingPrice = currentItem.getStartingPrice() != null ? currentItem.getStartingPrice() : currentHighestBid.multiply(new BigDecimal("0.7"));

        // 3. Render Text lên UI
        auctionTitleLabel.setText(currentItem.getTitle());
        sellerNameLabel.setText(sellerName);
        sellerSectionNameLabel.setText(sellerName);

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

    /**
     * Giả lập việc truy vấn DB: Lấy dữ liệu Item dựa vào ID
     */
    private void fetchItemDetailsMock(Integer itemId) {
        // Tạo Base Item
//        this.currentItem = new Item(
//                itemId,
//                99, // categoryId mock
//                101, // sellerId mock
//                "Item #" + itemId, // title default
//                "Detailed description for item ID #" + itemId, // desc default
//                "Used", // condition
//                "Unknown Brand", // brand
//                "Ho Chi Minh City, Vietnam", // location
//                new BigDecimal("100.00"), // startingPrice
//                null, // reservePrice
//                LocalDateTime.now(),
//                LocalDateTime.now()
//        );
//
//        this.sellerName = "Premium Seller";
//
//        // Ghi đè dữ liệu cụ thể để khớp với các ID 1, 2, 3 từ trang danh sách
//        if (itemId == 1) {
//            currentItem.setTitle("Đồng hồ Rolex Submariner 2020");
//            currentItem.setBrand("Rolex");
//            currentItem.setCondition("Like New");
//            currentItem.setDescription("Đồng hồ Rolex nguyên bản, đầy đủ giấy tờ, hộp sổ thẻ. Tình trạng hoàn hảo, chưa qua đánh bóng. Phù hợp cho giới sưu tầm.");
//            currentItem.setStartingPrice(new BigDecimal("150000000.00"));
//        } else if (itemId == 2) {
//            currentItem.setTitle("Bức tranh sơn dầu thế kỷ 19");
//            currentItem.setCondition("Vintage");
//            currentItem.setDescription("Tác phẩm nghệ thuật độc bản từ thế kỷ 19. Đã được thẩm định bởi chuyên gia mỹ thuật quốc tế.");
//            currentItem.setStartingPrice(new BigDecimal("50000000.00"));
//        } else if (itemId == 3) {
//            currentItem.setTitle("Siêu xe Ford Mustang 1969 Classic");
//            currentItem.setBrand("Ford");
//            currentItem.setDescription("Xe cơ bắp Mỹ cổ điển, động cơ V8 mạnh mẽ, đã phục chế toàn bộ nội ngoại thất giữ nguyên bản sắc năm 1969.");
//            currentItem.setStartingPrice(new BigDecimal("800000000.00"));
//        }
//
//        // Tạo sẵn lịch sử Bid giả lập
//        bidHistory.clear();
//        BigDecimal bid1 = currentAuction.getFinalSalePrice() != null ? currentAuction.getFinalSalePrice() : new BigDecimal("100");
//        BigDecimal bid2 = bid1.subtract(new BigDecimal("5000000")); // Mock trừ đi 5 triệu
//        BigDecimal bid3 = bid2.subtract(new BigDecimal("10000000"));
//
//        bidHistory.add(new BidHistoryEntry("Minh T.", bid1, LocalDateTime.now().minusHours(1), "Highest"));
//        bidHistory.add(new BidHistoryEntry("Alex N.", bid2, LocalDateTime.now().minusHours(3), "Outbid"));
//        if (bid3.compareTo(BigDecimal.ZERO) > 0) {
//            bidHistory.add(new BidHistoryEntry("Sarah L.", bid3, LocalDateTime.now().minusHours(5), "Outbid"));
//        }
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
        if (bidAmount == null) return "Please enter a bid amount.";
        if (bidAmount.compareTo(BigDecimal.ZERO) <= 0) return "Bid amount must be greater than 0.";
        if (!currentAuction.isActive()) return "You cannot bid because this auction is not active.";
        if (currentAuction.isDue(LocalDateTime.now())) return "You cannot bid because this auction is already due.";

        BigDecimal minimumRequiredBid = getMinimumNextBid();
        if (bidAmount.compareTo(minimumRequiredBid) < 0) {
            return "Your bid must be at least " + formatMoney(minimumRequiredBid) + ".";
        }
        return null;
    }

    public String formatMoney(BigDecimal amount) {
        if (amount == null) return "N/A";
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        return currencyFormat.format(amount);
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
                "active-status", "ended-status", "sold-status", "cancelled-status", "pending-status"
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
        boolean canBid = auction != null && auction.isActive() && !auction.isDue(LocalDateTime.now());

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
        addDetailRow(auctionInfoGrid, 5, "Start Date", formatDateTime(auction.getStartDateTime()));
        addDetailRow(auctionInfoGrid, 6, "End Date", formatDateTime(auction.getEndDateTime()));
        addDetailRow(auctionInfoGrid, 7, "Created At", formatDateTime(auction.getCreatedAt()));
        addDetailRow(auctionInfoGrid, 8, "Updated At", formatDateTime(auction.getUpdatedAt()));
        addDetailRow(auctionInfoGrid, 9, "Status", auction.getStatus() == null ? "N/A" : auction.getStatus().name());
    }

    private void populateProductDetailsGrid() {
        productDetailsGrid.getChildren().clear();
        addDetailRow(productDetailsGrid, 0, "Item Name", currentItem.getTitle());
        addDetailRow(productDetailsGrid, 1, "Category ID", valueOrNA(currentItem.getCategoryId())); // Tương lai ánh xạ ra tên Category
        addDetailRow(productDetailsGrid, 2, "Condition", valueOrNA(currentItem.getCondition()));
//        addDetailRow(productDetailsGrid, 3, "Brand", currentItem.getBrand());
//        addDetailRow(productDetailsGrid, 4, "Location", currentItem.getLocation());
        addDetailRow(productDetailsGrid, 5, "Seller", sellerName);
    }

    private void populateDescription() {
        descriptionLabel.setText(currentItem.getDescription());
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
        relatedAuctionsList.getChildren().add(createRelatedAuctionCard("Gaming Mouse Wireless", new BigDecimal("45.00"), "Ends in 8h"));
        relatedAuctionsList.getChildren().add(createRelatedAuctionCard("USB-C Docking Station", new BigDecimal("78.00"), "Ends in 1d 4h"));
        relatedAuctionsList.getChildren().add(createRelatedAuctionCard("Mechanical Keycap Set", new BigDecimal("32.00"), "Ends in 3d"));
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
        if (baseAmount == null) baseAmount = BigDecimal.ZERO;
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
        if (!bidMessageLabel.getStyleClass().contains("error-message")) bidMessageLabel.getStyleClass().add("error-message");
    }

    private void showBidSuccess(String message) {
        bidMessageLabel.setText(message);
        bidMessageLabel.getStyleClass().removeAll("error-message", "info-message");
        if (!bidMessageLabel.getStyleClass().contains("success-message")) bidMessageLabel.getStyleClass().add("success-message");
    }

    private void showBidInfo(String message) {
        bidMessageLabel.setText(message);
        bidMessageLabel.getStyleClass().removeAll("error-message", "success-message");
        if (!bidMessageLabel.getStyleClass().contains("info-message")) bidMessageLabel.getStyleClass().add("info-message");
    }

    private record BidHistoryEntry(String bidderName, BigDecimal amount, LocalDateTime bidTime, String status) {}
}