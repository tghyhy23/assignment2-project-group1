package com.group01.asm2.controllers;
import com.group01.asm2.services.NavigationService;
import com.group01.asm2.core.SessionManager;
import com.group01.asm2.models.Person;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;

import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.application.Platform;

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
        if (notificationButton != null) {
            Tooltip notiTooltip = new Tooltip("Notifications");
            notiTooltip.getStyleClass().add("custom-tooltip");
            bindCustomTooltip(notificationButton, notiTooltip, -25, 5);
        }

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