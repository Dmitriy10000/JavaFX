package com.example.javafx;
import java.util.Locale;
import java.util.ResourceBundle;
public class LanguageController {
    private static ResourceBundle bundle;
    private static String currentLanguage = "en";

    public static void setLanguage(String languageCode) {
        currentLanguage = languageCode;
        Locale locale = new Locale(languageCode);
        bundle = ResourceBundle.getBundle("com.example.javafx.messages", locale);
    }

    public static String getLanguage() {
        return currentLanguage;
    }

    public static String getString(String key) {
        return bundle.getString(key);
    }
}
