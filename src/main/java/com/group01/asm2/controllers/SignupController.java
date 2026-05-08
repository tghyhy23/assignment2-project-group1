package com.group01.asm2.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

public class SignupController {

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private DatePicker dateOfBirthPicker;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private Label messageLabel;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    @FXML
    private void initialize() {
        roleComboBox.getItems().clear();
        roleComboBox.getItems().addAll("Buyer", "Seller");
        roleComboBox.setValue("Buyer");
    }

    @FXML
    private void handleSignup() {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        LocalDate dateOfBirth = dateOfBirthPicker.getValue();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String role = roleComboBox.getValue();

        if (fullName.isEmpty() || email.isEmpty() || dateOfBirth == null ||
                password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill in all required fields.");
            return;
        }

        if (fullName.length() < 2) {
            showError("Full name must be at least 2 characters.");
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError("Please enter a valid email address.");
            return;
        }

        if (dateOfBirth.isAfter(LocalDate.now())) {
            showError("Date of birth cannot be in the future.");
            return;
        }

        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();

        if (age < 18) {
            showError("You must be at least 18 years old to create an auction account.");
            return;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Password and confirm password do not match.");
            return;
        }

        if (role == null || role.isEmpty()) {
            showError("Please select a role.");
            return;
        }

        messageLabel.getStyleClass().remove("error-message");
        if (!messageLabel.getStyleClass().contains("success-message")) {
            messageLabel.getStyleClass().add("success-message");
        }

        messageLabel.setText("Account created successfully!");

        System.out.println("Full name: " + fullName);
        System.out.println("Email: " + email);
        System.out.println("Date of birth: " + dateOfBirth);
        System.out.println("Role: " + role);
    }

    @FXML
    private void goToLogin(ActionEvent event) {
        switchScene(event, "/com/group01/asm2/views/login.fxml");
    }

    private void switchScene(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Cannot open the Login page.");
        }
    }

    private void showError(String message) {
        messageLabel.getStyleClass().remove("success-message");
        if (!messageLabel.getStyleClass().contains("error-message")) {
            messageLabel.getStyleClass().add("error-message");
        }

        messageLabel.setText(message);
    }
}