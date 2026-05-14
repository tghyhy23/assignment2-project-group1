package com.group01.asm2.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class AdminAuctionController {

    @FXML private Button overviewTab;
    @FXML private Button usersTab;
    @FXML private Button auctionsTab;
    @FXML private Button itemsTab;
    @FXML private Button paymentsTab;

    @FXML private AnchorPane contentArea;

    @FXML
    public void initialize() {
        showOverview();
    }

    @FXML
    private void showOverview() {
        setActiveTab(overviewTab);
        setContent("Overview", "Summary statistics and quick auction system overview.");
    }

    @FXML
    private void showUsers() {
        setActiveTab(usersTab);
        setContent("Users", "Manage registered users and admin accounts.");
    }

    @FXML
    private void showAuctions() {
        setActiveTab(auctionsTab);
        setContent("Auctions", "Manage auction lifecycle, status, winners, and due auctions.");
    }

    @FXML
    private void showItems() {
        setActiveTab(itemsTab);
        setContent("Items", "Moderate item listings, prices, conditions, and images.");
    }

    @FXML
    private void showPayments() {
        setActiveTab(paymentsTab);
        setContent("Payments", "Review payments, commissions, and seller payouts.");
    }

    private void setActiveTab(Button activeButton) {
        Button[] tabs = {
                overviewTab, usersTab, auctionsTab, itemsTab, paymentsTab
        };

        for (Button tab : tabs) {
            tab.getStyleClass().remove("active-tab");
        }

        activeButton.getStyleClass().add("active-tab");
    }

    private void setContent(String title, String desc) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-title");

        Label descLabel = new Label(desc);
        descLabel.getStyleClass().add("auction-subtitle");

        VBox box = new VBox(8, titleLabel, descLabel);
        box.setStyle("-fx-padding: 10;");

        contentArea.getChildren().setAll(box);

        AnchorPane.setTopAnchor(box, 0.0);
        AnchorPane.setRightAnchor(box, 0.0);
        AnchorPane.setBottomAnchor(box, 0.0);
        AnchorPane.setLeftAnchor(box, 0.0);
    }
}