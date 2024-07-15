package com.example.javafx;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.Objects;

public class ExportController {
    @FXML
    private VBox Language;

    @FXML
    private Label LanguageLabel;

    @FXML
    private Label SelectUserLabel;

    @FXML
    private Label ProgressBarLabel;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private CheckBox eCO2CheckBox;

    @FXML
    private Button exportDataBtn;

    @FXML
    private Button goToMainBtn;

    @FXML
    private DatePicker exportEndDatePicker;

    @FXML
    private DatePicker exportStartDatePicker;

    @FXML
    private CheckBox heartRateCheckBox;

    @FXML
    private CheckBox humidityCheckBox;

    @FXML
    private ChoiceBox<String> languageChoiceBox;

    @FXML
    private ComboBox<String> selectUserComboBox;

    @FXML
    private CheckBox pressureCheckBox;

    @FXML
    private CheckBox spO2CheckBox;

    @FXML
    private CheckBox tVOCCheckBox;

    @FXML
    private CheckBox temperatureCheckBox;

    @FXML
    private void initialize() throws SQLException {
        updateLanguage();
        // Подгрузка языков En, Ru, Kz
        languageChoiceBox.getItems().addAll("English", "Русский", "Қазақша");
        System.out.println(LanguageController.getLanguage());
        if (LanguageController.getLanguage().equals("en")) languageChoiceBox.setValue("English");
        if (LanguageController.getLanguage().equals("ru")) languageChoiceBox.setValue("Русский");
        if (LanguageController.getLanguage().equals("kz")) languageChoiceBox.setValue("Қазақша");

        if (Objects.equals(DBController.getUserType(), "doctor")) {
            SelectUserLabel.setVisible(true);
            selectUserComboBox.setVisible(true);
            List<String> users = DBController.searchUser("");
            selectUserComboBox.getItems().addAll(users);
        }

        goToMainBtn.setOnAction(actionEvent -> {
            try {
                SceneController.goToMain(actionEvent);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        exportDataBtn.setOnAction(actionEvent -> {
            Boolean eCO2 = eCO2CheckBox.isSelected();
            Boolean tVOC = tVOCCheckBox.isSelected();
            Boolean heartRate = heartRateCheckBox.isSelected();
            Boolean spO2 = spO2CheckBox.isSelected();
            Boolean pressure = pressureCheckBox.isSelected();
            Boolean humidity = humidityCheckBox.isSelected();
            Boolean temperature = temperatureCheckBox.isSelected();
            Timestamp startDate = Timestamp.valueOf(exportStartDatePicker.getValue().atStartOfDay());
            Timestamp endDate = Timestamp.valueOf(exportEndDatePicker.getValue().atStartOfDay());
            int userId = DBController.getUserId(selectUserComboBox.getValue());
            if (Objects.equals(DBController.getUserType(), "user")) {
                userId = DBController.getCurrentUserId();
            }
            if (userId == 0) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("User not found");
                alert.setContentText("Please select a user from the list");
                alert.showAndWait();
                return;
            }

            // Запрос на экспорт данных
            System.out.println("Найдено " + DBController.getDataCount(userId, startDate, endDate) + " записей для экспорта");
            progressBar.setVisible(true);
            ProgressBarLabel.setVisible(true);
            int count = DBController.getDataCount(userId, startDate, endDate);
            for (int i = 0; i < count; i += 1000) {
                progressBar.setProgress((double) i / count);
                exportDataToCSV(userId, startDate, endDate, eCO2, tVOC, heartRate, spO2, temperature, pressure, humidity, i, Math.min(i + 1000, count));
            }
            progressBar.setProgress(1);
            progressBar.setVisible(false);
            ProgressBarLabel.setVisible(false);
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
        });
    }

    // Вывод данных в CSV-файл (дописывание в конец файла)
    public static void exportDataToCSV(int userId, Timestamp start_date, Timestamp end_date, Boolean co2, Boolean tvoc, Boolean heart_rate, Boolean spO2, Boolean temperature, Boolean pressure, Boolean humidity, int from, int to) {
        try {
            File file = new File("export.csv");
            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            List<DataController> data = DBController.getDataToCSV(userId, start_date, end_date, co2, tvoc, heart_rate, spO2, temperature, pressure, humidity, from, to);
            for (DataController d : data) {
                bw.write(d.getData("pressure") + "," + d.getData("humidity") + "," + d.getData("temperature") + "," + d.getData("spo2") + "," + d.getData("heart rate") + "," + d.getData("co2") + "," + d.getData("tvoc") + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateLanguage() {
    }
}
