package com.group01.asm2.controllers;

import com.group01.asm2.exceptions.AppException;
import javafx.event.ActionEvent;
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

    protected abstract void handleAppException(AppException exception);

    protected void handleUnexpectedException() {
        System.out.println("Something went wrong. Please try again.");
    }
}