package com.example.sadaguessgame.enums;

/**
 * AppTheme — Feature 5: Available visual themes.
 *
 * Each theme maps to a set of color overrides stored in
 * res/values/themes_<key>.xml  (or handled programmatically via
 * ThemeManager).  The "key" value is persisted in SharedPreferences.
 *
 * To add a new theme:
 *  1. Add an entry here.
 *  2. Create matching color resources in ThemeManager.applyTheme().
 *  3. Add the display string in strings.xml:  theme_<key>
 */
public enum AppTheme {

    OCEAN("ocean"),          // deep blue / teal  (default)
    SUNSET("sunset"),        // warm orange / red
    FOREST("forest"),        // green / dark wood
    GALAXY("galaxy"),        // purple / deep space
    ROSE("rose"),            // pink / warm white
    MIDNIGHT("midnight");    // near-black / cool grey

    private final String key;

    AppTheme(String key) { this.key = key; }

    public String getKey() { return key; }

    public static AppTheme fromKey(String key) {
        if (key == null) return OCEAN;
        for (AppTheme t : values()) {
            if (t.key.equalsIgnoreCase(key)) return t;
        }
        return OCEAN;
    }
}