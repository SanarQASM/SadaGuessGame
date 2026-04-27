package com.example.sadaguessgame.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import com.example.sadaguessgame.R;
import com.example.sadaguessgame.enums.SoundTheme;

/**
 * Centralised audio manager.  All sound playback in the app must go through
 * this singleton so the selected SoundTheme is respected everywhere.
 *
 * Usage:
 *   SoundManager.getInstance(context).playCorrectAnswer();
 *   SoundManager.getInstance(context).playWinner();
 *
 * Sound files needed in res/raw/:
 *   Classic  : countdown_boom, winner_sound, draw_sound        (already exist)
 *   Dabke    : dabke_correct, dabke_winner, dabke_draw          (add these)
 *   All      : correct_ding                                     (add this)
 */
public class SoundManager {

    private static final String PREFS_NAME   = "settings_prefs";
    private static final String KEY_THEME    = "sound_theme";

    private static volatile SoundManager instance;

    private final Context appContext;
    private MediaPlayer   currentPlayer;

    private SoundManager(@NonNull Context context) {
        appContext = context.getApplicationContext();
    }

    public static SoundManager getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (SoundManager.class) {
                if (instance == null)
                    instance = new SoundManager(context.getApplicationContext());
            }
        }
        return instance;
    }

    // ─── Theme ───────────────────────────────────────────────────────────────

    public SoundTheme getCurrentTheme() {
        String key = appContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_THEME, SoundTheme.CLASSIC.getKey());
        return SoundTheme.fromKey(key);
    }

    public void setTheme(@NonNull SoundTheme theme) {
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY_THEME, theme.getKey()).apply();
    }

    // ─── Playback events ─────────────────────────────────────────────────────

    /** Played on every correct answer (score > 0). */
    public void playCorrectAnswer() {
        SoundTheme theme = getCurrentTheme();
        if (theme == SoundTheme.SILENT) return;
        if (theme == SoundTheme.DABKE) {
            play(R.raw.dabke_correct);
        } else {
            play(R.raw.correct_ding);
        }
    }

    /** Played when timer hits the warning threshold. */
    public void playTimerWarning() {
        // Always audible regardless of theme (gameplay safety cue)
        play(R.raw.countdown_boom);
    }

    /** Played on winner screen. */
    public void playWinner() {
        SoundTheme theme = getCurrentTheme();
        if (theme == SoundTheme.SILENT) return;
        if (theme == SoundTheme.DABKE) {
            play(R.raw.dabke_winner);
        } else {
            play(R.raw.winner_sound);
        }
    }

    /** Played on draw screen. */
    public void playDraw() {
        SoundTheme theme = getCurrentTheme();
        if (theme == SoundTheme.SILENT) return;
        if (theme == SoundTheme.DABKE) {
            play(R.raw.dabke_draw);
        } else {
            play(R.raw.draw_sound);
        }
    }

    /** Played when a combo streak reaches threshold (3+). */
    public void playCombo() {
        SoundTheme theme = getCurrentTheme();
        if (theme == SoundTheme.SILENT) return;
        play(R.raw.combo_sound);
    }

    /** Played on sudden death start. */
    public void playSuddenDeath() {
        play(R.raw.sudden_death_sound);
    }

    // ─── Core ────────────────────────────────────────────────────────────────

    public void stopCurrent() {
        if (currentPlayer != null) {
            try {
                if (currentPlayer.isPlaying()) currentPlayer.stop();
                currentPlayer.release();
            } catch (IllegalStateException ignored) {}
            currentPlayer = null;
        }
    }

    private void play(@RawRes int resId) {
        stopCurrent();
        try {
            currentPlayer = MediaPlayer.create(appContext, resId);
            if (currentPlayer != null) {
                currentPlayer.setOnCompletionListener(mp -> {
                    mp.release();
                    if (currentPlayer == mp) currentPlayer = null;
                });
                currentPlayer.start();
            }
        } catch (Exception ignored) {}
    }
}