package com.example.sadaguessgame;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.sadaguessgame.manager.AdsManager;
import com.example.sadaguessgame.manager.AppNotificationManager;
import com.example.sadaguessgame.manager.LiveUserCountManager;
import com.example.sadaguessgame.manager.NavigationStateManager;
import com.example.sadaguessgame.manager.ThemeManager;

import java.util.Locale;

public class MyApplication extends Application {

    private static final String PREFS_NAME = "settings_prefs";
    private static final String KEY_LANG   = "language";
    private static final String KEY_DARK   = "dark_mode";

    @Override
    public void onCreate() {
        super.onCreate();
        applyDarkMode();

        // ── Feature 4: Ads ─────────────────────────────────────────────────
        AdsManager.getInstance(this).initialize();

        // ── Feature 6: Notification channels ──────────────────────────────
        AppNotificationManager.getInstance(this); // creates channels on first call

        // ── Feature 2 & 3: Live count + nav-state warm-up (no-ops until used) ─
        LiveUserCountManager.getInstance(this);
        NavigationStateManager.getInstance(this);
        AdsManager.getInstance(this).initialize();
        // ── Feature 5: Theme warm-up ───────────────────────────────────────
        ThemeManager.getInstance(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(updateBaseContextLocale(base));
    }

    private Context updateBaseContextLocale(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String langCode = prefs.getString(KEY_LANG, "en");

        Locale locale = buildLocale(langCode);
        Locale.setDefault(locale);

        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);

        return context.createConfigurationContext(configuration);
    }

    private Locale buildLocale(String langCode) {
        switch (langCode) {
            case "ku":  return new Locale("ku");
            case "kmr": return new Locale("kmr");
            default:    return new Locale("en");
        }
    }

    private void applyDarkMode() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean(KEY_DARK, false);
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}