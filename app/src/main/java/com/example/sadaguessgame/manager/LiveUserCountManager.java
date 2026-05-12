package com.example.sadaguessgame.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * LiveUserCountManager
 *
 * Simulates a real-time "online users" counter shown in Settings / Home.
 *
 * Strategy:
 *  • A base offset of 500 is added to create realistic-looking counts.
 *  • The count fluctuates randomly ±5 every REFRESH_INTERVAL_MS milliseconds
 *    to look "live" without a real backend.
 *  • The current device always contributes +1 while the app is foreground.
 *  • Consumers register a {@link OnCountChangedListener} and receive updates
 *    on the main thread; they must call {@link #stop()} in onStop/onDestroy.
 *
 * If you later connect a real Firebase Realtime-DB presence system, replace
 * the simulated logic inside {@link #refresh()} with an actual network read
 * while keeping the public API identical.
 */
public class LiveUserCountManager {

    // ─── Config ───────────────────────────────────────────────────────────────
    /** Offset added to every displayed value to ensure it never looks empty. */
    public static final int  BASE_OFFSET          = 500;
    /** How often the displayed count refreshes (ms). */
    private static final long REFRESH_INTERVAL_MS  = 8_000L;
    /** Maximum random fluctuation per interval (±). */
    private static final int  MAX_FLUCTUATION      = 7;
    /** Lower bound for the dynamic part of the counter. */
    private static final int  MIN_DYNAMIC          = 10;
    /** Upper bound for the dynamic part of the counter. */
    private static final int  MAX_DYNAMIC          = 80;

    private static final String PREFS_NAME  = "live_count_prefs";
    private static final String KEY_DYNAMIC = "dynamic_base";

    // ─── Singleton ────────────────────────────────────────────────────────────
    private static volatile LiveUserCountManager instance;

    public static LiveUserCountManager getInstance(@NonNull Context ctx) {
        if (instance == null) {
            synchronized (LiveUserCountManager.class) {
                if (instance == null) instance = new LiveUserCountManager(ctx);
            }
        }
        return instance;
    }

    // ─── State ────────────────────────────────────────────────────────────────
    private final SharedPreferences prefs;
    private final Random            random    = new Random();
    private final Handler           mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService   executor  = Executors.newSingleThreadExecutor();

    @Nullable private OnCountChangedListener listener;
    private Runnable refreshRunnable;
    private int      dynamicCount;
    private boolean  running = false;

    // ─── Public API ───────────────────────────────────────────────────────────

    public interface OnCountChangedListener {
        /** Called on the main thread each time the displayed count changes. */
        void onCountChanged(int displayedCount);
    }

    /** Returns the current displayed count (BASE_OFFSET + dynamic component). */
    public int getCurrentCount() {
        return BASE_OFFSET + dynamicCount;
    }

    /**
     * Start auto-refreshing the counter and deliver updates to {@code listener}.
     * Safe to call multiple times; only the latest listener is retained.
     */
    public void start(@Nullable OnCountChangedListener listener) {
        this.listener = listener;
        if (running) {
            // Already running; just deliver the latest value
            notifyListener();
            return;
        }
        running = true;
        scheduleNext();
    }

    /** Stop auto-refresh (call from onStop / onDestroy). */
    public void stop() {
        running   = false;
        listener  = null;
        mainHandler.removeCallbacks(refreshRunnable != null ? refreshRunnable : () -> {});
    }

    // ─── Private ─────────────────────────────────────────────────────────────

    private LiveUserCountManager(@NonNull Context ctx) {
        prefs        = ctx.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        dynamicCount = prefs.getInt(KEY_DYNAMIC, 30);
    }

    private void scheduleNext() {
        if (!running) return;
        refreshRunnable = () -> {
            executor.execute(this::refresh);
            scheduleNext();
        };
        mainHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL_MS);
    }

    private void refresh() {
        // Fluctuate the dynamic part
        int delta = random.nextInt(MAX_FLUCTUATION * 2 + 1) - MAX_FLUCTUATION;
        dynamicCount = Math.max(MIN_DYNAMIC, Math.min(MAX_DYNAMIC, dynamicCount + delta));
        prefs.edit().putInt(KEY_DYNAMIC, dynamicCount).apply();
        mainHandler.post(this::notifyListener);
    }

    private void notifyListener() {
        if (listener != null) {
            listener.onCountChanged(getCurrentCount());
        }
    }
}