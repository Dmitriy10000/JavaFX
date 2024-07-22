package com.example.javafx;

import com.fazecast.jSerialComm.SerialPort;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class MainController {
//    @FXML
//    private Label eCO2Label;
//
//    @FXML
//    private Label tVOCLabel;

    private String Humidity;
    private String Temperature;
    private String HeartRate;
    private String Spo2;
    private String Pressure;

    @FXML
    private Label HeartRateLabel;

    @FXML
    private Label eCO2LabelData;

    @FXML
    private Label tVOCLabelData;

    @FXML
    private Label HumidityLabelData;

    @FXML
    private Label TemperatureLabelData;

    @FXML
    private Label HeartRateLabelData;

    @FXML
    private Label Spo2LabelData;

    @FXML
    private Label PressureLabelData;

    @FXML
    private Label ServoLeftTitle;

    @FXML
    private Label ServoRightTitle;

    @FXML
    private Label LanguageLabel;

    @FXML
    private Label SelectComPortLabel;

    @FXML
    private Button ConfirmSendBtn;

    @FXML
    private Button detailedGraphButton;

    @FXML
    private Button LogoutBtn;

    @FXML
    private Button GoToExportBtn;

    @FXML
    private Button goToProfileBtn;

    @FXML
    private Button goToDoctorMenuBtn;

    @FXML
    private Button goToEngineerMenuBtn;

    @FXML
    private ChoiceBox<String> languageChoiceBox;

    @FXML
    private Slider servo1Slider;

    @FXML
    private Slider servo2Slider;

    @FXML
    private ChoiceBox<String> ComPortChoiceBox;

    @FXML
    private LineChart<String, Number> lineChart;

    private XYChart.Series<String, Number> series; // Серия для данных в реальном времени
    private final String lineChartDataType = "heart rate";


    @FXML
    private void initialize() throws SQLException {
        updateLanguage();
        ComPortChoiceBox.getItems().addAll(listAvailableComPorts());
        languageChoiceBox.getItems().addAll("English", "Русский", "Қазақша");
        System.out.println(LanguageController.getLanguage());
        if (LanguageController.getLanguage().equals("en")) languageChoiceBox.setValue("English");
        if (LanguageController.getLanguage().equals("ru")) languageChoiceBox.setValue("Русский");
        if (LanguageController.getLanguage().equals("kz")) languageChoiceBox.setValue("Қазақша");

        int userId = DBController.getCurrentUserId();
        if (Objects.equals(DBController.getUserType(), "user")) {
            DBController.loadSensorsConfig(userId);
            GoToExportBtn.setVisible(true);
            detailedGraphButton.setVisible(true);
            goToProfileBtn.setVisible(true);
        }
        else if (Objects.equals(DBController.getUserType(), "doctor")) {
            /*DBController.loadSensorsConfig(DBController.getUserId(DBController.searchUser("").getFirst()));*/
            GoToExportBtn.setVisible(true);
            detailedGraphButton.setVisible(true);
            goToDoctorMenuBtn.setVisible(true);
        }
        else if (Objects.equals(DBController.getUserType(), "engineer")) {
            /*DBController.loadSensorsConfig(DBController.getUserId(DBController.searchUser("").getFirst()));*/
            goToEngineerMenuBtn.setVisible(true);
        }
        servo1Slider.setMin(DBController.SensorsConfig.valve1_min);
        servo1Slider.setMax(DBController.SensorsConfig.valve1_max);
        servo1Slider.setValue((DBController.SensorsConfig.valve1_min + DBController.SensorsConfig.valve1_max) / 2);
        servo1Slider.setShowTickLabels(true);
        servo1Slider.setShowTickMarks(true);
        servo1Slider.setMajorTickUnit(10);
        servo1Slider.setMinorTickCount(1);
        servo1Slider.setBlockIncrement(1);
        servo2Slider.setMin(DBController.SensorsConfig.valve2_min);
        servo2Slider.setMax(DBController.SensorsConfig.valve2_max);
        servo2Slider.setValue((DBController.SensorsConfig.valve2_min + DBController.SensorsConfig.valve2_max) / 2);
        servo2Slider.setShowTickLabels(true);
        servo2Slider.setShowTickMarks(true);
        servo2Slider.setMajorTickUnit(10);
        servo2Slider.setMinorTickCount(1);
        servo2Slider.setBlockIncrement(1);

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

        detailedGraphButton.setOnAction(actionEvent -> {
            try {
                cancelLiveDataUpdates();
                SceneController.goToGraph(actionEvent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // При выборе COM порта начинаем слушать его и отображать данные на графике за последние 10 секунд по сердцебиению
        ComPortChoiceBox.setOnAction(actionEvent -> {
            ComPortController.portName = ComPortChoiceBox.getValue();
            ComPortController comPortController = new ComPortController();
            comPortController.initializeSerialPort();
            series = new XYChart.Series<>();
            series.setName("Heart Rate");
            lineChart.getData().clear();
            lineChart.getData().add(series);
            scheduleLiveDataUpdates();
        });

        // При нажатии на кнопку отправить, отправляем данные на ардуино
        ConfirmSendBtn.setOnAction(actionEvent -> {
            System.out.println("servo1: " + servo1Slider.getValue());
            System.out.println("servo2: " + servo2Slider.getValue());
            ComPortController.sendServoCommands((int) servo1Slider.getValue(), (int) servo2Slider.getValue());
        });

        LogoutBtn.setOnAction(actionEvent -> {
            try {
                cancelLiveDataUpdates();
                SceneController.goToLogin(actionEvent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        GoToExportBtn.setOnAction(actionEvent -> {
            try {
                cancelLiveDataUpdates();
                SceneController.goToExport(actionEvent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        goToProfileBtn.setOnAction(actionEvent -> {
            try {
                cancelLiveDataUpdates();
                SceneController.goToProfile(actionEvent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        goToDoctorMenuBtn.setOnAction(actionEvent -> {
            try {
                cancelLiveDataUpdates();
                SceneController.goToDoctorMenu(actionEvent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        goToEngineerMenuBtn.setOnAction(actionEvent -> {
            try {
                cancelLiveDataUpdates();
                SceneController.goToEngineerMenu(actionEvent);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        // System.out.println(lineChartDataType);
        float dataValue = DataController.getData(lineChartDataType)/100.f; // Получить значение новой точки данных
        Timestamp timestamp = getCurrentTimestamp(); // Получить Timestamp для новой точки данных
        // Обрезаем timestamp, что бы отображались только секунды
        String timestampString = timestamp.toString();
        timestampString = timestampString.substring(17, 19);
        // System.out.println(timestampString + " " + dataValue);
        String finalTimestampString = timestampString;

        Platform.runLater(() -> {
            eCO2LabelData.setText("eCO2: " + String.valueOf((DataController.getData("co2")/100.0f)/10000.0f) + " %");
            tVOCLabelData.setText("tVOC: " + String.valueOf(((DataController.getData("tvoc")/100.0f)/1187.0f)*100.0f) + " %");
            HeartRateLabelData.setText(HeartRateLabel.getText() + ": " + String.valueOf(DataController.getData("heart rate")/100.0f) + " bpm");
            Spo2LabelData.setText("SpO2: " + String.valueOf(DataController.getData("spo2")/100.0f) + " %");
            TemperatureLabelData.setText(LanguageController.getString("temperature") + ": " + String.valueOf(DataController.getData("temperature")/100.0f) + " *C");
            PressureLabelData.setText(LanguageController.getString("pressure") + ": " + String.valueOf(((DataController.getData("pressure")/100.0f)/92110.3f)*100.0f) + " %");//среднегодовое атмосферное давление в алмате примерно равно: 92110,3
            HumidityLabelData.setText(LanguageController.getString("humidity") + ": " + String.valueOf(DataController.getData("humidity")/100.0f) + " %");

            series.getData().add(new XYChart.Data<>(finalTimestampString, dataValue));
            // Необязательно: ограничьте количество точек данных
            if (series.getData().size() > 10) {
                series.getData().remove(0);
            }
        });
    }

    private Timestamp getCurrentTimestamp() {
        // Реализуйте логику получения текущего Timestamp (например, из System.currentTimeMillis())
        return new Timestamp(System.currentTimeMillis());
    }

    public static String[] listAvailableComPorts() {
        SerialPort[] ports = SerialPort.getCommPorts();
        String[] stringPorts = new String[ports.length];
        System.out.println("Available COM ports:");
        int i =0;
        for (SerialPort port : ports) {
            System.out.println(port.getSystemPortName() + " - " + port.getDescriptivePortName());
            stringPorts[i] = port.getSystemPortName();
            i++;
        }
        return stringPorts;
    }

    private void updateLanguage() {
        LogoutBtn.setText(LanguageController.getString("logout"));
        SelectComPortLabel.setText(LanguageController.getString("selectComPort"));
        LanguageLabel.setText(LanguageController.getString("languageText"));
        HeartRateLabel.setText(LanguageController.getString("heartRate"));
        ServoLeftTitle.setText(LanguageController.getString("servoLeft"));
        ServoRightTitle.setText(LanguageController.getString("servoRight"));
        ConfirmSendBtn.setText(LanguageController.getString("confirmSend"));
        GoToExportBtn.setText(LanguageController.getString("exportData"));
        detailedGraphButton.setText(LanguageController.getString("graphButton"));
        goToProfileBtn.setText(LanguageController.getString("profile"));
        goToDoctorMenuBtn.setText(LanguageController.getString("doctorMenu"));
        goToEngineerMenuBtn.setText(LanguageController.getString("engineerMenu"));
        HeartRateLabelData.setText(HeartRateLabel.getText() + ": " + String.valueOf(DataController.getData("heart rate")/100.0f) + " bpm");
        Spo2LabelData.setText("SpO2: " + String.valueOf(DataController.getData("spo2")/100.0f) + " %");
        PressureLabelData.setText(LanguageController.getString("pressure") + ": " + String.valueOf(((DataController.getData("pressure")/100.0f)/92110.3f)*100.0f) + " %");//среднегодовое атмосферное давление в алмате примерно равно: 92110,3
        HumidityLabelData.setText(LanguageController.getString("humidity") + ": " + String.valueOf(DataController.getData("humidity")/100.0f) + " %");
        TemperatureLabelData.setText(LanguageController.getString("temperature") + ": " + String.valueOf(DataController.getData("temperature")/100.0f) + " *C");
    }
}
