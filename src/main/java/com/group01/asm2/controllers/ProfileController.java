package com.group01.asm2.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ProfileController {

    // =========================
    // Profile Header
    // =========================

    @FXML private Label avatarLabel;
    @FXML private Label fullNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label roleLabel;
    @FXML private Label ratingLabel;

    // =========================
    // Summary Labels
    // =========================

    @FXML private Label balanceLabel;
    @FXML private Label totalBidsLabel;
    @FXML private Label wonAuctionsLabel;
    @FXML private Label activeListingsLabel;
    @FXML private Label summaryRatingLabel;

    // =========================
    // Overview
    // =========================

    @FXML private Label selectedTabTitleLabel;
    @FXML private Label overviewBalanceLabel;
    @FXML private Label topUpStatusLabel;
    @FXML private Label recentAuctionStatusLabel;

    // =========================
    // Notification
    // =========================

    @FXML private Button notificationButton;
    @FXML private Label notificationCountLabel;

    // =========================
    // Tabs
    // =========================

    @FXML private TabPane profileTabPane;

    // =========================
    // Listings Table
    // =========================

    @FXML private TableView<Listing> listingsTable;
    @FXML private TableColumn<Listing, String> listingItemNameColumn;
    @FXML private TableColumn<Listing, String> listingStartingPriceColumn;
    @FXML private TableColumn<Listing, String> listingCurrentBidColumn;
    @FXML private TableColumn<Listing, String> listingStatusColumn;
    @FXML private TableColumn<Listing, String> listingCreatedDateColumn;

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
    // Mock User Data
    // =========================

    private String fullName = "Huyen Tran";
    private String email = "huyen@example.com";
    private String role = "User";
    private String phone = "0912 345 678";
    private String address = "District 7, Ho Chi Minh City";
    private double balance = 2450.00;
    private double rating = 4.8;

    private final ObservableList<Listing> listings = FXCollections.observableArrayList();
    private final ObservableList<Bid> bids = FXCollections.observableArrayList();
    private final ObservableList<WatchlistItem> watchlist = FXCollections.observableArrayList();
    private final ObservableList<Transaction> transactions = FXCollections.observableArrayList();
    private final ObservableList<ActivityLog> activities = FXCollections.observableArrayList();
    private final ObservableList<NotificationItem> notifications = FXCollections.observableArrayList();

    private Popup notificationPopup;

    private final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    @FXML
    public void initialize() {
        loadMockData();
        setupProfileHeader();
        setupTables();
        setupTabSwitching();
        setupNotificationPopup();
    }

    // =========================
    // Initial Setup
    // =========================

    private void loadMockData() {
        listings.addAll(
                new Listing("Vintage Film Camera", "$120.00", "$310.00", "Active", "02 May 2026"),
                new Listing("Mechanical Keyboard", "$80.00", "$145.00", "Active", "03 May 2026"),
                new Listing("Designer Handbag", "$250.00", "$420.00", "Under Review", "05 May 2026")
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

        notifications.addAll(
                new NotificationItem("Top-up request approved", "Your $500.00 top-up request has been approved.", true),
                new NotificationItem("Top-up request rejected", "Your previous top-up request was rejected by admin.", true),
                new NotificationItem("Auction won", "You won the auction for Smart Watch.", true),
                new NotificationItem("Outbid on watched item", "Someone placed a higher bid on Vintage Camera.", true),
                new NotificationItem("Auction ending soon", "Antique Desk Lamp is ending in less than 3 hours.", true),
                new NotificationItem("Payment processed", "Your payment for Smart Watch has been processed.", true),
                new NotificationItem("Listing moderated", "Your Designer Handbag listing is under admin review.", true)
        );
    }

    private void setupProfileHeader() {
        fullNameLabel.setText(fullName);
        emailLabel.setText(email);
        roleLabel.setText(role);
        ratingLabel.setText(String.format("%.1f / 5.0", rating));
        summaryRatingLabel.setText(String.format("%.1f", rating));

        updateAvatar();
        updateBalanceLabels();

        totalBidsLabel.setText(String.valueOf(bids.size()));
        wonAuctionsLabel.setText(String.valueOf(countWonAuctions()));
        activeListingsLabel.setText(String.valueOf(countActiveListings()));
        notificationCountLabel.setText(String.valueOf(countUnreadNotifications()));
    }

    private void updateAvatar() {
        String[] nameParts = fullName.trim().split("\\s+");

        if (nameParts.length >= 2) {
            avatarLabel.setText(
                    nameParts[0].substring(0, 1).toUpperCase()
                            + nameParts[nameParts.length - 1].substring(0, 1).toUpperCase()
            );
        } else if (!fullName.isBlank()) {
            avatarLabel.setText(fullName.substring(0, 1).toUpperCase());
        } else {
            avatarLabel.setText("U");
        }
    }

    private void updateBalanceLabels() {
        String formattedBalance = String.format("$%,.2f", balance);
        balanceLabel.setText(formattedBalance);
        overviewBalanceLabel.setText(formattedBalance);
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

    private int countUnreadNotifications() {
        int count = 0;

        for (NotificationItem notification : notifications) {
            if (notification.isUnread()) {
                count++;
            }
        }

        return count;
    }

    // =========================
    // Table Setup
    // =========================

    private void setupTables() {
        setupListingsTable();
        setupBidsTable();
        setupWatchlistTable();
        setupTransactionsTable();
        setupActivityTable();
    }

    private void setupListingsTable() {
        listingItemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        listingStartingPriceColumn.setCellValueFactory(new PropertyValueFactory<>("startingPrice"));
        listingCurrentBidColumn.setCellValueFactory(new PropertyValueFactory<>("currentBid"));
        listingStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        listingCreatedDateColumn.setCellValueFactory(new PropertyValueFactory<>("createdDate"));

        listingsTable.setItems(listings);
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
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Profile");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStyleClass().add("profile-dialog");
        dialogPane.getStylesheets().add(
                getClass().getResource("/com/group01/asm2/styles/profile.css").toExternalForm()
        );

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialogPane.getButtonTypes().addAll(saveButtonType, cancelButtonType);

        TextField nameField = createTextField(fullName);
        TextField phoneField = createTextField(phone);
        TextField addressField = createTextField(address);

        TextField roleField = createTextField(role);
        roleField.setDisable(true);

        TextField emailField = createTextField(email);
        emailField.setDisable(true);

        TextField ratingField = createTextField(String.format("%.1f / 5.0", rating));
        ratingField.setDisable(true);

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");

        VBox content = new VBox(12);
        content.getStyleClass().add("dialog-card");
        content.setPadding(new Insets(20));

        Label title = new Label("Edit Profile Information");
        title.getStyleClass().add("dialog-title");

        Label subtitle = new Label("You can update your name, phone and address. Role, email and rating are read-only.");
        subtitle.getStyleClass().add("dialog-subtitle");

        content.getChildren().addAll(
                title,
                subtitle,
                createFieldGroup("Full Name", nameField),
                createFieldGroup("Phone", phoneField),
                createFieldGroup("Address", addressField),
                createFieldGroup("Role", roleField),
                createFieldGroup("Email", emailField),
                createFieldGroup("Rating", ratingField),
                errorLabel
        );

        dialogPane.setContent(content);

        Node saveButton = dialogPane.lookupButton(saveButtonType);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (nameField.getText().trim().isEmpty()) {
                errorLabel.setText("Full name cannot be empty.");
                event.consume();
                return;
            }

            fullName = nameField.getText().trim();
            phone = phoneField.getText().trim();
            address = addressField.getText().trim();

            fullNameLabel.setText(fullName);
            updateAvatar();

            addActivity("Updated profile", "Updated profile information.");

            dialog.close();
        });

        dialog.showAndWait();
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

                notifications.add(0, new NotificationItem(
                        "Top-up request submitted",
                        String.format("Your $%,.2f top-up request is waiting for admin approval.", amount),
                        true
                ));

                notificationCountLabel.setText(String.valueOf(countUnreadNotifications()));
                setupNotificationPopup();

                dialog.close();

            } catch (NumberFormatException exception) {
                errorLabel.setText("Please enter a valid number.");
                event.consume();
            }
        });

        dialog.showAndWait();
    }

    // =========================
    // Notification Dropdown
    // =========================

    private void setupNotificationPopup() {
        notificationPopup = new Popup();
        notificationPopup.setAutoHide(true);

        VBox popupContent = new VBox(10);
        popupContent.getStyleClass().add("notification-popup");
        popupContent.setPrefWidth(340);

        Label title = new Label("Notifications");
        title.getStyleClass().add("notification-title");

        popupContent.getChildren().add(title);

        for (NotificationItem notification : notifications) {
            VBox itemBox = new VBox(4);
            itemBox.getStyleClass().add("notification-item");

            Label notificationTitle = new Label(notification.getTitle());
            notificationTitle.getStyleClass().add("notification-item-title");

            Label message = new Label(notification.getMessage());
            message.getStyleClass().add("notification-item-message");

            itemBox.getChildren().addAll(notificationTitle, message);
            popupContent.getChildren().add(itemBox);
        }

        notificationPopup.getContent().clear();
        notificationPopup.getContent().add(popupContent);
    }

    @FXML
    private void handleToggleNotifications() {
        if (notificationPopup.isShowing()) {
            notificationPopup.hide();
        } else {
            notificationPopup.show(
                    notificationButton,
                    notificationButton.localToScreen(0, 0).getX() - 300,
                    notificationButton.localToScreen(0, 0).getY() + 42
            );

            for (NotificationItem notification : notifications) {
                notification.setUnread(false);
            }

            notificationCountLabel.setText("0");
        }
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

    private void addActivity(String action, String description) {
        String now = LocalDateTime.now().format(dateTimeFormatter);
        activities.add(0, new ActivityLog(now, action, description));
    }

    // =========================
    // Simple Model Classes
    // You can move these to separate model files later.
    // Example package:
    // src/main/java/com/group01/asm2/models/
    // =========================

    public static class Listing {
        private final SimpleStringProperty itemName;
        private final SimpleStringProperty startingPrice;
        private final SimpleStringProperty currentBid;
        private final SimpleStringProperty status;
        private final SimpleStringProperty createdDate;

        public Listing(String itemName, String startingPrice, String currentBid, String status, String createdDate) {
            this.itemName = new SimpleStringProperty(itemName);
            this.startingPrice = new SimpleStringProperty(startingPrice);
            this.currentBid = new SimpleStringProperty(currentBid);
            this.status = new SimpleStringProperty(status);
            this.createdDate = new SimpleStringProperty(createdDate);
        }

        public String getItemName() {
            return itemName.get();
        }

        public String getStartingPrice() {
            return startingPrice.get();
        }

        public String getCurrentBid() {
            return currentBid.get();
        }

        public String getStatus() {
            return status.get();
        }

        public String getCreatedDate() {
            return createdDate.get();
        }
    }

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

    public static class NotificationItem {
        private final String title;
        private final String message;
        private boolean unread;

        public NotificationItem(String title, String message, boolean unread) {
            this.title = title;
            this.message = message;
            this.unread = unread;
        }

        public String getTitle() {
            return title;
        }

        public String getMessage() {
            return message;
        }

        public boolean isUnread() {
            return unread;
        }

        public void setUnread(boolean unread) {
            this.unread = unread;
        }
    }
}