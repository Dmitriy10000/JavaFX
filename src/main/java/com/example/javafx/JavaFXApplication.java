package com.example.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.Objects;

public class JavaFXApplication extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        // Set the main stage and load the login view
        primaryStage = stage;
        LanguageController.setLanguage("en");
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("login-view.fxml")));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Login");

        // Handle window close request (clicking X)
        stage.setOnCloseRequest(event -> {
            event.consume();  // Prevents the default close operation for custom handling
            closeApplication(event);
        });

        stage.show();
    }

    public static void closeApplication(WindowEvent event) {
        // Close the COM port if it's open
        ComPortController.closePort();

        // Proceed with closing the application
        System.out.println("Application closed, COM port released.");
        primaryStage.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
