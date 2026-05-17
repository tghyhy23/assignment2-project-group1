package com.group01.asm2.services;

/**
 * @author Group 01
 */

import com.group01.asm2.controllers.AuctionDetailsController;
import com.group01.asm2.controllers.ProfileController;
import com.group01.asm2.models.Auction;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class NavigationService {
    private static final String AUCTION_DETAILS_FXML =
        "/com/group01/asm2/views/auction-details.fxml";

    private static final String PROFILE_FXML =
        "/com/group01/asm2/views/profile.fxml";

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

            replaceContent(contentArea, pageNode);

        } catch (IOException e) {
            System.err.println("ERROR loading page: " + fxmlPath);
            e.printStackTrace();
        }
    }

    public static void goToAuctionDetails(Pane contentArea, Auction auction) {
        if (auction == null || auction.getId() == null) {
            System.err.println("ERROR: auction or auction ID is null.");
            return;
        }

        goToAuctionDetails(contentArea, auction.getId());
    }

    public static void goToAuctionDetails(Pane contentArea, Integer auctionId) {
        if (contentArea == null) {
            System.err.println("ERROR: contentArea is null.");
            return;
        }

        if (auctionId == null) {
            System.err.println("ERROR: auctionId is null.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                NavigationService.class.getResource(AUCTION_DETAILS_FXML)
            );

            Node detailsView = loader.load();

            AuctionDetailsController detailsController = loader.getController();

            detailsController.setContentArea(contentArea);
            detailsController.loadAuctionDetailsById(auctionId);

            replaceContent(contentArea, detailsView);

        } catch (IOException e) {
            System.err.println("ERROR loading Auction Details page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void goToProfilePage(Pane contentArea, Integer userId) {
        if (contentArea == null) {
            System.err.println("ERROR: contentArea is null.");
            return;
        }

        if (userId == null) {
            System.err.println("ERROR: userId is null.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                NavigationService.class.getResource(PROFILE_FXML)
            );

            Node profileView = loader.load();

            ProfileController profileController = loader.getController();

//            profileController.loadProfileByUserId(userId);

            replaceContent(contentArea, profileView);

        } catch (IOException e) {
            System.err.println("ERROR loading Profile page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void replaceContent(Pane contentArea, Node pageNode) {
        if (pageNode == null) {
            System.err.println("ERROR: pageNode is null.");
            return;
        }

        if (!pageNode.getStyleClass().contains("content-view")) {
            pageNode.getStyleClass().add("content-view");
        }

        contentArea.getChildren().setAll(pageNode);

        AnchorPane.setTopAnchor(pageNode, 0.0);
        AnchorPane.setBottomAnchor(pageNode, 0.0);
        AnchorPane.setLeftAnchor(pageNode, 0.0);
        AnchorPane.setRightAnchor(pageNode, 0.0);
    }
}