package com.group01.asm2.utils;

import javafx.scene.control.Label;

public final class UiMessageUtils {

    private UiMessageUtils() {
    }

    public static void clearMessage(Label label) {
        if (label == null) return;

        label.setText("");
        label.getStyleClass().removeAll(
                "error-message",
                "success-message",
                "info-message"
        );
    }

    public static void showError(Label label, String message) {
        showMessage(label, message, "error-message");
    }

    public static void showSuccess(Label label, String message) {
        showMessage(label, message, "success-message");
    }

    public static void showInfo(Label label, String message) {
        showMessage(label, message, "info-message");
    }

    private static void showMessage(Label label, String message, String styleClass) {
        if (label == null) return;

        label.setText(message == null ? "" : message);

        label.getStyleClass().removeAll(
                "error-message",
                "success-message",
                "info-message"
        );

        if (!label.getStyleClass().contains(styleClass)) {
            label.getStyleClass().add(styleClass);
        }
    }
}