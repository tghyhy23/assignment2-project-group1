package com.group01.asm2.controllers;

/**
 * @author Group 01
 */

import com.group01.asm2.dtos.AuctionFilter;
import com.group01.asm2.dtos.WonAuctionDto;
import com.group01.asm2.enums.AuctionStatus;
import com.group01.asm2.enums.ItemCondition;
import com.group01.asm2.enums.PaymentStatus;
import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.Auction;
import com.group01.asm2.models.Item;
import com.group01.asm2.services.AuctionService;
import com.group01.asm2.services.ItemService;
import com.group01.asm2.services.NavigationService;
import com.group01.asm2.models.ItemImage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

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

    private boolean profileMode = false;
    private Integer profileSellerId = null;
    private boolean canManageProfileListings = false;

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
    @FXML private Button filterButton;

    @FXML private StackPane filterModalOverlay;
    @FXML private ComboBox<Integer> categoryFilterBox;
    @FXML private ComboBox<ItemCondition> conditionFilterBox;
    @FXML private TextField minPriceField;
    @FXML private TextField maxPriceField;
    @FXML private CheckBox endingSoonCheck;

    private final ObservableList<Auction> auctionsList = FXCollections.observableArrayList();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");

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

    private final ObservableList<WonAuctionDto> wonAuctionsList =
        FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupDefaultFilter();
        setupFilterForm();

        if (auctionsTable != null) {
            loadLegacyAuctionTable();
            setupLegacyAuctionTable();
            setupLegacyAuctionTableNavigation();
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

        if (wonAuctionsTable != null) {
            setupWonAuctionTable();
            loadWonAuctions();
            updateWonAuctionSummaryCards();
            makeTableResponsive(wonAuctionsTable);
        }
    }

    public void enableExploreMode() {
        this.profileMode = false;
        this.profileSellerId = null;
        this.canManageProfileListings = false;

        if (browseTitleLabel != null) {
            browseTitleLabel.setVisible(true);
            browseTitleLabel.setManaged(true);
        }

        if (paginationBox != null) {
            paginationBox.setVisible(true);
            paginationBox.setManaged(true);
        }

        setupDefaultFilter();
        loadAllAuctionsToExplore();
    }

    public void enableProfileMode(Integer sellerId, boolean canManageProfileListings) {
        this.profileMode = true;
        this.profileSellerId = sellerId;
        this.canManageProfileListings = canManageProfileListings;

        if (browseTitleLabel != null) {
            browseTitleLabel.setVisible(false);
            browseTitleLabel.setManaged(false);
        }

        if (paginationBox != null) {
            paginationBox.setVisible(false);
            paginationBox.setManaged(false);
        }

        if (filterButton != null) {
            filterButton.setVisible(true);
            filterButton.setManaged(true);
        }

        if (allAuctionsRoot != null) {
            allAuctionsRoot.setMinHeight(360);
            allAuctionsRoot.setPrefHeight(360);
        }

        setupDefaultFilter();
        loadAllAuctionsToExplore();
    }

    public void applyProfileCategoryFilter(Integer categoryId) {
        if (!profileMode) {
            return;
        }

        setupDefaultFilter();

        if (categoryId != null) {
            currentFilter.setCategoryId(categoryId);
        }

        loadAllAuctionsToExplore();
    }

    private void setupDefaultFilter() {
        currentFilter = new AuctionFilter();
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
            categoryFilterBox.getItems().setAll(101, 102, 103, 104, 105, 106);
        }

        if (conditionFilterBox != null) {
            conditionFilterBox.getItems().setAll(ItemCondition.values());
        }
    }

    @FXML
    private void handleOpenFilterForm() {
        if (filterModalOverlay == null) {
            return;
        }

        filterModalOverlay.setVisible(true);
        filterModalOverlay.setManaged(true);
    }

    @FXML
    private void handleCloseFilterForm() {
        if (filterModalOverlay == null) {
            return;
        }

        filterModalOverlay.setVisible(false);
        filterModalOverlay.setManaged(false);
    }

    @FXML
    private void handleApplyFilters() {
        AuctionFilter filter = new AuctionFilter();
        filter.setStatus(AuctionStatus.ACTIVE);

        if (profileMode) {
            filter.setSellerId(profileSellerId);
            filter.setRecommendedOnly(false);
        } else {
            filter.setRecommendedOnly(true);
        }

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
        try {
            allAuctions = auctionService.readAuctions(currentFilter);
            currentPage = 0;
            renderCurrentPage();

        } catch (AppException exception) {
            allAuctions = List.of();
            renderCurrentPage();
            showError(exception.getMessage());

        } catch (Exception exception) {
            exception.printStackTrace();
            allAuctions = List.of();
            renderCurrentPage();
            showError("Could not load auctions.");
        }
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

            allAuctionsContainer.getChildren().add(new Label("No auctions found."));
            return;
        }

        int totalPages = (int) Math.ceil((double) allAuctions.size() / ITEMS_PER_PAGE);

        int startIndex;
        int endIndex;

        if (profileMode) {
            startIndex = 0;
            endIndex = allAuctions.size();
        } else {
            startIndex = currentPage * ITEMS_PER_PAGE;
            endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allAuctions.size());
        }

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

            // TODO: connect edit listing modal later
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
        if (auction == null || auction.getItemId() == null) {
            return;
        }

        try {
            itemService.deleteItem(auction.getItemId());
            loadAllAuctionsToExplore();

        } catch (AppException exception) {
            showError(exception.getMessage());
        } catch (Exception exception) {
            exception.printStackTrace();
            showError("Could not delete listing.");
        }
    }

    private VBox createAuctionCard(Auction auction) {
        Item item = null;

        try {
            item = itemService.readItem(auction.getItemId());
        } catch (Exception ignored) {
        }

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

        String imageUrl = resolvePrimaryImageUrl(item);

        if (imageUrl != null) {
            ImageView imageView = new ImageView();

            Image image = new Image(imageUrl, true);

            imageView.setImage(image);
            imageView.setFitWidth(230);
            imageView.setFitHeight(160);
            imageView.setPreserveRatio(false);
            imageView.setSmooth(true);

            imageView.getStyleClass().add("auction-image");

            imageBox.getChildren().add(imageView);
        } else {
            Label noImageLabel = new Label("No image");
            noImageLabel.getStyleClass().add("auction-image-empty-label");
            imageBox.getChildren().add(noImageLabel);
        }

        StackPane imageWrapper = new StackPane();
        imageWrapper.getChildren().add(imageBox);

        if (profileMode && canManageProfileListings) {
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

        Label statusLabel = new Label(
            auction.getStatus() == null ? "N/A" : auction.getStatus().name()
        );
        statusLabel.getStyleClass().add("auction-status");

        if (auction.getStatus() == AuctionStatus.ACTIVE) {
            statusLabel.getStyleClass().add("status-active");
        } else if (auction.getStatus() == AuctionStatus.SOLD) {
            statusLabel.getStyleClass().add("status-sold");
        } else {
            statusLabel.getStyleClass().add("status-ended");
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

        VBox card = new VBox(imageWrapper, contentBox);

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
            if (event.isConsumed()) {
                return;
            }

            Pane contentArea = (Pane) card.getScene().lookup("#contentArea");
            NavigationService.goToAuctionDetails(contentArea, auction);
        });

        return card;
    }

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

    private void loadWonAuctions() {
        try {
            wonAuctionsList.clear();

            wonAuctionsList.addAll(
                auctionService.readWonAuctionsForCurrentUser()
            );

            if (wonAuctionsTable != null) {
                wonAuctionsTable.setPlaceholder(
                    new Label("No won auctions found.")
                );
            }

        } catch (AppException exception) {
            wonAuctionsList.clear();

            if (wonAuctionsTable != null) {
                wonAuctionsTable.setPlaceholder(
                    new Label(exception.getMessage())
                );
            }

            if (exportPurchaseSummaryButton != null) {
                exportPurchaseSummaryButton.setDisable(true);
            }

        } catch (Exception exception) {
            exception.printStackTrace();

            wonAuctionsList.clear();

            if (wonAuctionsTable != null) {
                wonAuctionsTable.setPlaceholder(
                    new Label("Could not load your won auctions.")
                );
            }

            if (exportPurchaseSummaryButton != null) {
                exportPurchaseSummaryButton.setDisable(true);
            }
        }
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
            case "COMPLETED" -> badge.getStyleClass().add("payment-completed");
            case "PENDING" -> badge.getStyleClass().add("payment-pending");
            case "FAILED" -> badge.getStyleClass().add("payment-failed");
            default -> badge.getStyleClass().add("payment-unknown");
        }
    }

    private void applyAuctionStatusStyle(Label badge, String statusText) {
        if (statusText == null || statusText.equals("N/A")) {
            badge.getStyleClass().add("auction-ended");
            return;
        }

        switch (statusText) {
            case "SOLD" -> badge.getStyleClass().add("auction-sold");
            case "CANCELLED" -> badge.getStyleClass().add("auction-cancelled");
            default -> badge.getStyleClass().add("auction-ended");
        }
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

    private String formatDate(LocalDateTime dateTime) {
        return dateTime == null ? "N/A" : dateTime.format(formatter);
    }

    private String formatMoney(BigDecimal amount) {
        return amount == null ? "N/A" : "$" + amount.toPlainString();
    }

    private String formatText(String value) {
        return value == null || value.isBlank() ? "N/A" : value;
    }

    private String resolvePrimaryImageUrl(Item item) {
        if (item == null) {
            return null;
        }

        if (item.getImages() == null || item.getImages().isEmpty()) {
            return null;
        }

        return item.getImages().stream()
            .filter(image -> image != null && image.getImageUrl() != null && !image.getImageUrl().isBlank())
            .sorted((first, second) -> Integer.compare(first.getDisplayOrder(), second.getDisplayOrder()))
            .map(ItemImage::getImageUrl)
            .findFirst()
            .orElse(null);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Auction Error");
        alert.setHeaderText(null);
        alert.setContentText(
            message == null || message.trim().isEmpty()
                ? "Something went wrong."
                : message
        );
        alert.showAndWait();
    }
}