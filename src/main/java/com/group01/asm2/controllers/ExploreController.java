package com.group01.asm2.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.CacheHint;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class ExploreController implements Initializable {

    @FXML
    private ScrollPane exploreScrollPane;

    @FXML
    private VBox exploreRoot;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // 1. ÉP CACHE TOÀN BỘ TRANG (Chống giật lag đồ họa)
        exploreRoot.setCache(true);
        exploreRoot.setCacheShape(true);
        exploreRoot.setCacheHint(CacheHint.SPEED);

        // 2. TĂNG TỐC ĐỘ LĂN CHUỘT (Chống rít, tạo cảm giác mượt)
        final double SPEED_MULTIPLIER = 0.005; // Bạn có thể tăng/giảm số này để chỉnh độ nhạy của chuột

        exploreRoot.setOnScroll(event -> {
            // Lấy vị trí cuộn hiện tại
            double currentVvalue = exploreScrollPane.getVvalue();
            // Tính toán vị trí mới dựa trên lực lăn chuột
            double deltaY = event.getDeltaY() * SPEED_MULTIPLIER;
            // Ép ScrollPane cuộn đến vị trí mới
            exploreScrollPane.setVvalue(currentVvalue - deltaY);

            event.consume(); // Ngăn JavaFX dùng tốc độ cuộn mặc định
        });
    }
}