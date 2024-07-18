package com.example.javafx;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.Timestamp;
import java.time.LocalDate;


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
    private DatePicker DateOfBirth;

    @FXML
    private TextField Weight;

    @FXML
    private Label FirstNameLabel;

    @FXML
    private Label LastNameLabel;

    @FXML
    private Label PhoneNumberLabel;

    @FXML
    private Label DateOfBirthLabel;

    @FXML
    private Label WeightLabel;

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

    @FXML
    private void initialize() {
        updateLanguage();
        languageChoiceBox.getItems().addAll("English", "Русский", "Қазақша");
        System.out.println(LanguageController.getLanguage());
        if (LanguageController.getLanguage().equals("en")) languageChoiceBox.setValue("English");
        if (LanguageController.getLanguage().equals("ru")) languageChoiceBox.setValue("Русский");
        if (LanguageController.getLanguage().equals("kz")) languageChoiceBox.setValue("Қазақша");

        SaveButton.setOnAction(actionEvent -> {
            String firstName = FirstName.getText();
            String lastName = LastName.getText();
            String phoneNumber = PhoneNumber.getText();
            LocalDate dateOfBirth = DateOfBirth.getValue();
            String weight = Weight.getText();
            String groupChoice = GroupChoiceBox.getValue();
            String sexChoice = SexChoiceBox.getValue();

            // Call the function to save user profile information
            boolean success = DBController.saveUserProfile(firstName, lastName, phoneNumber, dateOfBirth, weight, groupChoice, sexChoice);

            if (success) {
                // Show success message or handle success case
                System.out.println("User profile saved successfully.");
            } else {
                // Show error message or handle error case
                System.err.println("Failed to save user profile.");
            }
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
        UserProfileLabel.setText(LanguageController.getString("userProfileLabel"));
        FirstName.setPromptText(LanguageController.getString("firstName"));
        LastName.setPromptText(LanguageController.getString("lastName"));
        PhoneNumber.setPromptText(LanguageController.getString("phoneNumber"));
        DateOfBirth.setPromptText(LanguageController.getString("dateOfBirth"));
        Weight.setPromptText(LanguageController.getString("weight"));
        GroupLabel.setText(LanguageController.getString("group"));
        SexLabel.setText(LanguageController.getString("sex"));
        FirstNameLabel.setText(LanguageController.getString("firstNameLabel"));
        LastNameLabel.setText(LanguageController.getString("lastNameLabel"));
        PhoneNumberLabel.setText(LanguageController.getString("phoneNumberLabel"));
        DateOfBirthLabel.setText(LanguageController.getString("dateOfBirthLabel"));
        WeightLabel.setText(LanguageController.getString("weightLabel"));
        SaveButton.setText(LanguageController.getString("saveBtn"));
        goToMainBtn.setText(LanguageController.getString("toMainBtn"));
    }

}
