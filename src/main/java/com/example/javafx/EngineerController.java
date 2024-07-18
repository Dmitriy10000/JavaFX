package com.example.javafx;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.List;

public class EngineerController
{
    @FXML
    private Label EngineerMenuLabel;

    @FXML
    private Label eCO2Label;

    @FXML
    private Label tVOCLabel;

    @FXML
    private Label HumidityLabel;

    @FXML
    private Label TemperatureLabel;

    @FXML
    private Label HeartRateLabel;

    @FXML
    private Label Spo2Label;

    @FXML
    private Label PressureLabel;

    @FXML
    private Label ServoMinText;

    @FXML
    private Label ServoMaxText;

    @FXML
    private Label ServoMinRightText;

    @FXML
    private Label ServoMaxRightText;

    @FXML
    private Label SpO2Threshold;

    @FXML
    private Label HeartRateThreshold;

    @FXML
    private Spinner<Integer> SpO2ThresholdSpinner = new Spinner<>();

    @FXML
    private Spinner<Integer> HeartRateThresholdSpinner = new Spinner<>();

    @FXML
    private Spinner<Integer> eco2Spinner = new Spinner<>();

    @FXML
    private Spinner<Integer> tVOCSpinner = new Spinner<>();

    @FXML
    private Spinner<Integer> HumiditySpinner = new Spinner<>();

    @FXML
    private Spinner<Integer> TemperatureSpinner = new Spinner<>();

    @FXML
    private Spinner<Integer> HeartRateSpinner = new Spinner<>();

    @FXML
    private Spinner<Integer> Spo2Spinner = new Spinner<>();

    @FXML
    private Spinner<Integer> PressureSpinner = new Spinner<>();

    @FXML
    private Spinner<Integer> LeftServoMinSpinner = new Spinner<>();

    @FXML
    private Spinner<Integer> LeftServoMaxSpinner = new Spinner<>();

    @FXML
    private Spinner<Integer> RightServoMinSpinner = new Spinner<>();

    @FXML
    private Spinner<Integer> RightServoMaxSpinner = new Spinner<>();

    @FXML
    private ChoiceBox<String> languageChoiceBox;

    @FXML
    private Label LanguageLabel;

    @FXML
    private Label SelectUserLabel;

    @FXML
    private ComboBox<String> selectUserComboBox;

    @FXML
    private Button goToMainBtn;

    @FXML
    private Button SaveButton;

    private void initialize() throws SQLException {
        languageChoiceBox.getItems().addAll("English", "Русский", "Қазақша");
        System.out.println(LanguageController.getLanguage());
        if (LanguageController.getLanguage().equals("en")) languageChoiceBox.setValue("English");
        if (LanguageController.getLanguage().equals("ru")) languageChoiceBox.setValue("Русский");
        if (LanguageController.getLanguage().equals("kz")) languageChoiceBox.setValue("Қазақша");

        List<String> users = DBController.searchUser("");
        selectUserComboBox.getItems().addAll(users);

        SaveButton.setOnAction(actionEvent -> {
            int HRTreshold = HeartRateThresholdSpinner.getValue();
            int O2Threshold = SpO2ThresholdSpinner.getValue();
            int eco2Coef = eco2Spinner.getValue();
            int tvocCoef = tVOCSpinner.getValue();
            int humidCoef = HumiditySpinner.getValue();
            int tempCoef = TemperatureSpinner.getValue();
            int hrCoef = HeartRateSpinner.getValue();
            int spo2Coef = Spo2Spinner.getValue();
            int pressureCoef = PressureSpinner.getValue();
            int LSMin = LeftServoMinSpinner.getValue();
            int LSMax = LeftServoMaxSpinner.getValue();
            int RSMin = RightServoMinSpinner.getValue();
            int RSMax = RightServoMaxSpinner.getValue();
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
    private void updateLanguage(){
        eCO2Label.setText("eCO2");
        tVOCLabel.setText("tVOC");
        Spo2Label.setText("Spo2");
        HumidityLabel.setText(LanguageController.getString("humidity"));
        TemperatureLabel.setText(LanguageController.getString("temperature"));
        HeartRateLabel.setText(LanguageController.getString("heartRate"));
        PressureLabel.setText(LanguageController.getString("pressure"));
        LanguageLabel.setText(LanguageController.getString("languageText"));
        ServoMinText.setText(LanguageController.getString("LeftServoMin"));
        ServoMaxText.setText(LanguageController.getString("LeftServoMax"));
        ServoMinRightText.setText(LanguageController.getString("RightServoMin"));
        ServoMaxRightText.setText(LanguageController.getString("RightServoMax"));
        SelectUserLabel.setText(LanguageController.getString("selectUserLabel"));
        SpO2Threshold.setText(LanguageController.getString("spo2thresh"));
        HeartRateThreshold.setText(LanguageController.getString("HRThresh"));
        EngineerMenuLabel.setText(LanguageController.getString("engineerMenuLabel"));
        goToMainBtn.setText(LanguageController.getString("toMainBtn"));
        SaveButton.setText(LanguageController.getString("saveBtn"));
    }
}
