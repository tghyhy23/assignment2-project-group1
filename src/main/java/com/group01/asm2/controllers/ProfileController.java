package com.group01.asm2.controllers;

import com.group01.asm2.dtos.ItemFilter;
import com.group01.asm2.models.Item;
import com.group01.asm2.models.TopUpRequest;
import com.group01.asm2.services.ItemService;
import com.group01.asm2.utils.ScrollUtils;
import com.group01.asm2.utils.TopUpValidator;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ProfileController {

    private final ItemService itemService = new ItemService();

    private final int currentUserId = 1;

    private final List<TopUpRequest> topUpRequests = new ArrayList<>();
    private final ObservableList<Item> listings = FXCollections.observableArrayList();
    private final ObservableList<ActivityLog> activities = FXCollections.observableArrayList();

    private final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    @FXML private ScrollPane profileScrollPane;
    @FXML private AuctionsController profileListingsController;

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

    @FXML private Label balanceLabel;
    @FXML private Label totalListingsLabel;
    @FXML private Label activeListingsLabel;
    @FXML private Label soldListingsLabel;
    @FXML private Label summaryRatingLabel;

    @FXML private TabPane profileTabPane;
    @FXML private Label selectedTabTitleLabel;

    @FXML private Label walletBalanceLabel;
    @FXML private Label topUpStatusLabel;
    @FXML private Label recentAuctionStatusLabel;

    @FXML private HBox summaryRow;
    @FXML private HBox walletPanelRow;

    @FXML private TableView<ActivityLog> activityTable;
    @FXML private TableColumn<ActivityLog, String> activityDateTimeColumn;
    @FXML private TableColumn<ActivityLog, String> activityActionColumn;
    @FXML private TableColumn<ActivityLog, String> activityDescriptionColumn;

    @FXML private StackPane editProfileOverlay;
    @FXML private Button editProfileButton;
    @FXML private TextField editFullNameField;
    @FXML private TextField editEmailField;
    @FXML private TextField editPhoneField;
    @FXML private TextField editDateOfBirthField;
    @FXML private TextField editAddressField;

    @FXML private StackPane topUpOverlay;
    @FXML private TextField topUpAmountField;
    @FXML private Label topUpErrorLabel;

    @FXML private StackPane addListingOverlay;
    @FXML private TextField addListingNameField;
    @FXML private ComboBox<String> addListingCategoryComboBox;
    @FXML private TextField addListingStartingPriceField;
    @FXML private TextField addListingCurrentBidField;
    @FXML private ComboBox<String> addListingStatusComboBox;
    @FXML private TextField addListingIconField;
    @FXML private Label addListingErrorLabel;

    private String fullName = "Sophia Bennett";
    private String email = "sophia.bennett@bidblitz.com";
    private String role = "User";
    private String phone = "+84 912 345 678";
    private String dateOfBirth = "15 March 2003";
    private String address = "District 7, Ho Chi Minh City";
    private String joinedDate = "12 January 2025";
    private String rating = "4.8 / 5.0";
    private double balance = 1250.75;

    @FXML
    public void initialize() {
        ScrollUtils.makeSmooth(profileScrollPane);

        loadDataFromServices();
        setupProfileHeader();
        updateSummaryLabels();
        setupTabSwitching();
        setupProfileListings();
        setupResponsiveEditProfileButton();
        setupActivityTable();
        makeTableResponsive(activityTable);
        setupAddListingModal();
        setupResponsiveLayout();
    }

    private void loadDataFromServices() {
        ItemFilter filter = new ItemFilter();
        listings.setAll(itemService.readItems(filter));

        activities.addAll(
                new ActivityLog("07 May 2026, 09:20", "Logged in", "User logged into BidBlitz successfully."),
                new ActivityLog("05 May 2026, 14:30", "Requested top-up", "Requested a balance top-up of $500.00."),
                new ActivityLog("02 May 2026, 20:15", "Updated profile", "Updated phone number and address.")
        );
    }

    private void setupProfileListings() {
        if (profileListingsController != null) {
            profileListingsController.enableProfileMode(currentUserId);
        }
    }

    private void reloadListingsFromService() {
        ItemFilter filter = new ItemFilter();
        listings.setAll(itemService.readItems(filter));

        updateSummaryLabels();

        if (profileListingsController != null) {
            profileListingsController.enableProfileMode(currentUserId);
        }
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

        avatarLabel.setText(createInitials(fullName));
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

    private void updateSummaryLabels() {
        balanceLabel.setText(formatPrice(balance));
        walletBalanceLabel.setText(formatPrice(balance));

        totalListingsLabel.setText(String.valueOf(listings.size()));
        activeListingsLabel.setText(String.valueOf(countActiveItems()));
        soldListingsLabel.setText("0");
        summaryRatingLabel.setText("4.8");
    }

    private int countActiveItems() {
        int count = 0;

        for (Item item : listings) {
            if (item != null) {
                count++;
            }
        }

        return count;
    }

    private void setupTabSwitching() {
        profileTabPane.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldTab, newTab) -> {
                    if (newTab != null) {
                        selectedTabTitleLabel.setText(newTab.getText());
                    }
                }
        );
    }

    private void setupResponsiveEditProfileButton() {
        editProfileButton.setMinWidth(Button.USE_PREF_SIZE);
        editProfileButton.setPrefWidth(Button.USE_COMPUTED_SIZE);
        editProfileButton.setMaxWidth(Double.MAX_VALUE);
    }

    private void setupActivityTable() {
        activityDateTimeColumn.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
        activityActionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        activityDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        activityTable.setItems(activities);
    }

    private void makeTableResponsive(TableView<?> table) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label(""));
        table.setFixedCellSize(48);

        table.prefHeightProperty().bind(
                Bindings.size(table.getItems())
                        .multiply(table.getFixedCellSize())
                        .add(52)
        );

        table.setMinHeight(Region.USE_PREF_SIZE);
        table.setMaxHeight(Region.USE_PREF_SIZE);
    }

    private void setupAddListingModal() {
        addListingCategoryComboBox.setItems(FXCollections.observableArrayList(
                "Electronics", "Fashion", "Collectibles", "Home", "Books", "Other"
        ));

        addListingStatusComboBox.setItems(FXCollections.observableArrayList(
                "Active", "Pending", "Sold", "Closed"
        ));

        addListingCategoryComboBox.setValue("Electronics");
        addListingStatusComboBox.setValue("Active");
    }

    private void setupResponsiveLayout() {
        bindEqualWidth(summaryRow);
        bindEqualWidth(walletPanelRow);
    }

    private void bindEqualWidth(HBox row) {
        if (row == null) return;

        row.widthProperty().addListener((observable, oldValue, newValue) -> {
            int count = row.getChildren().size();

            if (count == 0) return;

            double spacing = row.getSpacing() * (count - 1);
            double width = (newValue.doubleValue() - spacing) / count;

            for (Node node : row.getChildren()) {
                if (node instanceof Region region) {
                    region.setPrefWidth(width);
                    region.setMaxWidth(Double.MAX_VALUE);
                }
            }
        });
    }

    @FXML
    private void handleOpenEditProfile() {
        editFullNameField.setText(fullName);
        editEmailField.setText(email);
        editPhoneField.setText(phone);
        editDateOfBirthField.setText(dateOfBirth);
        editAddressField.setText(address);

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

        addActivity("Updated profile", "Updated profile information.");
    }

    @FXML
    private void handleOpenTopUp() {
        topUpAmountField.clear();
        topUpErrorLabel.setText("");

        topUpOverlay.setVisible(true);
        topUpOverlay.setManaged(true);

        topUpAmountField.requestFocus();
    }

    @FXML
    private void handleCloseTopUp() {
        topUpOverlay.setVisible(false);
        topUpOverlay.setManaged(false);
    }

    @FXML
    private void handleSubmitTopUp() {
        try {
            double amount = TopUpValidator.validateAmount(topUpAmountField.getText());

            TopUpRequest request = createTopUpRequest(currentUserId, amount);

            showPendingTopUpStatus(request.getAmount());

            addActivity(
                    "Requested top-up",
                    String.format(
                            "Requested a top-up of $%,.2f. Waiting for admin approval.",
                            request.getAmount()
                    )
            );

            handleCloseTopUp();

        } catch (IllegalArgumentException exception) {
            topUpErrorLabel.setText(exception.getMessage());
        }
    }

    private void showPendingTopUpStatus(double amount) {
        topUpStatusLabel.setText(String.format("Pending approval: $%,.2f", amount));

        topUpStatusLabel.getStyleClass().removeAll(
                "status-neutral",
                "status-success",
                "status-warning",
                "status-error"
        );

        topUpStatusLabel.getStyleClass().add("status-warning");
    }

    private TopUpRequest createTopUpRequest(int userId, double amount) {
        TopUpRequest request = new TopUpRequest(userId, amount);
        topUpRequests.add(request);

        return request;
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
    private void handleSaveListing() {
        String name = addListingNameField.getText().trim();
        String category = addListingCategoryComboBox.getValue();
        String icon = addListingIconField.getText().trim();

        if (name.isEmpty()) {
            addListingErrorLabel.setText("Item name is required.");
            return;
        }

        try {
            BigDecimal startingPrice =
                    new BigDecimal(addListingStartingPriceField.getText().trim());

            BigDecimal currentBid =
                    new BigDecimal(addListingCurrentBidField.getText().trim());

            if (startingPrice.compareTo(BigDecimal.ZERO) < 0
                    || currentBid.compareTo(BigDecimal.ZERO) < 0) {

                addListingErrorLabel.setText("Price values must be positive.");
                return;
            }

            if (icon.isEmpty()) {
                icon = "📦";
            }

            // TODO:
            // Create Item + Auction here when service method is ready.
            // Example:
            // Item newItem = new Item(...);
            // itemService.createItem(newItem);

            reloadListingsFromService();

            addActivity("Created item", "Created new listing: " + name + ".");

            handleCloseAddListing();

        } catch (NumberFormatException exception) {
            addListingErrorLabel.setText("Please enter valid price numbers.");
        }
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

    private void addActivity(String action, String description) {
        String now = LocalDateTime.now().format(dateTimeFormatter);
        activities.add(0, new ActivityLog(now, action, description));
    }

    private String formatPrice(double value) {
        return String.format("$%,.2f", value);
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
}