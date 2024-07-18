package com.example.javafx;

import javafx.scene.chart.XYChart;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class DBController {
    private static final String DB_PATH = "jdbc:sqlite:src/main/resources/database.db";
    private static int userId = 0;

    // Инициализация
    static {
        try {
            Class.forName("org.sqlite.JDBC");
            System.out.println("Драйвер JDBC SQLite загружен.");
            createDatabase();
        } catch (ClassNotFoundException e) {
            System.err.println("Ошибка загрузки драйвера JDBC SQLite: " + e.getMessage());
        }
    }

    // Проверка наличия базы данных
    public static boolean databaseExists() {
        File databaseFile = new File(DB_PATH);
        return databaseFile.exists();
    }

    // Создание базы данных и таблиц
    public static void createDatabase() {
        try (Connection connection = DriverManager.getConnection(DB_PATH)) {
            if (!databaseExists()) {
                Statement statement = connection.createStatement();

                String createUsersTable = """
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username VARCHAR(255) NOT NULL,
                        password VARCHAR(255) NOT NULL,
                        user_type TEXT CHECK( user_type IN ('user','engineer','doctor') ) NOT NULL DEFAULT 'user'
                    )
                """;
                statement.execute(createUsersTable);
                System.out.println("Таблица users создана успешно.");

                String createSensorsConfigTable = """
                    CREATE TABLE IF NOT EXISTS sensors_config (
                        id INTEGER PRIMARY KEY NOT NULL,
                        valve1_min INTEGER NOT NULL DEFAULT 0,
                        valve1_max INTEGER NOT NULL DEFAULT 180,
                        valve2_min INTEGER NOT NULL DEFAULT 0,
                        valve2_max INTEGER NOT NULL DEFAULT 180,
                        co2_coefficient INTEGER NOT NULL DEFAULT 1000,
                        tvoc_coefficient INTEGER NOT NULL DEFAULT 1000,
                        heart_rate_coefficient INTEGER NOT NULL DEFAULT 1000,
                        spo2_coefficient INTEGER NOT NULL DEFAULT 1000,
                        temperature_coefficient INTEGER NOT NULL DEFAULT 1000,
                        pressure_coefficient INTEGER NOT NULL DEFAULT 1000,
                        humidity_coefficient INTEGER NOT NULL DEFAULT 1000
                    )
                """;
                statement.execute(createSensorsConfigTable);
                System.out.println("Таблица sensors_config создана успешно.");

                String createSensorsDataTable = """
                    CREATE TABLE IF NOT EXISTS sensors_data (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        date TIMESTAMP NOT NULL,
                        co2 INTEGER,
                        tvoc INTEGER,
                        heart_rate INTEGER,
                        spo2 INTEGER,
                        temperature INTEGER,
                        pressure INTEGER,
                        humidity INTEGER,
                        FOREIGN KEY(user_id) REFERENCES users(id)
                    )
                """;
                statement.execute(createSensorsDataTable);
                System.out.println("Таблица sensors_data создана успешно.");

                String createUserData = """
                    CREATE TABLE IF NOT EXISTS user_data (
                        id INTEGER PRIMARY KEY NOT NULL,
                        name VARCHAR(255),
                        surname VARCHAR(255),
                        birth_date DATE,
                        weight INTEGER,
                        group_id INTEGER,
                        sex VARCHAR(255) CHECK(sex IN ('male', 'female')),
                        phone_number VARCHAR(255)
                    )
                """;
                statement.execute(createUserData);
                System.out.println("Таблица user_data создана успешно.");

                String createGroupTable = """
                    CREATE TABLE IF NOT EXISTS groups (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        en_name TEXT NOT NULL,
                        ru_name TEXT NOT NULL,
                        kz_name TEXT NOT NULL
                    )
                """;
                statement.execute(createGroupTable);
                System.out.println("Таблица groups создана успешно.");

                System.out.println("База данных и таблицы созданы успешно.");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка создания базы данных: " + e.getMessage());
        }
    }

    // Добавление данных в таблицу sensors_data
    public static void insertData(DataController dc) {
        if (userId == 0) {
            System.out.println("Пользователь не авторизован.");
            return;
        }
        try (Connection connection = DriverManager.getConnection(DB_PATH)) {
            String insertData = """
                INSERT INTO sensors_data (user_id, date, co2, tvoc, heart_rate, spo2, temperature, pressure, humidity)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
            PreparedStatement preparedStatement = connection.prepareStatement(insertData);
            preparedStatement.setInt(1, userId);
            preparedStatement.setTimestamp(2, dc.date);
            preparedStatement.setInt(3, dc.eCO2);
            preparedStatement.setInt(4, dc.TVOC);
            preparedStatement.setInt(5, dc.HeartRate);
            preparedStatement.setInt(6, dc.SpO2);
            preparedStatement.setInt(7, dc.Temperature);
            preparedStatement.setInt(8, dc.Pressure);
            preparedStatement.setInt(9, dc.Humidity);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Ошибка добавления данных в таблицу sensors_data: " + e.getMessage());
        }
    }

    // Добавление пользователя в таблицу users
    private static String insertUser(String username, String password, String user_type) {
        // Меняем user_type на английский
        String temp = LanguageController.getLanguage();
        if (!temp.equals("en")) {
            if (user_type.equals(LanguageController.getString("typeUser"))) {
                LanguageController.setLanguage("en");
                user_type = LanguageController.getString("typeUser");
            }
            else if (user_type.equals(LanguageController.getString("typeEngineer"))) {
                LanguageController.setLanguage("en");
                user_type = LanguageController.getString("typeEngineer");
            }
            else if (user_type.equals(LanguageController.getString("typeDoctor"))) {
                LanguageController.setLanguage("en");
                user_type = LanguageController.getString("typeDoctor");
            }
            LanguageController.setLanguage(temp);
        }
        user_type = user_type.toLowerCase();

        try (Connection connection = DriverManager.getConnection(DB_PATH)) {
            // Проверка наличия пользователя в таблице users
            String selectUser = """
                SELECT id FROM users WHERE username = ?
            """;
            PreparedStatement selectStatement = connection.prepareStatement(selectUser);
            selectStatement.setString(1, username);
            ResultSet resultSet = selectStatement.executeQuery();
            if (resultSet.next()) {
                System.out.println("Пользователь с таким именем уже существует.");
                return "User with this name already exists.";
            }
            String insertUser = """
                INSERT INTO users (username, password, user_type)
                VALUES (?, ?, ?)
            """;
            PreparedStatement preparedStatement = connection.prepareStatement(insertUser);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, PasswordUtils.encodePassword(password));
            preparedStatement.setString(3, user_type);
            preparedStatement.executeUpdate();
            System.out.println("Пользователь успешно добавлен в таблицу users.");

            // Получаем id добавленного пользователя
            String selectUserId = """
                SELECT id FROM users WHERE username = ?
            """;
            PreparedStatement selectUserIdStatement = connection.prepareStatement(selectUserId);
            selectUserIdStatement.setString(1, username);
            ResultSet resultSetUserId = selectUserIdStatement.executeQuery();
            if (resultSetUserId.next()) {
                userId = resultSetUserId.getInt("id");
            }

            // Добавляем дефолтные значения в таблицу sensors_config
            System.out.println(user_type);
            if (user_type.equals("user")) {
                String insertConfig = """
                    INSERT INTO sensors_config (id)
                    VALUES (?)
                """;
                PreparedStatement insertConfigStatement = connection.prepareStatement(insertConfig);
                insertConfigStatement.setInt(1, userId);
                insertConfigStatement.executeUpdate();

                String insertUserData = """
                    INSERT INTO user_data (id)
                    VALUES (?)
                """;
                PreparedStatement insertUserDataStatement = connection.prepareStatement(insertUserData);
                insertUserDataStatement.setInt(1, userId);
                insertUserDataStatement.executeUpdate();
            }
            return "Ok";
        } catch (SQLException e) {
            System.err.println("Ошибка добавления пользователя в таблицу users: " + e.getMessage());
        }
        return "Error";
    }

    // Проверка пользователя при авторизации
    private static boolean verifyUser(String username, String password) {
        try (Connection connection = DriverManager.getConnection(DB_PATH)) {
            String selectUser = """
                SELECT id, password FROM users WHERE username = ?
            """;
            PreparedStatement preparedStatement = connection.prepareStatement(selectUser);
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next() && PasswordUtils.verifyPassword(password, resultSet.getString("password"))) {
                userId = resultSet.getInt("id");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Ошибка проверки пользователя: " + e.getMessage());
        }
        return false;
    }

    // Регистрация пользователя
    public static String registerUser(String username, String password, String user_type) {
        if (username.length() < 3 || username.length() > 255) {
            System.out.println("Имя пользователя должно содержать от 3 до 255 символов.");
            return "Username must be between 3 and 255 characters.";
        }
        if (password.length() < 3 || password.length() > 255) {
            System.out.println("Пароль должен содержать от 3 до 255 символов.");
            return "Password must be between 3 and 255 characters.";
        }
        if (username.contains(" ") || password.contains(" ")) {
            System.out.println("Имя пользователя и пароль не должны содержать пробелы.");
            return "Username and password must not contain spaces.";
        }
        String result = insertUser(username, password, user_type);
        if (result.equals("Ok")) {
            System.out.println("Пользователь успешно зарегистрирован.");
            return "Ok";
        }
        return result;
    }

    // Авторизация пользователя
    public static String loginUser(String username, String password) {
        if (username.length() < 3 || username.length() > 255) {
            System.out.println("Имя пользователя должно содержать от 3 до 255 символов.");
            return "Username must be between 3 and 255 characters.";
        }
        if (password.length() < 3 || password.length() > 255) {
            System.out.println("Пароль должен содержать от 3 до 255 символов.");
            return "Password must be between 3 and 255 characters.";
        }
        if (username.contains(" ") || password.contains(" ")) {
            System.out.println("Имя пользователя и пароль не должны содержать пробелы.");
            return "Username and password must not contain spaces.";
        }
        if (verifyUser(username, password)) {
            System.out.println("Пользователь успешно авторизован.");
            return "Ok";
        } else {
            System.out.println("Неверное имя пользователя или пароль.");
            return "Incorrect username or password.";
        }
    }

    // Проверка статуса пользователя
    public static String getUserType() {
        try (Connection connection = DriverManager.getConnection(DB_PATH)) {
            String selectUserType = """
                SELECT user_type FROM users WHERE id = ?
            """;
            PreparedStatement preparedStatement = connection.prepareStatement(selectUserType);
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("user_type");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения типа пользователя: " + e.getMessage());
        }
        return null;
    }

    // Получение id пользователя по логину
    public static int getUserId(String username) {
        try (Connection connection = DriverManager.getConnection(DB_PATH)) {
            String selectUserId = """
                SELECT id FROM users WHERE username = ?
            """;
            PreparedStatement preparedStatement = connection.prepareStatement(selectUserId);
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("id");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения id пользователя: " + e.getMessage());
        }
        return 0;
    }

    // Получение id текущего пользователя
    public static int getCurrentUserId() {
        return userId;
    }

    // Изменение конфигурации датчиков
    public static void updateSensorsConfig() {
        if (userId == 0) {
            System.out.println("Пользователь не авторизован.");
            return;
        }
        if (!Objects.equals(getUserType(), "engineer")) {
            System.out.println("Только инженер может изменять конфигурацию датчиков.");
            return;
        }
        try (Connection connection = DriverManager.getConnection(DB_PATH)) {
            String updateConfig = """
                UPDATE sensors_config
                SET valve1_min = ?, valve1_max = ?, valve2_min = ?, valve2_max = ?,
                    co2_coefficient = ?, tvoc_coefficient = ?, heart_rate_coefficient = ?, spo2_coefficient = ?,
                    temperature_coefficient = ?, pressure_coefficient = ?, humidity_coefficient = ?
                WHERE id = 1
            """;
            PreparedStatement preparedStatement = connection.prepareStatement(updateConfig);
            preparedStatement.setInt(1, SensorsConfig.valve1_min);
            preparedStatement.setInt(2, SensorsConfig.valve1_max);
            preparedStatement.setInt(3, SensorsConfig.valve2_min);
            preparedStatement.setInt(4, SensorsConfig.valve2_max);
            preparedStatement.setInt(5, SensorsConfig.co2_coefficient);
            preparedStatement.setInt(6, SensorsConfig.tvoc_coefficient);
            preparedStatement.setInt(7, SensorsConfig.heart_rate_coefficient);
            preparedStatement.setInt(8, SensorsConfig.spo2_coefficient);
            preparedStatement.setInt(9, SensorsConfig.temperature_coefficient);
            preparedStatement.setInt(10, SensorsConfig.pressure_coefficient);
            preparedStatement.setInt(11, SensorsConfig.humidity_coefficient);
            preparedStatement.executeUpdate();
            System.out.println("Конфигурация датчиков успешно обновлена.");
        } catch (SQLException e) {
            System.err.println("Ошибка обновления конфигурации датчиков: " + e.getMessage());
        }
    }

    // Конфигурация датчиков
    public class SensorsConfig {
        static int valve1_min = 0;
        static int valve1_max = 0;
        static int valve2_min = 0;
        static int valve2_max = 0;
        static int co2_coefficient = 0;
        static int tvoc_coefficient = 0;
        static int heart_rate_coefficient = 0;
        static int spo2_coefficient = 0;
        static int temperature_coefficient = 0;
        static int pressure_coefficient = 0;
        static int humidity_coefficient = 0;
    }

    // Получение конфигурации датчиков
    public static void loadSensorsConfig(int userId) {
        try (Connection connection = DriverManager.getConnection(DB_PATH)) {
            String selectConfig = """
                SELECT * FROM sensors_config WHERE id = ?
            """;
            PreparedStatement preparedStatement = connection.prepareStatement(selectConfig);
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                SensorsConfig.valve1_min = resultSet.getInt("valve1_min");
                SensorsConfig.valve1_max = resultSet.getInt("valve1_max");
                SensorsConfig.valve2_min = resultSet.getInt("valve2_min");
                SensorsConfig.valve2_max = resultSet.getInt("valve2_max");
                SensorsConfig.co2_coefficient = resultSet.getInt("co2_coefficient");
                SensorsConfig.tvoc_coefficient = resultSet.getInt("tvoc_coefficient");
                SensorsConfig.heart_rate_coefficient = resultSet.getInt("heart_rate_coefficient");
                SensorsConfig.spo2_coefficient = resultSet.getInt("spo2_coefficient");
                SensorsConfig.temperature_coefficient = resultSet.getInt("temperature_coefficient");
                SensorsConfig.pressure_coefficient = resultSet.getInt("pressure_coefficient");
                SensorsConfig.humidity_coefficient = resultSet.getInt("humidity_coefficient");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения конфигурации датчиков: " + e.getMessage());
        }
    }

    // Массив данных, который возвращает метод getString
    public static class dataArray {
        static int dataCount = 60*60*24*366; // 1 год
        static Timestamp[] date = new Timestamp[dataCount];
        static float[] data = new float[dataCount];

        public XYChart.Series<String, Number> getSeries() {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            for (int i = 0; i < dataCount; i++) {
                if (date[i] != null) {
                    series.getData().add(new XYChart.Data<>(date[i].toString(), data[i]));
                }
            }
            return series;
        }
    }

    // Получение данных за определенный период
    public static dataArray getData(int userId, String type, Timestamp start_date, Timestamp end_date, int from, int to, int skip) {
        if (type.equals("co2") || type.equals("tvoc") || type.equals("heart_rate") || type.equals("spo2") || type.equals("spo2") || type.equals("temperature") || type.equals("pressure") || type.equals("humidity")) {
            dataArray data = new dataArray();
            try (Connection connection = DriverManager.getConnection(DB_PATH)) {
                String selectData = """
                    SELECT date, %s FROM sensors_data
                    WHERE user_id = ? AND date BETWEEN ? AND ? AND %s IS NOT NULL
                    ORDER BY date ASC
                    LIMIT ? OFFSET ?
                """.formatted(type, type);
                PreparedStatement preparedStatement = connection.prepareStatement(selectData);
                preparedStatement.setInt(1, userId);
                preparedStatement.setTimestamp(2, start_date);
                preparedStatement.setTimestamp(3, end_date);
                preparedStatement.setInt(4, to-from);
                preparedStatement.setInt(5, from);
                ResultSet resultSet = preparedStatement.executeQuery();
                int i = 0;
                while (resultSet.next()) {
                    data.date[i] = resultSet.getTimestamp("date");
                    data.data[i] = resultSet.getInt(type) / 100.0f;
                    i++;
                    for (int j = 0; j < skip; j++) {
                        resultSet.next();
                    }
                }
                data.dataCount = i;
            } catch (SQLException e) {
                System.err.println("Ошибка получения данных: " + e.getMessage());
            }
        }
        return null;
    }

    // Удаление всех данных из таблицы sensors_data
    public static void deleteSensorsData() {
        try (Connection connection = DriverManager.getConnection(DB_PATH)) {
            String deleteData = """
                DELETE FROM sensors_data
            """;
            Statement statement = connection.createStatement();
            statement.execute(deleteData);
            System.out.println("Все данные успешно удалены из таблицы sensors_data.");
        } catch (SQLException e) {
            System.err.println("Ошибка удаления данных из таблицы sensors_data: " + e.getMessage());
        }
    }

    // Получаем количество записей в таблице sensors_data
    public static int getDataCount(int userId, Timestamp start_date, Timestamp end_date) {
        try (Connection connection = DriverManager.getConnection(DB_PATH)) {
            String selectData = """
                SELECT COUNT(*) FROM sensors_data
                WHERE user_id = ? AND date BETWEEN ? AND ?
            """;
            PreparedStatement preparedStatement = connection.prepareStatement(selectData);
            preparedStatement.setInt(1, userId);
            preparedStatement.setTimestamp(2, start_date);
            preparedStatement.setTimestamp(3, end_date);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения количества записей: " + e.getMessage());
        }
        return 0;
    }

    // Получаем данные для экспорта в XLS
    public static List<DataController.data> getDataToXLS(int userId, Timestamp start_date, Timestamp end_date, Boolean co2, Boolean tvoc, Boolean heart_rate, Boolean spO2, Boolean temperature, Boolean pressure, Boolean humidity, int from, int to, int skip) {
        try (Connection connection = DriverManager.getConnection(DB_PATH)) {
            String selectData = "SELECT date";
            if (co2) selectData += ", co2";
            if (tvoc) selectData += ", tvoc";
            if (heart_rate) selectData += ", heart_rate";
            if (spO2) selectData += ", spo2";
            if (temperature) selectData += ", temperature";
            if (pressure) selectData += ", pressure";
            if (humidity) selectData += ", humidity";
            selectData += """
                FROM sensors_data
                WHERE user_id = ? AND date BETWEEN ? AND ?
                LIMIT ? OFFSET ?
            """;
            PreparedStatement preparedStatement = connection.prepareStatement(selectData);
            preparedStatement.setInt(1, userId);
            preparedStatement.setTimestamp(2, start_date);
            preparedStatement.setTimestamp(3, end_date);
            preparedStatement.setInt(4, to-from);
            preparedStatement.setInt(5, from);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<DataController.data> data = new ArrayList<>();
            while (resultSet.next()) {
                DataController.data dc = new DataController.data();
                dc.date = resultSet.getTimestamp("date");
                if (co2) dc.eCO2 = resultSet.getInt("co2");
                if (tvoc) dc.TVOC = resultSet.getInt("tvoc");
                if (heart_rate) dc.HeartRate = resultSet.getInt("heart_rate");
                if (spO2) dc.SpO2 = resultSet.getInt("spo2");
                if (temperature) dc.Temperature = resultSet.getInt("temperature");
                if (pressure) dc.Pressure = resultSet.getInt("pressure");
                if (humidity) dc.Humidity = resultSet.getInt("humidity");
                data.add(dc);
                for (int i = 0; i < skip; i++) {
                    resultSet.next();
                }
            }
            return data;
        } catch (SQLException e) {
            System.err.println("Ошибка экспорта данных в CSV-файл: " + e.getMessage());
        }
        return null;
    }

    // Возвращаем список пользователей по куску логина либо ничего не делаем при выборе пользователя из списка
    public static List<String> searchUser(String username) throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_PATH)) {
            String selectUser = """
                SELECT username FROM users WHERE username LIKE ?
            """;
            PreparedStatement preparedStatement = connection.prepareStatement(selectUser);
            preparedStatement.setString(1, "%" + username + "%");
            ResultSet resultSet = preparedStatement.executeQuery();

            List<String> users = new ArrayList<>();
            while (resultSet.next()) {
                users.add(resultSet.getString("username"));
            }
            return users;
        }
    }
}
