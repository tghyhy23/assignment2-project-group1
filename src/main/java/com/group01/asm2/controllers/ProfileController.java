package com.group01.asm2.controllers;

import com.group01.asm2.dtos.AuctionFilter;
import com.group01.asm2.dtos.ItemFilter;
import com.group01.asm2.enums.AuctionStatus;
import com.group01.asm2.models.Item;
import javafx.scene.shape.SVGPath;
import javafx.animation.TranslateTransition;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import com.group01.asm2.services.ItemService;
import com.group01.asm2.utils.ScrollUtils;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProfileController {

    private ItemService itemService = new ItemService();

    @FXML private ScrollPane profileScrollPane;

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

    @FXML private TextField listingSearchField;
    @FXML private TilePane listingsCardContainer;

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
    @FXML private Button editProfileButton;
    @FXML private TextField editFullNameField;
    @FXML private TextField editEmailField;
    @FXML private TextField editPhoneField;
    @FXML private TextField editDateOfBirthField;
    @FXML private TextField editAddressField;

    @FXML private StackPane addListingOverlay;
    @FXML private VBox addListingModal;
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

    private String selectedListingCategory = "All";

    private final ObservableList<Item> listings = FXCollections.observableArrayList();
    private final ObservableList<ActivityLog> activities = FXCollections.observableArrayList();

    private final DateTimeFormatter dateTimeFormatter =
        DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    @FXML
    public void initialize() {
        ScrollUtils.makeSmooth(profileScrollPane);

        loadDataFromServices();
        setupProfileHeader();
        updateSummaryLabels();
        setupTabSwitching();
        setupResponsiveEditProfileButton();
        setupActivityTable();
        makeTableResponsive(activityTable);
        setupListingsCards();
        setupAddListingModal();
        setupResponsiveLayout();

        listingSearchField.textProperty().addListener((observable, oldValue, newValue) -> filterListings());
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

    private void reloadListingsFromService() {
        ItemFilter filter = new ItemFilter();
        listings.setAll(itemService.readItems(filter));
        filterListings();
        updateSummaryLabels();
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
        avatarLabel.setText(createInitials(fullName));
    }

    private String createInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "U";

        String[] parts = name.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) {
                initials.append(Character.toUpperCase(part.charAt(0)));
            }

            if (initials.length() == 2) break;
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
        profileTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab != null) {
                selectedTabTitleLabel.setText(newTab.getText());
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

    private void setupListingsCards() {
        updateCategoryChipStyles();
        filterListings();

        listingsCardContainer.widthProperty().addListener((observable, oldValue, newValue) -> filterListings());
    }

    private void filterListings() {
        listingsCardContainer.getChildren().clear();

        String keyword = listingSearchField.getText() == null
            ? ""
            : listingSearchField.getText().trim().toLowerCase();

        for (Item item : listings) {
            if (item == null) continue;

            String title = item.getTitle() == null ? "" : item.getTitle().toLowerCase();

            boolean matchesSearch = title.contains(keyword);

            boolean matchesCategory = selectedListingCategory.equals("All")
                || convertCategoryToName(item.getCategoryId()).equalsIgnoreCase(selectedListingCategory);

            if (matchesSearch && matchesCategory) {
                listingsCardContainer.getChildren().add(createListingCard(item));
            }
        }
    }

    private VBox createListingCard(Item item) {
        VBox card = new VBox(12);
        card.getStyleClass().add("listing-card");

        double cardWidth = calculateCardWidth();
        card.setPrefWidth(cardWidth);
        card.setMinWidth(cardWidth);
        card.setMaxWidth(cardWidth);

        StackPane imageSection = createListingImageSection(item, card);
        setupListingCardHoverAnimation(card, imageSection);

        Rectangle clip = new Rectangle();

        clip.widthProperty().bind(imageSection.widthProperty());
        clip.heightProperty().bind(imageSection.heightProperty());

        clip.setArcWidth(0);
        clip.setArcHeight(0);

        imageSection.setClip(clip);

        VBox contentBox = new VBox(10);
        contentBox.getStyleClass().add("listing-card-content");

        Label titleLabel = new Label(item.getTitle() != null ? item.getTitle() : "Unknown Item");
        titleLabel.getStyleClass().add("item-title");
        titleLabel.setWrapText(true);

        titleLabel.setMinHeight(38);
        titleLabel.setPrefHeight(38);
        titleLabel.setMaxHeight(38);

        Label categoryLabel = new Label(convertCategoryToName(item.getCategoryId()));
        categoryLabel.getStyleClass().add("item-category-text");

        Label startingPriceLabel = new Label("Starting price");
        startingPriceLabel.getStyleClass().add("price-label");

        Label startingPriceValue = new Label(formatPrice(item.getStartingPrice()));
        startingPriceValue.getStyleClass().add("starting-price-value");

        Label currentBidLabel = new Label("Current bid");
        currentBidLabel.getStyleClass().add("price-label");

//        Label currentBidValue = new Label(formatPrice(item.getCurrentBid()));
//        currentBidValue.getStyleClass().add("current-bid-value");

        HBox priceRow = new HBox(22);
        priceRow.getChildren().addAll(
//                new VBox(3, startingPriceLabel, startingPriceValue),
//                new VBox(3, currentBidLabel, currentBidValue)
        );

        Label dateLabel = new Label(
            item.getCreatedAt() != null
                ? "Created: " + item.getCreatedAt().toLocalDate()
                : "Created: N/A"
        );
        dateLabel.getStyleClass().add("listing-date");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        contentBox.getChildren().addAll(
            titleLabel,
            categoryLabel,
            priceRow,
            spacer,
            dateLabel
        );

        card.getChildren().addAll(imageSection, contentBox);

        return card;
    }

    private void setupListingCardHoverAnimation(VBox card, Node imageContent) {
        TranslateTransition cardMove = new TranslateTransition(Duration.seconds(0.18), card);

        card.setOnMouseEntered(event -> {
            cardMove.setToY(-4);
            cardMove.playFromStart();
        });

        card.setOnMouseExited(event -> {
            cardMove.setToY(0);
            cardMove.playFromStart();
        });
    }

    private StackPane createListingImageSection(Item item, VBox card) {
        StackPane imageSection = new StackPane();
        imageSection.getStyleClass().add("item-image-section");

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(imageSection.widthProperty());
        clip.heightProperty().bind(imageSection.heightProperty());
        clip.setArcWidth(44);
        clip.setArcHeight(44);
        imageSection.setClip(clip);

        StackPane imagePlaceholder = new StackPane();
        imagePlaceholder.getStyleClass().add("item-image-placeholder");

        Label iconLabel = new Label(getItemIcon(item));
        iconLabel.getStyleClass().add("item-placeholder-icon");
        imagePlaceholder.getChildren().add(iconLabel);

        Button moreButton = new Button("...");
        moreButton.getStyleClass().add("item-more-button");

        VBox actionMenu = createItemActionMenu(item);
        actionMenu.setVisible(false);
        actionMenu.setManaged(false);

        moreButton.setOnAction(event -> {
            event.consume();

            boolean showing = actionMenu.isVisible();
            actionMenu.setVisible(!showing);
            actionMenu.setManaged(!showing);
        });

        imageSection.setOnMouseExited(event -> {
            actionMenu.setVisible(false);
            actionMenu.setManaged(false);
        });

        StackPane.setAlignment(moreButton, javafx.geometry.Pos.TOP_RIGHT);
        StackPane.setMargin(moreButton, new Insets(12, 12, 0, 0));

        StackPane.setAlignment(actionMenu, javafx.geometry.Pos.TOP_RIGHT);
        StackPane.setMargin(actionMenu, new Insets(54, 12, 0, 0));

        imageSection.getChildren().addAll(imagePlaceholder, moreButton, actionMenu);

        setupListingCardHoverAnimation(card, iconLabel);

        return imageSection;
    }

    private VBox createItemActionMenu(Item item) {
        VBox menu = new VBox(2);
        menu.setFillWidth(true);
        menu.setMaxWidth(Region.USE_PREF_SIZE);
        menu.setMaxHeight(Region.USE_PREF_SIZE);
        menu.getStyleClass().add("item-action-menu");

        Button editButton = new Button("Edit");
        editButton.getStyleClass().add("item-action-menu-button");
        editButton.setGraphic(createEditIcon());
        editButton.setGraphicTextGap(2);

        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().addAll("item-action-menu-button", "item-action-menu-delete");
        deleteButton.setGraphic(createDeleteIcon());
        deleteButton.setGraphicTextGap(2);

        editButton.setOnAction(event -> {
            event.consume();
            addActivity("Edit item", "Edit action selected for " + item.getTitle() + ".");
            menu.setVisible(false);
            menu.setManaged(false);
        });

        deleteButton.setOnAction(event -> {
            event.consume();
            handleDeleteItem(item);
            menu.setVisible(false);
            menu.setManaged(false);
        });

        menu.getChildren().addAll(editButton, deleteButton);

        return menu;
    }

    private double calculateCardWidth() {
        double containerWidth = listingsCardContainer.getWidth();

        if (containerWidth <= 0) {
            return 280;
        }

        double gap = listingsCardContainer.getHgap();
        double totalGap = gap * 3; // 4 items = 3 gaps

        return (containerWidth - totalGap) / 4;
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
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Request Top-up");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStyleClass().add("profile-dialog");
        dialogPane.getStylesheets().add(
            getClass().getResource("/com/group01/asm2/styles/views/profile.css").toExternalForm()
        );

        ButtonType submitButtonType = new ButtonType("Submit Request", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().addAll(submitButtonType, cancelButtonType);

        TextField amountField = new TextField();
        amountField.setPromptText("Enter amount between 10 and 5000");
        amountField.getStyleClass().add("profile-text-field");

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");

        VBox content = new VBox(12);
        content.getStyleClass().add("dialog-card");
        content.setPadding(new Insets(20));

        Label title = new Label("Request Account Top-up");
        title.getStyleClass().add("dialog-title");

        Label subtitle = new Label("Enter the amount you want to add. The request will be sent to an administrator for approval.");
        subtitle.getStyleClass().add("dialog-subtitle");

        content.getChildren().addAll(title, subtitle, createFieldGroup("Top-up Amount", amountField), errorLabel);
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

                topUpStatusLabel.setText(String.format("Pending approval: $%,.2f", amount));
                topUpStatusLabel.getStyleClass().removeAll("status-neutral", "status-success", "status-warning");
                topUpStatusLabel.getStyleClass().add("status-warning");

                addActivity("Requested top-up", String.format("Requested a top-up of $%,.2f.", amount));

                dialog.close();

            } catch (NumberFormatException exception) {
                errorLabel.setText("Please enter a valid number.");
                event.consume();
            }
        });

        dialog.showAndWait();
    }

    private VBox createFieldGroup(String labelText, TextField textField) {
        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");

        VBox group = new VBox(6);
        group.getChildren().addAll(label, textField);

        return group;
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
            BigDecimal startingPrice = new BigDecimal(addListingStartingPriceField.getText().trim());
            BigDecimal currentBid = new BigDecimal(addListingCurrentBidField.getText().trim());

            if (startingPrice.compareTo(BigDecimal.ZERO) < 0 || currentBid.compareTo(BigDecimal.ZERO) < 0) {
                addListingErrorLabel.setText("Price values must be positive.");
                return;
            }

            if (icon.isEmpty()) icon = "📦";

//            Item newItem = new Item(
//                    null,
//                    convertCategoryToId(category),
//                    99,
//                    name,
//                    "No description yet.",
//                    "Used",
//                    "Unknown",
//                    "Ho Chi Minh City",
//                    startingPrice,
//                    null,
//                    null,
//                    null
//            );
//
//            newItem.setCurrentBid(currentBid);
//            newItem.setBidCount(0);
//            newItem.setRecommended(false);
//            newItem.setMainBgClass(icon);

//            Item createdItem = ItemService.createItem(newItem);

            reloadListingsFromService();

//            addActivity("Created item", "Created new item: " + createdItem.getTitle() + ".");
            handleCloseAddListing();

        } catch (NumberFormatException exception) {
            addListingErrorLabel.setText("Please enter valid price numbers.");
        }
    }

    private void handleDeleteItem(Item item) {
        if (item == null || item.getId() == null) return;
        Item deletedItem = itemService.readItem(item.getId());
        itemService.deleteItem(item.getId());

        reloadListingsFromService();
        addActivity("Deleted item", "Deleted item: " + deletedItem.getTitle() + ".");
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

    private Integer convertCategoryToId(String category) {
        if (category == null) return 106;

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
        if (categoryId == null) return "Other";

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

    private String getItemIcon(Item item) {
        if (item == null) return "📦";

//        String mainBgClass = item.getMainBgClass();

//        if (mainBgClass != null && !mainBgClass.trim().isEmpty()) {
//            return mainBgClass;
//        }

        String category = convertCategoryToName(item.getCategoryId());

        return switch (category) {
            case "Electronics" -> "📱";
            case "Fashion" -> "👕";
            case "Collectibles" -> "🎴";
            case "Home" -> "💡";
            case "Books" -> "📚";
            default -> "📦";
        };
    }

    private void addActivity(String action, String description) {
        String now = LocalDateTime.now().format(dateTimeFormatter);
        activities.add(0, new ActivityLog(now, action, description));
    }

    private String formatPrice(double value) {
        return String.format("$%,.2f", value);
    }

    private String formatPrice(BigDecimal value) {
        if (value == null) return "N/A";
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

        public String getDateTime() { return dateTime.get(); }
        public String getAction() { return action.get(); }
        public String getDescription() { return description.get(); }
    }

    private SVGPath createEditIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent(
            "M21.174 6.812a1 1 0 0 0-3.986-3.987 " +
                "L3.842 16.174a2 2 0 0 0-.5.83 " +
                "l-1.321 4.352a.5.5 0 0 0 .623.622 " +
                "l4.353-1.32a2 2 0 0 0 .83-.497z " +
                "M15 5l4 4"
        );
        icon.getStyleClass().add("item-menu-icon");
        return icon;
    }

    private SVGPath createDeleteIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent(
            "M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6 " +
                "M3 6h18 " +
                "M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"
        );
        icon.getStyleClass().add("item-menu-icon");
        return icon;
    }
}
