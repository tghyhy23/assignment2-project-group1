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
    @FXML private Button auctionsButton;
    @FXML private Button itemsButton;
    @FXML private Button paymentsButton;
    @FXML private Button reportsButton;
    @FXML private Button profileButton;

    public void setContentArea(AnchorPane contentArea) {
        this.contentArea = contentArea;
    }

    @FXML
    private void initialize() {
        dashboardButton.setGraphic(createDashboardIcon());
        auctionsButton.setGraphic(createGavelIcon());
        itemsButton.setGraphic(createBoxIcon());
        paymentsButton.setGraphic(createWalletIcon());
        reportsButton.setGraphic(createChartIcon());
        profileButton.setGraphic(createUserIcon());

        setActiveButton(dashboardButton);
    }

    private void setActiveButton(Button activeButton) {
        Button[] buttons = {
                dashboardButton,
                auctionsButton,
                itemsButton,
                paymentsButton,
                reportsButton,
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
    }

    @FXML
    private void showAuctions() {
        setActiveButton(auctionsButton);
    }

    @FXML
    private void showItems() {
        setActiveButton(itemsButton);
    }

    @FXML
    private void showPayments() {
        setActiveButton(paymentsButton);
    }

    @FXML
    private void showReports() {
        setActiveButton(reportsButton);
    }

    @FXML
    private void showProfile() {
        setActiveButton(profileButton);
    }

    // =========================
    // Icon helpers
    // =========================

    private StackPane wrap(Shape... shapes) {
        StackPane pane = new StackPane();
        pane.setMinSize(22, 22);
        pane.setPrefSize(22, 22);
        pane.setMaxSize(22, 22);
        pane.getChildren().addAll(shapes);
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

        r1.setTranslateX(-4);
        r1.setTranslateY(-3);

        r2.setTranslateX(4);
        r2.setTranslateY(-5);

        r3.setTranslateX(4);
        r3.setTranslateY(3);

        r4.setTranslateX(-4);
        r4.setTranslateY(5);

        return wrap(r1, r2, r3, r4);
    }

    private StackPane createGavelIcon() {
        SVGPath p1 = new SVGPath();
        p1.setContent("M14 13l-7.5 7.5c-.83.83-2.17.83-3 0a2.12 2.12 0 010-3L11 10");

        SVGPath p2 = new SVGPath();
        p2.setContent("M16 16l6-6");

        SVGPath p3 = new SVGPath();
        p3.setContent("M8 8l6-6");

        SVGPath p4 = new SVGPath();
        p4.setContent("M9 7l8 8");

        SVGPath p5 = new SVGPath();
        p5.setContent("M21 11l-8-8");

        for (SVGPath p : new SVGPath[]{p1, p2, p3, p4, p5}) {
            styleStroke(p);
        }

        return wrap(p1, p2, p3, p4, p5);
    }

    private StackPane createBoxIcon() {
        Rectangle box = new Rectangle(14, 14);
        box.setArcWidth(3);
        box.setArcHeight(3);

        Line line1 = new Line(-7, 0, 7, 0);
        Line line2 = new Line(0, -7, 0, 7);

        styleStroke(box);
        styleStroke(line1);
        styleStroke(line2);

        return wrap(box, line1, line2);
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

        b1.setTranslateX(-3);
        b1.setTranslateY(5);

        b2.setTranslateX(1.5);
        b2.setTranslateY(3);

        b3.setTranslateX(6);
        b3.setTranslateY(1.5);

        return wrap(axisX, axisY, b1, b2, b3);
    }

    private StackPane createUserIcon() {
        Circle head = new Circle(4);
        head.setCenterY(-4);

        SVGPath body = new SVGPath();
        body.setContent("M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2");

        styleStroke(head);
        styleStroke(body);

        return wrap(head, body);
    }
}