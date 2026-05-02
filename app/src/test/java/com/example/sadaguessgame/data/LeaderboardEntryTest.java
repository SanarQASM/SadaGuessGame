package com.example.sadaguessgame.data;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for LeaderboardEntry.
 *
 * Place at:
 *   app/src/test/java/com/example/sadaguessgame/data/LeaderboardEntryTest.java
 *
 * Run with:
 *   ./gradlew :app:test
 */
public class LeaderboardEntryTest {

    private LeaderboardEntry entry;

    @Before
    public void setUp() {
        entry = new LeaderboardEntry("TeamA");
    }

    // ── recordWin ────────────────────────────────────────────────────────────

    @Test
    public void recordWin_incrementsWinsAndGames() {
        entry.recordWin(10);
        assertEquals(1, entry.totalWins);
        assertEquals(1, entry.gamesPlayed);
        assertEquals(10, entry.totalPointsScored);
    }

    @Test
    public void recordWin_incrementsWinStreak() {
        entry.recordWin(5);
        entry.recordWin(5);
        assertEquals(2, entry.currentWinStreak);
    }

    @Test
    public void recordWin_updatesMaxWinStreak() {
        entry.recordWin(5);
        entry.recordWin(5);
        entry.recordWin(5);
        assertEquals(3, entry.maxWinStreak);
    }

    // ── recordLoss ───────────────────────────────────────────────────────────

    @Test
    public void recordLoss_incrementsLossesAndGames() {
        entry.recordLoss(2);
        assertEquals(1, entry.totalLosses);
        assertEquals(1, entry.gamesPlayed);
    }

    @Test
    public void recordLoss_resetsCurrentWinStreak() {
        entry.recordWin(5);
        entry.recordWin(5);
        entry.recordLoss(2);
        assertEquals(0, entry.currentWinStreak);
    }

    @Test
    public void recordLoss_doesNotAffectMaxStreak() {
        entry.recordWin(5);
        entry.recordWin(5);
        entry.recordLoss(2);
        assertEquals(2, entry.maxWinStreak);
    }

    // ── recordDraw ───────────────────────────────────────────────────────────

    @Test
    public void recordDraw_incrementsDrawsAndGames() {
        entry.recordDraw(5);
        assertEquals(1, entry.totalDraws);
        assertEquals(1, entry.gamesPlayed);
    }

    @Test
    public void recordDraw_resetsCurrentWinStreak() {
        entry.recordWin(5);
        entry.recordDraw(5);
        assertEquals(0, entry.currentWinStreak);
    }

    // ── getWinRate ───────────────────────────────────────────────────────────

    @Test
    public void getWinRate_zeroWhenNoGamesPlayed() {
        assertEquals(0.0, entry.getWinRate(), 0.001);
    }

    @Test
    public void getWinRate_calculatesCorrectly() {
        entry.recordWin(10);
        entry.recordLoss(5);
        entry.recordLoss(5);
        entry.recordLoss(5);
        // 1 win / 4 games = 25%
        assertEquals(25.0, entry.getWinRate(), 0.001);
    }

    @Test
    public void getWinRateFormatted_containsPercentSign() {
        entry.recordWin(10);
        assertTrue(entry.getWinRateFormatted().contains("%"));
    }

    // ── getSortScore ─────────────────────────────────────────────────────────

    @Test
    public void getSortScore_higherForMoreWins() {
        LeaderboardEntry winner = new LeaderboardEntry("Winner");
        LeaderboardEntry loser  = new LeaderboardEntry("Loser");
        winner.recordWin(10);
        winner.recordWin(10);
        loser.recordLoss(2);
        assertTrue(winner.getSortScore() > loser.getSortScore());
    }
}