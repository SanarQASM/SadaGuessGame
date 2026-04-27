package com.example.sadaguessgame.data;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Singleton that persists the local leaderboard in SharedPreferences.
 *
 * All public methods are synchronized — safe to call from any thread.
 */
public class LeaderboardStorage {

    private static final String PREFS_NAME = "leaderboard_prefs";
    private static final String KEY_ENTRIES = "entries";

    private static volatile LeaderboardStorage instance;

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    private LeaderboardStorage(@NonNull Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static LeaderboardStorage getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (LeaderboardStorage.class) {
                if (instance == null) instance = new LeaderboardStorage(context);
            }
        }
        return instance;
    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    @NonNull
    public synchronized List<LeaderboardEntry> getAllEntries() {
        String json = prefs.getString(KEY_ENTRIES, null);
        if (json == null) return new ArrayList<>();
        try {
            Type type = new TypeToken<List<LeaderboardEntry>>() {}.getType();
            List<LeaderboardEntry> entries = gson.fromJson(json, type);
            return entries != null ? entries : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Nullable
    public synchronized LeaderboardEntry getEntry(@NonNull String groupName) {
        for (LeaderboardEntry e : getAllEntries()) {
            if (e.groupName.equalsIgnoreCase(groupName)) return e;
        }
        return null;
    }

    /** Returns the top N entries sorted by win score descending. */
    @NonNull
    public synchronized List<LeaderboardEntry> getTopEntries(int limit) {
        List<LeaderboardEntry> all = getAllEntries();
        Collections.sort(all, (a, b) -> Integer.compare(b.getSortScore(), a.getSortScore()));
        return all.size() <= limit ? all : all.subList(0, limit);
    }

    // ─── Write ────────────────────────────────────────────────────────────────

    /**
     * Records a finished game result into both group entries.
     * Creates entries on first encounter.
     */
    public synchronized void recordGameResult(@NonNull GameState game) {
        if (game.groupAName == null || game.groupBName == null) return;

        List<LeaderboardEntry> entries = getAllEntries();
        LeaderboardEntry entryA = findOrCreate(entries, game.groupAName);
        LeaderboardEntry entryB = findOrCreate(entries, game.groupBName);

        int scoreA = game.getTotalScoreA();
        int scoreB = game.getTotalScoreB();

        if (scoreA > scoreB) {
            entryA.recordWin(scoreA);
            entryB.recordLoss(scoreB);
        } else if (scoreB > scoreA) {
            entryB.recordWin(scoreB);
            entryA.recordLoss(scoreA);
        } else {
            entryA.recordDraw(scoreA);
            entryB.recordDraw(scoreB);
        }

        // Upsert both
        upsert(entries, entryA);
        upsert(entries, entryB);
        persist(entries);
    }

    public synchronized void clearAll() {
        prefs.edit().remove(KEY_ENTRIES).apply();
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private LeaderboardEntry findOrCreate(List<LeaderboardEntry> list, String name) {
        for (LeaderboardEntry e : list) {
            if (e.groupName.equalsIgnoreCase(name)) return e;
        }
        return new LeaderboardEntry(name);
    }

    private void upsert(List<LeaderboardEntry> list, LeaderboardEntry entry) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).groupName.equalsIgnoreCase(entry.groupName)) {
                list.set(i, entry);
                return;
            }
        }
        list.add(entry);
    }

    private void persist(List<LeaderboardEntry> entries) {
        prefs.edit().putString(KEY_ENTRIES, gson.toJson(entries)).apply();
    }
}