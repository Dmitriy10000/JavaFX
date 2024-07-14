package com.example.javafx;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.sql.Timestamp;
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
    private BorderPane borderPane;

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
            borderPane.setDisable(true);
            for (int i = 0; i < 100; i++) {
                progressBar.setProgress(i / 100.0);
            }
            DBController.exportDataToCSV(userId, startDate, endDate, eCO2, tVOC, heartRate, spO2, pressure, humidity, temperature, 10000, 1);
            borderPane.setDisable(false);
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

    private void updateLanguage() {
    }
}
