package com.group01.asm2.services;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;
import com.group01.asm2.controllers.AuctionDetailsController;
import com.group01.asm2.models.Auction;
import javafx.scene.layout.Pane;

public class NavigationService {

    public static void loadPage(Pane contentArea, String fxmlPath) {

        if (contentArea == null) {
            System.err.println("ERROR: contentArea is null.");
            return;
        }

        try {

            var resource = NavigationService.class.getResource(fxmlPath);

            if (resource == null) {
                System.err.println("ERROR: Cannot find FXML file: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);

            Node pageNode = loader.load();

            pageNode.getStyleClass().add("content-view");

            contentArea.getChildren().setAll(pageNode);

            AnchorPane.setTopAnchor(pageNode, 0.0);
            AnchorPane.setBottomAnchor(pageNode, 0.0);
            AnchorPane.setLeftAnchor(pageNode, 0.0);
            AnchorPane.setRightAnchor(pageNode, 0.0);

        } catch (IOException e) {

            System.err.println("ERROR loading page: " + fxmlPath);
            e.printStackTrace();

        }
    }

    public static void goToAuctionDetails(Pane contentArea, Auction auction) {
        if (contentArea == null) {
            System.err.println("ERROR: contentArea is null.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(NavigationService.class.getResource("/com/group01/asm2/views/auction-details.fxml"));
            Node detailsView = loader.load();

            // Lấy Controller và truyền dữ liệu
            AuctionDetailsController detailsController = loader.getController();
            detailsController.loadAuctionDetails(auction);

            // Gắn giao diện mới vào vùng hiển thị
            contentArea.getChildren().setAll(detailsView);
            AnchorPane.setTopAnchor(detailsView, 0.0);
            AnchorPane.setBottomAnchor(detailsView, 0.0);
            AnchorPane.setLeftAnchor(detailsView, 0.0);
            AnchorPane.setRightAnchor(detailsView, 0.0);

        } catch (IOException e) {
            System.err.println("Lỗi khi tải trang Auction Details: " + e.getMessage());
            e.printStackTrace();
        }
    }
}