package com.group01.asm2.controllers;

/**
 * @author Group 01
 */

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AdminDashboardController {

    @FXML private Label totalUsersLabel;
    @FXML private Label activeAuctionsLabel;
    @FXML private Label totalItemsLabel;
    @FXML private Label pendingTopupsLabel;

    @FXML
    public void initialize() {

        // TODO:
        // gọi service lấy thống kê

        totalUsersLabel.setText("24");
        activeAuctionsLabel.setText("18");
        totalItemsLabel.setText("63");
        pendingTopupsLabel.setText("5");
    }
}