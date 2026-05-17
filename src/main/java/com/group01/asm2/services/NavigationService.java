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
import java.net.URL;

public class NavigationService {
    private static final String AUCTION_DETAILS_FXML =
        "/com/group01/asm2/views/auction-details.fxml";

    private static final String PROFILE_FXML =
        "/com/group01/asm2/views/profile-view.fxml";

    private static Pane mainContentArea;

    public static void setMainContentArea(Pane contentArea) {
        if (contentArea == null) {
            System.err.println("ERROR: Tried to register null mainContentArea.");
            printCallerTrace();
            return;
        }

        mainContentArea = contentArea;
        System.out.println("NavigationService mainContentArea registered successfully.");
    }

    public static Pane getMainContentArea() {
        return mainContentArea;
    }

    public static void loadPage(Pane contentArea, String fxmlPath) {
        Pane targetArea = resolveContentArea(contentArea, "loadPage: " + fxmlPath);

        if (targetArea == null) {
            return;
        }

        if (fxmlPath == null || fxmlPath.isBlank()) {
            System.err.println("ERROR: fxmlPath is null or empty.");
            return;
        }

        try {
            FXMLLoader loader = createLoader(fxmlPath);
            Node pageNode = loader.load();

            injectContentArea(loader.getController(), targetArea);
            replaceContent(targetArea, pageNode);

        } catch (IOException exception) {
            System.err.println("ERROR loading page: " + fxmlPath);
            exception.printStackTrace();
        }
    }

    public static void loadPage(String fxmlPath) {
        loadPage(mainContentArea, fxmlPath);
    }

    public static void goToAuctionDetails(Pane contentArea, Auction auction) {
        if (auction == null || auction.getId() == null) {
            System.err.println("ERROR: auction or auction ID is null.");
            return;
        }

        goToAuctionDetails(contentArea, auction.getId());
    }

    public static void goToAuctionDetails(Auction auction) {
        if (auction == null || auction.getId() == null) {
            System.err.println("ERROR: auction or auction ID is null.");
            return;
        }

        goToAuctionDetails(mainContentArea, auction.getId());
    }

    public static void goToAuctionDetails(Integer auctionId) {
        goToAuctionDetails(mainContentArea, auctionId);
    }

    public static void goToAuctionDetails(Pane contentArea, Integer auctionId) {
        Pane targetArea = resolveContentArea(contentArea, "goToAuctionDetails");

        if (targetArea == null) {
            return;
        }

        if (auctionId == null) {
            System.err.println("ERROR: auctionId is null.");
            return;
        }

        try {
            FXMLLoader loader = createLoader(AUCTION_DETAILS_FXML);
            Node detailsView = loader.load();

            AuctionDetailsController detailsController = loader.getController();

            if (detailsController == null) {
                System.err.println("ERROR: AuctionDetailsController is null.");
                return;
            }

            detailsController.setContentArea(targetArea);
            detailsController.loadAuctionDetailsById(auctionId);

            replaceContent(targetArea, detailsView);

        } catch (IOException exception) {
            System.err.println("ERROR loading Auction Details page: " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    public static void goToProfilePage(Integer userId) {
        goToProfilePage(mainContentArea, userId);
    }

    public static void goToProfilePage(Pane contentArea, Integer userId) {
        Pane targetArea = resolveContentArea(contentArea, "goToProfilePage");

        if (targetArea == null) {
            return;
        }

        if (userId == null) {
            System.err.println("ERROR: userId is null.");
            return;
        }

        try {
            FXMLLoader loader = createLoader(PROFILE_FXML);
            Node profileView = loader.load();

            ProfileController profileController = loader.getController();

            if (profileController == null) {
                System.err.println("ERROR: ProfileController is null.");
                return;
            }

            profileController.loadProfile(userId);

            replaceContent(targetArea, profileView);

        } catch (IOException exception) {
            System.err.println("ERROR loading Profile page: " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    private static FXMLLoader createLoader(String fxmlPath) {
        URL resource = NavigationService.class.getResource(fxmlPath);

        if (resource == null) {
            throw new IllegalStateException(
                "ERROR: Cannot find FXML file: " + fxmlPath +
                    "\nExpected location: src/main/resources" + fxmlPath
            );
        }

        return new FXMLLoader(resource);
    }

    private static Pane resolveContentArea(Pane passedContentArea, String actionName) {
        if (passedContentArea != null) {
            return passedContentArea;
        }

        if (mainContentArea != null) {
            System.err.println(
                "WARNING: contentArea was null for " + actionName +
                    ", using registered mainContentArea instead."
            );
            printCallerTrace();
            return mainContentArea;
        }

        System.err.println(
            "ERROR: contentArea is null for " + actionName +
                ", and mainContentArea has not been registered."
        );
        printCallerTrace();
        return null;
    }

    private static void injectContentArea(Object controller, Pane contentArea) {
        if (controller instanceof AuctionDetailsController auctionDetailsController) {
            auctionDetailsController.setContentArea(contentArea);
        }
    }

    private static void replaceContent(Pane contentArea, Node pageNode) {
        if (contentArea == null) {
            System.err.println("ERROR: contentArea is null inside replaceContent.");
            printCallerTrace();
            return;
        }

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

    private static void printCallerTrace() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        System.err.println("Navigation caller trace:");

        for (int i = 3; i < Math.min(stackTrace.length, 10); i++) {
            System.err.println("  at " + stackTrace[i]);
        }
    }
}