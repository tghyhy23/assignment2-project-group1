package com.group01.asm2.controllers;

/**
 * @author Group 01
 */

import com.group01.asm2.exceptions.AppException;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBase;

import java.util.function.Consumer;

public abstract class BaseController {

    protected void bindAction(ButtonBase button, Consumer<ActionEvent> action) {
        button.setOnAction(event -> {
            try {
                action.accept(event);
            } catch (AppException exception) {
                handleAppException(exception);
            } catch (Exception exception) {
                exception.printStackTrace();
                handleUnexpectedException();
            }
        });
    }

    protected void handleAppException(AppException exception) {
        showError(exception.getMessage());
    }

    protected void handleUnexpectedException() {
        showError("Something went wrong. Please try again.");
    }

    protected void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    protected void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}