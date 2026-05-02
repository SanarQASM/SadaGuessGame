package com.example.sadaguessgame.data;

import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.*;

/**
 * Unit tests for GameState — the core model that drives every game session.
 *
 * Place at:
 *   app/src/test/java/com/example/sadaguessgame/data/GameStateTest.java
 *
 * Run with:
 *   ./gradlew :app:test
 */
public class GameStateTest {

    private GameState game;

    @Before
    public void setUp() {
        game = new GameState();
        game.groupAName = "Alpha";
        game.groupBName = "Beta";
        game.totalRounds = 10;
        game.currentRound = 1;
        game.minutePicker = 1;
        game.secondPicker = 30;
        game.ensureNonNullLists();
    }

    // ── Score helpers ────────────────────────────────────────────────────────

    @Test
    public void getTotalScoreA_returnsZeroOnEmptyList() {
        assertEquals(0, game.getTotalScoreA());
    }

    @Test
    public void getTotalScoreB_returnsZeroOnEmptyList() {
        assertEquals(0, game.getTotalScoreB());
    }

    @Test
    public void getTotalScoreA_sumsCorrectly() {
        game.scoresA.add(1);
        game.scoresA.add(2);
        game.scoresA.add(3);
        assertEquals(6, game.getTotalScoreA());
    }

    @Test
    public void getTotalScoreB_sumsCorrectly() {
        game.scoresB.add(3);
        game.scoresB.add(3);
        assertEquals(6, game.getTotalScoreB());
    }

    @Test
    public void getTotalScoreA_handlesNullListGracefully() {
        game.scoresA = null;
        assertEquals(0, game.getTotalScoreA());
    }

    // ── Streak / combo multiplier ────────────────────────────────────────────

    @Test
    public void recordCorrectGuess_groupA_firstStreakIsOne() {
        float multiplier = game.recordCorrectGuess(GameState.GROUP_A);
        assertEquals(1, game.streakA);
        assertEquals(1.0f, multiplier, 0.001f);
    }

    @Test
    public void recordCorrectGuess_groupA_streakThreeGivesOnePointFiveMultiplier() {
        game.recordCorrectGuess(GameState.GROUP_A);
        game.recordCorrectGuess(GameState.GROUP_A);
        float multiplier = game.recordCorrectGuess(GameState.GROUP_A);
        assertEquals(3, game.streakA);
        assertEquals(1.5f, multiplier, 0.001f);
    }

    @Test
    public void recordCorrectGuess_groupA_streakFiveGivesTwoMultiplier() {
        for (int i = 0; i < 5; i++) game.recordCorrectGuess(GameState.GROUP_A);
        assertEquals(5, game.streakA);
        float multiplier = game.recordCorrectGuess(GameState.GROUP_A);
        // streak is now 6, still 2x
        assertEquals(2.0f, multiplier, 0.001f);
    }

    @Test
    public void recordCorrectGuess_groupA_resetsGroupBStreak() {
        game.streakB = 5;
        game.recordCorrectGuess(GameState.GROUP_A);
        assertEquals(0, game.streakB);
    }

    @Test
    public void recordMiss_groupA_resetsStreakA() {
        game.streakA = 4;
        game.recordMiss(GameState.GROUP_A);
        assertEquals(0, game.streakA);
        // streakB untouched
        game.streakB = 3;
        game.recordMiss(GameState.GROUP_A);
        assertEquals(3, game.streakB);
    }

    @Test
    public void maxStreak_trackedCorrectly() {
        game.recordCorrectGuess(GameState.GROUP_A);
        game.recordCorrectGuess(GameState.GROUP_A);
        game.recordCorrectGuess(GameState.GROUP_A);
        game.recordMiss(GameState.GROUP_A);
        game.recordCorrectGuess(GameState.GROUP_A);
        // max should still be 3
        assertEquals(3, game.maxStreakA);
    }

    // ── Hints ────────────────────────────────────────────────────────────────

    @Test
    public void canUseHint_trueWhenHintsRemain() {
        assertTrue(game.canUseHint(GameState.GROUP_A));
    }

    @Test
    public void consumeHint_decrementsCount() {
        game.consumeHint(GameState.GROUP_A);
        assertEquals(1, game.getHintsRemaining(GameState.GROUP_A));
    }

    @Test
    public void canUseHint_falseAfterAllConsumed() {
        game.consumeHint(GameState.GROUP_A);
        game.consumeHint(GameState.GROUP_A);
        assertFalse(game.canUseHint(GameState.GROUP_A));
    }

    @Test
    public void consumeHint_doesNotGoBelowZero() {
        game.hintsRemainingA = 0;
        game.consumeHint(GameState.GROUP_A);
        assertEquals(0, game.getHintsRemaining(GameState.GROUP_A));
    }

    // ── Round / game state ───────────────────────────────────────────────────

    @Test
    public void isRoundComplete_trueWhenBothGroupsFinished() {
        game.turnGroupAFinish = true;
        game.turnGroupBFinish = true;
        assertTrue(game.isRoundComplete());
    }

    @Test
    public void isRoundComplete_falseWhenOnlyOneGroupFinished() {
        game.turnGroupAFinish = true;
        game.turnGroupBFinish = false;
        assertFalse(game.isRoundComplete());
    }

    @Test
    public void isGameOver_trueOnLastRoundWithBothGroupsFinished() {
        game.currentRound = 10;
        game.totalRounds = 10;
        game.turnGroupAFinish = true;
        game.turnGroupBFinish = true;
        assertTrue(game.isGameOver());
    }

    @Test
    public void isGameOver_falseBeforeLastRound() {
        game.currentRound = 5;
        game.totalRounds = 10;
        game.turnGroupAFinish = true;
        game.turnGroupBFinish = true;
        assertFalse(game.isGameOver());
    }

    @Test
    public void isExactDraw_trueWhenScoresEqualAndGameOver() {
        game.currentRound = 10;
        game.totalRounds = 10;
        game.turnGroupAFinish = true;
        game.turnGroupBFinish = true;
        game.scoresA.add(5);
        game.scoresB.add(5);
        assertTrue(game.isExactDraw());
    }

    @Test
    public void isExactDraw_falseWhenScoresDiffer() {
        game.currentRound = 10;
        game.totalRounds = 10;
        game.turnGroupAFinish = true;
        game.turnGroupBFinish = true;
        game.scoresA.add(6);
        game.scoresB.add(5);
        assertFalse(game.isExactDraw());
    }

    @Test
    public void getTimerDurationSeconds_calculatesCorrectly() {
        game.minutePicker = 2;
        game.secondPicker = 30;
        assertEquals(150, game.getTimerDurationSeconds());
    }

    @Test
    public void getCurrentGroupName_returnsGroupAName() {
        game.groupTurn = GameState.GROUP_A;
        assertEquals("Alpha", game.getCurrentGroupName());
    }

    @Test
    public void getCurrentGroupName_returnsGroupBName() {
        game.groupTurn = GameState.GROUP_B;
        assertEquals("Beta", game.getCurrentGroupName());
    }

    // ── Card tracking ────────────────────────────────────────────────────────

    @Test
    public void isCardUsed_falseForNewPath() {
        assertFalse(game.isCardUsed("animal/cat.jpg"));
    }

    @Test
    public void markCardUsed_andIsCardUsed_workTogether() {
        game.markCardUsed("animal/cat.jpg");
        assertTrue(game.isCardUsed("animal/cat.jpg"));
    }

    @Test
    public void clearUsedCards_resetsTracking() {
        game.markCardUsed("animal/cat.jpg");
        game.clearUsedCards();
        assertFalse(game.isCardUsed("animal/cat.jpg"));
    }

    // ── ensureNonNullLists ───────────────────────────────────────────────────

    @Test
    public void ensureNonNullLists_initializesNullCollections() {
        GameState fresh = new GameState();
        fresh.scoresA = null;
        fresh.scoresB = null;
        fresh.categories = null;
        fresh.usedCardPaths = null;
        fresh.ensureNonNullLists();

        assertNotNull(fresh.scoresA);
        assertNotNull(fresh.scoresB);
        assertNotNull(fresh.categories);
        assertNotNull(fresh.usedCardPaths);
        assertEquals("medium", fresh.difficultyLevel);
    }

    // ── canSkipRemainingRounds ───────────────────────────────────────────────

    @Test
    public void canSkipRemainingRounds_falseWhenGameIsAlreadyFinished() {
        game.isFinished = true;
        assertFalse(game.canSkipRemainingRounds());
    }

    @Test
    public void canSkipRemainingRounds_trueWhenLeadIsInsurpassable() {
        // 1 round left, max possible = 3, group A leads by 10
        game.currentRound = 10;
        game.totalRounds = 10;
        game.turnGroupAFinish = true;   // B hasn't played yet → +1 round left
        game.turnGroupBFinish = false;
        for (int i = 0; i < 5; i++) game.scoresA.add(2);  // 10
        for (int i = 0; i < 4; i++) game.scoresB.add(0);  // 0
        // 10 - 0 = 10 > 3 (max remaining for B)
        assertTrue(game.canSkipRemainingRounds());
    }
}