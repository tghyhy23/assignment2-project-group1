package com.group01.asm2.controllers;

import com.group01.asm2.services.NavigationService;
import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.geometry.Point2D;
import javafx.scene.control.Tooltip;

public class NavbarController {

    @FXML
    private HBox titleBar;

    @FXML
    private SVGPath maximizePath1;

    @FXML
    private SVGPath maximizePath2;

    @FXML
    private SVGPath maximizePath3;

    @FXML
    private SVGPath maximizePath4;

    @FXML
    private MenuButton avatarMenu;

    private double xOffset;
    private double yOffset;

    // Vùng chứa nội dung chính để thay đổi các trang
    private AnchorPane contentArea;

    // Setter để tiêm (inject) contentArea từ MainController vào
    public void setContentArea(AnchorPane contentArea) {
        this.contentArea = contentArea;
    }

    @FXML
    public void initialize() {
        enableWindowDrag();

        // 1. Create Tooltip with styleClass
        Tooltip profileTooltip = new Tooltip("Account manager for user");
        profileTooltip.getStyleClass().add("custom-tooltip");

        if (avatarMenu != null) {
            // Hover in: turn on Tooltip
            avatarMenu.setOnMouseEntered(event -> {
                // If menubar is shown -> turn off Tooltip
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

            // 3. Hover out: hide Tooltip
            avatarMenu.setOnMouseExited(event -> {
                profileTooltip.hide();
            });

            // Click -> turn off Tooltip
            avatarMenu.showingProperty().addListener((observable, wasShowing, isNowShowing) -> {
                if (isNowShowing) {
                    profileTooltip.hide();
                }
            });
        }
    }

    // =========================
    // Xử lý Navigation
    // =========================

    @FXML
    private void handleGoToProfile() {
        if (contentArea != null) {
            NavigationService.loadPage(contentArea, "/com/group01/asm2/views/profile-view.fxml");
        } else {
            System.err.println("Lỗi: contentArea chưa được khởi tạo trong NavbarController!");
        }
    }

    // =========================
    // Xử lý Window Controls
    // =========================

    private void enableWindowDrag() {
        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        titleBar.setOnMouseDragged(event -> {
            Stage stage = (Stage) titleBar.getScene().getWindow();
            if (stage != null && !stage.isMaximized()) {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            }
        });

        titleBar.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleMaximize();
            }
        });
    }

    @FXML
    private void handleMinimize() {
        Stage stage = (Stage) titleBar.getScene().getWindow();
        if (stage != null) stage.setIconified(true);
    }

    @FXML
    private void handleMaximize() {
        Stage stage = (Stage) titleBar.getScene().getWindow();
        if (stage == null) return;

        stage.setMaximized(!stage.isMaximized());

        if (stage.isMaximized()) {
            maximizePath1.setContent("m14 10 7-7");
            maximizePath2.setContent("M20 10h-6V4");
            maximizePath3.setContent("m3 21 7-7");
            maximizePath4.setContent("M4 14h6v6");
        } else {
            maximizePath1.setContent("M15 3h6v6");
            maximizePath2.setContent("m21 3-7 7");
            maximizePath3.setContent("m3 21 7-7");
            maximizePath4.setContent("M9 21H3v-6");
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) titleBar.getScene().getWindow();
        if (stage != null) stage.close();
    }
}