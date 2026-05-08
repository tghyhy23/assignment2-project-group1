package com.group01.asm2;

import com.group01.asm2.db.PostgreSQLInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.paint.Color;

import java.net.URL;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // 1. Tải file FXML
        URL url = getClass().getResource("/com/group01/asm2/layout/main-layout.fxml");

        if (url == null) {
            throw new IllegalStateException("Lỗi: Không tìm thấy file FXML tại đường dẫn: /com/group01/asm2/layout/main-layout.fxml");
        }

        FXMLLoader loader = new FXMLLoader(url);

        // 2. Khởi tạo Scene (Không cần set kích thước cố định ở đây vì sẽ dùng toàn màn hình)
        Scene scene = new Scene(loader.load());

        // 3. Thiết lập Style cho Stage
        // UNDECORATED: Xóa thanh tiêu đề mặc định để tự thiết kế giao diện riêng
        scene.setFill(Color.TRANSPARENT);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle("Client Application");
        stage.setScene(scene);

        // 4. Xử lý mặc định Full Màn Hình (Tránh bug "biến mất" trên macOS)
        // Lấy thông tin màn hình chính
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        // Ép app lấy đúng tọa độ và kích thước của vùng làm việc (trừ thanh Taskbar/Menu Bar)
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());

        // 5. Hiển thị App
        stage.show();
    }

    public static void main(String[] args) {
        // Khởi tạo Database trước khi chạy giao diện
        try {
            PostgreSQLInitializer.init();
        } catch (Exception e) {
            System.err.println("Lỗi khởi tạo Database: " + e.getMessage());
        }

        // Chạy JavaFX
        launch(args);
    }
}


//package com.group01.asm2;
//
//import javafx.application.Application;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Scene;
//import javafx.stage.Stage;
//
//public class MainApplication extends Application {
//
//    @Override
//    public void start(Stage stage) throws Exception {
//        FXMLLoader loader = new FXMLLoader(
//                getClass().getResource("/com/group01/asm2/views/login.fxml")
//        );
//
//        Scene scene = new Scene(loader.load());
//
//        stage.setTitle("BidBlitz");
//        stage.setScene(scene);
//        stage.setMinWidth(1000);
//        stage.setMinHeight(650);
//        stage.centerOnScreen();
//        stage.show();
//    }
//
//    public static void main(String[] args) {
//        launch();
//    }
//}