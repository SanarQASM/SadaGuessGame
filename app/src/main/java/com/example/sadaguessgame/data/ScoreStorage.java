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
    public Gson gson;

    private static ScoreStorage instance;

    private ScoreStorage(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // Singleton pattern to access repository globally
    public static ScoreStorage getInstance(Context context) {
        if (instance == null) {
            instance = new ScoreStorage(context.getApplicationContext());
        }
        return instance;
    }

    // Save or update current game
    public void saveCurrentGame(GameState gameState) {
        String gameJson = gson.toJson(gameState);
        sharedPreferences.edit().putString(KEY_CURRENT_GAME, gameJson).apply();
    }

    // Get the current game
    public GameState getCurrentGame() {
        String gameJson = sharedPreferences.getString(KEY_CURRENT_GAME, null);
        if (gameJson != null) {
            return gson.fromJson(gameJson, GameState.class);
        }
        return null;
    }

    // Save finished game to history
    public void saveFinishedGame(GameState gameState) {
        List<GameState> allGames = getAllGames();
        allGames.add(gameState);
        saveAllGames(allGames);
        // Clear current game
        sharedPreferences.edit().remove(KEY_CURRENT_GAME).apply();
    }

    // Get all games (finished and unfinished)
    public List<GameState> getAllGames() {
        String gamesJson = sharedPreferences.getString(KEY_ALL_GAMES, null);
        if (gamesJson != null) {
            Type type = new TypeToken<List<GameState>>() {}.getType();
            return gson.fromJson(gamesJson, type);
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Delete a specific game at the given index
     * @param index The index of the game to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteGame(int index) {
        try {
            List<GameState> games = getAllGames();
            if (games == null || index < 0 || index >= games.size()) {
                return false;
            }

            games.remove(index);
            saveAllGames(games);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Delete all games from history (does not delete current game)
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteAllGames() {
        try {
            sharedPreferences.edit().remove(KEY_ALL_GAMES).apply();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Save all games to SharedPreferences
     * @param games List of games to save
     */
    private void saveAllGames(List<GameState> games) {
        String gamesJson = gson.toJson(games);
        sharedPreferences.edit().putString(KEY_ALL_GAMES, gamesJson).apply();
    }

    // Get last unfinished game
    public GameState getLastUnfinishedGame() {
        GameState currentGame = getCurrentGame();
        if (currentGame != null && !currentGame.isFinished) {
            return currentGame;
        }
        return null;
    }

}