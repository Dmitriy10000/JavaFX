package com.example.javafx;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;

public class LoginController {
    @FXML
    private TextField loginField;

    @FXML
    private TextField passwordField;

    @FXML
    private Button loginBtn;

    @FXML
    private Button goToRegisterBtn;

    @FXML
    private Label titleText;

    @FXML
    private Label newHereText;

    @FXML
    private Label LanguageLabel;

    @FXML
    private Label errorText;

    @FXML
    private ChoiceBox<String> languageChoiceBox;

    @FXML
    private void initialize() {
        updateLanguage();
        // Подгрузка языков En, Ru, Kz
        languageChoiceBox.getItems().addAll("English", "Русский", "Қазақша");
        System.out.println(LanguageController.getLanguage());
        if (LanguageController.getLanguage().equals("en")) languageChoiceBox.setValue("English");
        if (LanguageController.getLanguage().equals("ru")) languageChoiceBox.setValue("Русский");
        if (LanguageController.getLanguage().equals("kz")) languageChoiceBox.setValue("Қазақша");

        loginBtn.setOnAction(actionEvent -> {
            String login = loginField.getText();
            String password = passwordField.getText();
            String result = DBController.loginUser(login, password);
            if (result.equals("Ok")) {
                errorText.setText("");
                try {
                    SceneController.goToMain(actionEvent);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                errorText.setText(result);
            }
        });

        goToRegisterBtn.setOnAction(actionEvent -> {
            try {
                SceneController.goToRegister(actionEvent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        languageChoiceBox.setOnAction(actionEvent -> {
            String language = languageChoiceBox.getValue();
            if (language.equals("English")) {
                LanguageController.setLanguage("en");
            } else if (language.equals("Русский")) {
                LanguageController.setLanguage("ru");
            } else if (language.equals("Қазақша")) {
                LanguageController.setLanguage("kz");
            }
            updateLanguage();
        });
    }

    private void updateLanguage() {
        loginField.setPromptText(LanguageController.getString("login"));
        passwordField.setPromptText(LanguageController.getString("password"));
        loginBtn.setText(LanguageController.getString("loginSignInButton"));
        goToRegisterBtn.setText(LanguageController.getString("createAccount"));
        titleText.setText(LanguageController.getString("loginSignIn"));
        newHereText.setText(LanguageController.getString("loginNewMessage"));
        LanguageLabel.setText(LanguageController.getString("languageText"));
    }
}
