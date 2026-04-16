package com.example.asm2.controllers;

import com.example.asm2.services.NavigationService;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

public class MainLayoutController {

    @FXML
    private AnchorPane contentArea;

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

    private double xOffset;
    private double yOffset;

    @FXML
    public void initialize() {
        NavigationService.loadPage(contentArea, "/com/example/asm2/views/dashboard-view.fxml");
        enableWindowDrag();
    }

    private void enableWindowDrag() {
        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        titleBar.setOnMouseDragged(event -> {
            Stage stage = (Stage) titleBar.getScene().getWindow();
            if (!stage.isMaximized()) {
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
        stage.setIconified(true);
    }

    @FXML
    private void handleMaximize() {
        Stage stage = (Stage) titleBar.getScene().getWindow();
        stage.setMaximized(!stage.isMaximized());

        if (stage.isMaximized()) {
            // switch to "restore" icon using your minimize SVG
            maximizePath1.setContent("m14 10 7-7");
            maximizePath2.setContent("M20 10h-6V4");
            maximizePath3.setContent("m3 21 7-7");
            maximizePath4.setContent("M4 14h6v6");
        } else {
            // switch back to maximize SVG
            maximizePath1.setContent("M15 3h6v6");
            maximizePath2.setContent("m21 3-7 7");
            maximizePath3.setContent("m3 21 7-7");
            maximizePath4.setContent("M9 21H3v-6");
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) titleBar.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void showDashboard() {
        NavigationService.loadPage(contentArea, "/com/example/asm2/views/dashboard-view.fxml");
    }

    @FXML
    private void showCustomers() {
        NavigationService.loadPage(contentArea, "/com/example/asm2/views/customers-view.fxml");
    }
}