package com.group01.asm2.security;

import com.group01.asm2.security.JavaFxSecurity;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;

public final class SecureSceneLoader {
    private SecureSceneLoader() {
    }

    public static Scene loadScene(Class<?> ownerClass, String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(ownerClass.getResource(fxmlPath));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        JavaFxSecurity.install(scene);

        return scene;
    }
}