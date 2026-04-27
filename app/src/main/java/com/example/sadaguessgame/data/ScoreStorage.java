package com.example.sadaguessgame.data;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton that persists GameState in SharedPreferences.
 * Updated v2: uses GameState.ensureNonNullLists() after every deserialization,
 * and automatically records finished games into LeaderboardStorage.
 */
public class ScoreStorage {

    private static final String PREFS_NAME       = "game_prefs";
    private static final String KEY_ALL_GAMES    = "all_games";
    private static final String KEY_CURRENT_GAME = "current_game";

    private static volatile ScoreStorage instance;

    private final SharedPreferences prefs;
    private final Gson              gson;
    private final Context           appContext;

    private ScoreStorage(Context context) {
        appContext = context.getApplicationContext();
        prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson  = new Gson();
    }

    public static ScoreStorage getInstance(Context context) {
        if (instance == null) {
            synchronized (ScoreStorage.class) {
                if (instance == null)
                    instance = new ScoreStorage(context.getApplicationContext());
            }
        }
        return instance;
    }

    // ─── Current game ────────────────────────────────────────────────────────

    public synchronized void saveCurrentGame(GameState game) {
        if (game == null) return;
        game.ensureNonNullLists();
        prefs.edit().putString(KEY_CURRENT_GAME, gson.toJson(game)).apply();
    }

    public synchronized GameState getCurrentGame() {
        String json = prefs.getString(KEY_CURRENT_GAME, null);
        if (json == null) return null;
        try {
            GameState game = gson.fromJson(json, GameState.class);
            if (game == null) return null;
            game.ensureNonNullLists();
            return game;
        } catch (Exception e) {
            return null;
        }
    }

    // ─── Finished games ──────────────────────────────────────────────────────

    /**
     * Marks game finished, moves it to the history list, clears current game,
     * and records the result in the leaderboard.
     */
    public synchronized void saveFinishedGame(GameState game) {
        if (game == null) return;
        game.ensureNonNullLists();
        game.isFinished = true;

        // Persist to history
        List<GameState> all = getAllGames();
        all.add(game);
        saveAllGames(all);

        // Clear current slot
        prefs.edit().remove(KEY_CURRENT_GAME).apply();

        // Update leaderboard
        LeaderboardStorage.getInstance(appContext).recordGameResult(game);
    }

    public synchronized List<GameState> getAllGames() {
        String json = prefs.getString(KEY_ALL_GAMES, null);
        if (json == null) return new ArrayList<>();
        try {
            Type type = new TypeToken<List<GameState>>() {}.getType();
            List<GameState> games = gson.fromJson(json, type);
            if (games == null) return new ArrayList<>();
            for (GameState g : games) g.ensureNonNullLists();
            return games;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public synchronized boolean deleteGame(int index) {
        List<GameState> games = getAllGames();
        if (index < 0 || index >= games.size()) return false;
        games.remove(index);
        saveAllGames(games);
        return true;
    }

    public synchronized boolean deleteAllGames() {
        prefs.edit().remove(KEY_ALL_GAMES).apply();
        return true;
    }

    public synchronized void clearCurrentGame() {
        prefs.edit().remove(KEY_CURRENT_GAME).apply();
    }

    public GameState getLastUnfinishedGame() {
        GameState current = getCurrentGame();
        return (current != null && !current.isFinished) ? current : null;
    }

    // ─── Private ─────────────────────────────────────────────────────────────

    private void saveAllGames(List<GameState> games) {
        prefs.edit().putString(KEY_ALL_GAMES, gson.toJson(games)).apply();
    }
}