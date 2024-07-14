// • Атмосферное давление = паскали (30000...110000Pa)
// • Качество воздуха (концентрация кислорода и других газов):
//      eCO2 = миллионная доля (400...8192 ppm)
//      tVOC = миллиардная доля (0...1187 ppb)
// • Сердцебиение пользователя = удары в минуту (30...200bpm)
// • Влажность окружающей среды = относительная влажность (0...100%)
// • Температура окружающей среды = градусы Цельсия (-40...125°С)
//
// Пример данных отправляемых на порт:
// eCO2: 400 ppm, TVOC: 0 ppb
// Heart Rate: 0.00
// Pressure: 67894.80 Pa
// Humidity: -6.00 %
// Temperature: -36.85 *C
package com.example.javafx;

import java.sql.*;

import static java.sql.Types.NULL;

public class DataController {
    public static Timestamp date = new Timestamp(System.currentTimeMillis());
    // ВСЕ ДАННЫЕ УМНОЖЕНЫ НА 100, ЧТОБЫ НОРМАЛЬНО ХРАНИТЬ В БД
    public static int eCO2 = NULL;
    public static int TVOC = NULL;
    public static int HeartRate = NULL;
    public static int SpO2 = NULL;
    public static int Pressure = NULL;
    public static int Humidity = NULL;
    public static int Temperature = NULL;


    // Парсинг данных с микроконтроллера
    public static void parseData(String line) {
        date = new Timestamp(System.currentTimeMillis());
        if (line.startsWith("eCO2: ") && line.contains(" ppm")) {
            String eCO2String = line.substring("eCO2: ".length(), line.indexOf(" ppm"));
            eCO2 = (int) (Float.parseFloat(eCO2String) * 100);
            eCO2 = eCO2 * DBController.SensorsConfig.co2_coefficient;
        }
        if (line.startsWith("TVOC: ") && line.contains(" ppb")) {
            String TVOCString = line.substring("TVOC: ".length(), line.indexOf(" ppb"));
            TVOC = (int) (Float.parseFloat(TVOCString) * 100);
            TVOC = TVOC * DBController.SensorsConfig.tvoc_coefficient;
        }
        if (line.startsWith("Heart Rate: ")) {
            String HeartRateString = line.substring("Heart Rate: ".length());
            HeartRate = (int) (Float.parseFloat(HeartRateString) * 100);
            HeartRate = HeartRate * DBController.SensorsConfig.heart_rate_coefficient;
        }
        if (line.startsWith("SpO2: ") && line.contains(" %")){
            String Spo2String = line.substring("SpO2: ".length(), line.indexOf(" %"));
            SpO2 = (int) (Float.parseFloat(Spo2String) * 100);
            SpO2 = SpO2 * DBController.SensorsConfig.spo2_coefficient;
        }
        if (line.startsWith("Pressure: ") && line.contains(" Pa")) {
            String PressureString = line.substring("Pressure: ".length(), line.indexOf(" Pa"));
            Pressure = (int) (Float.parseFloat(PressureString) * 100);
            Pressure = Pressure * DBController.SensorsConfig.pressure_coefficient;
        }
        if (line.startsWith("Humidity: ") && line.contains(" %")) {
            String HumidityString = line.substring("Humidity: ".length(), line.indexOf(" %"));
            Humidity = (int) (Float.parseFloat(HumidityString) * 100);
            Humidity = Humidity * DBController.SensorsConfig.humidity_coefficient;
        }
        if (line.startsWith("Temperature: ") && line.contains(" *C")) {
            String TemperatureString = line.substring("Temperature: ".length(), line.indexOf(" *C"));
            Temperature = (int) (Float.parseFloat(TemperatureString) * 100);
            Temperature = Temperature * DBController.SensorsConfig.temperature_coefficient;
        }
    }

    // Получение данных
    public static String getString() {
        return "eCO2: " + (float) eCO2/100 + " ppm\n" +
                "TVOC: " + (float) TVOC/100 + " ppb\n" +
                "Heart Rate: " + (float) HeartRate/100 + " bpm\n" +
                "SpO2: " + (float) SpO2/100 + " $\n" +
                "Pressure: " + (float) Pressure/100 + " Pa\n" +
                "Humidity: " + (float) Humidity/100 + " %\n" +
                "Temperature: " + (float) Temperature/100 + " *C\n";
    }

    // Получение данных
    public static DataController getData() {
        return new DataController();
    }

    // Получение данных по типу
    public static int getData(String dataType) {
        return switch (dataType) {
            case "pressure" -> DataController.Pressure;
            case "humidity" -> DataController.Humidity;
            case "temperature" -> DataController.Temperature;
            case "spo2" -> DataController.SpO2;
            case "heart rate" -> DataController.HeartRate;
            case "co2" -> DataController.eCO2;
            case "tvoc" -> DataController.TVOC;
            default -> 0;
        };
    }

    // Тестовая генерация данных для отладки, пока не подключен микроконтроллер
    public static String generateData() {
        eCO2 = (int) (Math.random() * (819200 - 40000 + 1) + 40000);
        TVOC = (int) (Math.random() * (118700 - 0 + 1) + 0);
        HeartRate = (int) (Math.random() * (20000 - 3000 + 1) + 3000);
        SpO2 = (int) (Math.random() * (10000 - 0 + 1) + 0);
        Pressure = (int) (Math.random() * (11000000 - 3000000 + 1) + 3000000);
        Humidity = (int) (Math.random() * (10000 - 0 + 1) + 0);
        Temperature = (int) (Math.random() * (12500 - (-4000) + 1) + (-4000));
        return getString();
    }

    // Генерация данных, отклоняющихся не больше чем на 1% от предыдущих данных и не выходящих за пределы нормы
    public static void regenerateData() {
        date = new Timestamp(System.currentTimeMillis());
        eCO2 = eCO2 + (int) (Math.random() * (eCO2 / 100 + 1) - eCO2 / 200);
        if (eCO2 < 40000) eCO2 = 40000;
        if (eCO2 > 819200) eCO2 = 819200;
        TVOC = TVOC + (int) (Math.random() * (TVOC / 100 + 1) - TVOC / 200);
        if (TVOC < 0) TVOC = 0;
        if (TVOC > 118700) TVOC = 118700;
        HeartRate = HeartRate + (int) (Math.random() * (HeartRate / 100 + 1) - HeartRate / 200);
        if (HeartRate < 3000) HeartRate = 3000;
        if (HeartRate > 20000) HeartRate = 20000;
        SpO2 = SpO2 + (int) (Math.random() * (SpO2 / 100 + 1) - SpO2 / 200);
        if (SpO2 < 0) SpO2 = 0;
        if (SpO2 > 10000) SpO2 = 10000;
        Pressure = Pressure + (int) (Math.random() * (Pressure / 100 + 1) - Pressure / 200);
        if (Pressure < 3000000) Pressure = 3000000;
        if (Pressure > 11000000) Pressure = 11000000;
        Humidity = Humidity + (int) (Math.random() * (Humidity / 100 + 1) - Humidity / 200);
        if (Humidity < 0) Humidity = 0;
        if (Humidity > 10000) Humidity = 10000;
        Temperature = Temperature + (int) (Math.random() * (Temperature / 100 + 1) - Temperature / 200);
        if (Temperature < -4000) Temperature = -4000;
        if (Temperature > 12500) Temperature = 12500;
    }
}
