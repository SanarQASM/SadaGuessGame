package com.example.sadaguessgame.data;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;

/**
 * Single source of truth for game data.
 * Wraps ScoreStorage with a clean API.
 */
public class GameRepository {

    private static volatile GameRepository instance;
    private final ScoreStorage storage;

    private GameRepository(@NonNull Context context) {
        this.storage = ScoreStorage.getInstance(context);
    }

    public static GameRepository getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (GameRepository.class) {
                if (instance == null) {
                    instance = new GameRepository(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    @Nullable
    public GameState getCurrentGame() {
        return storage.getCurrentGame();
    }

    public void saveCurrentGame(@NonNull GameState game) {
        storage.saveCurrentGame(game);
    }

    public void finishGame(@NonNull GameState game) {
        game.isFinished = true;
        storage.saveFinishedGame(game);
    }

    @NonNull
    public List<GameState> getAllFinishedGames() {
        return storage.getAllGames();
    }

    public boolean deleteGame(int index) {
        return storage.deleteGame(index);
    }

    public boolean deleteAllGames() {
        return storage.deleteAllGames();
    }

    @Nullable
    public GameState getLastUnfinishedGame() {
        return storage.getLastUnfinishedGame();
    }
}