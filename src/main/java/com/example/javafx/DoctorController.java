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
    private void initialize() {
        updateLanguage();
        languageChoiceBox.getItems().addAll("English", "Русский", "Қазақша");
        System.out.println(LanguageController.getLanguage());
        if (LanguageController.getLanguage().equals("en")) languageChoiceBox.setValue("English");
        if (LanguageController.getLanguage().equals("ru")) languageChoiceBox.setValue("Русский");
        if (LanguageController.getLanguage().equals("kz")) languageChoiceBox.setValue("Қазақша");



    }
    private void updateLanguage() {

    }
}
