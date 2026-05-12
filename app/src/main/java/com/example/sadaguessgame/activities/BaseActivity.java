package com.example.sadaguessgame.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.sadaguessgame.manager.NavigationStateManager;
import com.example.sadaguessgame.manager.ThemeManager;

import java.util.Locale;

/**
 * BaseActivity (updated for Features 3 & 5).
 *
 * Changes vs original:
 *  • onResume  → persists last-visited screen name (Feature 3)
 *  • onCreate  → applies theme status-bar tint (Feature 5)
 */
public class BaseActivity extends AppCompatActivity {

    private static final String PREFS_NAME   = "settings_prefs";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_DARK_MODE= "dark_mode";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(applyLocale(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyDarkMode();
        super.onCreate(savedInstanceState);
        applyThemeStatusBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        forceLayoutDirection();
        // Feature 3 — save the last visited screen so the app can restore it
        NavigationStateManager.getInstance(this)
                .saveLastScreen(getClass().getSimpleName());
    }

    @Override
    protected void onStart() {
        super.onStart();
        forceLayoutDirection();
    }

    // ─── Feature 5: tint status bar with current theme color ─────────────────

    private void applyThemeStatusBar() {
        try {
            ThemeManager tm = ThemeManager.getInstance(this);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(tm.getPrimaryColor());
        } catch (Exception ignored) {
            // Non-critical; some older devices don't support this
        }
    }

    // ─── Locale ───────────────────────────────────────────────────────────────

    private Context applyLocale(Context context) {
        String langCode = getLangCode(context);
        Locale locale   = buildLocale(langCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale);
        config.setLayoutDirection(locale);

        return context.createConfigurationContext(config);
    }

    private Locale buildLocale(String langCode) {
        switch (langCode) {
            case "ku":  return new Locale("ku");
            case "kmr": return new Locale("kmr");
            default:    return new Locale("en");
        }
    }

    private String getLangCode(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_LANGUAGE, "en");
    }

    // ─── Dark Mode ────────────────────────────────────────────────────────────

    protected void applyDarkMode() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isDark = prefs.getBoolean(KEY_DARK_MODE, false);
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    protected boolean isDarkMode() {
        return getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_DARK_MODE, false);
    }

    protected boolean isRtlLanguage() {
        String lang = getLangCode(this);
        return lang.equals("ku") || lang.equals("kmr");
    }

    protected void forceLayoutDirection() {
        View root = findViewById(android.R.id.content);
        if (root == null) return;
        int direction = isRtlLanguage()
                ? View.LAYOUT_DIRECTION_RTL
                : View.LAYOUT_DIRECTION_LTR;
        applyDirectionRecursive(root, direction);
    }

    private void applyDirectionRecursive(View view, int direction) {
        if (view == null) return;
        view.setLayoutDirection(direction);
        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                applyDirectionRecursive(vg.getChildAt(i), direction);
            }
        }
    }
}