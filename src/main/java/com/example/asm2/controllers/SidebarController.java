package com.example.asm2.controllers;

import com.example.asm2.services.NavigationService;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

public class SidebarController {

    private AnchorPane contentArea;

    // MainLayout call function and transmit contentArea
    public void setContentArea(AnchorPane contentArea) {
        this.contentArea = contentArea;
    }

    @FXML
    private void showDashboard() {
        if (contentArea != null) {
            NavigationService.loadPage(contentArea, "/com/example/asm2/views/dashboard-view.fxml");
        }
    }

    @FXML
    private void showCustomers() {
        if (contentArea != null) {
            NavigationService.loadPage(contentArea, "/com/example/asm2/views/customers-view.fxml");
        }
    }
}