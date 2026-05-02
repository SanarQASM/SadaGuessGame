package com.example.sadaguessgame.data;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

/**
 * Robolectric tests for GameRepository — verifies it delegates correctly
 * to ScoreStorage and exposes the right API.
 *
 * Place at:
 *   app/src/test/java/com/example/sadaguessgame/data/GameRepositoryTest.java
 *
 * Run with:
 *   ./gradlew :app:test
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class GameRepositoryTest {

    private GameRepository repo;
    private ScoreStorage   storage;

    @Before
    public void setUp() {
        Context ctx = ApplicationProvider.getApplicationContext();

        // Reset singletons
        resetSingleton(GameRepository.class, "instance");
        resetSingleton(ScoreStorage.class, "instance");
        resetSingleton(LeaderboardStorage.class, "instance");

        repo    = GameRepository.getInstance(ctx);
        storage = ScoreStorage.getInstance(ctx);
    }

    @Test
    public void getCurrentGame_returnsNullWhenEmpty() {
        assertNull(repo.getCurrentGame());
    }

    @Test
    public void saveCurrentGame_persistsGame() {
        GameState game = buildGame("A", "B");
        repo.saveCurrentGame(game);
        assertNotNull(repo.getCurrentGame());
    }

    @Test
    public void finishGame_marksGameAsFinished() {
        GameState game = buildGame("A", "B");
        repo.saveCurrentGame(game);
        repo.finishGame(game);
        assertTrue(game.isFinished);
    }

    @Test
    public void finishGame_appearsInAllFinishedGames() {
        GameState game = buildGame("A", "B");
        repo.finishGame(game);
        assertFalse(repo.getAllFinishedGames().isEmpty());
    }

    @Test
    public void getLastUnfinishedGame_returnsGameWhenPresent() {
        GameState game = buildGame("A", "B");
        game.isFinished = false;
        repo.saveCurrentGame(game);
        assertNotNull(repo.getLastUnfinishedGame());
    }

    @Test
    public void deleteGame_reducesCount() {
        repo.finishGame(buildGame("A", "B"));
        repo.finishGame(buildGame("C", "D"));
        repo.deleteGame(0);
        assertEquals(1, repo.getAllFinishedGames().size());
    }

    @Test
    public void deleteAllGames_emptiesHistory() {
        repo.finishGame(buildGame("A", "B"));
        repo.deleteAllGames();
        assertTrue(repo.getAllFinishedGames().isEmpty());
    }

    // ── singleton thread-safety ──────────────────────────────────────────────

    @Test
    public void getInstance_returnsSameInstance() {
        Context ctx = ApplicationProvider.getApplicationContext();
        GameRepository r1 = GameRepository.getInstance(ctx);
        GameRepository r2 = GameRepository.getInstance(ctx);
        assertSame(r1, r2);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private GameState buildGame(String a, String b) {
        GameState g = new GameState();
        g.groupAName = a;
        g.groupBName = b;
        g.gameId = "test_" + System.nanoTime();
        g.ensureNonNullLists();
        return g;
    }

    private void resetSingleton(Class<?> clazz, String fieldName) {
        try {
            java.lang.reflect.Field f = clazz.getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(null, null);
        } catch (Exception ignored) {}
    }
}