package com.example.sadaguessgame.data;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ScoreStorage {
    private static final String PREFS_NAME = "game_prefs";
    private static final String KEY_ALL_GAMES = "all_games";
    private static final String KEY_CURRENT_GAME = "current_game";

    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    private static volatile ScoreStorage instance;

    private ScoreStorage(Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static ScoreStorage getInstance(Context context) {
        if (instance == null) {
            synchronized (ScoreStorage.class) {
                if (instance == null) {
                    instance = new ScoreStorage(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public synchronized void saveCurrentGame(GameState gameState) {
        if (gameState == null) return;
        // Ensure non-null lists
        if (gameState.scoresA == null) gameState.scoresA = new ArrayList<>();
        if (gameState.scoresB == null) gameState.scoresB = new ArrayList<>();
        if (gameState.categories == null) gameState.categories = new ArrayList<>();
        if (gameState.usedCardPaths == null) gameState.usedCardPaths = new ArrayList<>();
        sharedPreferences.edit()
                .putString(KEY_CURRENT_GAME, gson.toJson(gameState))
                .apply();
    }

    public synchronized GameState getCurrentGame() {
        String gameJson = sharedPreferences.getString(KEY_CURRENT_GAME, null);
        if (gameJson == null) return null;
        try {
            GameState game = gson.fromJson(gameJson, GameState.class);
            if (game == null) return null;
            // Ensure non-null lists after deserialization
            if (game.scoresA == null) game.scoresA = new ArrayList<>();
            if (game.scoresB == null) game.scoresB = new ArrayList<>();
            if (game.categories == null) game.categories = new ArrayList<>();
            if (game.usedCardPaths == null) game.usedCardPaths = new ArrayList<>();
            return game;
        } catch (Exception e) {
            return null;
        }
    }

    public synchronized void saveFinishedGame(GameState gameState) {
        if (gameState == null) return;
        gameState.isFinished = true;
        List<GameState> allGames = getAllGames();
        allGames.add(gameState);
        saveAllGames(allGames);
        // Clear current game
        sharedPreferences.edit().remove(KEY_CURRENT_GAME).apply();
    }

    public synchronized List<GameState> getAllGames() {
        String gamesJson = sharedPreferences.getString(KEY_ALL_GAMES, null);
        if (gamesJson == null) return new ArrayList<>();
        try {
            Type type = new TypeToken<List<GameState>>() {}.getType();
            List<GameState> games = gson.fromJson(gamesJson, type);
            if (games == null) return new ArrayList<>();
            // Ensure non-null lists in each game
            for (GameState game : games) {
                if (game.scoresA == null) game.scoresA = new ArrayList<>();
                if (game.scoresB == null) game.scoresB = new ArrayList<>();
                if (game.categories == null) game.categories = new ArrayList<>();
                if (game.usedCardPaths == null) game.usedCardPaths = new ArrayList<>();
            }
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
        sharedPreferences.edit().remove(KEY_ALL_GAMES).apply();
        return true;
    }

    private void saveAllGames(List<GameState> games) {
        sharedPreferences.edit()
                .putString(KEY_ALL_GAMES, gson.toJson(games))
                .apply();
    }

    public GameState getLastUnfinishedGame() {
        GameState currentGame = getCurrentGame();
        return (currentGame != null && !currentGame.isFinished) ? currentGame : null;
    }

    /**
     * Clear the current game without saving to history.
     */
    public synchronized void clearCurrentGame() {
        sharedPreferences.edit().remove(KEY_CURRENT_GAME).apply();
    }
}