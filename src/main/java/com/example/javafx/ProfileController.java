package com.example.javafx;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;


public class ProfileController {
    @FXML
    private ChoiceBox<String> languageChoiceBox;

    @FXML
    private Label LanguageLabel;

    @FXML
    private Button goToMainBtn;

    @FXML
    private Label UserProfileLabel;

    @FXML
    private TextField FirstName;

    @FXML
    private TextField LastName;

    @FXML
    private TextField PhoneNumber;

    @FXML
    private TextField Age;

    @FXML
    private TextField Weight;

    @FXML
    private Label GroupLabel;

    @FXML
    private Label SexLabel;

    @FXML
    private ChoiceBox<String> GroupChoiceBox;

    @FXML
    private ChoiceBox<String> SexChoiceBox;

    @FXML
    private Button SaveButton;

    private void initialize(){
        languageChoiceBox.getItems().addAll("English", "Русский", "Қазақша");
        System.out.println(LanguageController.getLanguage());
        if (LanguageController.getLanguage().equals("en")) languageChoiceBox.setValue("English");
        if (LanguageController.getLanguage().equals("ru")) languageChoiceBox.setValue("Русский");
        if (LanguageController.getLanguage().equals("kz")) languageChoiceBox.setValue("Қазақша");

        SaveButton.setOnAction(actionEvent -> {
            String firstName = FirstName.getText();
            String lastName = LastName.getText();
            String phoneNumber = PhoneNumber.getText();
            //int age = Age.getText();
            String weight = Weight.getText();
            String groupChoice = GroupChoiceBox.getValue();
            String sexChoice = SexChoiceBox.getValue();
        });

        goToMainBtn.setOnAction(actionEvent -> {
            try {
                SceneController.goToMain(actionEvent);
            } catch (Exception e) {
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
        LanguageLabel.setText(LanguageController.getString("languageText"));
        UserProfileLabel.setText(LanguageController.getString(""));
        FirstName.setPromptText(LanguageController.getString(""));
        LastName.setPromptText(LanguageController.getString(""));
        PhoneNumber.setPromptText(LanguageController.getString(""));
        Age.setPromptText(LanguageController.getString(""));
        Weight.setPromptText(LanguageController.getString(""));
        GroupLabel.setText(LanguageController.getString(""));
        SexLabel.setText(LanguageController.getString(""));
    }

}
