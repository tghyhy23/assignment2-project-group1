package com.example.asm2.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.MenuButton; // Đã thêm dòng import này
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

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

    // Đã gộp 2 hàm initialize() lại làm 1
    @FXML
    public void initialize() {
        enableWindowDrag();

        if (avatarMenu != null) {
            avatarMenu.setOnMouseEntered(event -> avatarMenu.show());
        }
    }

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