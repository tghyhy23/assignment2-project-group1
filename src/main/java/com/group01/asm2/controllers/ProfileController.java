package com.group01.asm2.controllers;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Popup;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProfileController {

    // =========================
    // Profile Header
    // =========================

    @FXML private Label avatarLabel;
    @FXML private Label statusLabel;
    @FXML private Label fullNameLabel;
    @FXML private Label roleLabel;
    @FXML private Label emailLabel;
    @FXML private Label ratingLabel;
    @FXML private Label phoneLabel;
    @FXML private Label dateOfBirthLabel;
    @FXML private Label addressLabel;
    @FXML private Label joinedDateLabel;
    @FXML private StackPane editProfileOverlay;
    @FXML private Label modalAvatarLabel;
    @FXML private Label modalNamePreviewLabel;
    @FXML private Label modalEmailPreviewLabel;
    @FXML private TextField editFullNameField;
    @FXML private TextField editEmailField;
    @FXML private TextField editPhoneField;
    @FXML private TextField editDateOfBirthField;
    @FXML private TextField editAddressField;
    @FXML private TextField editRoleField;
    @FXML private VBox editProfileModal;
    @FXML private Button editProfileButton;
    @FXML private ScrollPane editProfileModalScrollPane;

    // =========================
    // Summary Labels
    // =========================

    @FXML private Label balanceLabel;
    @FXML private Label totalBidsLabel;
    @FXML private Label wonAuctionsLabel;
    @FXML private Label activeListingsLabel;
    @FXML private Label summaryRatingLabel;

    // =========================
    // Wallet
    // =========================

    @FXML private Label selectedTabTitleLabel;
    @FXML private Label walletBalanceLabel;
    @FXML private Label topUpStatusLabel;
    @FXML private Label recentAuctionStatusLabel;

    // =========================
    // Tabs
    // =========================

    @FXML private TabPane profileTabPane;

    // =========================
    // Listings Table
    // =========================
    @FXML private TextField listingSearchField;
    @FXML private TilePane listingsCardContainer;
    @FXML private Button categoryAllButton;
    @FXML private Button categoryElectronicsButton;
    @FXML private Button categoryFashionButton;
    @FXML private Button categoryCollectiblesButton;
    @FXML private Button categoryHomeButton;
    @FXML private Button categoryBooksButton;
    @FXML private Button categoryOtherButton;
    private String selectedListingCategory = "All";
    @FXML private VBox addListingModal;

    // =========================
    // Bids Table
    // =========================

    @FXML private TableView<Bid> bidsTable;
    @FXML private TableColumn<Bid, String> bidItemNameColumn;
    @FXML private TableColumn<Bid, String> bidMyAmountColumn;
    @FXML private TableColumn<Bid, String> bidHighestAmountColumn;
    @FXML private TableColumn<Bid, String> bidStatusColumn;
    @FXML private TableColumn<Bid, String> bidDateColumn;

    // =========================
    // Watchlist Table
    // =========================

    @FXML private TableView<WatchlistItem> watchlistTable;
    @FXML private TableColumn<WatchlistItem, String> watchItemNameColumn;
    @FXML private TableColumn<WatchlistItem, String> watchCurrentPriceColumn;
    @FXML private TableColumn<WatchlistItem, String> watchTimeRemainingColumn;
    @FXML private TableColumn<WatchlistItem, String> watchStatusColumn;
    @FXML private TableColumn<WatchlistItem, Void> watchActionColumn;

    // =========================
    // Transactions Table
    // =========================

    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, String> transactionDateColumn;
    @FXML private TableColumn<Transaction, String> transactionTypeColumn;
    @FXML private TableColumn<Transaction, String> transactionItemColumn;
    @FXML private TableColumn<Transaction, String> transactionAmountColumn;
    @FXML private TableColumn<Transaction, String> transactionStatusColumn;

    // =========================
    // Activity Table
    // =========================
    @FXML private TableView<ActivityLog> activityTable;
    @FXML private TableColumn<ActivityLog, String> activityDateTimeColumn;
    @FXML private TableColumn<ActivityLog, String> activityActionColumn;
    @FXML private TableColumn<ActivityLog, String> activityDescriptionColumn;

    // =========================
    // Add Item Overlay
    // =========================
    @FXML private StackPane addListingOverlay;

    @FXML private TextField addListingNameField;
    @FXML private ComboBox<String> addListingCategoryComboBox;
    @FXML private TextField addListingStartingPriceField;
    @FXML private TextField addListingCurrentBidField;
    @FXML private ComboBox<String> addListingStatusComboBox;
    @FXML private TextField addListingIconField;
    @FXML private Label addListingErrorLabel;

    @FXML private HBox summaryRow;
    @FXML private HBox walletPanelRow;

    // =========================
    // Mock User Data
    // =========================

    private String fullName = "Sophia Bennett";
    private String email = "sophia.bennett@bidblitz.com";
    private String role = "User";
    private String phone = "+84 912 345 678";
    private String dateOfBirth = "15 March 2003";
    private String address = "District 7, Ho Chi Minh City";
    private String joinedDate = "12 January 2025";
    private String rating = "4.8 / 5.0";
    private double balance = 1250.75;

    private final ObservableList<Listing> listings = FXCollections.observableArrayList();
    private final ObservableList<Bid> bids = FXCollections.observableArrayList();
    private final ObservableList<WatchlistItem> watchlist = FXCollections.observableArrayList();
    private final ObservableList<Transaction> transactions = FXCollections.observableArrayList();
    private final ObservableList<ActivityLog> activities = FXCollections.observableArrayList();

    private final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    @FXML
    public void initialize() {
        loadMockData();
        setupProfileHeader();
        setupTables();
        setupTabSwitching();
        setupResponsiveEditProfileButton();
        setupResponsiveEditProfileModal();

        fitTabPaneToSelectedContent();
        makeTableResponsive(bidsTable);
        makeTableResponsive(watchlistTable);
        makeTableResponsive(transactionsTable);
        makeTableResponsive(activityTable);
        setupListingsCards();

        setupAddListingModal();
        setupAddListingModalSize();
        setupResponsiveLayout();

        listingSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterListings();
        });
    }

    // =========================
    // Initial Setup
    // =========================

    private void loadMockData() {
        listings.addAll(
                new Listing("iPhone 13 Pro", "Electronics", 450.00, 620.00, "Active", "2026-05-01", "📱"),
                new Listing("Vintage Denim Jacket", "Fashion", 35.00, 58.00, "Active", "2026-05-02", "👕"),
                new Listing("Rare Pokémon Card", "Collectibles", 80.00, 145.00, "Pending", "2026-05-03", "🎴"),
                new Listing("Modern Desk Lamp", "Home", 20.00, 32.00, "Sold", "2026-05-04", "💡"),
                new Listing("Java Programming Book", "Books", 15.00, 24.00, "Closed", "2026-05-05", "📚"),
                new Listing("Wireless Keyboard", "Electronics", 25.00, 41.00, "Active", "2026-05-06", "⌨️"),
                new Listing("Handmade Tote Bag", "Fashion", 18.00, 29.00, "Pending", "2026-05-07", "👜"),
                new Listing("Mystery Auction Box", "Other", 10.00, 19.00, "Active", "2026-05-08", "📦")
        );

        bids.addAll(
                new Bid("Vintage Camera", "$300.00", "$310.00", "Outbid", "06 May 2026"),
                new Bid("Gaming Monitor", "$260.00", "$260.00", "Winning", "06 May 2026"),
                new Bid("Rare Sneaker Pair", "$190.00", "$220.00", "Lost", "01 May 2026"),
                new Bid("Smart Watch", "$180.00", "$180.00", "Won", "29 Apr 2026")
        );

        watchlist.addAll(
                new WatchlistItem("Antique Desk Lamp", "$85.00", "2h 15m", "Ending Soon"),
                new WatchlistItem("Gaming Chair", "$155.00", "1d 4h", "Active"),
                new WatchlistItem("Collectible Watch", "$490.00", "3d 8h", "Active")
        );

        transactions.addAll(
                new Transaction("06 May 2026", "Purchase", "Smart Watch", "$180.00", "Payment Processed"),
                new Transaction("04 May 2026", "Sale", "Bluetooth Speaker", "$95.00", "Completed"),
                new Transaction("01 May 2026", "Purchase", "Rare Sneaker Pair", "$220.00", "Lost Auction"),
                new Transaction("28 Apr 2026", "Sale", "Coffee Machine", "$160.00", "Completed")
        );

        activities.addAll(
                new ActivityLog("07 May 2026, 09:20", "Logged in", "User logged into BidBlitz successfully."),
                new ActivityLog("06 May 2026, 18:45", "Placed bid", "Placed a bid on Gaming Monitor."),
                new ActivityLog("06 May 2026, 17:10", "Added watchlist", "Added Antique Desk Lamp to watchlist."),
                new ActivityLog("05 May 2026, 14:30", "Requested top-up", "Requested a balance top-up of $500.00."),
                new ActivityLog("03 May 2026, 11:00", "Listed item", "Listed Mechanical Keyboard for auction."),
                new ActivityLog("02 May 2026, 20:15", "Updated profile", "Updated phone number and address.")
        );
    }

    private void setupProfileHeader() {
        fullNameLabel.setText(fullName);
        emailLabel.setText(email);
        roleLabel.setText(role);
        phoneLabel.setText(phone);
        dateOfBirthLabel.setText(dateOfBirth);
        addressLabel.setText(address);
        joinedDateLabel.setText(joinedDate);
        ratingLabel.setText("★ " + rating);

        statusLabel.setText("Verified User");

        updateAvatar();
    }

    private void updateAvatar() {
        if (fullName == null || fullName.trim().isEmpty()) {
            avatarLabel.setText("U");
            return;
        }

        String[] nameParts = fullName.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();

        for (String part : nameParts) {
            if (!part.isEmpty()) {
                initials.append(Character.toUpperCase(part.charAt(0)));
            }

            if (initials.length() == 2) {
                break;
            }
        }

        avatarLabel.setText(initials.toString());
    }

    private void setupResponsiveEditProfileButton() {
        editProfileButton.setMinWidth(Button.USE_PREF_SIZE);
        editProfileButton.setPrefWidth(Button.USE_COMPUTED_SIZE);
        editProfileButton.setMaxWidth(Double.MAX_VALUE);
    }

    private void setupResponsiveEditProfileModal() {
        editProfileModalScrollPane.maxWidthProperty().bind(
                editProfileOverlay.widthProperty().multiply(0.72)
        );

        editProfileModalScrollPane.maxHeightProperty().bind(
                editProfileOverlay.heightProperty().multiply(0.86)
        );

        editProfileModal.prefWidthProperty().bind(
                editProfileModalScrollPane.widthProperty().subtract(24)
        );

        editProfileModal.setMaxHeight(Region.USE_PREF_SIZE);
    }

    private void updateBalanceLabels() {
        String formattedBalance = String.format("$%,.2f", balance);
        balanceLabel.setText(formattedBalance);
        walletBalanceLabel.setText(formattedBalance);
    }

    private int countWonAuctions() {
        int count = 0;

        for (Bid bid : bids) {
            if ("Won".equalsIgnoreCase(bid.getStatus())) {
                count++;
            }
        }

        return count;
    }

    private int countActiveListings() {
        int count = 0;

        for (Listing listing : listings) {
            if ("Active".equalsIgnoreCase(listing.getStatus())) {
                count++;
            }
        }

        return count;
    }

    // =========================
    // Table Setup
    // =========================

    private void setupTables() {
        setupBidsTable();
        setupWatchlistTable();
        setupTransactionsTable();
        setupActivityTable();
    }

    private void fitTabPaneToSelectedContent() {
        profileTabPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) return;

            profileTabPane.applyCss();
            profileTabPane.layout();

            Runnable updateHeight = () -> {
                Tab selectedTab = profileTabPane.getSelectionModel().getSelectedItem();
                if (selectedTab == null || selectedTab.getContent() == null) return;

                Node content = selectedTab.getContent();

                content.applyCss();
                content.autosize();

                Node tabHeaderArea = profileTabPane.lookup(".tab-header-area");

                double headerHeight = tabHeaderArea == null
                        ? 0
                        : tabHeaderArea.getBoundsInParent().getHeight();

                double contentHeight = content.prefHeight(profileTabPane.getWidth());

                profileTabPane.setMinHeight(headerHeight + contentHeight);
                profileTabPane.setPrefHeight(headerHeight + contentHeight);
                profileTabPane.setMaxHeight(headerHeight + contentHeight);
            };

            updateHeight.run();

            profileTabPane.getSelectionModel()
                    .selectedItemProperty()
                    .addListener((tabObs, oldTab, newTab) -> updateHeight.run());

            profileTabPane.widthProperty()
                    .addListener((widthObs, oldWidth, newWidth) -> updateHeight.run());
        });
    }

    private void makeTableResponsive(TableView<?> table) {
        // Make columns fill 100% of available table width
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Remove ugly empty placeholder text
        table.setPlaceholder(new Label(""));

        // Let table height depend on real row count, not screen height
        table.setFixedCellSize(48);

        table.prefHeightProperty().bind(
                Bindings.size(table.getItems())
                        .multiply(table.getFixedCellSize())
                        .add(52)
        );

        table.setMinHeight(Region.USE_PREF_SIZE);
        table.setMaxHeight(Region.USE_PREF_SIZE);
    }

    private void setupBidsTable() {
        bidItemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        bidMyAmountColumn.setCellValueFactory(new PropertyValueFactory<>("myBidAmount"));
        bidHighestAmountColumn.setCellValueFactory(new PropertyValueFactory<>("currentHighestBid"));
        bidStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        bidDateColumn.setCellValueFactory(new PropertyValueFactory<>("bidDate"));

        bidsTable.setItems(bids);
    }

    private void setupWatchlistTable() {
        watchItemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        watchCurrentPriceColumn.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        watchTimeRemainingColumn.setCellValueFactory(new PropertyValueFactory<>("timeRemaining"));
        watchStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        addRemoveButtonToWatchlistTable();

        watchlistTable.setItems(watchlist);
    }

    private void addRemoveButtonToWatchlistTable() {
        watchActionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button removeButton = new Button("Remove");

            {
                removeButton.getStyleClass().add("danger-small-button");

                removeButton.setOnAction(event -> {
                    WatchlistItem item = getTableView().getItems().get(getIndex());
                    watchlist.remove(item);

                    addActivity("Removed watchlist",
                            "Removed " + item.getItemName() + " from watchlist.");
                });
            }

            @Override
            protected void updateItem(Void value, boolean empty) {
                super.updateItem(value, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeButton);
                }
            }
        });
    }

    private void setupTransactionsTable() {
        transactionDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        transactionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        transactionItemColumn.setCellValueFactory(new PropertyValueFactory<>("item"));
        transactionAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        transactionStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        transactionsTable.setItems(transactions);
    }

    private void setupActivityTable() {
        activityDateTimeColumn.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
        activityActionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        activityDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        activityTable.setItems(activities);
    }

    // =========================
    // Tab Handling
    // =========================

    private void setupTabSwitching() {
        profileTabPane.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldTab, newTab) -> {
                    if (newTab != null) {
                        selectedTabTitleLabel.setText(newTab.getText());
                    }
                });
    }

    @FXML
    private void handleGoToTransactions() {
        profileTabPane.getSelectionModel().select(4);
    }

    @FXML
    private void handleGoToWatchlist() {
        profileTabPane.getSelectionModel().select(3);
    }

    // =========================
    // Edit Profile Modal
    // =========================

    @FXML
    private void handleOpenEditProfile() {
        editFullNameField.setText(fullName);
        editEmailField.setText(email);
        editPhoneField.setText(phone);
        editDateOfBirthField.setText(dateOfBirth);
        editAddressField.setText(address);
        editRoleField.setText(role);

        modalNamePreviewLabel.setText(fullName);
        modalEmailPreviewLabel.setText(email);
        modalAvatarLabel.setText(createInitials(fullName));

        editProfileOverlay.setVisible(true);
        editProfileOverlay.setManaged(true);
    }

    @FXML
    private void handleCloseEditProfile() {
        editProfileOverlay.setVisible(false);
        editProfileOverlay.setManaged(false);
    }

    @FXML
    private void handleSaveEditProfile() {
        fullName = editFullNameField.getText().trim();
        email = editEmailField.getText().trim();
        phone = editPhoneField.getText().trim();
        dateOfBirth = editDateOfBirthField.getText().trim();
        address = editAddressField.getText().trim();

        setupProfileHeader();

        handleCloseEditProfile();
    }

    private String createInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "U";
        }

        String[] parts = name.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) {
                initials.append(Character.toUpperCase(part.charAt(0)));
            }

            if (initials.length() == 2) {
                break;
            }
        }

        return initials.toString();
    }

    // =========================
    // Top-up Modal
    // =========================

    @FXML
    private void handleOpenTopUp() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Request Top-up");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStyleClass().add("profile-dialog");
        dialogPane.getStylesheets().add(
                getClass().getResource("/com/group01/asm2/styles/profile.css").toExternalForm()
        );

        ButtonType submitButtonType = new ButtonType("Submit Request", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialogPane.getButtonTypes().addAll(submitButtonType, cancelButtonType);

        TextField amountField = createTextField("");
        amountField.setPromptText("Enter amount between 10 and 5000");

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");

        VBox content = new VBox(12);
        content.getStyleClass().add("dialog-card");
        content.setPadding(new Insets(20));

        Label title = new Label("Request Account Top-up");
        title.getStyleClass().add("dialog-title");

        Label subtitle = new Label(
                "Enter the amount you want to add. The request will be sent to an administrator for approval. " +
                        "Your balance will not change until the request is approved."
        );
        subtitle.getStyleClass().add("dialog-subtitle");

        content.getChildren().addAll(
                title,
                subtitle,
                createFieldGroup("Top-up Amount", amountField),
                errorLabel
        );

        dialogPane.setContent(content);

        Node submitButton = dialogPane.lookupButton(submitButtonType);
        submitButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                double amount = Double.parseDouble(amountField.getText().trim());

                if (amount < 10 || amount > 5000) {
                    errorLabel.setText("Amount must be between $10 and $5000.");
                    event.consume();
                    return;
                }

                /*
                 * Important:
                 * Do not update the balance here.
                 * A top-up request requires administrator approval first.
                 * The balance should only be updated after admin approves the request.
                 */

                topUpStatusLabel.setText(String.format("Pending approval: $%,.2f", amount));
                topUpStatusLabel.getStyleClass().removeAll("status-neutral", "status-success");
                topUpStatusLabel.getStyleClass().add("status-warning");

                addActivity("Requested top-up",
                        String.format("Requested a top-up of $%,.2f. Waiting for admin approval.", amount));

                dialog.close();

            } catch (NumberFormatException exception) {
                errorLabel.setText("Please enter a valid number.");
                event.consume();
            }
        });

        dialog.showAndWait();
    }

    // =========================
    // Helper Methods
    // =========================

    private TextField createTextField(String value) {
        TextField textField = new TextField(value);
        textField.getStyleClass().add("profile-text-field");
        return textField;
    }

    private VBox createFieldGroup(String labelText, TextField textField) {
        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");

        VBox group = new VBox(6);
        group.getChildren().addAll(label, textField);

        return group;
    }

    private VBox createComboFieldGroup(String labelText, ComboBox<String> comboBox) {
        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");

        VBox group = new VBox(6);
        group.getChildren().addAll(label, comboBox);

        return group;
    }

    private void addActivity(String action, String description) {
        String now = LocalDateTime.now().format(dateTimeFormatter);
        activities.add(0, new ActivityLog(now, action, description));
    }

    // =========================
    // Model Classes
    // =========================

    public static class Bid {
        private final SimpleStringProperty itemName;
        private final SimpleStringProperty myBidAmount;
        private final SimpleStringProperty currentHighestBid;
        private final SimpleStringProperty status;
        private final SimpleStringProperty bidDate;

        public Bid(String itemName, String myBidAmount, String currentHighestBid, String status, String bidDate) {
            this.itemName = new SimpleStringProperty(itemName);
            this.myBidAmount = new SimpleStringProperty(myBidAmount);
            this.currentHighestBid = new SimpleStringProperty(currentHighestBid);
            this.status = new SimpleStringProperty(status);
            this.bidDate = new SimpleStringProperty(bidDate);
        }

        public String getItemName() {
            return itemName.get();
        }

        public String getMyBidAmount() {
            return myBidAmount.get();
        }

        public String getCurrentHighestBid() {
            return currentHighestBid.get();
        }

        public String getStatus() {
            return status.get();
        }

        public String getBidDate() {
            return bidDate.get();
        }
    }

    public static class WatchlistItem {
        private final SimpleStringProperty itemName;
        private final SimpleStringProperty currentPrice;
        private final SimpleStringProperty timeRemaining;
        private final SimpleStringProperty status;

        public WatchlistItem(String itemName, String currentPrice, String timeRemaining, String status) {
            this.itemName = new SimpleStringProperty(itemName);
            this.currentPrice = new SimpleStringProperty(currentPrice);
            this.timeRemaining = new SimpleStringProperty(timeRemaining);
            this.status = new SimpleStringProperty(status);
        }

        public String getItemName() {
            return itemName.get();
        }

        public String getCurrentPrice() {
            return currentPrice.get();
        }

        public String getTimeRemaining() {
            return timeRemaining.get();
        }

        public String getStatus() {
            return status.get();
        }
    }

    public static class Transaction {
        private final SimpleStringProperty date;
        private final SimpleStringProperty type;
        private final SimpleStringProperty item;
        private final SimpleStringProperty amount;
        private final SimpleStringProperty status;

        public Transaction(String date, String type, String item, String amount, String status) {
            this.date = new SimpleStringProperty(date);
            this.type = new SimpleStringProperty(type);
            this.item = new SimpleStringProperty(item);
            this.amount = new SimpleStringProperty(amount);
            this.status = new SimpleStringProperty(status);
        }

        public String getDate() {
            return date.get();
        }

        public String getType() {
            return type.get();
        }

        public String getItem() {
            return item.get();
        }

        public String getAmount() {
            return amount.get();
        }

        public String getStatus() {
            return status.get();
        }
    }

    public static class ActivityLog {
        private final SimpleStringProperty dateTime;
        private final SimpleStringProperty action;
        private final SimpleStringProperty description;

        public ActivityLog(String dateTime, String action, String description) {
            this.dateTime = new SimpleStringProperty(dateTime);
            this.action = new SimpleStringProperty(action);
            this.description = new SimpleStringProperty(description);
        }

        public String getDateTime() {
            return dateTime.get();
        }

        public String getAction() {
            return action.get();
        }

        public String getDescription() {
            return description.get();
        }
    }

    //My Listing
    private void setupListingsCards() {
        updateCategoryChipStyles();
        filterListings();
    }

    private void filterListings() {
        listingsCardContainer.getChildren().clear();

        String keyword = listingSearchField.getText() == null
                ? ""
                : listingSearchField.getText().trim().toLowerCase();

        for (Listing listing : listings) {
            boolean matchesSearch = listing.getItemName().toLowerCase().contains(keyword);

            boolean matchesCategory = selectedListingCategory.equals("All")
                    || listing.getCategory().equalsIgnoreCase(selectedListingCategory);

            if (matchesSearch && matchesCategory) {
                listingsCardContainer.getChildren().add(createListingCard(listing));
            }
        }
    }

    private VBox createListingCard(Listing listing) {
        VBox card = new VBox(12);
        card.getStyleClass().add("listing-card");

        double containerWidth = listingsCardContainer.getWidth();

        double cardWidth;

        if (containerWidth >= 1200) {
            cardWidth = (containerWidth - 90) / 6;
        } else if (containerWidth >= 950) {
            cardWidth = (containerWidth - 72) / 5;
        } else if (containerWidth >= 720) {
            cardWidth = (containerWidth - 54) / 4;
        } else if (containerWidth >= 520) {
            cardWidth = (containerWidth - 36) / 3;
        } else {
            cardWidth = (containerWidth - 18) / 2;
        }

        card.setPrefWidth(cardWidth);
        card.setMinWidth(cardWidth);
        card.setMaxWidth(cardWidth);
        StackPane imagePlaceholder = new StackPane();
        imagePlaceholder.getStyleClass().add("item-image-placeholder");

        Label iconLabel = new Label(listing.getIcon());
        iconLabel.getStyleClass().add("item-placeholder-icon");
        imagePlaceholder.getChildren().add(iconLabel);

        Label titleLabel = new Label(listing.getItemName());
        titleLabel.getStyleClass().add("item-title");
        titleLabel.setWrapText(true);

        Label categoryLabel = new Label(listing.getCategory());
        categoryLabel.getStyleClass().add("item-category-text");

        Label startingPriceLabel = new Label("Starting price");
        startingPriceLabel.getStyleClass().add("price-label");

        Label startingPriceValue = new Label(formatPrice(listing.getStartingPrice()));
        startingPriceValue.getStyleClass().add("starting-price-value");

        VBox startingPriceBox = new VBox(3, startingPriceLabel, startingPriceValue);

        Label currentPriceLabel = new Label("Current bid");
        currentPriceLabel.getStyleClass().add("price-label");

        Label currentPriceValue = new Label(formatPrice(listing.getCurrentPrice()));
        currentPriceValue.getStyleClass().add("current-price-value");

        VBox currentPriceBox = new VBox(3, currentPriceLabel, currentPriceValue);

        HBox priceRow = new HBox(28, startingPriceBox, currentPriceBox);
        priceRow.setAlignment(Pos.CENTER_LEFT);

        Label statusPill = new Label(listing.getStatus());
        statusPill.getStyleClass().addAll("listing-status-pill", getStatusStyleClass(listing.getStatus()));

        Label createdDateLabel = new Label("Created: " + listing.getCreatedDate());
        createdDateLabel.getStyleClass().add("created-date-text");

        HBox bottomRow = new HBox(10, statusPill, createdDateLabel);
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(
                imagePlaceholder,
                titleLabel,
                categoryLabel,
                priceRow,
                bottomRow
        );

        return card;
    }

    private void setupAddListingModal() {
        addListingCategoryComboBox.getItems().setAll(
                "Electronics",
                "Fashion",
                "Collectibles",
                "Home",
                "Books",
                "Other"
        );

        addListingStatusComboBox.getItems().setAll(
                "Active",
                "Pending",
                "Sold",
                "Closed"
        );

        addListingCategoryComboBox.setValue("Electronics");
        addListingStatusComboBox.setValue("Active");
    }

    private void setupAddListingModalSize() {
        addListingModal.prefWidthProperty().bind(
                addListingOverlay.widthProperty().multiply(0.55)
        );

        addListingModal.maxHeightProperty().set(Region.USE_PREF_SIZE);
    }

    private String formatPrice(double price) {
        return String.format("$%.2f", price);
    }

    private String getStatusStyleClass(String status) {
        return switch (status.toLowerCase()) {
            case "active" -> "status-active";
            case "pending" -> "status-pending";
            case "sold" -> "status-sold";
            case "closed" -> "status-closed";
            default -> "status-closed";
        };
    }

    @FXML
    private void handleCategoryAll() {
        selectedListingCategory = "All";
        updateCategoryChipStyles();
        filterListings();
    }

    @FXML
    private void handleCategoryElectronics() {
        selectedListingCategory = "Electronics";
        updateCategoryChipStyles();
        filterListings();
    }

    @FXML
    private void handleCategoryFashion() {
        selectedListingCategory = "Fashion";
        updateCategoryChipStyles();
        filterListings();
    }

    @FXML
    private void handleCategoryCollectibles() {
        selectedListingCategory = "Collectibles";
        updateCategoryChipStyles();
        filterListings();
    }

    @FXML
    private void handleCategoryHome() {
        selectedListingCategory = "Home";
        updateCategoryChipStyles();
        filterListings();
    }

    @FXML
    private void handleCategoryBooks() {
        selectedListingCategory = "Books";
        updateCategoryChipStyles();
        filterListings();
    }

    @FXML
    private void handleCategoryOther() {
        selectedListingCategory = "Other";
        updateCategoryChipStyles();
        filterListings();
    }

    private void updateCategoryChipStyles() {
        Button[] categoryButtons = {
                categoryAllButton,
                categoryElectronicsButton,
                categoryFashionButton,
                categoryCollectiblesButton,
                categoryHomeButton,
                categoryBooksButton,
                categoryOtherButton
        };

        for (Button button : categoryButtons) {
            button.getStyleClass().remove("category-chip-active");
        }

        switch (selectedListingCategory) {
            case "All" -> categoryAllButton.getStyleClass().add("category-chip-active");
            case "Electronics" -> categoryElectronicsButton.getStyleClass().add("category-chip-active");
            case "Fashion" -> categoryFashionButton.getStyleClass().add("category-chip-active");
            case "Collectibles" -> categoryCollectiblesButton.getStyleClass().add("category-chip-active");
            case "Home" -> categoryHomeButton.getStyleClass().add("category-chip-active");
            case "Books" -> categoryBooksButton.getStyleClass().add("category-chip-active");
            case "Other" -> categoryOtherButton.getStyleClass().add("category-chip-active");
        }
    }

    @FXML
    private void handleAddListing() {
        clearAddListingForm();

        addListingOverlay.setVisible(true);
        addListingOverlay.setManaged(true);
    }

    @FXML
    private void handleCloseAddListing() {
        addListingOverlay.setVisible(false);
        addListingOverlay.setManaged(false);
    }

    @FXML
    private void handleSubmitAddListing() {
        String itemName = addListingNameField.getText().trim();
        String category = addListingCategoryComboBox.getValue();
        String status = addListingStatusComboBox.getValue();
        String icon = addListingIconField.getText().trim();

        if (itemName.isEmpty()) {
            addListingErrorLabel.setText("Item name is required.");
            return;
        }

        if (category == null || category.isEmpty()) {
            addListingErrorLabel.setText("Please select a category.");
            return;
        }

        if (status == null || status.isEmpty()) {
            addListingErrorLabel.setText("Please select a status.");
            return;
        }

        double startingPrice;
        double currentBid;

        try {
            startingPrice = Double.parseDouble(addListingStartingPriceField.getText().trim());
            currentBid = Double.parseDouble(addListingCurrentBidField.getText().trim());
        } catch (NumberFormatException exception) {
            addListingErrorLabel.setText("Starting price and current bid must be valid numbers.");
            return;
        }

        if (startingPrice <= 0 || currentBid <= 0) {
            addListingErrorLabel.setText("Prices must be greater than 0.");
            return;
        }

        if (currentBid < startingPrice) {
            addListingErrorLabel.setText("Current bid cannot be lower than starting price.");
            return;
        }

        if (icon.isEmpty()) {
            icon = "📦";
        }

        String createdDate = LocalDateTime.now().toLocalDate().toString();

        Listing newListing = new Listing(
                itemName,
                category,
                startingPrice,
                currentBid,
                status,
                createdDate,
                icon
        );

        listings.add(0, newListing);

        selectedListingCategory = "All";
        updateCategoryChipStyles();
        filterListings();

        activeListingsLabel.setText(String.valueOf(countActiveListings()));

        addActivity(
                "Listed item",
                "Created a new listing for " + itemName + "."
        );

        handleCloseAddListing();
    }

    private void clearAddListingForm() {
        addListingNameField.clear();
        addListingStartingPriceField.clear();
        addListingCurrentBidField.clear();
        addListingIconField.clear();
        addListingErrorLabel.setText("");

        addListingCategoryComboBox.setValue("Electronics");
        addListingStatusComboBox.setValue("Active");
    }

    private static class Listing {
        private final String itemName;
        private final String category;
        private final double startingPrice;
        private final double currentPrice;
        private final String status;
        private final String createdDate;
        private final String icon;

        public Listing(String itemName,
                       String category,
                       double startingPrice,
                       double currentPrice,
                       String status,
                       String createdDate,
                       String icon) {
            this.itemName = itemName;
            this.category = category;
            this.startingPrice = startingPrice;
            this.currentPrice = currentPrice;
            this.status = status;
            this.createdDate = createdDate;
            this.icon = icon;
        }

        public String getItemName() {
            return itemName;
        }

        public String getCategory() {
            return category;
        }

        public double getStartingPrice() {
            return startingPrice;
        }

        public double getCurrentPrice() {
            return currentPrice;
        }

        public String getStatus() {
            return status;
        }

        public String getCreatedDate() {
            return createdDate;
        }

        public String getIcon() {
            return icon;
        }
    }

    private void setupResponsiveLayout() {
        setupResponsiveSummaryCards();
        setupResponsiveWalletPanels();
        setupResponsiveListingCards();
        setupResponsiveModals();
    }

    private void setupResponsiveSummaryCards() {
        if (summaryRow == null) return;

        summaryRow.widthProperty().addListener((observable, oldValue, newValue) -> {
            double availableWidth = newValue.doubleValue();
            double cardWidth = (availableWidth - 64) / 5;

            for (javafx.scene.Node node : summaryRow.getChildren()) {
                if (node instanceof Region region) {
                    region.setPrefWidth(cardWidth);
                    region.setMaxWidth(Double.MAX_VALUE);
                }
            }
        });
    }

    private void setupResponsiveWalletPanels() {
        if (walletPanelRow == null) return;

        walletPanelRow.widthProperty().addListener((observable, oldValue, newValue) -> {
            double availableWidth = newValue.doubleValue();
            double panelWidth = (availableWidth - 32) / 3;

            for (javafx.scene.Node node : walletPanelRow.getChildren()) {
                if (node instanceof Region region) {
                    region.setPrefWidth(panelWidth);
                    region.setMaxWidth(Double.MAX_VALUE);
                }
            }
        });
    }

    private void setupResponsiveListingCards() {
        if (listingsCardContainer == null) return;

        listingsCardContainer.widthProperty().addListener((observable, oldValue, newValue) -> {
            filterListings();
        });
    }

    private void setupResponsiveModals() {
        if (addListingOverlay != null && addListingModal != null) {
            addListingModal.prefWidthProperty().bind(
                    addListingOverlay.widthProperty().multiply(0.55)
            );

            addListingModal.minWidthProperty().bind(
                    addListingOverlay.widthProperty().multiply(0.38)
            );

            addListingModal.maxWidthProperty().bind(
                    addListingOverlay.widthProperty().multiply(0.70)
            );

            addListingModal.setMaxHeight(Region.USE_PREF_SIZE);
        }

        if (editProfileOverlay != null && editProfileModal != null) {
            editProfileModal.prefWidthProperty().bind(
                    editProfileOverlay.widthProperty().multiply(0.50)
            );

            editProfileModal.minWidthProperty().bind(
                    editProfileOverlay.widthProperty().multiply(0.35)
            );

            editProfileModal.maxWidthProperty().bind(
                    editProfileOverlay.widthProperty().multiply(0.65)
            );

            editProfileModal.setMaxHeight(Region.USE_PREF_SIZE);
        }
    }
}