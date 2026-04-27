package com.example.sadaguessgame.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.Locale;

/**
 * Wrapper around Android TextToSpeech for the voice clue feature.
 *
 * Lifecycle: call init() in onResume / after user enables TTS.
 *            call shutdown() in onDestroy.
 *
 * The TTS engine speaks the card word in the language currently selected
 * in the app settings (en → English, ku/kmr → best available Kurdish/Arabic
 * fallback, since most devices lack Kurdish TTS).
 */
public class VoiceClueManager {

    private static final String TAG        = "VoiceClueManager";
    private static final String PREFS_NAME = "settings_prefs";
    private static final String KEY_LANG   = "language";
    private static final String KEY_VOICE  = "voice_clue_enabled";

    private static volatile VoiceClueManager instance;

    private final Context appContext;
    private TextToSpeech  tts;
    private boolean       ready = false;

    private VoiceClueManager(@NonNull Context context) {
        appContext = context.getApplicationContext();
    }

    public static VoiceClueManager getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (VoiceClueManager.class) {
                if (instance == null)
                    instance = new VoiceClueManager(context.getApplicationContext());
            }
        }
        return instance;
    }

    // ─── Lifecycle ───────────────────────────────────────────────────────────

    public void init(@Nullable Runnable onReady) {
        if (tts != null) { if (onReady != null) onReady.run(); return; }
        tts = new TextToSpeech(appContext, status -> {
            if (status == TextToSpeech.SUCCESS) {
                applyLocale();
                ready = true;
                Log.d(TAG, "TTS initialized successfully");
                if (onReady != null) onReady.run();
            } else {
                Log.w(TAG, "TTS initialization failed with status: " + status);
                ready = false;
            }
        });
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
            ready = false;
        }
    }

    // ─── Speaking ────────────────────────────────────────────────────────────

    /**
     * Speaks the given word if TTS is ready and voice clue mode is enabled.
     * Falls back silently if TTS is unavailable.
     */
    public void speakWord(@NonNull String word) {
        if (!isEnabled() || !ready || tts == null) return;
        tts.stop();
        tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, "card_word");
    }

    public void stop() {
        if (tts != null && ready) tts.stop();
    }

    // ─── Settings ────────────────────────────────────────────────────────────

    public boolean isEnabled() {
        return getPrefs().getBoolean(KEY_VOICE, false);
    }

    public void setEnabled(boolean enabled) {
        getPrefs().edit().putBoolean(KEY_VOICE, enabled).apply();
        if (enabled && tts == null) init(null);
    }

    // ─── Private ─────────────────────────────────────────────────────────────

    private void applyLocale() {
        if (tts == null) return;
        String langCode = getPrefs().getString(KEY_LANG, "en");
        Locale locale;
        switch (langCode) {
            case "ku":
            case "kmr":
                // Kurdish TTS rarely available; fallback to Arabic for Sorani,
                // Turkish for Badini (both are intelligible approximations).
                locale = tryLocale("ckb") != null ? new Locale("ckb")
                        : tryLocale("ar")  != null ? new Locale("ar")
                        : Locale.ENGLISH;
                break;
            default:
                locale = Locale.ENGLISH;
        }
        int result = tts.setLanguage(locale);
        if (result == TextToSpeech.LANG_MISSING_DATA
                || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts.setLanguage(Locale.ENGLISH);
        }
    }

    @Nullable
    private Locale tryLocale(String tag) {
        try {
            Locale l = Locale.forLanguageTag(tag);
            if (tts == null) return null;
            int r = tts.isLanguageAvailable(l);
            return (r >= TextToSpeech.LANG_AVAILABLE) ? l : null;
        } catch (Exception e) {
            return null;
        }
    }

    private SharedPreferences getPrefs() {
        return appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}