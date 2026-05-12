package com.example.sadaguessgame.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.example.sadaguessgame.enums.AppTheme;

/**
 * ThemeManager — Feature 5: Runtime color-theme switching.
 *
 * Android's compile-time theme system (styles.xml) cannot switch palettes
 * at runtime without restarting the process.  Instead of requiring a restart
 * we store per-theme colour tokens here and provide them to whichever
 * component asks.  Colours are intentionally NOT hard-coded; they derive
 * from the enum so adding a theme means only touching this class.
 *
 * Integration points:
 *  • BaseActivity.onCreate() → ThemeManager.getInstance(ctx).applyTheme(this)
 *  • SettingFragment → reads / writes the preference via setTheme()
 *  • Any view that needs the accent colour calls getPrimaryButtonColor()
 *
 * Because we override Window.setStatusBarColor and apply tints programmatically
 * the XML drawables (@color/primary_text etc.) remain as-is; only the tinted
 * views notice the change.  For a full restyle (backgrounds included) call
 * Activity.recreate() after setTheme() — the activity will then re-read the
 * stored theme.
 */
public class ThemeManager {

    private static final String PREFS_NAME  = "settings_prefs";
    private static final String KEY_THEME   = "app_theme";

    private static volatile ThemeManager instance;

    public static ThemeManager getInstance(@NonNull Context ctx) {
        if (instance == null) {
            synchronized (ThemeManager.class) {
                if (instance == null)
                    instance = new ThemeManager(ctx.getApplicationContext());
            }
        }
        return instance;
    }

    private final SharedPreferences prefs;

    private ThemeManager(@NonNull Context ctx) {
        prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ─── Preference ───────────────────────────────────────────────────────────

    @NonNull
    public AppTheme getCurrentTheme() {
        return AppTheme.fromKey(prefs.getString(KEY_THEME, AppTheme.OCEAN.getKey()));
    }

    /** Persist a new theme and return true if the value actually changed. */
    public boolean setTheme(@NonNull AppTheme theme) {
        if (getCurrentTheme() == theme) return false;
        prefs.edit().putString(KEY_THEME, theme.getKey()).apply();
        return true;
    }

    // ─── Color tokens per theme ───────────────────────────────────────────────

    /** Primary brand / background color (used for deep backgrounds). */
    @ColorInt
    public int getPrimaryColor() {
        switch (getCurrentTheme()) {
            case SUNSET:   return Color.parseColor("#1A0A00");
            case FOREST:   return Color.parseColor("#0A1A08");
            case GALAXY:   return Color.parseColor("#100520");
            case ROSE:     return Color.parseColor("#1A0810");
            case MIDNIGHT: return Color.parseColor("#08080F");
            default:       return Color.parseColor("#0F2137"); // OCEAN
        }
    }

    /** Secondary surface color (cards, bottom sheets). */
    @ColorInt
    public int getSecondaryColor() {
        switch (getCurrentTheme()) {
            case SUNSET:   return Color.parseColor("#2D1100");
            case FOREST:   return Color.parseColor("#102B0D");
            case GALAXY:   return Color.parseColor("#1A0A30");
            case ROSE:     return Color.parseColor("#2B1020");
            case MIDNIGHT: return Color.parseColor("#111118");
            default:       return Color.parseColor("#162D4A"); // OCEAN
        }
    }

    /** Accent / button / interactive element color. */
    @ColorInt
    public int getPrimaryButtonColor() {
        switch (getCurrentTheme()) {
            case SUNSET:   return Color.parseColor("#FF9E57");  // warm orange
            case FOREST:   return Color.parseColor("#6DD08C");  // leaf green
            case GALAXY:   return Color.parseColor("#C5B8E8");  // soft purple
            case ROSE:     return Color.parseColor("#F48FB1");  // rose pink
            case MIDNIGHT: return Color.parseColor("#90CAF9");  // cool blue
            default:       return Color.parseColor("#9CDFEA");  // OCEAN sky
        }
    }

    /** Primary text color (high contrast on dark background). */
    @ColorInt
    public int getPrimaryTextColor() {
        switch (getCurrentTheme()) {
            case SUNSET:   return Color.parseColor("#FFE0B2");
            case FOREST:   return Color.parseColor("#C8E6C9");
            case GALAXY:   return Color.parseColor("#EDE7F6");
            case ROSE:     return Color.parseColor("#FCE4EC");
            case MIDNIGHT: return Color.parseColor("#E3F2FD");
            default:       return Color.parseColor("#9CDFEA");  // OCEAN
        }
    }

    /** Error color appropriate for the current theme. */
    @ColorInt
    public int getErrorColor() {
        switch (getCurrentTheme()) {
            case SUNSET:   return Color.parseColor("#FF6E40");
            case FOREST:   return Color.parseColor("#EF9A9A");
            case GALAXY:   return Color.parseColor("#CF6679");
            case ROSE:     return Color.parseColor("#D32F2F");
            case MIDNIGHT: return Color.parseColor("#EF5350");
            default:       return Color.parseColor("#CF6679");
        }
    }

    /**
     * Convenience: ColorStateList wrapping {@link #getPrimaryButtonColor()}.
     * Useful for MaterialButton.setBackgroundTintList().
     */
    @NonNull
    public ColorStateList getPrimaryButtonColorStateList() {
        return ColorStateList.valueOf(getPrimaryButtonColor());
    }

    // ─── Display info ─────────────────────────────────────────────────────────

    /** Localised display name string-resource suffix (theme_ocean, theme_sunset …). */
    @NonNull
    public String getDisplayNameKey() {
        return "theme_" + getCurrentTheme().getKey();
    }
}