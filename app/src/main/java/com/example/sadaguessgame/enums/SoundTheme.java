package com.example.sadaguessgame.enums;

/**
 * Sound theme selection stored in SharedPreferences under key "sound_theme".
 *
 * CLASSIC  → existing sounds (countdown_boom, winner_sound, draw_sound)
 * DABKE    → Kurdish dabke drum loop on correct answer + custom winner jingle
 * SILENT   → all game-event sounds muted (timer warnings still audible)
 */
public enum SoundTheme {

    CLASSIC("classic"),
    DABKE("dabke"),
    SILENT("silent");

    private final String key;

    SoundTheme(String key) { this.key = key; }

    public String getKey() { return key; }

    public static SoundTheme fromKey(String key) {
        if (key == null) return CLASSIC;
        switch (key) {
            case "dabke":  return DABKE;
            case "silent": return SILENT;
            default:       return CLASSIC;
        }
    }
}