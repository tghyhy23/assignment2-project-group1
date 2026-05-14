package com.group01.asm2.controllers;

import com.group01.asm2.dtos.AuctionFilter;
import com.group01.asm2.enums.AuctionStatus;
import com.group01.asm2.enums.ItemCondition;
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
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
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
    private AuctionFilter currentFilter = new AuctionFilter();

    @FXML private TableView<Auction> auctionsTable;
    @FXML private HBox paginationBox;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Label pageInfoLabel;

    @FXML private TableColumn<Auction, String> auctionDateColumn;
    @FXML private TableColumn<Auction, String> auctionTypeColumn;
    @FXML private TableColumn<Auction, String> auctionItemColumn;
    @FXML private TableColumn<Auction, String> auctionAmountColumn;
    @FXML private TableColumn<Auction, String> auctionStatusColumn;

    @FXML private FlowPane allAuctionsContainer;
    @FXML private StackPane allAuctionsRoot;
    @FXML private Label browseTitleLabel;

    // Filter modal fields
    @FXML private StackPane filterModalOverlay;
    @FXML private ComboBox<Integer> categoryFilterBox;
    @FXML private ComboBox<ItemCondition> conditionFilterBox;
    @FXML private TextField minPriceField;
    @FXML private TextField maxPriceField;
    @FXML private CheckBox endingSoonCheck;

    private final ObservableList<Auction> auctionsList = FXCollections.observableArrayList();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private boolean profileMode = false;
    private Integer profileSellerId = null;

    @FXML
    public void initialize() {
        setupDefaultFilter();
        setupFilterForm();

        if (auctionsTable != null) {
            auctionsList.setAll(auctionService.readAuctions(currentFilter));

            setupAuctionTable();
            makeTableResponsive(auctionsTable);

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

        if (allAuctionsContainer != null) {
            loadAllAuctionsToExplore();
        }
    }

    public void enableProfileMode(Integer sellerId) {
        this.profileMode = true;
        this.profileSellerId = sellerId;

        if (browseTitleLabel != null) {
            browseTitleLabel.setVisible(false);
            browseTitleLabel.setManaged(false);
        }

        if (paginationBox != null) {
            paginationBox.setVisible(false);
            paginationBox.setManaged(false);
        }

        if (allAuctionsRoot != null) {
            allAuctionsRoot.setMinHeight(360);
            allAuctionsRoot.setPrefHeight(360);
        }

        setupDefaultFilter();

        currentFilter.setSellerId(sellerId);
        currentFilter.setRecommendedOnly(false);

        loadAllAuctionsToExplore();
    }

    private void setupDefaultFilter() {
        currentFilter = new AuctionFilter();
        currentFilter.setRecommendedOnly(true);
        currentFilter.setStatus(AuctionStatus.ACTIVE);

        if (profileMode) {
            currentFilter.setSellerId(profileSellerId);
            currentFilter.setRecommendedOnly(false);
        } else {
            currentFilter.setRecommendedOnly(true);
        }
    }

    private void setupFilterForm() {
        if (categoryFilterBox != null) {
            // Tạm thời hard-code category ID
            categoryFilterBox.getItems().setAll(1, 2, 3, 4);
        }

        if (conditionFilterBox != null) {
            conditionFilterBox.getItems().setAll(ItemCondition.values());
        }
    }

    @FXML
    private void handleOpenFilterForm() {
        if (filterModalOverlay == null) return;

        filterModalOverlay.setVisible(true);
        filterModalOverlay.setManaged(true);

    }

    @FXML
    private void handleCloseFilterForm() {
        if (filterModalOverlay == null) return;

        filterModalOverlay.setVisible(false);
        filterModalOverlay.setManaged(false);
    }

    @FXML
    private void handleApplyFilters() {
        AuctionFilter filter = new AuctionFilter();

        filter.setRecommendedOnly(true);
        filter.setStatus(AuctionStatus.ACTIVE);

        if (categoryFilterBox != null && categoryFilterBox.getValue() != null) {
            filter.setCategoryId(categoryFilterBox.getValue());
        }

        if (conditionFilterBox != null && conditionFilterBox.getValue() != null) {
            filter.setCondition(conditionFilterBox.getValue());
        }

        BigDecimal minPrice = parsePrice(minPriceField);
        BigDecimal maxPrice = parsePrice(maxPriceField);

        if (minPrice != null) {
            filter.setMinPrice(minPrice);
        }

        if (maxPrice != null) {
            filter.setMaxPrice(maxPrice);
        }

        if (endingSoonCheck != null && endingSoonCheck.isSelected()) {
            filter.setEndingAfter(LocalDateTime.now());
            filter.setEndingBefore(LocalDateTime.now().plusHours(24));
        }

        currentFilter = filter;

        loadAllAuctionsToExplore();
        handleCloseFilterForm();
    }

    @FXML
    private void handleResetFilters() {
        if (categoryFilterBox != null) {
            categoryFilterBox.setValue(null);
        }

        if (conditionFilterBox != null) {
            conditionFilterBox.setValue(null);
        }

        if (minPriceField != null) {
            minPriceField.clear();
        }

        if (maxPriceField != null) {
            maxPriceField.clear();
        }

        if (endingSoonCheck != null) {
            endingSoonCheck.setSelected(false);
        }

        setupDefaultFilter();
        loadAllAuctionsToExplore();
    }

    private BigDecimal parsePrice(TextField textField) {
        if (textField == null) {
            return null;
        }

        String value = textField.getText();

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void loadAllAuctionsToExplore() {
        allAuctions = auctionService.readAuctions(currentFilter);
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
        if (allAuctions == null || allAuctions.isEmpty()) {
            return;
        }

        int totalPages = (int) Math.ceil((double) allAuctions.size() / ITEMS_PER_PAGE);

        if (currentPage < totalPages - 1) {
            currentPage++;
            renderCurrentPage();
        }
    }

    private VBox createAuctionActionMenu(Auction auction) {
        VBox menu = new VBox(2);
        menu.setFillWidth(true);
        menu.setMaxWidth(Region.USE_PREF_SIZE);
        menu.setMaxHeight(Region.USE_PREF_SIZE);
        menu.getStyleClass().add("item-action-menu");

        Button editButton = new Button("Edit");
        editButton.getStyleClass().add("item-action-menu-button");

        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().addAll(
                "item-action-menu-button",
                "item-action-menu-delete"
        );

        editButton.setOnAction(event -> {
            event.consume();

            // TODO: mở modal edit auction/item
            System.out.println("Edit auction: " + auction.getId());

            menu.setVisible(false);
            menu.setManaged(false);
        });

        deleteButton.setOnAction(event -> {
            event.consume();

            handleDeleteAuction(auction);

            menu.setVisible(false);
            menu.setManaged(false);
        });

        menu.getChildren().addAll(editButton, deleteButton);

        return menu;
    }

    private void handleDeleteAuction(Auction auction) {
        if (auction == null || auction.getId() == null) return;

        auctionService.deleteAuction(auction.getId());

        allAuctions.remove(auction);
        renderCurrentPage();
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

        StackPane imageWrapper = new StackPane();
        imageWrapper.getChildren().add(imageBox);

        if (profileMode) {
            Button moreButton = new Button("...");
            moreButton.getStyleClass().add("item-more-button");

            VBox actionMenu = createAuctionActionMenu(auction);
            actionMenu.setVisible(false);
            actionMenu.setManaged(false);

            moreButton.setOnAction(event -> {
                event.consume();

                boolean showing = actionMenu.isVisible();
                actionMenu.setVisible(!showing);
                actionMenu.setManaged(!showing);
            });

            imageWrapper.setOnMouseExited(event -> {
                actionMenu.setVisible(false);
                actionMenu.setManaged(false);
            });

            StackPane.setAlignment(moreButton, javafx.geometry.Pos.TOP_RIGHT);
            StackPane.setMargin(moreButton, new Insets(12, 12, 0, 0));

            StackPane.setAlignment(actionMenu, javafx.geometry.Pos.TOP_RIGHT);
            StackPane.setMargin(actionMenu, new Insets(54, 12, 0, 0));

            imageWrapper.getChildren().addAll(moreButton, actionMenu);
        }
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

        VBox contentBox = new VBox(
                nameLabel,
                bidSection,
                statusLabel,
                dateLabel
        );

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

    private void setupAuctionTable() {
        auctionDateColumn.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getEndDateTime();
            return new SimpleStringProperty(date != null ? date.format(formatter) : "N/A");
        });

        auctionTypeColumn.setCellValueFactory(cellData -> {
            boolean hasWinner = cellData.getValue().hasWinner();
            return new SimpleStringProperty(hasWinner ? "Purchase" : "Sale");
        });

        auctionItemColumn.setCellValueFactory(cellData -> {
            Integer itemId = cellData.getValue().getItemId();
            Item item = itemService.readItem(itemId);
            String itemName = (item != null) ? item.getTitle() : "Unknown Item";
            return new SimpleStringProperty(itemName);
        });

        auctionAmountColumn.setCellValueFactory(cellData -> {
            BigDecimal price = cellData.getValue().getFinalSalePrice();
            return new SimpleStringProperty(price != null ? "$" + price : "N/A");
        });

        auctionStatusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus().name())
        );

        auctionsTable.setItems(auctionsList);
    }

    private void makeTableResponsive(TableView<?> table) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No history found."));
        table.setFixedCellSize(48);
        table.prefHeightProperty().bind(
                Bindings.size(table.getItems())
                        .multiply(table.getFixedCellSize())
                        .add(52)
        );
        table.setMinHeight(Region.USE_PREF_SIZE);
        table.setMaxHeight(Region.USE_PREF_SIZE);
    }
}