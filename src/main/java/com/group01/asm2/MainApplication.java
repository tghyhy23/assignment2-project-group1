package com.group01.asm2;

import com.group01.asm2.db.PostgreSQLInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        URL url = getClass().getResource("/com/group01/asm2/layout/main-layout.fxml");

        if (url == null) {
            throw new IllegalStateException("Cannot find FXML file: /com/group01/asm2/layout/main-layout.fxml");
        }

        FXMLLoader loader = new FXMLLoader(url);
        Scene scene = new Scene(loader.load(), 1100, 650);

        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Client Application");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        PostgreSQLInitializer.init();
        launch(args); // JavaFX
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