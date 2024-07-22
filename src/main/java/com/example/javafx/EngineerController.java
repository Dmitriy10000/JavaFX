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
    private Spinner<Double> eco2Spinner = new Spinner<Double>();

    @FXML
    private Spinner<Double> tVOCSpinner = new Spinner<>();

    @FXML
    private Spinner<Double> HumiditySpinner = new Spinner<>();

    @FXML
    private Spinner<Double> TemperatureSpinner = new Spinner<>();

    @FXML
    private Spinner<Double> HeartRateSpinner = new Spinner<>();

    @FXML
    private Spinner<Double> Spo2Spinner = new Spinner<>();

    @FXML
    private Spinner<Double> PressureSpinner = new Spinner<>();

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

    @FXML
    private void initialize() throws SQLException {
        updateLanguage();
        languageChoiceBox.getItems().addAll("English", "Русский", "Қазақша");
        System.out.println(LanguageController.getLanguage());
        if (LanguageController.getLanguage().equals("en")) languageChoiceBox.setValue("English");
        if (LanguageController.getLanguage().equals("ru")) languageChoiceBox.setValue("Русский");
        if (LanguageController.getLanguage().equals("kz")) languageChoiceBox.setValue("Қазақша");

        try {
            List<String> users = DBController.searchUser("");
            selectUserComboBox.getItems().addAll(users);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        SaveButton.setOnAction(actionEvent -> {
            // TODO
//            int HRTreshold = HeartRateThresholdSpinner.getValue();
//            int O2Threshold = SpO2ThresholdSpinner.getValue();

            DBController.SensorsConfig.co2_coefficient = (int) (eco2Spinner.getValue() * 1000);
            DBController.SensorsConfig.tvoc_coefficient = (int) (tVOCSpinner.getValue() * 1000);
            DBController.SensorsConfig.heart_rate_coefficient = (int) (HeartRateSpinner.getValue() * 1000);
            DBController.SensorsConfig.spo2_coefficient = (int) (Spo2Spinner.getValue() * 1000);
            DBController.SensorsConfig.temperature_coefficient = (int) (TemperatureSpinner.getValue() * 1000);
            DBController.SensorsConfig.pressure_coefficient = (int) (PressureSpinner.getValue() * 1000);
            DBController.SensorsConfig.humidity_coefficient = (int) (HumiditySpinner.getValue() * 1000);
            DBController.SensorsConfig.valve1_min = LeftServoMinSpinner.getValue();
            DBController.SensorsConfig.valve1_max = LeftServoMaxSpinner.getValue();
            DBController.SensorsConfig.valve2_min = RightServoMinSpinner.getValue();
            DBController.SensorsConfig.valve2_max = RightServoMaxSpinner.getValue();
            DBController.SensorsConfig.spo2_threshold = SpO2ThresholdSpinner.getValue();
            DBController.SensorsConfig.heart_rate_threshold = HeartRateThresholdSpinner.getValue();

            DBController.updateSensorsConfig(DBController.getUserId(selectUserComboBox.getValue()));
        });

        // При вводе в поле поиска пользователей обновляется список пользователей
        selectUserComboBox.setOnAction(actionEvent -> {
            try {
                List<String> users = DBController.searchUser(selectUserComboBox.getEditor().getText());
                selectUserComboBox.getItems().clear();
                selectUserComboBox.getItems().addAll(users);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // При выборе из выпадающего списка обновляется поле ввода
            selectUserComboBox.getEditor().setText(selectUserComboBox.getValue());

            // При выборе пользователя обновляются все поля
            int userId = DBController.getUserId(selectUserComboBox.getValue());
            DBController.loadSensorsConfig(userId);
            // -10.0f, 10.0f, DBController.SensorsConfig.co2_coefficient/1000.0f, 0.01f
            eco2Spinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-10, 10, DBController.SensorsConfig.co2_coefficient/1000.0f, 0.01f));
            tVOCSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-10, 10, DBController.SensorsConfig.tvoc_coefficient/1000.0f, 0.01f));
            HumiditySpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-10, 10, DBController.SensorsConfig.humidity_coefficient/1000.0f, 0.01f));
            TemperatureSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-10, 10, DBController.SensorsConfig.temperature_coefficient/1000.0f, 0.01f));
            HeartRateSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-10, 10, DBController.SensorsConfig.heart_rate_coefficient/1000.0f, 0.01f));
            Spo2Spinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-10, 10, DBController.SensorsConfig.spo2_coefficient/1000.0f, 0.01f));
            PressureSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-10, 10, DBController.SensorsConfig.pressure_coefficient/1000.0f, 0.01f));
            LeftServoMinSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-1000, 1000, DBController.SensorsConfig.valve1_min, 1));
            LeftServoMaxSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-1000, 1000, DBController.SensorsConfig.valve1_max, 1));
            RightServoMinSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-1000, 1000, DBController.SensorsConfig.valve2_min, 1));
            RightServoMaxSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-1000, 1000, DBController.SensorsConfig.valve2_max, 1));
            SpO2ThresholdSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, DBController.SensorsConfig.spo2_threshold, 1));
            HeartRateThresholdSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, DBController.SensorsConfig.heart_rate_threshold, 1));
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
