package com.example.sadaguessgame.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.Locale;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context base) {
        Context updatedContext = updateBaseContextLocale(base);
        super.attachBaseContext(updatedContext);
    }

    private Context updateBaseContextLocale(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE);
        String langCode = prefs.getString("language", "en");

        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(locale);

        // Set layout direction for RTL languages
        if (langCode.equals("ku") || langCode.equals("kmr")) {
            configuration.setLayoutDirection(locale);
        } else {
            configuration.setLayoutDirection(new Locale("en"));
        }

        return context.createConfigurationContext(configuration);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply dark mode BEFORE super.onCreate()
        applyDarkMode();

        super.onCreate(savedInstanceState);

        // CRITICAL: Force layout direction BEFORE anything else
        forceLayoutDirection();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        // Force again after content view is set
        forceLayoutDirection();
        // Force RTL on all LinearLayouts
        forceLinearLayoutDirection();
    }

    private void forceLayoutDirection() {
        SharedPreferences prefs = getSharedPreferences("settings_prefs", Context.MODE_PRIVATE);
        String langCode = prefs.getString("language", "en");

        int direction = (langCode.equals("ku") || langCode.equals("kmr"))
                ? View.LAYOUT_DIRECTION_RTL
                : View.LAYOUT_DIRECTION_LTR;

        // Force on window
        getWindow().getDecorView().setLayoutDirection(direction);

        // Force on all root views
        if (findViewById(android.R.id.content) != null) {
            findViewById(android.R.id.content).setLayoutDirection(direction);
        }
    }

    // NEW METHOD: Force layout direction on all LinearLayouts
    private void forceLinearLayoutDirection() {
        SharedPreferences prefs = getSharedPreferences("settings_prefs", Context.MODE_PRIVATE);
        String langCode = prefs.getString("language", "en");

        int direction = (langCode.equals("ku") || langCode.equals("kmr"))
                ? View.LAYOUT_DIRECTION_RTL
                : View.LAYOUT_DIRECTION_LTR;

        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            forceLayoutDirectionRecursive(rootView, direction);
        }
    }

    // Recursively set layout direction on all views, especially LinearLayouts
    private void forceLayoutDirectionRecursive(View view, int direction) {
        if (view == null) return;

        view.setLayoutDirection(direction);

        if (view instanceof android.view.ViewGroup) {
            android.view.ViewGroup viewGroup = (android.view.ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                forceLayoutDirectionRecursive(viewGroup.getChildAt(i), direction);
            }
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