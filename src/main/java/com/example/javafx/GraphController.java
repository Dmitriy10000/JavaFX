package com.example.javafx;

import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.application.Platform;
import javafx.scene.layout.HBox;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class GraphController {
    @FXML
    private LineChart<String, Number> lineChart;

    @FXML
    private CategoryAxis categoryAxis;

    @FXML
    private NumberAxis numberAxis;

    @FXML
    private ChoiceBox<String> dataTypeChoiceBox;

    @FXML
    private ChoiceBox<String> languageChoiceBox;

    @FXML
    private ComboBox<String> selectUserComboBox;

    @FXML
    private Label SelectUserLabel;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private Button showGraphBtn;

    @FXML
    private Button goToMainBtn;

    @FXML
    private ToggleButton liveToggleButton;

    @FXML
    private HBox dateHBox;

    private XYChart.Series<String, Number> series; // Серия для данных в реальном времени


    @FXML
    private void initialize() {
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
            try {
                List<String> users = DBController.searchUser("");
                selectUserComboBox.getItems().addAll(users);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ComPortController comPortController = new ComPortController();
        comPortController.initializeSerialPort();

        dataTypeChoiceBox.getItems().addAll("CO2", "tVOC", "SpO2", "Heart Rate", "Pressure", "Humidity", "Temperature");
        dataTypeChoiceBox.setValue("CO2");

        showGraphBtn.setOnAction(e -> {
            String dataType = dataTypeChoiceBox.getValue().toLowerCase().equals("heart rate") ? "heart_rate" : dataTypeChoiceBox.getValue().toLowerCase();
            Timestamp startDate = Timestamp.valueOf(startDatePicker.getValue().atStartOfDay());
            Timestamp endDate = Timestamp.valueOf(endDatePicker.getValue().atStartOfDay());
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

            final int fullCount = DBController.getDataCount(userId, startDate, endDate);
            final int userIdFinal = userId;
            final int maxDotCount = 1000;
            final int skipCoefficient = fullCount / maxDotCount;
            final int uploadStep = 10000;
            System.out.println("fullCount: " + fullCount);
            System.out.println("skipCoefficient: " + skipCoefficient);
            new Thread(() -> {
                try {
                    Platform.runLater(() -> lineChart.getData().clear());
                    for (int i = 0; i < fullCount; i += uploadStep*skipCoefficient) {
                        DBController.dataArray data = DBController.getData(userIdFinal, dataType, startDate, endDate, i, i+uploadStep*skipCoefficient, skipCoefficient);
                        XYChart.Series<String, Number> series = new XYChart.Series<>();
                        // Делаем подпись нашего графика
                        series.setName(dataType);
                        for (int j = 0; j < data.dataCount; j++) {
                            int finalJ = j;
                            series.getData().add(new XYChart.Data<>(data.date[finalJ].toString(), data.data[finalJ]));
                        }
                        Platform.runLater(() -> lineChart.getData().add(series));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();

//            DBController.dataArray data = DBController.getData(userId, dataType, startDate, endDate, 0, 0, 0);
//
//            XYChart.Series<String, Number> series = new XYChart.Series<>();
//            // Делаем подпись нашего графика
//            series.setName(dataType);
//
//            for (int i = 0; i < data.dataCount; i++) {
//                series.getData().add(new XYChart.Data<>(data.date[i].toString(), data.data[i]));
//            }
//            lineChart.getData().clear();
//            lineChart.getData().add(series);
        });

        // Отобразить график в реальном времени за последние 60 секунд
        liveToggleButton.setOnAction(e -> {
            if (liveToggleButton.isSelected()) {
                dateHBox.setDisable(true);
                // Делаем тень для кнопки и меняем цвет
                liveToggleButton.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0); -fx-background-color: rgb(235, 227, 213)");
                series = new XYChart.Series<>();
                // Делаем подпись нашего графика
                String dataType = dataTypeChoiceBox.getValue().toLowerCase();
                series.setName(dataType);
                lineChart.getData().clear();
                lineChart.getData().add(series);
                scheduleLiveDataUpdates();
            } else {
                dateHBox.setDisable(false);
                // Убираем тень для кнопки
                liveToggleButton.setStyle("-fx-background-color: rgb(235, 227, 213)");
                cancelLiveDataUpdates();
            }
        });

        // При смене выбора типа данных обновить график
        dataTypeChoiceBox.setOnAction(e -> {
            if (liveToggleButton.isSelected()) {
                series.getData().clear();
                // Делаем подпись нашего графика
                String dataType = dataTypeChoiceBox.getValue().toLowerCase();
                series.setName(dataType);
            }
        });

        goToMainBtn.setOnAction(actionEvent -> {
            try {
                cancelLiveDataUpdates();
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

    Timer timer = new Timer();

    private void scheduleLiveDataUpdates() {
        // Создать объект Timer для планирования повторяющихся обновлений
        if (timer == null) {
            timer = new Timer();
        }

        // Настроить TimerTask для вызова updateLiveData() каждую секунду
        TimerTask updateTask = new TimerTask() {
            @Override
            public void run() {
                updateLiveData();
            }
        };

        // Запланировать TimerTask на выполнение каждые 1000 миллисекунд (1 секунда)
        timer.scheduleAtFixedRate(updateTask, 0, 1000);
    }

    private void cancelLiveDataUpdates() {
        // Реализуйте логику остановки запланированной задачи, если это необходимо (например, с помощью флага)
        if (timer != null) {
            timer.cancel(); // Cancel the scheduled Timer task
            timer = null; // Reset the Timer reference
        }
    }

    private void updateLiveData() {
        // ... (получите последние данные, как и раньше)
        String dataType = dataTypeChoiceBox.getValue().toLowerCase();
        System.out.println(dataType);
        float dataValue = DataController.getData(dataType)/100.0f; // Получить значение новой точки данных
        Timestamp timestamp = getCurrentTimestamp(); // Получить Timestamp для новой точки данных
        System.out.println(timestamp + " " + dataValue);


        Platform.runLater(() -> {
            series.getData().add(new XYChart.Data<>(timestamp.toString(), dataValue));
            // Необязательно: ограничьте количество точек данных
            if (dataType.equals("humidity")) {
                if (series.getData().size() > 120) {
                    series.getData().remove(0);
                }
            }
            else if (series.getData().size() > 60) {
                series.getData().remove(0);
            }
        });
    }

    private Timestamp getCurrentTimestamp() {
        // Реализуйте логику получения текущего Timestamp (например, из System.currentTimeMillis())
        return new Timestamp(System.currentTimeMillis());
    }

    private void updateLanguage() {
    }
}
