package com.example.sadaguessgame;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatDelegate;

import java.util.Locale;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        applyDarkMode();
        forceLayoutDirection();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(updateBaseContextLocale(base));
    }

    private Context updateBaseContextLocale(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE);
        String langCode = prefs.getString("language", "en");

        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(locale);

        if (langCode.equals("ku") || langCode.equals("kmr")) {
            configuration.setLayoutDirection(locale);
        } else {
            configuration.setLayoutDirection(new Locale("en"));
        }

        return context.createConfigurationContext(configuration);
    }

    private void forceLayoutDirection() {
        SharedPreferences prefs = getSharedPreferences("settings_prefs", Context.MODE_PRIVATE);
        String langCode = prefs.getString("language", "en");

        if (langCode.equals("ku") || langCode.equals("kmr")) {
            // Force RTL for entire app
            getResources().getConfiguration().setLayoutDirection(new Locale(langCode));
        }
    }

    // Apply saved dark mode preference
    private void applyDarkMode() {
        SharedPreferences prefs = getSharedPreferences("settings_prefs", Context.MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);

        AppCompatDelegate.setDefaultNightMode(
                isDarkMode
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}