package com.example.javafx;

import javafx.scene.chart.XYChart;
import org.springframework.javapoet.JavaFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class DBController {
    public static void createDatabaseInAppData() throws IOException {
        File appData = new File(System.getenv("APPDATA") + "\\HealthMonitor");
        System.out.println(appData);
        if (!appData.exists()) {
            appData.mkdir();
        }
        File database = new File(appData + "\\database.sqlite");
        if (!database.exists()) {
            InputStream in = DBController.class.getResourceAsStream("/database.sqlite");
            if (in != null) {
                byte[] buffer = new byte[in.available()];
                in.read(buffer);
                File db = new File(appData + "\\database.sqlite");
                db.createNewFile();
                java.io.FileOutputStream fos = new java.io.FileOutputStream(db);
                fos.write(buffer);
                fos.close();
            }
        }
    }
    private static final String DB_PATH = "jdbc:sqlite:" + System.getenv("APPDATA") + "\\HealthMonitor\\database.sqlite";
    private static int userId = 0;

    // Инициализация
    static {
        try {
            createDatabaseInAppData();
            Class.forName("org.sqlite.JDBC");
            System.out.println("Драйвер JDBC SQLite загружен.");
            createDatabase();
        } catch (ClassNotFoundException | IOException e ) {
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
                        humidity_coefficient INTEGER NOT NULL DEFAULT 1000,
                        heart_rate_threshold INTEGER NOT NULL DEFAULT 170,
                        spo2_threshold INTEGER NOT NULL DEFAULT 85
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
                        sex VARCHAR(255),
                        phone_number VARCHAR(255),
                        height VARCHAR(255)
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

    public static boolean saveUserProfile(String firstName, String lastName, String phoneNumber, LocalDate dateOfBirth, String weight, String groupChoice, String sexChoice, String height, String language) {
        int userId = getCurrentUserId();
        if (userId == 0) {
            System.out.println("User not authorized.");
            return false;
        }

        try (Connection connection = DriverManager.getConnection(DB_PATH)) {
            String updateProfile = """
            INSERT INTO user_data (id, name, surname, birth_date, weight, group_id, sex, phone_number, height)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                name = excluded.name,
                surname = excluded.surname,
                birth_date = excluded.birth_date,
                weight = excluded.weight,
                group_id = excluded.group_id,
                sex = excluded.sex,
                phone_number = excluded.phone_number,
                height = excluded.height
        """;
            PreparedStatement preparedStatement = connection.prepareStatement(updateProfile);
            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, firstName);
            preparedStatement.setString(3, lastName);
            preparedStatement.setDate(4, java.sql.Date.valueOf(dateOfBirth));
            preparedStatement.setInt(5, Integer.parseInt(weight));
            preparedStatement.setInt(6, getGroupId(groupChoice, language)); // Assuming you have a method to get group ID from group name
            preparedStatement.setString(7, sexChoice.toLowerCase());
            preparedStatement.setString(8, phoneNumber);
            preparedStatement.setString(9, height);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error saving user profile: " + e.getMessage());
            return false;
        }
    }

    public static UserProfile getUserProfile(int userId) {
        UserProfile userProfile = null;
        try (Connection connection = DriverManager.getConnection(DB_PATH)) {
            String selectProfile = """
            SELECT name, surname, phone_number, birth_date, weight, group_id, sex, height
            FROM user_data
            WHERE id = ?
        """;
            PreparedStatement preparedStatement = connection.prepareStatement(selectProfile);
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                LocalDate birthDate = null;
                Date birthDateSql = resultSet.getDate("birth_date");
                if (birthDateSql != null) {
                    birthDate = birthDateSql.toLocalDate();
                }

                userProfile = new UserProfile(
                        resultSet.getString("name"),
                        resultSet.getString("surname"),
                        resultSet.getString("phone_number"),
                        birthDate,
                        resultSet.getString("weight"),
                        resultSet.getInt("group_id"),
                        resultSet.getString("sex"),
                        resultSet.getString("height")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving user profile: " + e.getMessage());
        }
        return userProfile;
    }

    // Class to hold user profile information
    public static class UserProfile {
        private String name;
        private String surname;
        private String phoneNumber;
        private LocalDate birthDate;
        private String weight;
        private int groupId;
        private String sex;
        private String height;

        public UserProfile(String name, String surname, String phoneNumber, LocalDate birthDate, String weight, int groupId, String sex, String height) {
            this.name = name;
            this.surname = surname;
            this.phoneNumber = phoneNumber;
            this.birthDate = birthDate;
            this.weight = weight;
            this.groupId = groupId;
            this.sex = sex;
            this.height = height;
        }

        // Getters
        public String getName() { return name; }
        public String getSurname() { return surname; }
        public String getPhoneNumber() { return phoneNumber; }
        public LocalDate getBirthDate() { return birthDate; }
        public String getWeight() { return weight; }
        public int getGroupId() { return groupId; }
        public String getSex() { return sex; }
        public String getHeight() { return height; }
    }

    public static boolean createGroup(String enName, String ruName, String kzName) {
        try (Connection connection = DriverManager.getConnection(DB_PATH)) {
            String insertGroup = """
            INSERT INTO groups (en_name, ru_name, kz_name)
            VALUES (?, ?, ?)
        """;
            PreparedStatement preparedStatement = connection.prepareStatement(insertGroup);
            preparedStatement.setString(1, enName);
            preparedStatement.setString(2, ruName);
            preparedStatement.setString(3, kzName);
            preparedStatement.executeUpdate();
            System.out.println("Group created successfully.");
            return true;
        } catch (SQLException e) {
            System.err.println("Error creating group: " + e.getMessage());
            return false;
        }
    }

    private static int getGroupId(String groupChoice, String language) {
        try (Connection connection = DriverManager.getConnection(DB_PATH)) {
            String selectGroup = null;

            // Determine the column to search based on the language
            switch (language) {
                case "en":
                    selectGroup = "SELECT id FROM groups WHERE en_name = ?";
                    break;
                case "ru":
                    selectGroup = "SELECT id FROM groups WHERE ru_name = ?";
                    break;
                case "kz":
                    selectGroup = "SELECT id FROM groups WHERE kz_name = ?";
                    break;
                default:
                    System.err.println("Unsupported language: " + language);
                    return 0;
            }

            // Prepare and execute the query
            PreparedStatement preparedStatement = connection.prepareStatement(selectGroup);
            preparedStatement.setString(1, groupChoice);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("id");
            }
        } catch (SQLException e) {
            System.err.println("Error getting group ID: " + e.getMessage());
        }
        return 0; // Return a default value or handle error case
    }

    public static List<String> getAllGroups(String language) {
        List<String> groups = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DB_PATH)) {
            String selectGroups = null;

            // Determine the column to select based on the language
            switch (language) {
                case "en":
                    selectGroups = "SELECT en_name FROM groups";
                    break;
                case "ru":
                    selectGroups = "SELECT ru_name FROM groups";
                    break;
                case "kz":
                    selectGroups = "SELECT kz_name FROM groups";
                    break;
                default:
                    System.err.println("Unsupported language: " + language);
                    return groups;
            }

            // Prepare and execute the query
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(selectGroups);

            while (resultSet.next()) {
                groups.add(resultSet.getString(1));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving groups: " + e.getMessage());
        }
        return groups;
    }

    public static boolean isUserProfileCreated(int userId) {
        try (Connection connection = DriverManager.getConnection(DB_PATH)) {
            String selectProfile = """
            SELECT COUNT(*) FROM user_data WHERE id = ?
        """;
            PreparedStatement preparedStatement = connection.prepareStatement(selectProfile);
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking if user profile is created: " + e.getMessage());
        }
        return false;
    }

    // Получение id текущего пользователя
    public static int getCurrentUserId() {
        return userId;
    }

    // Изменение конфигурации датчиков
    public static void updateSensorsConfig(int userId) {
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
                    temperature_coefficient = ?, pressure_coefficient = ?, humidity_coefficient = ?,
                    heart_rate_threshold = ?, spo2_threshold = ?
                WHERE id = ?
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
            preparedStatement.setInt(12, SensorsConfig.heart_rate_threshold);
            preparedStatement.setInt(13, SensorsConfig.spo2_threshold);
            preparedStatement.setInt(14, userId);
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
        static int heart_rate_threshold = 170;
        static int spo2_threshold = 85;
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
                SensorsConfig.heart_rate_threshold = resultSet.getInt("heart_rate_threshold");
                SensorsConfig.spo2_threshold = resultSet.getInt("spo2_threshold");
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
                SELECT username FROM users WHERE username LIKE ? AND user_type = 'user'
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

    public static String getGroupNameById(int groupId, String language) {
        try (Connection connection = DriverManager.getConnection(DB_PATH)) {
            String selectGroup = null;

            switch (language) {
                case "en":
                    selectGroup = "SELECT en_name FROM groups WHERE id = ?";
                    break;
                case "ru":
                    selectGroup = "SELECT ru_name FROM groups WHERE id = ?";
                    break;
                case "kz":
                    selectGroup = "SELECT kz_name FROM groups WHERE id = ?";
                    break;
                default:
                    return null;
            }

            PreparedStatement preparedStatement = connection.prepareStatement(selectGroup);
            preparedStatement.setInt(1, groupId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting group name: " + e.getMessage());
        }
        return null;
    }
}
