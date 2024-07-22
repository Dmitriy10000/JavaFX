package com.example.javafx;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.w3c.dom.Text;

public class DoctorController {
    @FXML
    private ChoiceBox<String> languageChoiceBox;

    @FXML
    private Label LanguageLabel;

    @FXML
    private Label doctorTitle;

    @FXML
    private Label englishLabel;

    @FXML
    private Label russianLabel;

    @FXML
    private Label kazakhLabel;

    @FXML
    private TextField english;

    @FXML
    private TextField russian;

    @FXML
    private TextField kazakh;

    @FXML
    private Button saveButton;

    @FXML
    private Button goToMainBtn;

    @FXML
    private void initialize() {
        updateLanguage();
        languageChoiceBox.getItems().addAll("English", "Русский", "Қазақша");
        System.out.println(LanguageController.getLanguage());
        if (LanguageController.getLanguage().equals("en")) languageChoiceBox.setValue("English");
        if (LanguageController.getLanguage().equals("ru")) languageChoiceBox.setValue("Русский");
        if (LanguageController.getLanguage().equals("kz")) languageChoiceBox.setValue("Қазақша");


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
        saveButton.setOnAction(actionEvent -> {

        });
    }
    private void updateLanguage() {
        LanguageLabel.setText(LanguageController.getString("languageText"));
        doctorTitle.setText(LanguageController.getString("doctorTitle"));
        englishLabel.setText(LanguageController.getString("titleEnglish"));
        russianLabel.setText(LanguageController.getString("titleRussian"));
        kazakhLabel.setText(LanguageController.getString("titleKazakh"));
        saveButton.setText(LanguageController.getString("createBtn"));
        goToMainBtn.setText(LanguageController.getString("toMainBtn"));
    }
}
