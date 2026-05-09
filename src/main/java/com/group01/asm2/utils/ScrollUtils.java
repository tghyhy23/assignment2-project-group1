package com.group01.asm2.utils;

import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;

public class ScrollUtils {

    // Tốc độ lăn chuột mặc định cho toàn bộ app
    private static final double DEFAULT_SPEED = 0.005;

    /**
     * Hàm này dùng để làm mượt bất kỳ ScrollPane nào được truyền vào
     */
    public static void makeSmooth(ScrollPane scrollPane) {
        // Lấy cái ruột (VBox/HBox) nằm bên trong ScrollPane
        Node content = scrollPane.getContent();

        if (content != null) {
            // 1. Ép Cache toàn bộ nội dung để chống lag đồ họa
            content.setCache(true);
            content.setCacheHint(CacheHint.SPEED);

            // 2. Gắn sự kiện lăn chuột mượt
            content.setOnScroll(event -> {
                double currentVvalue = scrollPane.getVvalue();
                double deltaY = event.getDeltaY() * DEFAULT_SPEED;
                scrollPane.setVvalue(currentVvalue - deltaY);

                event.consume(); // Ngăn hành vi cuộn mặc định của JavaFX
            });
        }
    }

    /**
     * Hàm nạp chồng (Overload) phòng trường hợp có trang nào đó
     * bạn muốn cuộn nhanh/chậm hơn bình thường
     */
    public static void makeSmooth(ScrollPane scrollPane, double customSpeed) {
        Node content = scrollPane.getContent();
        if (content != null) {
            content.setCache(true);
            content.setCacheHint(CacheHint.SPEED);
            content.setOnScroll(event -> {
                double currentVvalue = scrollPane.getVvalue();
                double deltaY = event.getDeltaY() * customSpeed;
                scrollPane.setVvalue(currentVvalue - deltaY);
                event.consume();
            });
        }
    }
}