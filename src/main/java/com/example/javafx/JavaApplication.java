package com.example.javafx;

import javafx.application.Application;
import javafx.stage.Stage;

import java.sql.Timestamp;

public class JavaApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Тестирование таблицы users
        // Так происходит регистрация пользователя
        // (при регистрации пользователь автоматически входит в систему)
//        System.out.println(DBController.registerUser("Admin", "12345678", "user"));
        // Так происходит вход пользователя в систему
        // (делать вход после регистрации не обязательно)
        System.out.println(DBController.loginUser("Admin", "123"));


        // Тестирование таблицы sensors_config
        // Так происходит изменение конфигурации датчиков инженером
//        DBController.updateSensorsConfig(1, 90, 90, 1, 1, 1, 1, 1, 1);


        // Тестирование таблицы sensors_data
        // Так происходит отправка данных c микроконтроллера в бд
        DataController dc = new DataController();
        dc.generateData();
        DBController.insertData(dc);
        // Вместо DataController.generateData() должны быть данные с микроконтроллера
        // Примерный код закомментирован ниже (код выше закомментировать после подключения микроконтроллера)
//        String arduinoOutputString = "сюда добавишь тот прием данных через блютуз";
//        DataController dc = new DataController();
//        dc.parseData(arduinoOutputString);
        // Тут генерируются данные, близкие к предыдущим (для теста)
        for (int i = 0; i < 1000000; i++) {
            dc.regenerateData();
            dc.date = new Timestamp(System.currentTimeMillis() + i * 1000);
            DBController.insertData(dc);
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            if (i % 10000 == 0) System.out.println(i + " data inserted");
        }
        System.out.println("Data inserted");


        // Так происходит получение данных из бд
//        Timestamp endDate = new Timestamp(System.currentTimeMillis());
//        Timestamp startDate = new Timestamp(System.currentTimeMillis() - 1000 * 60);
//        DBController.getData("co2", startDate, endDate);
//        for (int i = 0; i < DBController.dataArray.dataCount; i++) {
//            System.out.println(DBController.dataArray.date[i] + " " + DBController.dataArray.data[i]);
//        }
    }
}
