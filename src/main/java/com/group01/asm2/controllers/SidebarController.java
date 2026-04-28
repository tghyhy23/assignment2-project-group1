package com.group01.asm2.controllers;

import com.group01.asm2.services.NavigationService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;

public class SidebarController {

    private AnchorPane contentArea;

    @FXML private Button dashboardButton;
    @FXML private Button bidsHistoryButton;
    @FXML private Button auctionsButton;
//    @FXML private Button paymentsButton;
//    @FXML private Button reportsButton;
    @FXML private Button profileButton;

    public void setContentArea(AnchorPane contentArea) {
        this.contentArea = contentArea;
    }

    @FXML
    private void initialize() {
        dashboardButton.setGraphic(createDashboardIcon());
        bidsHistoryButton.setGraphic(createGavelIcon());
        auctionsButton.setGraphic(createBoxIcon());
//        paymentsButton.setGraphic(createWalletIcon());
//        reportsButton.setGraphic(createChartIcon());
        profileButton.setGraphic(createUserIcon());

        setActiveButton(dashboardButton);
    }

    private void setActiveButton(Button activeButton) {
        Button[] buttons = {
                dashboardButton,
                bidsHistoryButton,
                auctionsButton,
//                paymentsButton,
//                reportsButton,
                profileButton
        };

        for (Button button : buttons) {
            button.getStyleClass().remove("active");
        }

        if (activeButton != null && !activeButton.getStyleClass().contains("active")) {
            activeButton.getStyleClass().add("active");
        }
    }

    @FXML
    private void showDashboard() {
        setActiveButton(dashboardButton);
        NavigationService.loadPage(contentArea, "/com/group01/asm2/views/explore-view.fxml");
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

//    @FXML
//    private void showPayments() {
//        setActiveButton(paymentsButton);
//    }
//
//    @FXML
//    private void showReports() {
//        setActiveButton(reportsButton);
//    }

    @FXML
    private void showProfile() {
        setActiveButton(profileButton);
        NavigationService.loadPage(contentArea, "/com/group01/asm2/views/profile-view.fxml");
    }

    // =========================
    // Icon helpers
    // =========================

    private StackPane wrap(Shape... shapes) {
        // Đưa tất cả nét vẽ vào 1 Group để giữ nguyên toạ độ gốc (đặc biệt là với SVG)
        javafx.scene.Group group = new javafx.scene.Group(shapes);

        // TÙY CHỈNH KÍCH THƯỚC TẠI ĐÂY (0.75 = 75% kích thước gốc)
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
    }

    private StackPane createDashboardIcon() {
        Rectangle r1 = new Rectangle(6, 8);
        Rectangle r2 = new Rectangle(6, 4);
        Rectangle r3 = new Rectangle(6, 8);
        Rectangle r4 = new Rectangle(6, 4);

        for (Rectangle r : new Rectangle[]{r1, r2, r3, r4}) {
            r.setArcWidth(2);
            r.setArcHeight(2);
            styleStroke(r);
        }

        r1.setTranslateX(-4); r1.setTranslateY(-3);
        r2.setTranslateX(4);  r2.setTranslateY(-5);
        r3.setTranslateX(4);  r3.setTranslateY(3);
        r4.setTranslateX(-4); r4.setTranslateY(5);

        return wrap(r1, r2, r3, r4);
    }

    private StackPane createGavelIcon() {
        // Icon Bids History (Shopping Bag)
        SVGPath p1 = new SVGPath(); p1.setContent("M16 10a4 4 0 0 1-8 0");
        SVGPath p2 = new SVGPath(); p2.setContent("M3.103 6.034h17.794");
        SVGPath p3 = new SVGPath(); p3.setContent("M3.4 5.467a2 2 0 0 0-.4 1.2V20a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6.667a2 2 0 0 0-.4-1.2l-2-2.667A2 2 0 0 0 17 2H7a2 2 0 0 0-1.6.8z");

        for (SVGPath p : new SVGPath[]{p1, p2, p3}) {
            styleStroke(p);
        }

        return wrap(p1, p2, p3);
    }

    private StackPane createBoxIcon() {
        // Icon Auction (Package Check)
        SVGPath p1 = new SVGPath(); p1.setContent("M12 22V12");
        SVGPath p2 = new SVGPath(); p2.setContent("m16 17 2 2 4-4");
        SVGPath p3 = new SVGPath(); p3.setContent("M21 11.127V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.729l7 4a2 2 0 0 0 2 .001l1.32-.753");
        SVGPath p4 = new SVGPath(); p4.setContent("M3.29 7 12 12l8.71-5");
        SVGPath p5 = new SVGPath(); p5.setContent("m7.5 4.27 8.997 5.148");

        for (SVGPath p : new SVGPath[]{p1, p2, p3, p4, p5}) {
            styleStroke(p);
        }

        return wrap(p1, p2, p3, p4, p5);
    }

    private StackPane createWalletIcon() {
        Rectangle body = new Rectangle(16, 11);
        body.setArcWidth(4);
        body.setArcHeight(4);

        Rectangle flap = new Rectangle(8, 4);
        flap.setArcWidth(2);
        flap.setArcHeight(2);
        flap.setTranslateX(4);
        flap.setTranslateY(-1);

        Circle dot = new Circle(1.2);
        dot.setFill(Color.web("#DCE6FF"));
        dot.setTranslateX(5);
        dot.setTranslateY(-1);

        styleStroke(body);
        styleStroke(flap);

        return wrap(body, flap, dot);
    }

    private StackPane createChartIcon() {
        Line axisX = new Line(-8, 8, 8, 8);
        Line axisY = new Line(-8, 8, -8, -8);

        Rectangle b1 = new Rectangle(3, 5);
        Rectangle b2 = new Rectangle(3, 9);
        Rectangle b3 = new Rectangle(3, 12);

        styleStroke(axisX);
        styleStroke(axisY);
        styleStroke(b1);
        styleStroke(b2);
        styleStroke(b3);

        b1.setTranslateX(-3); b1.setTranslateY(5);
        b2.setTranslateX(1.5); b2.setTranslateY(3);
        b3.setTranslateX(6);   b3.setTranslateY(1.5);

        return wrap(axisX, axisY, b1, b2, b3);
    }

    private StackPane createUserIcon() {
        // Icon Profile (Circle User Round)
        SVGPath p1 = new SVGPath();
        p1.setContent("M17.925 20.056a6 6 0 0 0-11.851.001");

        Circle c1 = new Circle(12, 11, 4); // cx="12" cy="11" r="4"
        Circle c2 = new Circle(12, 12, 10); // cx="12" cy="12" r="10"

        styleStroke(p1);
        styleStroke(c1);
        styleStroke(c2);

        return wrap(p1, c1, c2);
    }
}