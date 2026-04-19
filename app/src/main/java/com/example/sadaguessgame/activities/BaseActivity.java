package com.example.sadaguessgame.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import java.util.Locale;

public class BaseActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "settings_prefs";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String LANG_SORANI = "ku";
    private static final String LANG_BADINI = "kmr";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(applyLocale(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyDarkMode();
        super.onCreate(savedInstanceState);
    }

    // ─── Locale ───────────────────────────────────────────────

    private Context applyLocale(Context context) {
        String langCode = getLangCode(context);
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale);

        boolean isRtl = langCode.equals(LANG_SORANI) || langCode.equals(LANG_BADINI);
        config.setLayoutDirection(isRtl ? locale : Locale.ENGLISH);

        return context.createConfigurationContext(config);
    }

    private String getLangCode(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_LANGUAGE, "en");
    }

    // ─── Dark Mode ────────────────────────────────────────────

    protected void applyDarkMode() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isDark = prefs.getBoolean(KEY_DARK_MODE, false);
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    protected boolean isDarkMode() {
        return getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_DARK_MODE, false);
    }

    protected boolean isRtlLanguage() {
        String lang = getLangCode(this);
        return lang.equals(LANG_SORANI) || lang.equals(LANG_BADINI);
    }
}