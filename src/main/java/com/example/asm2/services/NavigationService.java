package com.example.asm2.services;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

public class NavigationService {

    public static void loadPage(AnchorPane container, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    NavigationService.class.getResource(fxmlPath)
            );
            Node view = loader.load();

            container.getChildren().clear();
            container.getChildren().add(view);

            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}