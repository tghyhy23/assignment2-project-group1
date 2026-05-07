package com.group01.asm2.services;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;

public class NavigationService {

    public static void loadPage(AnchorPane contentArea, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationService.class.getResource(fxmlPath));
            Node pageNode = loader.load();

            // ==========================================
            // ĐÂY LÀ DÒNG CODE QUAN TRỌNG ĐỂ BO GÓC DƯỚI
            // ==========================================
            // Tự động gắn class 'content-view' cho trang FXML mới load
            pageNode.getStyleClass().add("content-view");
            // ==========================================

            contentArea.getChildren().setAll(pageNode);

            // Set neo Anchor để trang mới tràn đầy contentArea
            AnchorPane.setTopAnchor(pageNode, 0.0);
            AnchorPane.setBottomAnchor(pageNode, 0.0);
            AnchorPane.setLeftAnchor(pageNode, 0.0);
            AnchorPane.setRightAnchor(pageNode, 0.0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}