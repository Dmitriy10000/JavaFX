package com.example.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

public class JavaFXApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        LanguageController.setLanguage("en");
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("login-view.fxml")));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Login");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
