package com.group01.asm2.controllers;
import com.group01.asm2.services.NavigationService;
import com.group01.asm2.core.SessionManager;
import com.group01.asm2.models.Person;
import com.group01.asm2.models.Item;

import java.io.IOException;
import java.util.List;

import com.group01.asm2.utils.ScrollUtils;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;
import com.group01.asm2.models.NotificationItem;

import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.util.Duration;
import java.math.BigDecimal;

public class NavbarController {

    @FXML
    private HBox navbar;

    @FXML
    private SVGPath maximizePath1;
    @FXML
    private SVGPath maximizePath2;
    @FXML
    private SVGPath maximizePath3;
    @FXML
    private SVGPath maximizePath4;

    @FXML
    private Button wishlistButton;
    @FXML
    private HBox brandButton;
    @FXML
    private Button notificationButton;
    @FXML
    private Label notificationBadgeLabel;
    @FXML
    private Button loginButton;
    @FXML
    private MenuButton avatarMenu;
    @FXML
    private MenuItem profileMenuItem;
    @FXML
    private MenuItem signOutMenuItem;
    @FXML
    private HBox authenticatedActionsBox;
    @FXML
    private HBox searchBox;

    private AnchorPane contentArea;

    // --- BIẾN PHỤC VỤ KÉO THẢ VÀ TỰ ĐIỀU CHỈNH KÍCH THƯỚC ---
    private double xOffset;
    private double yOffset;

    // customMaximized = true vì file MainApplication của bạn đang khởi động mặc định ở dạng Full Màn Hình
    private boolean customMaximized = true;

    // Kích thước mặc định khi người dùng bấm "Thu nhỏ cửa sổ"
    private double savedWidth = 1000;
    private double savedHeight = 550;
    private double savedX;
    private double savedY;
    private boolean authPageMode = false;

    //Notification
    private Popup notificationPopup;
    private VBox notificationListBox;
    private PauseTransition hideNotificationDelay;
    private boolean mouseInsideNotificationButton = false;
    private boolean mouseInsideNotificationPopup = false;
    private static final double NOTIFICATION_POPUP_WIDTH = 380;

    public void setContentArea(AnchorPane contentArea) {
        this.contentArea = contentArea;
    }

    @FXML
    public void initialize() {

        enableWindowDrag();

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        savedX = bounds.getMinX() + (bounds.getWidth() - savedWidth) / 2;
        savedY = bounds.getMinY() + (bounds.getHeight() - savedHeight) / 2;

        maximizePath1.setContent("M20 10h-6V4");
        maximizePath2.setContent("m21 3-7 7");
        maximizePath3.setContent("m3 21 7-7");
        maximizePath4.setContent("M4 14h6v6");

        setupTooltips();

        setupNotificationDropdown();

        syncNavbarWithSession();

        Platform.runLater(() -> {
            if (navbar.getScene() != null && navbar.getScene().getRoot() != null) {
                navbar.getScene().getRoot().getStyleClass().add("window-root");
                navbar.getScene().getRoot().getStyleClass().add("maximized");
                navbar.getStyleClass().add("maximized");
            }
        });
    }

    // =====================================
    // SETUP TOOLTIPS
    // =====================================
    private void setupTooltips() {
        // 1. Tooltip Wishlist
        if (wishlistButton != null) {
            Tooltip wishlistTooltip = new Tooltip("Your Wishlist");
            wishlistTooltip.getStyleClass().add("custom-tooltip");
            bindCustomTooltip(wishlistButton, wishlistTooltip, -25, 5);
        }

        // 2. Tooltip Notifications
//        if (notificationButton != null) {
//            Tooltip notiTooltip = new Tooltip("Notifications");
//            notiTooltip.getStyleClass().add("custom-tooltip");
//            bindCustomTooltip(notificationButton, notiTooltip, -25, 5);
//        }

        // 3. Tooltip Profile
        if (avatarMenu != null) {
            Tooltip profileTooltip = new Tooltip("Account manager for user");
            profileTooltip.getStyleClass().add("custom-tooltip");

            avatarMenu.setOnMouseEntered(event -> {
                if (avatarMenu.isShowing()) return;

                if (avatarMenu.getScene() != null && avatarMenu.getScene().getWindow() != null) {
                    Point2D p = avatarMenu.localToScreen(
                            avatarMenu.getLayoutBounds().getMinX(),
                            avatarMenu.getLayoutBounds().getMaxY()
                    );
                    if (p != null) {
                        profileTooltip.show(avatarMenu, p.getX() - 60, p.getY() + 5);
                    }
                }
            });

            avatarMenu.setOnMouseExited(event -> profileTooltip.hide());

            avatarMenu.showingProperty().addListener((observable, wasShowing, isNowShowing) -> {
                if (isNowShowing) profileTooltip.hide();
            });
        }
    }

    private void setNodeVisible(Node node, boolean visible) {
        if (node == null) {
            return;
        }

        node.setVisible(visible);
        node.setManaged(visible);
    }

    private void syncNavbarWithSession() {
        Person currentUser = SessionManager.getCurrentUser();
        boolean isLoggedIn = currentUser != null;

        if (authPageMode) {
            setNodeVisible(searchBox, false);
            setNodeVisible(loginButton, false);
            setNodeVisible(authenticatedActionsBox, false);
            return;
        }

        setNodeVisible(searchBox, true);
        setNodeVisible(loginButton, !isLoggedIn);
        setNodeVisible(authenticatedActionsBox, isLoggedIn);

        if (!isLoggedIn) {
            return;
        }

        avatarMenu.setText(resolveAccountDisplayName(currentUser));
    }

    private String resolveAccountDisplayName(Person person) {
        if (person.getUsername() != null && !person.getUsername().isBlank()) {
            return person.getUsername();
        }

        if (person.getFullName() != null && !person.getFullName().isBlank()) {
            return person.getFullName();
        }

        return "Account";
    }

    private void bindCustomTooltip(Node node, Tooltip tooltip, double offsetX, double offsetY) {
        node.setOnMouseEntered(event -> {
            if (node.getScene() != null && node.getScene().getWindow() != null) {
                Point2D p = node.localToScreen(
                        node.getLayoutBounds().getMinX(),
                        node.getLayoutBounds().getMaxY()
                );
                if (p != null) {
                    tooltip.show(node, p.getX() + offsetX, p.getY() + offsetY);
                }
            }
        });
        node.setOnMouseExited(event -> tooltip.hide());
    }

    //Notification component
    private void setupNotificationDropdown() {
        if (notificationButton == null) {
            System.err.println("notificationButton is null. Check fx:id.");
            return;
        }

        hideNotificationDelay = new PauseTransition(Duration.millis(220));
        hideNotificationDelay.setOnFinished(event -> {
            if (!isMouseOverButtonOrPopup()) {
                hideNotificationDropdown();
            }
        });

        notificationPopup = new Popup();
        notificationPopup.setAutoHide(false);
        notificationPopup.setHideOnEscape(true);

        StackPane popupRoot = buildNotificationPopupContent();
        notificationPopup.getContent().add(popupRoot);

        notificationButton.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            mouseInsideNotificationButton = true;
            cancelHideNotificationDropdown();
            showNotificationDropdown();
        });

        notificationButton.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
            mouseInsideNotificationButton = false;
            hideNotificationDropdownWithDelay();
        });

        notificationButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            cancelHideNotificationDropdown();

            if (notificationPopup.isShowing()) {
                hideNotificationDropdown();
            } else {
                showNotificationDropdown();
            }
        });

        popupRoot.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            mouseInsideNotificationPopup = true;
            cancelHideNotificationDropdown();
        });

        popupRoot.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
            mouseInsideNotificationPopup = false;
            hideNotificationDropdownWithDelay();
        });

        populateMockNotifications();
    }

    private StackPane buildNotificationPopupContent() {
        StackPane popupRoot = new StackPane();
        popupRoot.getStyleClass().add("notification-popup-root");

        var navbarCssUrl = getClass().getResource("/com/group01/asm2/styles/layout/navbar.css");
        var exploreCssUrl = getClass().getResource("/com/group01/asm2/styles/views/explore.css");

        if (navbarCssUrl != null) {
            popupRoot.getStylesheets().add(navbarCssUrl.toExternalForm());
        } else {
            System.err.println("navbar.css not found for notification popup.");
        }

        if (exploreCssUrl != null) {
            popupRoot.getStylesheets().add(exploreCssUrl.toExternalForm());
        } else {
            System.err.println("explore.css not found for item image classes.");
        }

        VBox mainWrapper = new VBox();
        mainWrapper.setAlignment(Pos.TOP_RIGHT);
        mainWrapper.setPickOnBounds(false);

        VBox card = new VBox();
        card.getStyleClass().add("notification-popup-card");
        card.setMinWidth(NOTIFICATION_POPUP_WIDTH);
        card.setPrefWidth(NOTIFICATION_POPUP_WIDTH);
        card.setMaxWidth(NOTIFICATION_POPUP_WIDTH);

        VBox header = new VBox(4);
        header.getStyleClass().add("notification-header");

        Label title = new Label("New Notifications");
        title.getStyleClass().add("notification-title");

        Label subtitle = new Label("Latest updates from your auctions");
        subtitle.getStyleClass().add("notification-subtitle");

        header.getChildren().addAll(title, subtitle);

        notificationListBox = new VBox();
        notificationListBox.getStyleClass().add("notification-list");

        ScrollPane scrollPane = createNotificationScrollPane();

        card.getChildren().addAll(header, scrollPane);
        mainWrapper.getChildren().add(card);
        popupRoot.getChildren().add(mainWrapper);

        return popupRoot;
    }

    private boolean isMouseOverButtonOrPopup() {
        if (notificationButton == null) return false;

        try {
            Point2D mouse = notificationButton.getScene().getWindow().getScene().getRoot()
                    .localToScreen(0, 0);

            Bounds buttonBounds = notificationButton.localToScreen(notificationButton.getBoundsInLocal());

            if (buttonBounds == null) return false;

            /*
             * JavaFX does not provide global mouse position directly here,
             * so this method mainly works together with entered/exited flags.
             */
            return mouseInsideNotificationButton || mouseInsideNotificationPopup;

        } catch (Exception exception) {
            return mouseInsideNotificationButton || mouseInsideNotificationPopup;
        }
    }

    private void populateMockNotifications() {
        Item rolex = new Item(
                1,
                "Rolex Submariner 2020",
                new BigDecimal("150000000"),
                new BigDecimal("168000000"),
                24,
                true,
                "item-bg-watch",
                new String[]{"item-bg-watch", "item-bg-luxury", "item-bg-jewelry"}
        );

        Item camera = new Item(
                2,
                "Vintage Camera Collection",
                new BigDecimal("4500000"),
                new BigDecimal("6200000"),
                18,
                false,
                "item-bg-camera",
                new String[]{"item-bg-camera", "item-bg-vintage"}
        );

        Item chair = new Item(
                3,
                "Antique Wooden Chair",
                new BigDecimal("2500000"),
                new BigDecimal("3900000"),
                12,
                false,
                "item-bg-furniture",
                new String[]{"item-bg-furniture", "item-bg-antique"}
        );

        Item painting = new Item(
                4,
                "19th Century Oil Painting",
                new BigDecimal("45000000"),
                new BigDecimal("51000000"),
                9,
                true,
                "item-bg-art",
                new String[]{"item-bg-art", "item-bg-painting"}
        );

        List<NotificationItem> mockNotifications = List.of(
                new NotificationItem(
                        rolex,
                        "Top-up request approved",
                        "Your top-up request has been approved. You can now continue bidding.",
                        "2 min ago",
                        true
                ),
                new NotificationItem(
                        camera,
                        "You have been outbid",
                        "Someone placed a higher bid on " + camera.getTitle() + ".",
                        "8 min ago",
                        true
                ),
                new NotificationItem(
                        rolex,
                        "Auction ending soon",
                        rolex.getTitle() + " will end in less than 30 minutes.",
                        "15 min ago",
                        true
                ),
                new NotificationItem(
                        chair,
                        "You won an auction",
                        "Congratulations! You won the auction for " + chair.getTitle() + ".",
                        "1 hour ago",
                        false
                ),
                new NotificationItem(
                        painting,
                        "New bid on your listing",
                        "A buyer has placed a new bid on " + painting.getTitle() + ".",
                        "3 hours ago",
                        false
                ),
                new NotificationItem(
                        camera,
                        "Watchlist item updated",
                        camera.getTitle() + " has a new current highest bid.",
                        "Yesterday",
                        false
                )
        );

        notificationListBox.getChildren().clear();

        if (mockNotifications.isEmpty()) {
            Label emptyLabel = new Label("No notifications yet.");
            emptyLabel.getStyleClass().add("notification-empty-text");
            notificationListBox.getChildren().add(emptyLabel);

            if (notificationBadgeLabel != null) {
                notificationBadgeLabel.setVisible(false);
                notificationBadgeLabel.setManaged(false);
            }

            return;
        }

        for (NotificationItem item : mockNotifications) {
            notificationListBox.getChildren().add(createNotificationRow(item));
        }

        long unreadCount = mockNotifications.stream()
                .filter(NotificationItem::isUnread)
                .count();

        updateNotificationBadge(unreadCount);
    }

    private HBox createNotificationRow(NotificationItem notification) {
        HBox row = new HBox(12);
        row.getStyleClass().add("notification-row");
        row.setAlignment(Pos.TOP_LEFT);

        StackPane itemImageBox = new StackPane();
        itemImageBox.getStyleClass().add("notification-item-image");

        Item item = notification.getItem();

        if (item != null && item.getMainBgClass() != null && !item.getMainBgClass().isBlank()) {
            itemImageBox.getStyleClass().add(item.getMainBgClass());
        } else {
            itemImageBox.getStyleClass().add("notification-item-image-placeholder");
        }

        VBox textBox = new VBox(5);
        textBox.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Label titleLabel = new Label(notification.getTitle());
        titleLabel.getStyleClass().add("notification-item-title");

        Label messageLabel = new Label(notification.getMessage());
        messageLabel.getStyleClass().add("notification-item-message");
        messageLabel.setWrapText(true);

        Label timeLabel = new Label(notification.getTime());
        timeLabel.getStyleClass().add("notification-time");

        textBox.getChildren().addAll(titleLabel, messageLabel, timeLabel);

        VBox rightBox = new VBox();
        rightBox.setAlignment(Pos.TOP_RIGHT);

        if (notification.isUnread()) {
            Region unreadDot = new Region();
            unreadDot.getStyleClass().add("notification-unread-dot");
            rightBox.getChildren().add(unreadDot);
        }

        row.getChildren().addAll(itemImageBox, textBox, rightBox);

        row.setOnMouseClicked(event -> {
            System.out.println("Notification clicked: " + notification.getTitle());

            if (notification.getItem() != null) {
                System.out.println("Related item: " + notification.getItem().getTitle());
            }
        });

        return row;
    }

    private void showNotificationDropdown() {
        if (notificationPopup == null || notificationButton == null) return;

        cancelHideNotificationDropdown();

        Bounds buttonBounds = notificationButton.localToScreen(notificationButton.getBoundsInLocal());

        if (buttonBounds == null) {
            System.err.println("Cannot get notification button bounds.");
            return;
        }

        double popupX = buttonBounds.getMaxX() - NOTIFICATION_POPUP_WIDTH;
        double popupY = buttonBounds.getMaxY() - 2;

        if (!notificationPopup.isShowing()) {
            notificationPopup.show(notificationButton, popupX, popupY);
        }
    }

    private void hideNotificationDropdownWithDelay() {
        if (hideNotificationDelay != null) {
            hideNotificationDelay.playFromStart();
        }
    }

    private void cancelHideNotificationDropdown() {
        if (hideNotificationDelay != null) {
            hideNotificationDelay.stop();
        }
    }

    private void hideNotificationDropdown() {
        if (notificationPopup != null && notificationPopup.isShowing()) {
            notificationPopup.hide();
        }
    }

    private void updateNotificationBadge(long unreadCount) {
        if (notificationBadgeLabel == null) return;

        if (unreadCount <= 0) {
            notificationBadgeLabel.setVisible(false);
            notificationBadgeLabel.setManaged(false);
            return;
        }

        notificationBadgeLabel.setVisible(true);
        notificationBadgeLabel.setManaged(true);

        if (unreadCount > 9) {
            notificationBadgeLabel.setText("9+");
        } else {
            notificationBadgeLabel.setText(String.valueOf(unreadCount));
        }
    }

    private ScrollPane createNotificationScrollPane() {
        ScrollPane scrollPane = new ScrollPane(notificationListBox);

        scrollPane.getStyleClass().add("notification-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        scrollPane.setPrefViewportHeight(340);
        scrollPane.setMaxHeight(340);

        // Use existing app utility for smooth scrolling
        ScrollUtils.makeSmooth(scrollPane, 0.003);

        return scrollPane;
    }

    // =========================
    // Handle Navigation
    // =========================
    @FXML
    private void handleGoToProfile() {
        if (!SessionManager.isLoggedIn()) {
            goToLoginScene();
            return;
        }

        if (contentArea != null) {
            NavigationService.loadPage(contentArea, "/com/group01/asm2/views/profile-view.fxml");
        }
    }

    @FXML
    private void handleGoToExplore() {
        if (contentArea != null && !authPageMode) {
            NavigationService.loadPage(
                contentArea,
                "/com/group01/asm2/views/explore-view.fxml"
            );
            return;
        }

        goToMainLayoutScene();
    }

    @FXML
    private void handleGoToLogin() {
        goToLoginScene();
    }

    public void setAuthPageMode(boolean authPageMode) {
        this.authPageMode = authPageMode;
        syncNavbarWithSession();
    }

    @FXML
    private void handleSignOut() {
        SessionManager.logout();
        syncNavbarWithSession();
        goToLoginScene();
    }

    private void goToLoginScene() {
        try {
            var authLayoutUrl = getClass().getResource("/com/group01/asm2/layout/auth-layout.fxml");

            if (authLayoutUrl == null) {
                System.out.println("Cannot find auth-layout.fxml at /com/group01/asm2/layout/auth-layout.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(authLayoutUrl);
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) navbar.getScene().getWindow();

            if (stage == null) {
                System.out.println("Cannot navigate because stage is null.");
                return;
            }

            stage.setScene(scene);

            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());

            stage.show();

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @FXML
    private void handleGoToWatchList() {
        if (!SessionManager.isLoggedIn()) {
            goToLoginScene();
            return;
        }

        if (contentArea != null) {
            NavigationService.loadPage(
                    contentArea,
                    "/com/group01/asm2/views/watchlist-view.fxml"
            );
        }
    }

    private void goToMainLayoutScene() {
        try {
            var mainLayoutUrl = getClass().getResource("/com/group01/asm2/layout/main-layout.fxml");

            if (mainLayoutUrl == null) {
                System.out.println("Cannot find main-layout.fxml at /com/group01/asm2/layout/main-layout.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(mainLayoutUrl);
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) navbar.getScene().getWindow();

            if (stage == null) {
                System.out.println("Cannot navigate because stage is null.");
                return;
            }

            stage.setScene(scene);

            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());

            stage.show();

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    // =========================
    // Window Controls & Drag
    // =========================
    private void enableWindowDrag() {
        navbar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        navbar.setOnMouseDragged(event -> {
            Stage stage = (Stage) navbar.getScene().getWindow();
            // allows to move the app when minimize
            if (stage != null && !customMaximized) {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            }
        });

        navbar.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleMaximize();
            }
        });
    }

    @FXML
    private void handleMinimize() {
        Stage stage = (Stage) navbar.getScene().getWindow();
        if (stage != null) stage.setIconified(true);
    }

    @FXML
    private void handleMaximize() {
        Stage stage = (Stage) navbar.getScene().getWindow();
        if (stage == null) return;

        if (customMaximized) {
            // maximize -> minimize
            stage.setX(savedX);
            stage.setY(savedY);
            stage.setWidth(savedWidth);
            stage.setHeight(savedHeight);
            customMaximized = false;

            maximizePath1.setContent("M15 3h6v6");
            maximizePath2.setContent("m21 3-7 7");
            maximizePath3.setContent("m3 21 7-7");
            maximizePath4.setContent("M9 21H3v-6");

            navbar.getScene().getRoot().getStyleClass().remove("maximized");
            navbar.getStyleClass().remove("maximized");
        } else {
            // minimize -> maximize
            savedX = stage.getX();
            savedY = stage.getY();
            savedWidth = stage.getWidth();
            savedHeight = stage.getHeight();

            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
            customMaximized = true;

            maximizePath1.setContent("m14 10 7-7");
            maximizePath2.setContent("M20 10h-6V4");
            maximizePath3.setContent("m3 21 7-7");
            maximizePath4.setContent("M4 14h6v6");

            if (!navbar.getScene().getRoot().getStyleClass().contains("maximized")) {
                navbar.getScene().getRoot().getStyleClass().add("maximized");
            }
            if (!navbar.getStyleClass().contains("maximized")) {
                navbar.getStyleClass().add("maximized");
            }

        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) navbar.getScene().getWindow();
        if (stage != null) stage.close();
    }
}