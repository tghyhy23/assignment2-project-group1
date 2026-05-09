package com.group01.asm2.controllers;

import com.group01.asm2.enums.UserRole;
import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.Person;
import com.group01.asm2.services.AuthService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox rememberMeCheckBox;

    @FXML
    private Label messageLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            Person loggedInUser = authService.login(username, password);
            showSuccess("Login successful. Welcome back, " + loggedInUser.getFullName() + "!");
            navigateAfterLogin(event, loggedInUser);

        } catch (AppException exception) {
            showError(exception.getMessage());
        } catch (Exception exception) {
            exception.printStackTrace();
            showError("Something went wrong while logging in.");
        }
    }

    @FXML
    private void goToSignup(ActionEvent event) {
        switchScene(event, "/com/group01/asm2/views/signup.fxml", "Cannot open the Sign Up page.");
    }

    private void navigateAfterLogin(ActionEvent event, Person loggedInUser) {
        UserRole role = loggedInUser.getRole();

        if (role == UserRole.AUCTION_ADMINISTRATOR) {
            showSuccess("Auction administrator login successful.");
            // Later:
            // switchScene(event, "/com/group01/asm2/views/auction-management.fxml", "Cannot open Auction Management page.");
            return;
        }

        if (role == UserRole.SYSTEM_ADMINISTRATOR) {
            showSuccess("System administrator login successful.");
            // Later:
            // switchScene(event, "/com/group01/asm2/views/system-management.fxml", "Cannot open System Management page.");
            return;
        }

        if (role == UserRole.BUYER || role == UserRole.SELLER) {
            showSuccess("User login successful.");
            // Later:
            // switchScene(event, "/com/group01/asm2/views/explore.fxml", "Cannot open Explore page.");
        }
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
}