package com.example.javafx;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class SceneController {
    private static Stage stage;
    private static Scene scene;
    private static Parent root;

    public static void goToLogin(ActionEvent event) throws IOException {
        root = FXMLLoader.load(Objects.requireNonNull(SceneController.class.getResource("login-view.fxml")));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Login");
        stage.show();
    }

    public static void goToRegister(ActionEvent event) throws IOException {
        root = FXMLLoader.load(Objects.requireNonNull(SceneController.class.getResource("register-view.fxml")));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Register");
        stage.show();
    }

    public static void goToGraph(ActionEvent event) throws IOException {
        root = FXMLLoader.load(Objects.requireNonNull(SceneController.class.getResource("graph-view.fxml")));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Graph");
        stage.show();
    }

    public static void goToMain(ActionEvent event) throws IOException {
        root = FXMLLoader.load(Objects.requireNonNull(SceneController.class.getResource("main-view.fxml")));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Main");
        stage.show();
    }

    public static void goToExport(ActionEvent event) throws IOException {
        root = FXMLLoader.load(Objects.requireNonNull(SceneController.class.getResource("export-view.fxml")));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Export");
        stage.show();
    }
}
