package com.example.sadaguessgame.manager;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import androidx.annotation.NonNull;

/**
 * Thin wrapper around Android Vibrator / VibratorManager.
 * Handles API level differences (< 26, 26-30, 31+) automatically.
 *
 * Patterns:
 *   tick()     → single short 30ms pulse (every second in last 10s)
 *   warning()  → double pulse (timer warning threshold)
 *   success()  → long positive buzz (correct answer / winner)
 */
public class HapticManager {

    private static volatile HapticManager instance;
    private final Vibrator vibrator;

    private HapticManager(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vm = (VibratorManager)
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = (vm != null) ? vm.getDefaultVibrator() : null;
        } else {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }
    }

    public static HapticManager getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (HapticManager.class) {
                if (instance == null)
                    instance = new HapticManager(context.getApplicationContext());
            }
        }
        return instance;
    }

    // ─── Public API ──────────────────────────────────────────────────────────

    /** Single tick – used once per second in the last 10 seconds. */
    public void tick() {
        vibrate(30);
    }

    /** Double pulse – played at warning threshold. */
    public void warning() {
        if (vibrator == null || !vibrator.hasVibrator()) return;
        long[] pattern = {0, 60, 80, 60};
        vibratePattern(pattern);
    }

    /** Long positive buzz – correct answer or win. */
    public void success() {
        vibrate(120);
    }

    /** Stop any ongoing vibration. */
    public void cancel() {
        if (vibrator != null) vibrator.cancel();
    }

    // ─── Private ─────────────────────────────────────────────────────────────

    private void vibrate(long ms) {
        if (vibrator == null || !vibrator.hasVibrator()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(ms,
                    VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //noinspection deprecation
            vibrator.vibrate(ms);
        }
    }

    private void vibratePattern(long[] pattern) {
        if (vibrator == null || !vibrator.hasVibrator()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
        } else {
            //noinspection deprecation
            vibrator.vibrate(pattern, -1);
        }
    }
}