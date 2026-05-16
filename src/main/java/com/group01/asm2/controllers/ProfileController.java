package com.group01.asm2.controllers;

import com.group01.asm2.dtos.ItemFilter;
import com.group01.asm2.dtos.UserProfileStatisticsDto;
import com.group01.asm2.dtos.UserProfileViewDto;
import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.Item;
import com.group01.asm2.models.TopUpRequest;
import com.group01.asm2.models.User;
import com.group01.asm2.services.ItemService;
import com.group01.asm2.services.UserService;
import com.group01.asm2.utils.ScrollUtils;
import com.group01.asm2.utils.TopUpValidator;

import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Group 01
 */
public class ProfileController {

    private final UserService userService = new UserService();
    private final ItemService itemService = new ItemService();

    private UserProfileViewDto currentProfile;
    private UserProfileStatisticsDto currentStatistics;
    private User profileUser;
    private Integer profileUserId;

    private final List<Tab> originalProfileTabs = new ArrayList<>();

    @FXML private ScrollPane profileScrollPane;

    @FXML private Label avatarLabel;
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
    @FXML private Tab walletTab;
    @FXML private Tab myListingsTab;
    @FXML private Tab activityLogTab;
    @FXML private Tab sellerStatisticsTab;
    @FXML private Label selectedTabTitleLabel;

    @FXML private HBox privateDetailsRow;
    @FXML private HBox summaryRow;
    @FXML private HBox walletPanelRow;

    @FXML private Label walletBalanceLabel;
    @FXML private Label topUpStatusLabel;
    @FXML private Label recentAuctionStatusLabel;

    @FXML private Button editProfileButton;
    @FXML private Button addItemButton;
    @FXML private Button requestTopUpButton;

    @FXML private Button categoryAllButton;
    @FXML private Button categoryElectronicsButton;
    @FXML private Button categoryFashionButton;
    @FXML private Button categoryCollectiblesButton;
    @FXML private Button categoryHomeButton;
    @FXML private Button categoryBooksButton;
    @FXML private Button categoryOtherButton;

    @FXML private TableView<ActivityLog> activityTable;
    @FXML private TableColumn<ActivityLog, String> activityDateTimeColumn;
    @FXML private TableColumn<ActivityLog, String> activityActionColumn;
    @FXML private TableColumn<ActivityLog, String> activityDescriptionColumn;

    @FXML private StackPane editProfileOverlay;
    @FXML private VBox editProfileModal;
    @FXML private TextField editFullNameField;
    @FXML private TextField editEmailField;
    @FXML private TextField editPhoneField;
    @FXML private DatePicker editDateOfBirthPicker;
    @FXML private TextField editAddressField;

    @FXML private StackPane topUpOverlay;
    @FXML private VBox topUpModal;
    @FXML private TextField topUpAmountField;
    @FXML private Label topUpErrorLabel;

    @FXML private StackPane addListingOverlay;
    @FXML private VBox addListingModal;
    @FXML private ComboBox<String> addListingCategoryComboBox;
    @FXML private TextField addListingStartingPriceField;
    @FXML private TextField addListingTitleField;
    @FXML private TextArea addListingDescriptionArea;
    @FXML private TextField addListingReservePriceField;
    @FXML private ComboBox<String> addListingConditionComboBox;
    @FXML private Label addListingImageNameLabel;
    @FXML private ImageView addListingImagePreview;
    @FXML private StackPane addListingImagePreviewBox;
    @FXML private Label addListingErrorLabel;

    @FXML private Label itemsSoldLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label commissionFeesLabel;
    @FXML private Label soldRatioLabel;

    @FXML private BarChart<String, Number> averageSalePriceChart;
    @FXML private LineChart<String, Number> listingTrendChart;

    private File selectedListingImageFile;
    private String selectedListingCategory = "All";

    private final ObservableList<Item> listings = FXCollections.observableArrayList();
    private final ObservableList<ActivityLog> activities = FXCollections.observableArrayList();
    private final List<TopUpRequest> topUpRequests = new ArrayList<>();

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    @FXML
    public void initialize() {
        ScrollUtils.makeSmooth(profileScrollPane);

        captureOriginalProfileTabs();
        setupTabSwitching();
        setupResponsiveEditProfileButton();
        setupActivityTable();
        makeTableResponsive(activityTable);
        setupAddListingModal();
        setupResponsiveLayout();
        updateCategoryChipStyles();

        loadProfile(null);
    }

    public void loadProfile(Integer userId) {
        this.profileUserId = userId;
        refreshProfilePage();
    }

    private void refreshProfilePage() {
        try {
            currentProfile = userService.readProfilePage(profileUserId);
            currentStatistics = userService.readProfileStatistics(profileUserId);
            profileUser = currentProfile.getUser();

            renderProfilePage();

        } catch (AppException exception) {
            showError(exception.getMessage());
        } catch (Exception exception) {
            exception.printStackTrace();
            showError("Could not load profile page.");
        }
    }

    private void renderProfilePage() {
        renderProfileHeader();
        applyProfilePermissions();
        loadDataFromServices();
        renderSummaryLabels();
        renderWalletPanel();
        renderSellerStatistics();
        renderActivityTable();
    }

    private void loadDataFromServices() {
        listings.clear();
        activities.clear();

        if (currentProfile == null || profileUser == null) {
            return;
        }

        if (currentProfile.canViewListings()) {
            loadListingsForProfile();
        }

        if (currentProfile.canViewActivityLog()) {
            loadActivityLogsForProfile();
        }
    }

    private void loadListingsForProfile() {
        try {
            /*
             * Temporary:
             * This still reads from the existing ItemService method.
             * Later, replace with itemService.readItemsBySellerId(profileUser.getId())
             * when we add that method.
             */
            ItemFilter filter = new ItemFilter();
            listings.setAll(itemService.readItems(filter));

        } catch (Exception exception) {
            listings.clear();
        }
    }

    private void loadActivityLogsForProfile() {
        /*
         * Temporary:
         * Real data should later come from ActivityLogService.
         * For now, keep this empty instead of fake mock logs.
         */
        activities.clear();
    }

    private void renderProfileHeader() {
        if (currentProfile == null || profileUser == null) {
            return;
        }

        String displayName = defaultText(profileUser.getFullName(), profileUser.getUsername());
        String displayRole = formatRole(profileUser.getRole() == null ? null : profileUser.getRole().name());

        fullNameLabel.setText(displayName);
        roleLabel.setText(displayRole);
        ratingLabel.setText("★ " + formatRating(profileUser.getRating()));

        if (currentProfile.canViewPrivateDetails()) {
            emailLabel.setText(defaultText(profileUser.getEmail(), "N/A"));
            phoneLabel.setText(defaultText(profileUser.getPhone(), "N/A"));
            addressLabel.setText(defaultText(profileUser.getAddress(), "N/A"));
            dateOfBirthLabel.setText(
                profileUser.getDateOfBirth() == null
                    ? "N/A"
                    : profileUser.getDateOfBirth().format(dateFormatter)
            );
        } else {
            emailLabel.setText("Private");
            phoneLabel.setText("Private");
            addressLabel.setText("Private");
            dateOfBirthLabel.setText("Private");
        }

        joinedDateLabel.setText(
            profileUser.getCreatedAt() == null
                ? "N/A"
                : profileUser.getCreatedAt().toLocalDate().format(dateFormatter)
        );

        avatarLabel.setText(createInitials(displayName));
    }

    private void renderSummaryLabels() {
        if (currentStatistics == null || currentProfile == null) {
            return;
        }

        if (currentProfile.canViewWallet()) {
            balanceLabel.setText(formatPrice(currentStatistics.getBalance()));
        } else {
            balanceLabel.setText("Private");
        }

        totalListingsLabel.setText(String.valueOf(currentStatistics.getTotalListings()));
        activeListingsLabel.setText(String.valueOf(currentStatistics.getActiveListings()));
        soldListingsLabel.setText(String.valueOf(currentStatistics.getSoldListings()));
        summaryRatingLabel.setText(formatRating(currentStatistics.getRating()));
    }

    private void renderWalletPanel() {
        if (currentStatistics == null || currentProfile == null) {
            return;
        }

        if (currentProfile.canViewWallet()) {
            walletBalanceLabel.setText(formatPrice(currentStatistics.getBalance()));
        } else {
            walletBalanceLabel.setText("Private");
        }

        if (currentProfile.canRequestTopUp()) {
            topUpStatusLabel.setText("No pending request");
            replaceStyle(topUpStatusLabel, "status-neutral");
        } else {
            topUpStatusLabel.setText("Not available");
            replaceStyle(topUpStatusLabel, "status-neutral");
        }

        recentAuctionStatusLabel.setText("No recent auction update.");
        replaceStyle(recentAuctionStatusLabel, "status-success");
    }

    private void renderSellerStatistics() {
        if (currentStatistics == null) {
            clearSellerStatistics();
            return;
        }

        itemsSoldLabel.setText(String.valueOf(currentStatistics.getItemsSold()));
        totalRevenueLabel.setText(formatPrice(currentStatistics.getTotalRevenue()));
        commissionFeesLabel.setText(formatPrice(currentStatistics.getCommissionFees()));
        soldRatioLabel.setText(String.format("%.1f%%", currentStatistics.getSoldRatio()));

        renderAverageSalePriceChart();
        renderListingTrendChart();
    }

    private void renderAverageSalePriceChart() {
        averageSalePriceChart.getData().clear();

        if (currentStatistics == null || currentStatistics.getAverageSalePriceByCategory().isEmpty()) {
            return;
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        for (Map.Entry<Integer, BigDecimal> entry : currentStatistics.getAverageSalePriceByCategory().entrySet()) {
            series.getData().add(
                new XYChart.Data<>(
                    convertCategoryToName(entry.getKey()),
                    entry.getValue()
                )
            );
        }

        averageSalePriceChart.getData().add(series);
    }

    private void renderListingTrendChart() {
        listingTrendChart.getData().clear();

        if (currentStatistics == null) {
            return;
        }

        XYChart.Series<String, Number> soldSeries = new XYChart.Series<>();
        soldSeries.setName("Sold");

        XYChart.Series<String, Number> unsoldSeries = new XYChart.Series<>();
        unsoldSeries.setName("Unsold");

        for (Map.Entry<String, Integer> entry : currentStatistics.getSoldListingsByMonth().entrySet()) {
            String month = entry.getKey();

            soldSeries.getData().add(
                new XYChart.Data<>(month, entry.getValue())
            );

            Integer unsoldValue = currentStatistics.getUnsoldListingsByMonth().getOrDefault(month, 0);

            unsoldSeries.getData().add(
                new XYChart.Data<>(month, unsoldValue)
            );
        }

        listingTrendChart.getData().addAll(soldSeries, unsoldSeries);
    }

    private void clearSellerStatistics() {
        itemsSoldLabel.setText("0");
        totalRevenueLabel.setText(formatPrice(BigDecimal.ZERO));
        commissionFeesLabel.setText(formatPrice(BigDecimal.ZERO));
        soldRatioLabel.setText("0%");

        averageSalePriceChart.getData().clear();
        listingTrendChart.getData().clear();
    }

    private void renderActivityTable() {
        activityTable.setItems(activities);
    }

    private void applyProfilePermissions() {
        if (currentProfile == null) {
            return;
        }

        setNodeVisible(privateDetailsRow, currentProfile.canViewPrivateDetails());
        setNodeVisible(editProfileButton, currentProfile.canEditProfile());
        setNodeVisible(addItemButton, currentProfile.isOwner() && currentProfile.isSellerProfile());
        setNodeVisible(requestTopUpButton, currentProfile.canRequestTopUp());

        setTabAvailable(walletTab, currentProfile.canViewWallet());
        setTabAvailable(myListingsTab, currentProfile.canViewListings());
        setTabAvailable(activityLogTab, currentProfile.canViewActivityLog());
        setTabAvailable(sellerStatisticsTab, currentProfile.canViewSellerStatistics());

        if (!profileTabPane.getTabs().isEmpty()) {
            Tab selectedTab = profileTabPane.getSelectionModel().getSelectedItem();

            if (selectedTab == null || !profileTabPane.getTabs().contains(selectedTab)) {
                profileTabPane.getSelectionModel().selectFirst();
            }

            selectedTabTitleLabel.setText(profileTabPane.getSelectionModel().getSelectedItem().getText());
        }
    }

    private void captureOriginalProfileTabs() {
        originalProfileTabs.clear();
        originalProfileTabs.addAll(profileTabPane.getTabs());
    }

    private void setTabAvailable(Tab tab, boolean available) {
        if (tab == null) {
            return;
        }

        boolean currentlyAdded = profileTabPane.getTabs().contains(tab);

        if (available && !currentlyAdded) {
            int targetIndex = calculateOriginalTabIndex(tab);
            profileTabPane.getTabs().add(targetIndex, tab);
            return;
        }

        if (!available && currentlyAdded) {
            profileTabPane.getTabs().remove(tab);
        }
    }

    private int calculateOriginalTabIndex(Tab tab) {
        int index = 0;

        for (Tab originalTab : originalProfileTabs) {
            if (originalTab == tab) {
                break;
            }

            if (profileTabPane.getTabs().contains(originalTab)) {
                index++;
            }
        }

        return Math.min(index, profileTabPane.getTabs().size());
    }

    private void setNodeVisible(Node node, boolean visible) {
        if (node == null) {
            return;
        }

        node.setVisible(visible);
        node.setManaged(visible);
    }

    private void setupTabSwitching() {
        profileTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab != null) {
                selectedTabTitleLabel.setText(newTab.getText());

                Node tabContent = newTab.getContent();

                if (tabContent instanceof Region region) {
                    profileTabPane.setMinHeight(Region.USE_COMPUTED_SIZE);
                    profileTabPane.setPrefHeight(region.prefHeight(-1) + 50);
                    profileTabPane.setMaxHeight(Region.USE_PREF_SIZE);
                }
            }
        });
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

        addListingConditionComboBox.setItems(FXCollections.observableArrayList(
            "New", "Used", "Refurbished"
        ));

        addListingCategoryComboBox.setValue("Electronics");
        addListingConditionComboBox.setValue("Used");
    }

    private void setupResponsiveLayout() {
        bindEqualWidth(summaryRow);
        bindEqualWidth(walletPanelRow);
    }

    private void bindEqualWidth(HBox row) {
        if (row == null) {
            return;
        }

        row.widthProperty().addListener((observable, oldValue, newValue) -> {
            int count = row.getChildren().size();

            if (count == 0) {
                return;
            }

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
        if (currentProfile == null || profileUser == null || !currentProfile.canEditProfile()) {
            showError("You are not allowed to edit this profile.");
            return;
        }

        editFullNameField.setText(defaultText(profileUser.getFullName(), ""));
        editEmailField.setText(defaultText(profileUser.getEmail(), ""));
        editPhoneField.setText(defaultText(profileUser.getPhone(), ""));
        editDateOfBirthPicker.setValue(profileUser.getDateOfBirth());
        editAddressField.setText(defaultText(profileUser.getAddress(), ""));

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
        if (currentProfile == null || profileUser == null || !currentProfile.canEditProfile()) {
            showError("You are not allowed to update this profile.");
            return;
        }

        try {
            userService.updateUserProfile(
                profileUser.getId(),
                editFullNameField.getText(),
                editDateOfBirthPicker.getValue(),
                editEmailField.getText(),
                editPhoneField.getText(),
                editAddressField.getText(),
                profileUser.getUsername()
            );

            handleCloseEditProfile();
            refreshProfilePage();

        } catch (AppException exception) {
            showError(exception.getMessage());
        } catch (Exception exception) {
            exception.printStackTrace();
            showError("Could not update profile.");
        }
    }

    @FXML
    private void handleOpenTopUp() {
        if (currentProfile == null || !currentProfile.canRequestTopUp()) {
            showError("You are not allowed to request top-up for this profile.");
            return;
        }

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
            if (currentProfile == null || profileUser == null || !currentProfile.canRequestTopUp()) {
                topUpErrorLabel.setText("You are not allowed to request top-up for this profile.");
                return;
            }

            double amount = TopUpValidator.validateAmount(topUpAmountField.getText());

            /*
             * Temporary local object.
             * Later replace with TopUpRequestService.createTopUpRequest(amount).
             */
            TopUpRequest request = createTopUpRequest(profileUser.getId(), amount);

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
        replaceStyle(topUpStatusLabel, "status-warning");
    }

    private TopUpRequest createTopUpRequest(int userId, double amount) {
        TopUpRequest request = new TopUpRequest(userId, amount);
        topUpRequests.add(request);
        return request;
    }

    @FXML
    private void handleAddListing() {
        if (currentProfile == null || !currentProfile.isOwner() || !currentProfile.isSellerProfile()) {
            showError("Only the seller owner can add listings from this profile.");
            return;
        }

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
        if (currentProfile == null || !currentProfile.isOwner() || !currentProfile.isSellerProfile()) {
            addListingErrorLabel.setText("You are not allowed to add listings for this profile.");
            return;
        }

        String title = safeTrim(addListingTitleField.getText());
        String description = safeTrim(addListingDescriptionArea.getText());
        String category = addListingCategoryComboBox.getValue();
        String condition = addListingConditionComboBox.getValue();
        String startingPriceText = safeTrim(addListingStartingPriceField.getText());
        String reservePriceText = safeTrim(addListingReservePriceField.getText());

        if (title.isEmpty()) {
            addListingErrorLabel.setText("Title is required.");
            return;
        }

        if (description.isEmpty()) {
            addListingErrorLabel.setText("Description is required.");
            return;
        }

        if (category == null || category.trim().isEmpty()) {
            addListingErrorLabel.setText("Category is required.");
            return;
        }

        if (condition == null || condition.trim().isEmpty()) {
            addListingErrorLabel.setText("Condition is required.");
            return;
        }

        if (startingPriceText.isEmpty()) {
            addListingErrorLabel.setText("Starting price is required.");
            return;
        }

        if (selectedListingImageFile == null) {
            addListingErrorLabel.setText("Item image is required.");
            return;
        }

        try {
            BigDecimal startingPrice = new BigDecimal(startingPriceText);

            if (startingPrice.compareTo(BigDecimal.ZERO) <= 0) {
                addListingErrorLabel.setText("Starting price must be greater than 0.");
                return;
            }

            if (!reservePriceText.isEmpty()) {
                BigDecimal reservePrice = new BigDecimal(reservePriceText);

                if (reservePrice.compareTo(BigDecimal.ZERO) <= 0) {
                    addListingErrorLabel.setText("Reserve price must be greater than 0.");
                    return;
                }

                if (reservePrice.compareTo(startingPrice) < 0) {
                    addListingErrorLabel.setText("Reserve price must be greater than or equal to starting price.");
                    return;
                }
            }

            /*
             * Temporary:
             * Real creation should later call ItemService.createItem(...)
             * using the same seller-owned flow we designed.
             */
            addActivity("Created item", "Created new item: " + title + ".");
            handleCloseAddListing();
            refreshProfilePage();

        } catch (NumberFormatException exception) {
            addListingErrorLabel.setText("Please enter valid price numbers.");
        }
    }

    @FXML
    private void handleUploadListingImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Item Image");

        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.webp")
        );

        File selectedFile = fileChooser.showOpenDialog(addListingOverlay.getScene().getWindow());

        if (selectedFile == null) {
            return;
        }

        selectedListingImageFile = selectedFile;
        addListingImageNameLabel.setText(selectedFile.getName());

        Image image = new Image(selectedFile.toURI().toString());
        addListingImagePreview.setImage(image);

        addListingImagePreviewBox.setVisible(true);
        addListingImagePreviewBox.setManaged(true);
    }

    private void clearAddListingForm() {
        addListingTitleField.clear();
        addListingDescriptionArea.clear();
        addListingStartingPriceField.clear();
        addListingReservePriceField.clear();
        addListingErrorLabel.setText("");

        addListingCategoryComboBox.setValue("Electronics");
        addListingConditionComboBox.setValue("Used");

        selectedListingImageFile = null;
        addListingImageNameLabel.setText("No image selected");
        addListingImagePreview.setImage(null);
        addListingImagePreviewBox.setVisible(false);
        addListingImagePreviewBox.setManaged(false);
    }

    private void reloadListingsFromService() {
        loadListingsForProfile();
        renderSummaryLabels();
    }

    private void filterListings() {
        /*
         * Not used now because profile listings are currently rendered by included all_auctions.fxml.
         */
    }

    @FXML private void handleCategoryAll() { changeCategory("All"); }
    @FXML private void handleCategoryElectronics() { changeCategory("Electronics"); }
    @FXML private void handleCategoryFashion() { changeCategory("Fashion"); }
    @FXML private void handleCategoryCollectibles() { changeCategory("Collectibles"); }
    @FXML private void handleCategoryHome() { changeCategory("Home"); }
    @FXML private void handleCategoryBooks() { changeCategory("Books"); }
    @FXML private void handleCategoryOther() { changeCategory("Other"); }

    private void changeCategory(String category) {
        selectedListingCategory = category;
        updateCategoryChipStyles();
        filterListings();
    }

    private void updateCategoryChipStyles() {
        Button[] buttons = {
            categoryAllButton,
            categoryElectronicsButton,
            categoryFashionButton,
            categoryCollectiblesButton,
            categoryHomeButton,
            categoryBooksButton,
            categoryOtherButton
        };

        for (Button button : buttons) {
            if (button != null) {
                button.getStyleClass().remove("category-chip-active");
            }
        }

        switch (selectedListingCategory) {
            case "All" -> addStyle(categoryAllButton, "category-chip-active");
            case "Electronics" -> addStyle(categoryElectronicsButton, "category-chip-active");
            case "Fashion" -> addStyle(categoryFashionButton, "category-chip-active");
            case "Collectibles" -> addStyle(categoryCollectiblesButton, "category-chip-active");
            case "Home" -> addStyle(categoryHomeButton, "category-chip-active");
            case "Books" -> addStyle(categoryBooksButton, "category-chip-active");
            case "Other" -> addStyle(categoryOtherButton, "category-chip-active");
        }
    }

    private Integer convertCategoryToId(String category) {
        if (category == null) {
            return 106;
        }

        return switch (category) {
            case "Electronics" -> 101;
            case "Fashion" -> 102;
            case "Collectibles" -> 103;
            case "Home" -> 104;
            case "Books" -> 105;
            case "Other" -> 106;
            default -> 106;
        };
    }

    private String convertCategoryToName(Integer categoryId) {
        if (categoryId == null) {
            return "Other";
        }

        return switch (categoryId) {
            case 101 -> "Electronics";
            case 102 -> "Fashion";
            case 103 -> "Collectibles";
            case 104 -> "Home";
            case 105 -> "Books";
            case 106 -> "Other";
            default -> "Other";
        };
    }

    private void addActivity(String action, String description) {
        String now = LocalDateTime.now().format(dateTimeFormatter);
        activities.add(0, new ActivityLog(now, action, description));
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

    private String defaultText(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }

        return value.trim();
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private String formatRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            return "User";
        }

        return switch (role) {
            case "BUYER" -> "Buyer";
            case "SELLER" -> "Seller";
            case "AUCTION_ADMINISTRATOR" -> "Auction Admin";
            case "SYSTEM_ADMINISTRATOR" -> "System Admin";
            default -> role;
        };
    }

    private String formatRating(double rating) {
        return String.format("%.1f / 5.0", rating);
    }

    private String formatPrice(double value) {
        return String.format("$%,.2f", value);
    }

    private String formatPrice(BigDecimal value) {
        if (value == null) {
            return "$0.00";
        }

        return String.format("$%,.2f", value);
    }

    private void replaceStyle(Label label, String newStyleClass) {
        if (label == null) {
            return;
        }

        label.getStyleClass().removeAll(
            "status-neutral",
            "status-success",
            "status-warning",
            "status-error"
        );

        label.getStyleClass().add(newStyleClass);
    }

    private void addStyle(Node node, String styleClass) {
        if (node == null || styleClass == null) {
            return;
        }

        if (!node.getStyleClass().contains(styleClass)) {
            node.getStyleClass().add(styleClass);
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Profile Error");
        alert.setHeaderText(null);
        alert.setContentText(
            message == null || message.trim().isEmpty()
                ? "Something went wrong."
                : message
        );
        alert.showAndWait();
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