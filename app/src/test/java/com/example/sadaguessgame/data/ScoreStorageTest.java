package com.example.sadaguessgame.data;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Robolectric tests for ScoreStorage — tests SharedPreferences persistence
 * without a real device or emulator.
 *
 * Place at:
 *   app/src/test/java/com/example/sadaguessgame/data/ScoreStorageTest.java
 *
 * Dependencies required in app/build.gradle:
 *   testImplementation 'org.robolectric:robolectric:4.11.1'
 *   testImplementation 'androidx.test:core:1.5.0'
 *
 * Run with:
 *   ./gradlew :app:test
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class ScoreStorageTest {

    private ScoreStorage storage;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        // Reset singleton between tests
        try {
            java.lang.reflect.Field f = ScoreStorage.class.getDeclaredField("instance");
            f.setAccessible(true);
            f.set(null, null);
        } catch (Exception ignored) {}

        storage = ScoreStorage.getInstance(context);
    }

    // ── saveCurrentGame / getCurrentGame ─────────────────────────────────────

    @Test
    public void getCurrentGame_returnsNullWhenNothingSaved() {
        assertNull(storage.getCurrentGame());
    }

    @Test
    public void saveAndGetCurrentGame_roundtripsGroupNames() {
        GameState game = buildGame("Lions", "Tigers");
        storage.saveCurrentGame(game);
        GameState loaded = storage.getCurrentGame();
        assertNotNull(loaded);
        assertEquals("Lions", loaded.groupAName);
        assertEquals("Tigers", loaded.groupBName);
    }

    @Test
    public void saveCurrentGame_ensuresNonNullListsAfterLoad() {
        GameState game = buildGame("A", "B");
        game.scoresA = null; // deliberately null to simulate old data
        storage.saveCurrentGame(game);
        GameState loaded = storage.getCurrentGame();
        assertNotNull(loaded);
        assertNotNull(loaded.scoresA);
    }

    // ── saveFinishedGame ─────────────────────────────────────────────────────

    @Test
    public void saveFinishedGame_movesToHistoryAndClearsCurrentGame() {
        GameState game = buildGame("A", "B");
        storage.saveCurrentGame(game);
        storage.saveFinishedGame(game);

        assertNull(storage.getCurrentGame());
        List<GameState> all = storage.getAllGames();
        assertEquals(1, all.size());
        assertTrue(all.get(0).isFinished);
    }

    @Test
    public void saveFinishedGame_appendsMultipleGames() {
        storage.saveFinishedGame(buildGame("A", "B"));
        storage.saveFinishedGame(buildGame("C", "D"));
        assertEquals(2, storage.getAllGames().size());
    }

    // ── deleteGame ───────────────────────────────────────────────────────────

    @Test
    public void deleteGame_removesCorrectEntry() {
        storage.saveFinishedGame(buildGame("A", "B"));
        storage.saveFinishedGame(buildGame("C", "D"));
        boolean result = storage.deleteGame(0);
        assertTrue(result);
        assertEquals(1, storage.getAllGames().size());
        assertEquals("C", storage.getAllGames().get(0).groupAName);
    }

    @Test
    public void deleteGame_returnsFalseForInvalidIndex() {
        storage.saveFinishedGame(buildGame("A", "B"));
        assertFalse(storage.deleteGame(99));
    }

    // ── deleteAllGames ───────────────────────────────────────────────────────

    @Test
    public void deleteAllGames_clearsHistory() {
        storage.saveFinishedGame(buildGame("A", "B"));
        storage.deleteAllGames();
        assertTrue(storage.getAllGames().isEmpty());
    }

    // ── getLastUnfinishedGame ────────────────────────────────────────────────

    @Test
    public void getLastUnfinishedGame_returnsCurrentGameWhenNotFinished() {
        GameState game = buildGame("A", "B");
        game.isFinished = false;
        storage.saveCurrentGame(game);
        assertNotNull(storage.getLastUnfinishedGame());
    }

    @Test
    public void getLastUnfinishedGame_returnsNullWhenGameIsFinished() {
        GameState game = buildGame("A", "B");
        game.isFinished = true;
        storage.saveCurrentGame(game);
        assertNull(storage.getLastUnfinishedGame());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private GameState buildGame(String groupA, String groupB) {
        GameState g = new GameState();
        g.groupAName = groupA;
        g.groupBName = groupB;
        g.gameId = "test_" + System.currentTimeMillis();
        g.ensureNonNullLists();
        return g;
    }
}