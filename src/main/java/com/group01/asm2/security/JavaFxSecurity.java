package com.group01.asm2.security;

import com.group01.asm2.exceptions.AppException;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputControl;

public final class JavaFxSecurity {

    private JavaFxSecurity() {
    }

    public static void install(Scene scene) {
        if (scene == null || scene.getRoot() == null) {
            return;
        }

        installInputSanitizers(scene.getRoot());
        installRateLimitFilter(scene);
    }

    private static void installInputSanitizers(Node node) {
        if (node instanceof TextInputControl inputControl) {
            installTextSanitizer(inputControl);
        }

        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                installInputSanitizers(child);
            }
        }
    }

    private static void installTextSanitizer(TextInputControl inputControl) {
        if (inputControl instanceof PasswordField) {
            return;
        }

        if (inputControl.getTextFormatter() != null) {
            return;
        }

        inputControl.setTextFormatter(new TextFormatter<String>(change -> {
            String sanitizedText = InputSanitizer.sanitizeText(change.getText());
            change.setText(sanitizedText);
            return change;
        }));
    }

    private static void installRateLimitFilter(Scene scene) {
        scene.addEventFilter(ActionEvent.ACTION, event -> {
            if (!(event.getSource() instanceof Node sourceNode)) {
                return;
            }

            SecureActionConfig.from(sourceNode.getUserData()).ifPresent(config -> {
                try {
                    String identity = resolveIdentity(scene, config.identityNodeId());
                    InMemoryRateLimiter.check(config.rateLimitPolicy(), identity);
                } catch (AppException exception) {
                    event.consume();
                    showError(scene, config.messageNodeId(), exception.getMessage());
                }
            });
        });
    }

    private static String resolveIdentity(Scene scene, String identityNodeId) {
        if (identityNodeId == null || identityNodeId.isBlank()) {
            return "unknown";
        }

        Node identityNode = scene.lookup("#" + identityNodeId);

        if (identityNode instanceof TextInputControl inputControl) {
            return inputControl.getText();
        }

        return "unknown";
    }

    private static void showError(Scene scene, String messageNodeId, String message) {
        if (messageNodeId == null || messageNodeId.isBlank()) {
            return;
        }

        Node messageNode = scene.lookup("#" + messageNodeId);

        if (messageNode instanceof Label label) {
            label.getStyleClass().remove("success-message");

            if (!label.getStyleClass().contains("error-message")) {
                label.getStyleClass().add("error-message");
            }

            label.setText(message);
        }
    }
}