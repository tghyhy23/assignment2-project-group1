package com.example.asm2;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;

public class MainApplication extends javafx.application.Application {

    @Override
    public void start(Stage stage) throws Exception {
        URL url = getClass().getResource("/com/example/asm2/layout/main-layout.fxml");

        FXMLLoader loader = new FXMLLoader(url);
        Scene scene = new Scene(loader.load(), 1100, 650);

        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Client Application");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}