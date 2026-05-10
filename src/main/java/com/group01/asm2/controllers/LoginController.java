package com.group01.asm2.controllers;

import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.Person;
import com.group01.asm2.services.AuthService;
import com.group01.asm2.utils.SecureSceneLoader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController extends BaseController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheckBox;
    @FXML private Label messageLabel;
    @FXML private Button loginButton;
    @FXML private Hyperlink signupLink;

    private final AuthService authService = new AuthService();

    @FXML
    private void initialize() {
        bindAction(loginButton, this::handleLogin);
        bindAction(signupLink, this::goToSignup);
    }

    private void handleLogin(ActionEvent event) {
        Person loggedInUser = authService.login(
            usernameField.getText(),
            passwordField.getText()
        );

        showSuccess("Login successful. Welcome back, " + loggedInUser.getFullName() + "!");
        navigateAfterLogin(event);
    }

    private void goToSignup(ActionEvent event) {
        switchScene(
            event,
            "/com/group01/asm2/views/signup.fxml",
            "Cannot open the Sign Up page."
        );
    }

    private void navigateAfterLogin(ActionEvent event) {
        switchScene(
            event,
            "/com/group01/asm2/layout/main-layout.fxml",
            "Cannot open the main page."
        );
    }

    private void switchScene(ActionEvent event, String fxmlPath, String errorMessage) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(SecureSceneLoader.loadScene(getClass(), fxmlPath));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException exception) {
            exception.printStackTrace();
            showError(errorMessage);
        }
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
}