package com.group01.asm2.controllers;

import com.group01.asm2.services.NavigationService;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

public class AuthLayoutController {

    @FXML
    private AnchorPane contentArea;

    @FXML
    private NavbarController navbarController;

    @FXML
    public void initialize() {
        if (navbarController != null) {
            navbarController.setContentArea(contentArea);
            navbarController.setAuthPageMode(true);
        }

        NavigationService.loadPage(
            contentArea,
            "/com/group01/asm2/views/login.fxml"
        );
    }
}