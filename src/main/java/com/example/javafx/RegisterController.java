package com.example.javafx;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.Locale;

public class RegisterController {
    @FXML
    private TextField loginField;

    @FXML
    private TextField passwordField;

    @FXML
    private Button registerBtn;

    @FXML
    private Button goToLoginBtn;

    @FXML
    private Label titleText;

    @FXML
    private Label registeredAlreadyText;

    @FXML
    private Label LanguageLabel;

    @FXML
    private Label errorText;

    @FXML
    private Label userTypeText;

    @FXML
    private ChoiceBox<String> languageChoiceBox;

    @FXML
    private ChoiceBox<String> userTypeChoiceBox;


    @FXML
    private void initialize() {
        userTypeChoiceBox.getItems().addAll("User", "Engineer", "Doctor");
        userTypeChoiceBox.setValue("User");
        updateLanguage();
        // Подгрузка языков En, Ru, Kz
        languageChoiceBox.getItems().addAll("English", "Русский", "Қазақша");
        System.out.println(LanguageController.getLanguage());
        if (LanguageController.getLanguage().equals("en")) languageChoiceBox.setValue("English");
        if (LanguageController.getLanguage().equals("ru")) languageChoiceBox.setValue("Русский");
        if (LanguageController.getLanguage().equals("kz")) languageChoiceBox.setValue("Қазақша");
        // Подгрузка типов пользователей


        registerBtn.setOnAction(actionEvent -> {
            String login = loginField.getText();
            String password = passwordField.getText();
            String userType = userTypeChoiceBox.getValue();
            String result = DBController.registerUser(login, password, userType);
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

        goToLoginBtn.setOnAction(actionEvent -> {
            try {
                SceneController.goToLogin(actionEvent);
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
        registerBtn.setText(LanguageController.getString("registerBtn"));
        goToLoginBtn.setText(LanguageController.getString("toLoginBtn"));
        titleText.setText(LanguageController.getString("registerTitle"));
        registeredAlreadyText.setText(LanguageController.getString("alreadyRegisteredText"));
        LanguageLabel.setText(LanguageController.getString("languageText"));
        userTypeText.setText(LanguageController.getString("userTypeText"));
        userTypeChoiceBox.getItems().clear();
        userTypeChoiceBox.getItems().addAll(
                LanguageController.getString("typeUser"),
                LanguageController.getString("typeEngineer"),
                LanguageController.getString("typeDoctor")
        );
    }
}
