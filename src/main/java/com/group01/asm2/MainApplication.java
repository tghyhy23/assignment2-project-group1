package com.group01.asm2;

import com.group01.asm2.db.PostgreSQLInitializer;
import com.group01.asm2.seeds.DatabaseSeeder;
import com.group01.asm2.security.SecureSceneLoader;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        Scene scene = SecureSceneLoader.loadScene(
            getClass(),
            "/com/group01/asm2/views/login.fxml"
        );

        scene.setFill(Color.TRANSPARENT);

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle("BidBlitz");
        stage.setScene(scene);

        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());

        stage.show();
    }

    public static void main(String[] args) {
        try {
            PostgreSQLInitializer.initSchema();
            DatabaseSeeder.seedAll();
        } catch (Exception e) {
            System.err.println("Lỗi khởi tạo Database: " + e.getMessage());
        }

        launch(args);
    }
}