package com.group01.asm2.controllers;

import com.group01.asm2.enums.UserRole;
import com.group01.asm2.services.NavigationService;
import com.group01.asm2.core.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;

public class SidebarController {

    private AnchorPane contentArea;

    @FXML private Button exploreButton;
    @FXML private Button usersButton;
    @FXML private Button adminAuctionsButton;
    @FXML private Button categoriesButton;
    @FXML private Button itemsButton;
    @FXML private Button paymentsButton;
    @FXML private Button reportsButton;
    @FXML private Button adminDashboardButton;
    @FXML private Button bidsHistoryButton;
    @FXML private Button auctionsButton;
    @FXML private Button profileButton;

    public void setContentArea(AnchorPane contentArea) {
        this.contentArea = contentArea;
    }

    @FXML
    private void initialize() {
        exploreButton.setGraphic(createExploreIcon());
        adminDashboardButton.setGraphic(createAdminDashboardIcon());
        bidsHistoryButton.setGraphic(createBidsHistoryIcon());
        auctionsButton.setGraphic(createAuctionIcon());
        profileButton.setGraphic(createProfileIcon());
        usersButton.setGraphic(createUserIcon());
        adminAuctionsButton.setGraphic(createAuctionIcon());
        categoriesButton.setGraphic(createCategoryIcon());
        itemsButton.setGraphic(createItemIcon());
        paymentsButton.setGraphic(createPaymentIcon());
        reportsButton.setGraphic(createReportIcon());

        boolean isAuctionAdmin =
                SessionManager.getCurrentUser() != null
                        && (
                        SessionManager.getCurrentUser().getRole() == UserRole.AUCTION_ADMINISTRATOR
                                || SessionManager.getCurrentUser().getRole() == UserRole.SYSTEM_ADMINISTRATOR
                );

        setAdminVisible(isAuctionAdmin);

        adminDashboardButton.setVisible(isAuctionAdmin);
        adminDashboardButton.setManaged(isAuctionAdmin);

        setActiveButton(exploreButton);
    }

    private void setAdminVisible(boolean visible) {
        adminSectionTitle.setVisible(visible);
        adminSectionTitle.setManaged(visible);

        adminDashboardButton.setVisible(visible);
        adminDashboardButton.setManaged(visible);

        usersButton.setVisible(visible);
        usersButton.setManaged(visible);

        adminAuctionsButton.setVisible(visible);
        adminAuctionsButton.setManaged(visible);

        categoriesButton.setVisible(visible);
        categoriesButton.setManaged(visible);

        itemsButton.setVisible(visible);
        itemsButton.setManaged(visible);

        paymentsButton.setVisible(visible);
        paymentsButton.setManaged(visible);

        reportsButton.setVisible(visible);
        reportsButton.setManaged(visible);

        adminSeparator.setVisible(visible);
        adminSeparator.setManaged(visible);
    }

    private void setActiveButton(Button activeButton) {
        Button[] buttons = {
                adminDashboardButton,
                usersButton,
                adminAuctionsButton,
                categoriesButton,
                itemsButton,
                paymentsButton,
                reportsButton,
                exploreButton,
                bidsHistoryButton,
                auctionsButton,
                profileButton
        };

        for (Button button : buttons) {
            if (button != null) {
                button.getStyleClass().remove("active");
            }
        }

        if (activeButton != null && !activeButton.getStyleClass().contains("active")) {
            activeButton.getStyleClass().add("active");
        }
    }
    @FXML private Label adminSectionTitle;
    @FXML private Separator adminSeparator;

    @FXML
    private void showExplore() {
        setActiveButton(exploreButton);
        NavigationService.loadPage(contentArea, "/com/group01/asm2/views/explore-view.fxml");
    }

    @FXML
    private void showAdminDashboard() {
        setActiveButton(adminDashboardButton);
        NavigationService.loadPage(contentArea, "/com/group01/asm2/views/admin/admin-dashboard.fxml");
    }

    @FXML
    private void showBidsHistory() {
        setActiveButton(bidsHistoryButton);
        NavigationService.loadPage(contentArea, "/com/group01/asm2/views/bids_history-view.fxml");
    }

    @FXML
    private void showAuctions() {
        setActiveButton(auctionsButton);
        NavigationService.loadPage(contentArea, "/com/group01/asm2/views/auctions-view.fxml");
    }

    @FXML
    private void showProfile() {
        setActiveButton(profileButton);
        NavigationService.loadPage(contentArea, "/com/group01/asm2/views/profile-view.fxml");
    }

    @FXML
    private void showUsersManagement() {
        setActiveButton(usersButton);
        NavigationService.loadPage(contentArea, "/com/group01/asm2/views/admin/users-management.fxml");
    }

    @FXML
    private void showAuctionsManagement() {
        setActiveButton(adminAuctionsButton);
        NavigationService.loadPage(contentArea, "/com/group01/asm2/views/admin/auctions-management.fxml");
    }

    @FXML
    private void showCategoriesManagement() {
        setActiveButton(categoriesButton);
        NavigationService.loadPage(contentArea, "/com/group01/asm2/views/admin/categories-management.fxml");
    }

    @FXML
    private void showItemsManagement() {
        setActiveButton(itemsButton);
        NavigationService.loadPage(contentArea, "/com/group01/asm2/views/admin/items-management.fxml");
    }

    @FXML
    private void showPaymentsManagement() {
        setActiveButton(paymentsButton);
        NavigationService.loadPage(contentArea, "/com/group01/asm2/views/admin/payments-management.fxml");
    }

    @FXML
    private void showReports() {
        setActiveButton(reportsButton);
        NavigationService.loadPage(contentArea, "/com/group01/asm2/views/admin/reports.fxml");
    }

    // =========================
    // Icon helpers
    // =========================
    private StackPane wrap(Shape... shapes) {
        javafx.scene.Group group = new javafx.scene.Group(shapes);

        double scaleFactor = 0.75;
        group.setScaleX(scaleFactor);
        group.setScaleY(scaleFactor);

        StackPane pane = new StackPane(group);
        pane.setMinSize(22, 22);
        pane.setPrefSize(22, 22);
        pane.setMaxSize(22, 22);

        return pane;
    }

    private void styleStroke(Shape shape) {
        shape.setFill(Color.TRANSPARENT);
        shape.setStroke(Color.web("#DCE6FF"));
        shape.setStrokeWidth(1.8);
        shape.getStyleClass().add("icon-shape");
    }

    private StackPane createCategoryIcon() {
        SVGPath p1 = new SVGPath();
        p1.setContent("M11 13v4");

        SVGPath p2 = new SVGPath();
        p2.setContent("M15 5v4");

        SVGPath p3 = new SVGPath();
        p3.setContent("M3 3v16a2 2 0 0 0 2 2h16");

        SVGPath p4 = new SVGPath();
        p4.setContent("M7 13h9a1 1 0 0 1 1 1v2a1 1 0 0 1-1 1H7a1 1 0 0 1-1-1v-2a1 1 0 0 1 1-1");

        SVGPath p5 = new SVGPath();
        p5.setContent("M7 5h12a1 1 0 0 1 1 1v2a1 1 0 0 1-1 1H7a1 1 0 0 1-1-1V6a1 1 0 0 1 1-1");

        for (SVGPath path : new SVGPath[]{p1, p2, p3, p4, p5}) {
            styleStroke(path);
        }

        return wrap(p1, p2, p3, p4, p5);
    }

    private StackPane createProfileIcon() {
        SVGPath p1 = new SVGPath();
        p1.setContent("M17.925 20.056a6 6 0 0 0-11.851.001");

        Circle c1 = new Circle(12, 11, 4);

        Circle c2 = new Circle(12, 12, 10);

        styleStroke(p1);
        styleStroke(c1);
        styleStroke(c2);

        return wrap(p1, c1, c2);
    }

    private StackPane createItemIcon() {
        SVGPath p1 = new SVGPath();
        p1.setContent("M3 3h7v7H3z");

        SVGPath p2 = new SVGPath();
        p2.setContent("M3 14h7v7H3z");

        SVGPath p3 = new SVGPath();
        p3.setContent("M14 4h7");

        SVGPath p4 = new SVGPath();
        p4.setContent("M14 9h7");

        SVGPath p5 = new SVGPath();
        p5.setContent("M14 15h7");

        SVGPath p6 = new SVGPath();
        p6.setContent("M14 20h7");

        for (SVGPath path : new SVGPath[]{p1, p2, p3, p4, p5, p6}) {
            styleStroke(path);
        }

        return wrap(p1, p2, p3, p4, p5, p6);
    }

    private StackPane createPaymentIcon() {
        SVGPath p1 = new SVGPath();
        p1.setContent("M3.85 8.62a4 4 0 0 1 4.78-4.77 4 4 0 0 1 6.74 0 4 4 0 0 1 4.78 4.78 4 4 0 0 1 0 6.74 4 4 0 0 1-4.77 4.78 4 4 0 0 1-6.75 0 4 4 0 0 1-4.78-4.77 4 4 0 0 1 0-6.76Z");

        SVGPath p2 = new SVGPath();
        p2.setContent("M7 12h5");

        SVGPath p3 = new SVGPath();
        p3.setContent("M15 9.4a4 4 0 1 0 0 5.2");

        styleStroke(p1);
        styleStroke(p2);
        styleStroke(p3);

        return wrap(p1, p2, p3);
    }

    private StackPane createReportIcon() {
        SVGPath p1 = new SVGPath();
        p1.setContent("M6 22a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h8a2.4 2.4 0 0 1 1.704.706l3.588 3.588A2.4 2.4 0 0 1 20 8v12a2 2 0 0 1-2 2z");

        SVGPath p2 = new SVGPath();
        p2.setContent("M14 2v5a1 1 0 0 0 1 1h5");

        SVGPath p3 = new SVGPath();
        p3.setContent("M10 9H8");

        SVGPath p4 = new SVGPath();
        p4.setContent("M16 13H8");

        SVGPath p5 = new SVGPath();
        p5.setContent("M16 17H8");

        for (SVGPath path : new SVGPath[]{p1, p2, p3, p4, p5}) {
            styleStroke(path);
        }

        return wrap(p1, p2, p3, p4, p5);
    }

    private StackPane createExploreIcon() {
        Circle c1 = new Circle(12, 12, 10);

        SVGPath p1 = new SVGPath();
        p1.setContent("m16.24 7.76-1.804 5.411a2 2 0 0 1-1.265 1.265L7.76 16.24l1.804-5.411a2 2 0 0 1 1.265-1.265z");

        styleStroke(c1);
        styleStroke(p1);

        return wrap(c1, p1);
    }

    private StackPane createAdminDashboardIcon() {
        SVGPath rect1 = new SVGPath();
        rect1.setContent("M3 4a1 1 0 0 1 1-1h5a1 1 0 0 1 1 1v7a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1z");

        SVGPath rect2 = new SVGPath();
        rect2.setContent("M14 4a1 1 0 0 1 1-1h5a1 1 0 0 1 1 1v3a1 1 0 0 1-1 1h-5a1 1 0 0 1-1-1z");

        SVGPath rect3 = new SVGPath();
        rect3.setContent("M14 13a1 1 0 0 1 1-1h5a1 1 0 0 1 1 1v7a1 1 0 0 1-1 1h-5a1 1 0 0 1-1-1z");

        SVGPath rect4 = new SVGPath();
        rect4.setContent("M3 17a1 1 0 0 1 1-1h5a1 1 0 0 1 1 1v3a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1z");

        for (SVGPath path : new SVGPath[]{rect1, rect2, rect3, rect4}) {
            styleStroke(path);
        }

        return wrap(rect1, rect2, rect3, rect4);
    }

    private StackPane createBidsHistoryIcon() {
        SVGPath p1 = new SVGPath();
        p1.setContent("M16 10a4 4 0 0 1-8 0");

        SVGPath p2 = new SVGPath();
        p2.setContent("M3.103 6.034h17.794");

        SVGPath p3 = new SVGPath();
        p3.setContent("M3.4 5.467a2 2 0 0 0-.4 1.2V20a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6.667a2 2 0 0 0-.4-1.2l-2-2.667A2 2 0 0 0 17 2H7a2 2 0 0 0-1.6.8z");

        for (SVGPath path : new SVGPath[]{p1, p2, p3}) {
            styleStroke(path);
        }

        return wrap(p1, p2, p3);
    }

    private StackPane createAuctionIcon() {
        SVGPath p1 = new SVGPath();
        p1.setContent("M12 22V12");

        SVGPath p2 = new SVGPath();
        p2.setContent("m16 17 2 2 4-4");

        SVGPath p3 = new SVGPath();
        p3.setContent("M21 11.127V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.729l7 4a2 2 0 0 0 2 .001l1.32-.753");

        SVGPath p4 = new SVGPath();
        p4.setContent("M3.29 7 12 12l8.71-5");

        SVGPath p5 = new SVGPath();
        p5.setContent("m7.5 4.27 8.997 5.148");

        for (SVGPath path : new SVGPath[]{p1, p2, p3, p4, p5}) {
            styleStroke(path);
        }

        return wrap(p1, p2, p3, p4, p5);
    }

    private StackPane createUserIcon() {
        SVGPath p1 = new SVGPath();
        p1.setContent("M18 21a8 8 0 0 0-16 0");

        Circle c1 = new Circle(10, 8, 5);

        SVGPath p2 = new SVGPath();
        p2.setContent("M22 20c0-3.37-2-6.5-4-8a5 5 0 0 0-.45-8.3");

        styleStroke(p1);
        styleStroke(c1);
        styleStroke(p2);

        return wrap(p1, c1, p2);
    }
}