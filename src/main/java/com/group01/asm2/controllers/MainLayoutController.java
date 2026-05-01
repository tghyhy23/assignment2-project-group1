package com.group01.asm2.controllers;

import com.group01.asm2.services.NavigationService;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

public class MainLayoutController {
    @FXML
    private NavbarController navbarController;

    @FXML
    private AnchorPane contentArea;

    @FXML
    private SidebarController sidebarController;

    @FXML
    public void initialize() {
        if (sidebarController != null) {
            sidebarController.setContentArea(contentArea);
        }
        if (navbarController != null) {
            navbarController.setContentArea(contentArea);
        }

        NavigationService.loadPage(contentArea, "/com/group01/asm2/views/explore-view.fxml");
    }
}