package com.example.sadaguessgame.manager;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * NavigationStateManager — Feature 3: Last-screen persistence.
 *
 * Saves the user's last-visited tab (fragment) index so that when the app
 * is re-opened after being killed / backgrounded the correct tab is
 * re-selected automatically.
 *
 * Also persists the last active Activity class name so deep screens (e.g.
 * a game in progress) can present a "Continue where you left off" prompt.
 *
 * Usage in MainActivity:
 *   onCreate → int tab = NavigationStateManager.getInstance(ctx).getLastTab();
 *   selectTab(tab);
 *
 *   onTabSelected → NavigationStateManager.getInstance(ctx).saveLastTab(index);
 */
public class NavigationStateManager {

    private static final String PREFS_NAME       = "nav_state_prefs";
    private static final String KEY_LAST_TAB     = "last_tab";
    private static final String KEY_LAST_SCREEN  = "last_screen";
    private static final String KEY_LAST_TS      = "last_timestamp";

    /** Maximum age (ms) after which we no longer restore the last screen. */
    private static final long MAX_RESTORE_AGE_MS = 30L * 60L * 1000L; // 30 min

    private static volatile NavigationStateManager instance;

    public static NavigationStateManager getInstance(@NonNull Context ctx) {
        if (instance == null) {
            synchronized (NavigationStateManager.class) {
                if (instance == null)
                    instance = new NavigationStateManager(ctx.getApplicationContext());
            }
        }
        return instance;
    }

    private final SharedPreferences prefs;

    private NavigationStateManager(@NonNull Context ctx) {
        prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ─── Tab index ────────────────────────────────────────────────────────────

    /** Persists the selected bottom-nav tab index (0-based). */
    public void saveLastTab(int tabIndex) {
        prefs.edit()
                .putInt(KEY_LAST_TAB, tabIndex)
                .putLong(KEY_LAST_TS, System.currentTimeMillis())
                .apply();
    }

    /**
     * Returns the tab index to restore, or 0 (Home) if the session is too old
     * or never saved.
     */
    public int getLastTab() {
        long savedAt = prefs.getLong(KEY_LAST_TS, 0L);
        if (System.currentTimeMillis() - savedAt > MAX_RESTORE_AGE_MS) {
            return 0; // Too old — go to Home
        }
        return prefs.getInt(KEY_LAST_TAB, 0);
    }

    // ─── Last screen (Activity class name) ───────────────────────────────────

    /**
     * Saves the simple class name of the currently active Activity.
     * Call this from BaseActivity.onResume().
     */
    public void saveLastScreen(@NonNull String activitySimpleName) {
        prefs.edit()
                .putString(KEY_LAST_SCREEN, activitySimpleName)
                .putLong(KEY_LAST_TS, System.currentTimeMillis())
                .apply();
    }

    /**
     * Returns the last saved Activity simple class name, or null if none / expired.
     * Use this to show a "Continue" prompt on re-launch.
     */
    @Nullable
    public String getLastScreen() {
        long savedAt = prefs.getLong(KEY_LAST_TS, 0L);
        if (System.currentTimeMillis() - savedAt > MAX_RESTORE_AGE_MS) return null;
        return prefs.getString(KEY_LAST_SCREEN, null);
    }

    /** Clears all saved navigation state (e.g. after logout or explicit reset). */
    public void clear() {
        prefs.edit().clear().apply();
    }
}