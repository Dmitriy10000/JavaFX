package com.example.javafx;

import com.fazecast.jSerialComm.SerialPort;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

import java.sql.Timestamp;
import java.util.Timer;
import java.util.TimerTask;

public class MainController {
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
    private Label HeartRateLabel2;

    @FXML
    private Label PressureLabel;

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
    private Label ServoMinText;

    @FXML
    private Label ServoMaxText;

    @FXML
    private Label ServoMinRightText;

    @FXML
    private Label ServoMaxRightText;

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
    private ChoiceBox<String> languageChoiceBox;

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
    private void initialize(){
        updateLanguage();
        ComPortChoiceBox.getItems().addAll(listAvailableComPorts());
        languageChoiceBox.getItems().addAll("English", "Русский", "Қазақша");
        System.out.println(LanguageController.getLanguage());
        if (LanguageController.getLanguage().equals("en")) languageChoiceBox.setValue("English");
        if (LanguageController.getLanguage().equals("ru")) languageChoiceBox.setValue("Русский");
        if (LanguageController.getLanguage().equals("kz")) languageChoiceBox.setValue("Қазақша");

        int userId = DBController.getCurrentUserId();
        DBController.loadSensorsConfig(userId);
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
            Spo2LabelData.setText(Spo2Label.getText() + ": " + String.valueOf(DataController.getData("spo2")/100.0f) + " %");
            TemperatureLabelData.setText(TemperatureLabel.getText() + ": " + String.valueOf(DataController.getData("temperature")/100.0f) + " *C");
            PressureLabelData.setText(PressureLabel.getText() + ": " + String.valueOf(((DataController.getData("pressure")/100.0f)/92110.3f)*100.0f) + " %");//среднегодовое атмосферное давление в алмате примерно равно: 92110,3
            HumidityLabelData.setText(HumidityLabel.getText() + ": " + String.valueOf(DataController.getData("humidity")/100.0f) + " %");

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
        eCO2Label.setText("eCO2");
        tVOCLabel.setText("tVOC");
        Spo2Label.setText("Spo2");
        HumidityLabel.setText(LanguageController.getString("humidity"));
        TemperatureLabel.setText(LanguageController.getString("temperature"));
        HeartRateLabel.setText(LanguageController.getString("heartRate"));
        HeartRateLabel2.setText(LanguageController.getString("heartRate"));
        PressureLabel.setText(LanguageController.getString("pressure"));
        LanguageLabel.setText(LanguageController.getString("languageText"));
        SelectComPortLabel.setText(LanguageController.getString("selectComPort"));
        detailedGraphButton.setText(LanguageController.getString("graphButton"));
        ServoLeftTitle.setText(LanguageController.getString("servoLeft"));
        ServoRightTitle.setText(LanguageController.getString("servoRight"));
        ConfirmSendBtn.setText(LanguageController.getString("confirmSend"));
        ServoMinText.setText(LanguageController.getString("servoMin"));
        ServoMaxText.setText(LanguageController.getString("servoMax"));
        ServoMinRightText.setText(LanguageController.getString("servoMin"));
        ServoMaxRightText.setText(LanguageController.getString("servoMax"));
        GoToExportBtn.setText(LanguageController.getString("exportData"));
        HeartRateLabelData.setText(HeartRateLabel.getText() + ": " + String.valueOf(DataController.getData("heart rate")/100.0f) + " bpm");
        Spo2LabelData.setText(Spo2Label.getText() + ": " + String.valueOf(DataController.getData("spo2")/100.0f) + " %");
        TemperatureLabelData.setText(TemperatureLabel.getText() + ": " + String.valueOf(DataController.getData("temperature")/100.0f) + " *C");
        PressureLabelData.setText(PressureLabel.getText() + ": " + String.valueOf(((DataController.getData("pressure")/100.0f)/92110.3f)*100.0f) + " %");//среднегодовое атмосферное давление в алмате примерно равно: 92110,3
        HumidityLabelData.setText(HumidityLabel.getText() + ": " + String.valueOf(DataController.getData("humidity")/100.0f) + " %");
    }
}
