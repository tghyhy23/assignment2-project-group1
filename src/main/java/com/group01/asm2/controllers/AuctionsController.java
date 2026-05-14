package com.group01.asm2.controllers;

/**
 * @author Group 01
 */

import com.group01.asm2.dtos.AuctionFilter;
import com.group01.asm2.dtos.WonAuctionDto;
import com.group01.asm2.enums.AuctionStatus;
import com.group01.asm2.enums.PaymentStatus;
import com.group01.asm2.models.Auction;
import com.group01.asm2.models.Item;
import com.group01.asm2.services.AuctionService;
import com.group01.asm2.services.ItemService;
import com.group01.asm2.services.NavigationService;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AuctionsController {

    private final AuctionService auctionService = new AuctionService();
    private final ItemService itemService = new ItemService();

    private static final int ITEMS_PER_PAGE = 20;

    private int currentPage = 0;
    private List<Auction> allAuctions;

    // =========================
    // Browse Auctions Card UI
    // Used by components/all_auctions.fxml
    // =========================
    @FXML private FlowPane allAuctionsContainer;
    @FXML private HBox paginationBox;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Label pageInfoLabel;

    // =========================
    // Legacy Auction Table UI
    // Kept for old table pages if they still exist
    // =========================
    @FXML private TableView<Auction> auctionsTable;
    @FXML private TableColumn<Auction, String> auctionDateColumn;
    @FXML private TableColumn<Auction, String> auctionTypeColumn;
    @FXML private TableColumn<Auction, String> auctionItemColumn;
    @FXML private TableColumn<Auction, String> auctionAmountColumn;
    @FXML private TableColumn<Auction, String> auctionStatusColumn;

    // =========================
    // Won Auctions UI
    // Used by won-auctions-view.fxml
    // =========================
    @FXML private TableView<WonAuctionDto> wonAuctionsTable;

    @FXML private TableColumn<WonAuctionDto, String> wonDateColumn;
    @FXML private TableColumn<WonAuctionDto, String> itemColumn;
    @FXML private TableColumn<WonAuctionDto, String> sellerColumn;
    @FXML private TableColumn<WonAuctionDto, String> categoryColumn;
    @FXML private TableColumn<WonAuctionDto, String> finalPriceColumn;
    @FXML private TableColumn<WonAuctionDto, String> paymentAmountColumn;
    @FXML private TableColumn<WonAuctionDto, String> paymentStatusColumn;
    @FXML private TableColumn<WonAuctionDto, String> paymentDateColumn;
    @FXML private TableColumn<WonAuctionDto, String> wonAuctionStatusColumn;
    @FXML private TableColumn<WonAuctionDto, Void> actionColumn;

    @FXML private Button exportPurchaseSummaryButton;

    @FXML private Label totalWonLabel;
    @FXML private Label totalSpentLabel;
    @FXML private Label completedPaymentsLabel;

    private final ObservableList<Auction> auctionsList =
        FXCollections.observableArrayList();

    private final ObservableList<WonAuctionDto> wonAuctionsList =
        FXCollections.observableArrayList();

    private final DateTimeFormatter formatter =
        DateTimeFormatter.ofPattern("dd MMM yyyy");

    @FXML
    public void initialize() {
        if (allAuctionsContainer != null) {
            loadAllAuctionsToExplore();
        }

        if (auctionsTable != null) {
            loadLegacyAuctionTable();
            setupLegacyAuctionTable();
            setupLegacyAuctionTableNavigation();
            makeTableResponsive(auctionsTable, "No history found.");
        }

        if (wonAuctionsTable != null) {
            setupWonAuctionTable();
            loadWonAuctionsForUiOnly();
            updateWonAuctionSummaryCards();
            makeTableResponsive(wonAuctionsTable, "No won auctions found.");
        }
    }

    // =========================================================
    // Won Auctions Table
    // =========================================================
    private void setupWonAuctionTable() {
        if (wonDateColumn != null) {
            wonDateColumn.setCellValueFactory(cellData -> {
                LocalDateTime date = cellData.getValue().getWonDateTime();
                return new SimpleStringProperty(formatDate(date));
            });
        }

        if (itemColumn != null) {
            itemColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatText(cellData.getValue().getItemTitle()))
            );
        }

        if (sellerColumn != null) {
            sellerColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatText(cellData.getValue().getSellerUsername()))
            );
        }

        if (categoryColumn != null) {
            categoryColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatText(cellData.getValue().getCategoryName()))
            );
        }

        if (finalPriceColumn != null) {
            finalPriceColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatMoney(cellData.getValue().getFinalSalePrice()))
            );
        }

        if (paymentAmountColumn != null) {
            paymentAmountColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatMoney(cellData.getValue().getTotalAmount()))
            );
        }

        if (paymentStatusColumn != null) {
            paymentStatusColumn.setCellValueFactory(cellData -> {
                PaymentStatus status = cellData.getValue().getPaymentStatus();
                return new SimpleStringProperty(status == null ? "N/A" : status.name());
            });

            paymentStatusColumn.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(String statusText, boolean empty) {
                    super.updateItem(statusText, empty);

                    if (empty) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    Label badge = createStatusBadge(formatText(statusText));
                    applyPaymentStatusStyle(badge, statusText);

                    setText(null);
                    setGraphic(badge);
                }
            });
        }

        if (paymentDateColumn != null) {
            paymentDateColumn.setCellValueFactory(cellData -> {
                LocalDateTime date = cellData.getValue().getPaymentDateTime();
                return new SimpleStringProperty(formatDate(date));
            });
        }

        if (wonAuctionStatusColumn != null) {
            wonAuctionStatusColumn.setCellValueFactory(cellData -> {
                AuctionStatus status = cellData.getValue().getAuctionStatus();
                return new SimpleStringProperty(status == null ? "N/A" : status.name());
            });

            wonAuctionStatusColumn.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(String statusText, boolean empty) {
                    super.updateItem(statusText, empty);

                    if (empty) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    Label badge = createStatusBadge(formatText(statusText));
                    applyAuctionStatusStyle(badge, statusText);

                    setText(null);
                    setGraphic(badge);
                }
            });
        }

        if (actionColumn != null) {
            actionColumn.setCellFactory(column -> new TableCell<>() {
                private final Button viewButton = new Button("View");

                {
                    viewButton.getStyleClass().add("table-action-button");

                    viewButton.setOnAction(event -> {
                        int rowIndex = getIndex();

                        if (rowIndex < 0 || rowIndex >= getTableView().getItems().size()) {
                            return;
                        }

                        WonAuctionDto selectedRecord =
                            getTableView().getItems().get(rowIndex);

                        handleViewWonAuction(selectedRecord);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : viewButton);
                }
            });
        }

        wonAuctionsTable.setItems(wonAuctionsList);
    }

    private void loadWonAuctionsForUiOnly() {
        wonAuctionsList.clear();

        /*
         * Frontend-only placeholder.
         *
         * Later, replace this with backend/service data:
         *
         * wonAuctionsList.addAll(
         *     wonAuctionService.readWonAuctionsForCurrentUser()
         * );
         */
    }

    private void updateWonAuctionSummaryCards() {
        int totalWon = wonAuctionsList.size();

        BigDecimal totalSpent = wonAuctionsList.stream()
            .map(WonAuctionDto::getTotalAmount)
            .filter(amount -> amount != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        long completedPayments = wonAuctionsList.stream()
            .filter(record -> record.getPaymentStatus() == PaymentStatus.COMPLETED)
            .count();

        if (totalWonLabel != null) {
            totalWonLabel.setText(String.valueOf(totalWon));
        }

        if (totalSpentLabel != null) {
            totalSpentLabel.setText(formatMoney(totalSpent));
        }

        if (completedPaymentsLabel != null) {
            completedPaymentsLabel.setText(String.valueOf(completedPayments));
        }

        if (exportPurchaseSummaryButton != null) {
            exportPurchaseSummaryButton.setDisable(wonAuctionsList.isEmpty());
        }
    }

    @FXML
    private void handleExportPurchaseSummary() {
        System.out.println("Export purchase summary clicked.");
    }

    private void handleViewWonAuction(WonAuctionDto selectedRecord) {
        if (selectedRecord == null) {
            return;
        }

        System.out.println("View won auction ID: " + selectedRecord.getAuctionId());
    }

    // =========================================================
    // Browse Auctions Card Grid
    // =========================================================
    private void loadAllAuctionsToExplore() {
        AuctionFilter filter = new AuctionFilter();
        filter.setRecommendedOnly(true);
        filter.setStatus(AuctionStatus.ACTIVE);

        allAuctions = auctionService.readAuctions(filter);
        currentPage = 0;

        renderCurrentPage();
    }

    private void renderCurrentPage() {
        if (allAuctionsContainer == null) {
            return;
        }

        allAuctionsContainer.getChildren().clear();

        if (allAuctions == null || allAuctions.isEmpty()) {
            if (pageInfoLabel != null) {
                pageInfoLabel.setText("Page 0 / 0");
            }

            if (prevPageButton != null) {
                prevPageButton.setDisable(true);
            }

            if (nextPageButton != null) {
                nextPageButton.setDisable(true);
            }

            return;
        }

        int totalPages = (int) Math.ceil((double) allAuctions.size() / ITEMS_PER_PAGE);

        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allAuctions.size());

        for (int i = startIndex; i < endIndex; i++) {
            VBox card = createAuctionCard(allAuctions.get(i));
            allAuctionsContainer.getChildren().add(card);
        }

        if (pageInfoLabel != null) {
            pageInfoLabel.setText("Page " + (currentPage + 1) + " / " + totalPages);
        }

        if (prevPageButton != null) {
            prevPageButton.setDisable(currentPage == 0);
        }

        if (nextPageButton != null) {
            nextPageButton.setDisable(currentPage >= totalPages - 1);
        }
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 0) {
            currentPage--;
            renderCurrentPage();
        }
    }

    @FXML
    private void handleNextPage() {
        if (allAuctions == null || allAuctions.isEmpty()) {
            return;
        }

        int totalPages = (int) Math.ceil((double) allAuctions.size() / ITEMS_PER_PAGE);

        if (currentPage < totalPages - 1) {
            currentPage++;
            renderCurrentPage();
        }
    }

    private VBox createAuctionCard(Auction auction) {
        Item item = itemService.readItem(auction.getItemId());

        String itemName = item != null
            ? item.getTitle()
            : "Unknown Item";

        String price = auction.getFinalSalePrice() != null
            ? "$" + auction.getFinalSalePrice().toPlainString()
            : "N/A";

        String endDate = auction.getEndDateTime() != null
            ? auction.getEndDateTime().format(formatter)
            : "N/A";

        StackPane imageBox = new StackPane();
        imageBox.getStyleClass().add("auction-image-placeholder");
        imageBox.setMaxWidth(Double.MAX_VALUE);

        Label nameLabel = new Label(itemName);
        nameLabel.getStyleClass().add("auction-name");
        nameLabel.setWrapText(true);
        nameLabel.setMinHeight(38);
        nameLabel.setPrefHeight(38);
        nameLabel.setMaxHeight(38);

        Label startingPriceTitle = new Label("Starting price");
        startingPriceTitle.getStyleClass().add("auction-price-title");

        Label startingPriceValue = new Label(
            item != null && item.getStartingPrice() != null
                ? "$" + item.getStartingPrice().toPlainString()
                : "N/A"
        );
        startingPriceValue.getStyleClass().add("auction-starting-price");

        Label currentBidTitle = new Label("Current bid");
        currentBidTitle.getStyleClass().add("auction-price-title");

        Label currentBidValue = new Label(price);
        currentBidValue.getStyleClass().add("auction-price");

        VBox leftPriceBox = new VBox(startingPriceTitle, startingPriceValue);
        leftPriceBox.setSpacing(2);

        VBox rightPriceBox = new VBox(currentBidTitle, currentBidValue);
        rightPriceBox.setSpacing(2);

        HBox bidSection = new HBox(leftPriceBox, rightPriceBox);
        bidSection.setSpacing(28);

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

        VBox contentBox = new VBox(nameLabel, bidSection, statusLabel, dateLabel);
        contentBox.setSpacing(8);
        contentBox.setStyle("-fx-padding: 12;");

        VBox card = new VBox(imageBox, contentBox);
        card.setSpacing(8);
        card.setFillWidth(true);
        card.getStyleClass().add("auction-card");

        TranslateTransition cardMove =
            new TranslateTransition(Duration.seconds(0.18), card);

        card.setOnMouseEntered(event -> {
            cardMove.setToY(-5);
            cardMove.playFromStart();
        });

        card.setOnMouseExited(event -> {
            cardMove.setToY(0);
            cardMove.playFromStart();
        });

        card.setOnMouseClicked(event -> {
            Pane contentArea = (Pane) card.getScene().lookup("#contentArea");
            NavigationService.goToAuctionDetails(contentArea, auction);
        });

        return card;
    }

    // =========================================================
    // Legacy Auction Table
    // =========================================================
    private void loadLegacyAuctionTable() {
        auctionsList.clear();

        AuctionFilter filter = new AuctionFilter();
        filter.setRecommendedOnly(true);
        filter.setStatus(AuctionStatus.ACTIVE);

        auctionsList.addAll(auctionService.readAuctions(filter));
    }

    private void setupLegacyAuctionTable() {
        if (auctionDateColumn != null) {
            auctionDateColumn.setCellValueFactory(cellData -> {
                LocalDateTime date = cellData.getValue().getEndDateTime();
                return new SimpleStringProperty(formatDate(date));
            });
        }

        if (auctionTypeColumn != null) {
            auctionTypeColumn.setCellValueFactory(cellData -> {
                boolean hasWinner = cellData.getValue().hasWinner();
                return new SimpleStringProperty(hasWinner ? "Purchase" : "Sale");
            });
        }

        if (auctionItemColumn != null) {
            auctionItemColumn.setCellValueFactory(cellData -> {
                Integer itemId = cellData.getValue().getItemId();
                Item item = itemService.readItem(itemId);
                String itemName = item != null ? item.getTitle() : "Unknown Item";
                return new SimpleStringProperty(itemName);
            });
        }

        if (auctionAmountColumn != null) {
            auctionAmountColumn.setCellValueFactory(cellData -> {
                BigDecimal price = cellData.getValue().getFinalSalePrice();
                return new SimpleStringProperty(formatMoney(price));
            });
        }

        if (auctionStatusColumn != null) {
            auctionStatusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus().name())
            );
        }

        auctionsTable.setItems(auctionsList);
    }

    private void setupLegacyAuctionTableNavigation() {
        auctionsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1
                && auctionsTable.getSelectionModel().getSelectedItem() != null) {

                Auction selectedAuction =
                    auctionsTable.getSelectionModel().getSelectedItem();

                Pane contentArea =
                    (Pane) auctionsTable.getScene().lookup("#contentArea");

                NavigationService.goToAuctionDetails(contentArea, selectedAuction);
            }
        });
    }

    // =========================================================
    // Shared Helpers
    // =========================================================
    private Label createStatusBadge(String text) {
        Label badge = new Label(text);
        badge.getStyleClass().add("status-badge");
        return badge;
    }

    private void applyPaymentStatusStyle(Label badge, String statusText) {
        if (statusText == null || statusText.equals("N/A")) {
            badge.getStyleClass().add("payment-unknown");
            return;
        }

        switch (statusText) {
            case "COMPLETED":
                badge.getStyleClass().add("payment-completed");
                break;
            case "PENDING":
                badge.getStyleClass().add("payment-pending");
                break;
            case "FAILED":
                badge.getStyleClass().add("payment-failed");
                break;
            default:
                badge.getStyleClass().add("payment-unknown");
                break;
        }
    }

    private void applyAuctionStatusStyle(Label badge, String statusText) {
        if (statusText == null || statusText.equals("N/A")) {
            badge.getStyleClass().add("auction-ended");
            return;
        }

        switch (statusText) {
            case "SOLD":
                badge.getStyleClass().add("auction-sold");
                break;
            case "CANCELLED":
                badge.getStyleClass().add("auction-cancelled");
                break;
            default:
                badge.getStyleClass().add("auction-ended");
                break;
        }
    }

    private void makeTableResponsive(TableView<?> table, String placeholderText) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label(placeholderText));
        table.setFixedCellSize(52);

        table.prefHeightProperty().bind(
            Bindings.size(table.getItems())
                .multiply(table.getFixedCellSize())
                .add(58)
        );

        table.setMinHeight(220);
        table.setMaxHeight(Region.USE_COMPUTED_SIZE);
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime == null ? "N/A" : dateTime.format(formatter);
    }

    private String formatMoney(BigDecimal amount) {
        return amount == null ? "N/A" : "$" + amount.toPlainString();
    }

    private String formatText(String value) {
        return value == null || value.isBlank() ? "N/A" : value;
    }
}