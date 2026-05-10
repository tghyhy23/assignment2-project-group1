package com.group01.asm2.controllers;

import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.Person;
import com.group01.asm2.security.RateLimitPolicy;
import com.group01.asm2.services.AuthService;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.Duration;

public class LoginController extends BaseController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox rememberMeCheckBox;

    @FXML
    private Label messageLabel;

    @FXML
    private HBox authContainer;

    @FXML
    private VBox leftPanel;

    @FXML
    private VBox rightPanel;

    @FXML
    private VBox authCard;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        guarded(RateLimitPolicy.LOGIN, username, () -> {
            Person loggedInUser = authService.login(username, password);

            showSuccess("Login successful. Welcome back, " + loggedInUser.getFullName() + "!");
            navigateAfterLogin(event, loggedInUser);
        });
    }

    @FXML
    private void goToSignup(ActionEvent event) {
        guarded(() -> {
            switchScene(
                event,
                "/com/group01/asm2/views/signup.fxml",
                "Cannot open the Sign Up page."
            );
        });
    }

    private void navigateAfterLogin(ActionEvent event, Person loggedInUser) {
        switchScene(
            event,
            "/com/group01/asm2/layout/main-layout.fxml",
            "Cannot open the main page."
        );
    }

    private void switchScene(ActionEvent event, String fxmlPath, String errorMessage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException exception) {
            exception.printStackTrace();
            showError(errorMessage);
        }
    }

    private String normalizeRateLimitKey(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }

        return value.trim().toLowerCase();
    }

    @Override
    protected void handleAppException(AppException exception) {
        showError(exception.getMessage());
    }

    @Override
    protected void handleUnexpectedException() {
        showError("Something went wrong while logging in.");
    }

    private void showError(String message) {
        messageLabel.getStyleClass().remove("success-message");

        if (!messageLabel.getStyleClass().contains("error-message")) {
            messageLabel.getStyleClass().add("error-message");
        }

        messageLabel.setText(message);
    }

    private void showSuccess(String message) {
        messageLabel.getStyleClass().remove("error-message");

        if (!messageLabel.getStyleClass().contains("success-message")) {
            messageLabel.getStyleClass().add("success-message");
        }

        messageLabel.setText(message);
    }

    @FXML
    private void initialize() {
        if (authContainer == null || leftPanel == null || rightPanel == null || authCard == null) {
            return;
        }

        leftPanel.prefWidthProperty().bind(
                authContainer.widthProperty().multiply(0.45)
        );

        rightPanel.prefWidthProperty().bind(
                authContainer.widthProperty().multiply(0.55)
        );

        authCard.prefWidthProperty().bind(
                Bindings.createDoubleBinding(() -> {
                    double rightWidth = rightPanel.getWidth();

                    if (rightWidth <= 0) {
                        return 0.0;
                    }

                    double targetWidth;

                    if (authContainer.getWidth() < 900) {
                        targetWidth = rightWidth * 0.82;
                    } else {
                        targetWidth = rightWidth * 0.70;
                    }

                    return clamp(targetWidth, 360, 520);

                }, rightPanel.widthProperty(), authContainer.widthProperty())
        );

        authCard.maxWidthProperty().bind(authCard.prefWidthProperty());

        updateResponsivePadding();

        authContainer.widthProperty().addListener((obs, oldValue, newValue) -> updateResponsivePadding());
        authCard.widthProperty().addListener((obs, oldValue, newValue) -> updateResponsivePadding());
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    private void updateResponsivePadding() {
        double windowWidth = authContainer.getWidth();
        double cardWidth = authCard.getWidth();

        if (windowWidth <= 0 || cardWidth <= 0) {
            return;
        }

        double cardPadding = clamp(cardWidth * 0.075, 24, 40);
        double panelPadding = clamp(windowWidth * 0.035, 24, 56);

        authCard.setPadding(new Insets(
                cardPadding,
                cardPadding,
                cardPadding,
                cardPadding
        ));

        leftPanel.setPadding(new Insets(panelPadding));
        rightPanel.setPadding(new Insets(panelPadding));
    }
}