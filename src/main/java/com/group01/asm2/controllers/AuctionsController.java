package com.group01.asm2.controllers;

import com.group01.asm2.dtos.AuctionFilter;
import com.group01.asm2.enums.AuctionStatus;
import com.group01.asm2.models.Auction;
import com.group01.asm2.models.Item;
import javafx.scene.layout.HBox;
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
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import java.math.BigDecimal;
import javafx.scene.layout.StackPane;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import java.util.List;

public class AuctionsController {
    private AuctionService auctionService = new AuctionService();
    private ItemService itemService = new ItemService();
    private static final int ITEMS_PER_PAGE = 20;
    private int currentPage = 0;
    private List<Auction> allAuctions;

    @FXML private TableView<Auction> auctionsTable;
    @FXML private HBox paginationBox;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Label pageInfoLabel;
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
        AuctionFilter filter = new AuctionFilter();
        filter.setRecommendedOnly(true);
        filter.setStatus(AuctionStatus.ACTIVE);

        auctionsList.addAll(auctionService.readAuctions(filter));

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
        AuctionFilter filter = new AuctionFilter();
        filter.setRecommendedOnly(true);
        filter.setStatus(AuctionStatus.ACTIVE);

        allAuctions = auctionService.readAuctions(filter);
        currentPage = 0;

        renderCurrentPage();
    }

    private void renderCurrentPage() {
        allAuctionsContainer.getChildren().clear();

        if (allAuctions == null || allAuctions.isEmpty()) {
            pageInfoLabel.setText("Page 0 / 0");
            prevPageButton.setDisable(true);
            nextPageButton.setDisable(true);
            return;
        }

        int totalPages = (int) Math.ceil((double) allAuctions.size() / ITEMS_PER_PAGE);

        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allAuctions.size());

        for (int i = startIndex; i < endIndex; i++) {
            VBox card = createAuctionCard(allAuctions.get(i));
            allAuctionsContainer.getChildren().add(card);
        }

        pageInfoLabel.setText("Page " + (currentPage + 1) + " / " + totalPages);

        prevPageButton.setDisable(currentPage == 0);
        nextPageButton.setDisable(currentPage >= totalPages - 1);
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
        int totalPages = (int) Math.ceil((double) allAuctions.size() / ITEMS_PER_PAGE);

        if (currentPage < totalPages - 1) {
            currentPage++;
            renderCurrentPage();
        }
    }

    //    Create AuctionCard
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

        VBox leftPriceBox = new VBox(
                startingPriceTitle,
                startingPriceValue
        );

        leftPriceBox.setSpacing(2);

        VBox rightPriceBox = new VBox(
                currentBidTitle,
                currentBidValue
        );

        rightPriceBox.setSpacing(2);

        HBox bidSection = new HBox(
                leftPriceBox,
                rightPriceBox
        );

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

        VBox contentBox = new VBox(
                nameLabel,
                bidSection,
                statusLabel,
                dateLabel
        );

        contentBox.setSpacing(8);
        contentBox.setStyle("-fx-padding: 12;");

        VBox card = new VBox(
                imageBox,
                contentBox
        );

        card.setSpacing(8);
        card.setFillWidth(true);
        card.getStyleClass().add("auction-card");

        TranslateTransition cardMove = new TranslateTransition(Duration.seconds(0.18), card);

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
            Item item = itemService.readItem(itemId);
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