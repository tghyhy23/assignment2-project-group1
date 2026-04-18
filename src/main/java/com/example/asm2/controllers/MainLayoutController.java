package com.example.asm2.controllers;

import com.example.asm2.services.NavigationService;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

public class MainLayoutController {

    @FXML
    private AnchorPane contentArea;

    @FXML
    private SidebarController sidebarController;

    @FXML
    public void initialize() {
        if (sidebarController != null) {
            sidebarController.setContentArea(contentArea);
        }

        NavigationService.loadPage(contentArea, "/com/example/asm2/views/dashboard-view.fxml");
    }
}