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
 * Robolectric tests for LeaderboardStorage.
 *
 * Place at:
 *   app/src/test/java/com/example/sadaguessgame/data/LeaderboardStorageTest.java
 *
 * Run with:
 *   ./gradlew :app:test
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class LeaderboardStorageTest {

    private LeaderboardStorage storage;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        try {
            java.lang.reflect.Field f = LeaderboardStorage.class.getDeclaredField("instance");
            f.setAccessible(true);
            f.set(null, null);
        } catch (Exception ignored) {}

        storage = LeaderboardStorage.getInstance(context);
        storage.clearAll(); // ensure clean slate
    }

    // ── getAllEntries on empty storage ────────────────────────────────────────

    @Test
    public void getAllEntries_emptyWhenNothingRecorded() {
        assertTrue(storage.getAllEntries().isEmpty());
    }

    // ── recordGameResult ─────────────────────────────────────────────────────

    @Test
    public void recordGameResult_createsEntriesForBothGroups() {
        storage.recordGameResult(buildGame("Alpha", "Beta", 5, 3));
        List<LeaderboardEntry> entries = storage.getAllEntries();
        assertEquals(2, entries.size());
    }

    @Test
    public void recordGameResult_correctlyAssignsWinAndLoss() {
        storage.recordGameResult(buildGame("Alpha", "Beta", 5, 3));
        LeaderboardEntry alpha = storage.getEntry("Alpha");
        LeaderboardEntry beta  = storage.getEntry("Beta");
        assertNotNull(alpha);
        assertNotNull(beta);
        assertEquals(1, alpha.totalWins);
        assertEquals(0, alpha.totalLosses);
        assertEquals(1, beta.totalLosses);
        assertEquals(0, beta.totalWins);
    }

    @Test
    public void recordGameResult_correctlyAssignsDraw() {
        storage.recordGameResult(buildGame("Alpha", "Beta", 5, 5));
        LeaderboardEntry alpha = storage.getEntry("Alpha");
        LeaderboardEntry beta  = storage.getEntry("Beta");
        assertNotNull(alpha);
        assertNotNull(beta);
        assertEquals(1, alpha.totalDraws);
        assertEquals(1, beta.totalDraws);
    }

    @Test
    public void recordGameResult_accumulatesAcrossMultipleGames() {
        storage.recordGameResult(buildGame("Alpha", "Beta", 5, 3));
        storage.recordGameResult(buildGame("Alpha", "Beta", 2, 4));
        LeaderboardEntry alpha = storage.getEntry("Alpha");
        assertNotNull(alpha);
        assertEquals(1, alpha.totalWins);
        assertEquals(1, alpha.totalLosses);
        assertEquals(2, alpha.gamesPlayed);
    }

    // ── getTopEntries ────────────────────────────────────────────────────────

    @Test
    public void getTopEntries_respectsLimit() {
        storage.recordGameResult(buildGame("A", "B", 5, 3));
        storage.recordGameResult(buildGame("C", "D", 5, 3));
        storage.recordGameResult(buildGame("E", "F", 5, 3));
        List<LeaderboardEntry> top2 = storage.getTopEntries(2);
        assertTrue(top2.size() <= 2);
    }

    @Test
    public void getTopEntries_sortsByWins() {
        storage.recordGameResult(buildGame("Winner", "Loser", 10, 2));
        storage.recordGameResult(buildGame("Winner", "Loser", 10, 2));
        storage.recordGameResult(buildGame("Loser",  "Dummy", 2, 8));

        List<LeaderboardEntry> top = storage.getTopEntries(5);
        assertFalse(top.isEmpty());
        assertEquals("Winner", top.get(0).groupName);
    }

    // ── clearAll ─────────────────────────────────────────────────────────────

    @Test
    public void clearAll_removesAllEntries() {
        storage.recordGameResult(buildGame("A", "B", 5, 3));
        storage.clearAll();
        assertTrue(storage.getAllEntries().isEmpty());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private GameState buildGame(String a, String b, int scoreA, int scoreB) {
        GameState g = new GameState();
        g.groupAName = a;
        g.groupBName = b;
        g.ensureNonNullLists();
        for (int i = 0; i < scoreA; i++) g.scoresA.add(1);
        for (int i = 0; i < scoreB; i++) g.scoresB.add(1);
        g.totalRounds = 10;
        g.currentRound = 10;
        g.turnGroupAFinish = true;
        g.turnGroupBFinish = true;
        g.isFinished = true;
        return g;
    }
}