package com.example.javafx;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

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
            final int fullCount = DBController.getDataCount(userId, startDate, endDate);
            System.out.println("Найдено " + fullCount + " записей для экспорта");
            AtomicInteger count = new AtomicInteger(DBController.getDataCount(userId, startDate, endDate));
            // Выполняем экспорт в отдельном потоке, а потом обновляем прогрессбар
            // exportDataToXLS(userId, startDate, endDate, eCO2, tVOC, heartRate, spO2, temperature, pressure, humidity, i, Math.min(i + 1000, count));
            final int finalUserId = userId;
            new Thread(() -> {
                Platform.runLater(() -> progressBar.setProgress(0));
                Platform.runLater(() -> progressBar.setVisible(true));
                Platform.runLater(() -> ProgressBarLabel.setVisible(true));
                // Левый край диапазона - начало дня
                Timestamp left = startDate;
                // Правый край диапазона - конец дня
                Timestamp right = Timestamp.valueOf(startDate.toLocalDateTime().plusDays(1).toLocalDate().atStartOfDay());
                // Пока правый край диапазона меньше или равен конечной дате
                int countBefore = 0;
                while (right.compareTo(endDate) <= 0) {
                    count.set(DBController.getDataCount(finalUserId, left, right));
                    System.out.println("left: " + left + "\tright: " + right + "\tcount:" + count.get());
                    if (count.get() > 0) {
                        for (int i = 0; i < count.get(); i += 1000) {
                            System.out.println("Экспорт с " + i + " по " + Math.min(i + 1000, count.get()) + " запись");
                            exportDataToXLS(finalUserId, left, right, eCO2, tVOC, heartRate, spO2, temperature, pressure, humidity, i, Math.min(i + 1000, count.get()));
                            final int finalI = i + countBefore;
                            Platform.runLater(() -> progressBar.setProgress((double) finalI / fullCount));
                        }
                    }
                    left = right;
                    right = Timestamp.valueOf(right.toLocalDateTime().plusDays(1).toLocalDate().atStartOfDay());
                    countBefore += count.get();
                }
                Platform.runLater(() -> progressBar.setProgress(1));
                Platform.runLater(() -> progressBar.setVisible(false));
                Platform.runLater(() -> ProgressBarLabel.setVisible(false));
            }).start();
            System.out.println("Экспорт завершен");
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

    // Вывод данных в XLS таблицу через JExcelApi
    public static void exportDataToXLS(int userId, Timestamp start_date, Timestamp end_date, Boolean co2, Boolean tvoc, Boolean heart_rate, Boolean spO2, Boolean temperature, Boolean pressure, Boolean humidity, int from, int to) {
        try {
            // Создаем новый файл, если его нет
            File file = new File("export.xls");
            if (!file.exists()) {
                WritableWorkbook workbook = Workbook.createWorkbook(new File("export.xls"));
                WritableSheet sheet = workbook.createSheet(start_date.toString().substring(0, 10), 0);

                // Заполняем шапку таблицы
                int i = 0;
                sheet.addCell(new jxl.write.Label(i++, 0, "Time"));
                if (co2) sheet.addCell(new jxl.write.Label(i++, 0, "eCO2 (%)"));
                if (tvoc) sheet.addCell(new jxl.write.Label(i++, 0, "TVOC (%)"));
                if (heart_rate) sheet.addCell(new jxl.write.Label(i++, 0, "Heart Rate"));
                if (spO2) sheet.addCell(new jxl.write.Label(i++, 0, "SpO2 (%)"));
                if (temperature) sheet.addCell(new jxl.write.Label(i++, 0, "Temperature"));
                if (pressure) sheet.addCell(new jxl.write.Label(i++, 0, "Pressure"));
                if (humidity) sheet.addCell(new jxl.write.Label(i, 0, "Humidity (%)"));

                workbook.write();
                workbook.close();
            }

            // Открываем лист который подписан датой из start_date обрезая время, если его нет, то создаем новый
            Workbook copy = Workbook.getWorkbook(new File("export.xls"));
            WritableWorkbook workbook = Workbook.createWorkbook(new File("export.xls"), copy);
            WritableSheet sheet = workbook.getSheet(start_date.toString().substring(0, 10));
            if (sheet == null) {
                System.out.println("Создаем новый лист");
                sheet = workbook.createSheet(start_date.toString().substring(0, 10), workbook.getNumberOfSheets());

                // Заполняем шапку таблицы
                int i = 0;
                sheet.addCell(new jxl.write.Label(i++, 0, "Time"));
                if (co2) sheet.addCell(new jxl.write.Label(i++, 0, "eCO2 (%)"));
                if (tvoc) sheet.addCell(new jxl.write.Label(i++, 0, "TVOC (%)"));
                if (heart_rate) sheet.addCell(new jxl.write.Label(i++, 0, "Heart Rate"));
                if (spO2) sheet.addCell(new jxl.write.Label(i++, 0, "SpO2 (%)"));
                if (temperature) sheet.addCell(new jxl.write.Label(i++, 0, "Temperature"));
                if (pressure) sheet.addCell(new jxl.write.Label(i++, 0, "Pressure"));
                if (humidity) sheet.addCell(new jxl.write.Label(i, 0, "Humidity (%)"));
            }

            // Получаем данные из БД
            List<DataController.data> data = DBController.getDataToXLS(userId, start_date, end_date, co2, tvoc, heart_rate, spO2, temperature, pressure, humidity, from, to, 1);

            // Вводим данные на первую свободную строку
            int row = sheet.getRows();
            assert data != null;
            for (DataController.data d : data) {
                int i = 0;
                // Вырезаем дату из Timestamp
                sheet.addCell(new jxl.write.Label(i++, row, d.date.toString().substring(11, 19)));
                if (co2) sheet.addCell(new jxl.write.Number(i++, row, d.eCO2/100.0f/10000.0f));
                if (tvoc) sheet.addCell(new jxl.write.Number(i++, row, d.TVOC/100.0f/1187.0f*100.0f));
                if (heart_rate) sheet.addCell(new jxl.write.Number(i++, row, d.HeartRate/100.0f));
                if (spO2) sheet.addCell(new jxl.write.Number(i++, row, d.SpO2/100.0f));
                if (temperature) sheet.addCell(new jxl.write.Number(i++, row, d.Temperature/100.0f));
                if (pressure) sheet.addCell(new jxl.write.Number(i++, row, d.Pressure/100.0f));
                if (humidity) sheet.addCell(new jxl.write.Number(i, row, d.Humidity/100.0f));
                row++;
            }

            workbook.write();
            workbook.close();
        } catch (IOException | BiffException | WriteException e) {
            e.printStackTrace();
        }
    }

    private void updateLanguage() {
    }
}
