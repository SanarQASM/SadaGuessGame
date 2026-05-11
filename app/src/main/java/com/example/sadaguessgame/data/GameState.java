package com.example.sadaguessgame.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Central model representing the complete state of one game session.
 * Serialized to JSON via Gson and persisted in SharedPreferences.
 *
 * v3 changes:
 *  - hintsRemainingA/B now initialised from configurable hintCount field
 *  - hintsRevealedLettersA/B track how many letters have been revealed per card
 *  - timerSoundEnabled per-game toggle
 */
public class GameState {

    // ─── Group constants ──────────────────────────────────────────────────────
    public static final int GROUP_A   = 0;
    public static final int GROUP_B   = 1;
    public static final int NO_WINNER = -1;

    /** Default hint tokens when the user has not configured a custom value. */
    public static final int DEFAULT_HINT_COUNT = 2;

    // ─── Identity ─────────────────────────────────────────────────────────────
    public String gameId = "";

    // ─── Scores ───────────────────────────────────────────────────────────────
    public List<Integer> scoresA      = new ArrayList<>();
    public List<Integer> scoresB      = new ArrayList<>();
    public List<Boolean> comboScoresA = new ArrayList<>();
    public List<Boolean> comboScoresB = new ArrayList<>();

    // ─── Streak ───────────────────────────────────────────────────────────────
    public int streakA    = 0;
    public int streakB    = 0;
    public int maxStreakA = 0;
    public int maxStreakB = 0;

    // ─── Hints ────────────────────────────────────────────────────────────────
    /** Total hint tokens configured for this game (set by the user before start). */
    public int hintCount       = DEFAULT_HINT_COUNT;
    public int hintsRemainingA = DEFAULT_HINT_COUNT;
    public int hintsRemainingB = DEFAULT_HINT_COUNT;
    /**
     * How many letters have already been revealed for the CURRENT card
     * for each group.  Reset to 0 each time a new card is loaded.
     */
    public int hintsRevealedLettersA = 0;
    public int hintsRevealedLettersB = 0;

    // ─── Difficulty ───────────────────────────────────────────────────────────
    public String difficultyLevel = "medium";

    // ─── Voice clue ───────────────────────────────────────────────────────────
    public boolean voiceClueModeEnabled = false;

    // ─── Timer sound toggle (per-game) ───────────────────────────────────────
    public boolean timerSoundEnabled = true;

    // ─── Sudden death ─────────────────────────────────────────────────────────
    public boolean isSuddenDeath     = false;
    public int     suddenDeathWinner = NO_WINNER;

    // ─── Custom word pack ─────────────────────────────────────────────────────
    public long wordPackId = -1L;

    // ─── Categories & cards ───────────────────────────────────────────────────
    public List<String> categories    = new ArrayList<>();
    public List<String> usedCardPaths = new ArrayList<>();

    // ─── Timer ────────────────────────────────────────────────────────────────
    public int totalRounds  = 10;
    public int currentRound = 1;
    public int minutePicker = 1;
    public int secondPicker = 0;

    // ─── Groups ───────────────────────────────────────────────────────────────
    public String groupAName  = "";
    public String groupBName  = "";
    public int    groupTurn   = GROUP_A;
    public int    groupAColor = 0;
    public int    groupBColor = 0;

    // ─── Turn flags ───────────────────────────────────────────────────────────
    public boolean isFinished       = false;
    public boolean turnGroupAFinish = false;
    public boolean turnGroupBFinish = false;

    // ═════════════════════════════════════════════════════════════════════════
    // Score helpers
    // ═════════════════════════════════════════════════════════════════════════

    public int getTotalScoreA() {
        int total = 0;
        if (scoresA == null) return 0;
        for (int s : scoresA) total += s;
        return total;
    }

    public int getTotalScoreB() {
        int total = 0;
        if (scoresB == null) return 0;
        for (int s : scoresB) total += s;
        return total;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Streak helpers
    // ═════════════════════════════════════════════════════════════════════════

    public float recordCorrectGuess(int group) {
        if (group == GROUP_A) {
            streakA++;
            streakB = 0;
            if (streakA > maxStreakA) maxStreakA = streakA;
            return comboMultiplier(streakA);
        } else {
            streakB++;
            streakA = 0;
            if (streakB > maxStreakB) maxStreakB = streakB;
            return comboMultiplier(streakB);
        }
    }

    public void recordMiss(int group) {
        if (group == GROUP_A) streakA = 0;
        else                  streakB = 0;
    }

    public int getCurrentStreak(int group) {
        return group == GROUP_A ? streakA : streakB;
    }

    private float comboMultiplier(int streak) {
        if (streak >= 5) return 2.0f;
        if (streak >= 3) return 1.5f;
        return 1.0f;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Hint helpers (v3 — progressive letter reveal)
    // ═════════════════════════════════════════════════════════════════════════

    public boolean canUseHint(int group) {
        return group == GROUP_A ? hintsRemainingA > 0 : hintsRemainingB > 0;
    }

    /**
     * Consume one hint token for the given group and return which letter
     * index (0-based) should be revealed next.
     * Returns -1 if no hints are available.
     */
    public int consumeHintAndGetLetterIndex(int group) {
        if (!canUseHint(group)) return -1;

        int letterIndex;
        if (group == GROUP_A) {
            letterIndex = hintsRevealedLettersA;
            hintsRevealedLettersA++;
            hintsRemainingA--;
        } else {
            letterIndex = hintsRevealedLettersB;
            hintsRevealedLettersB++;
            hintsRemainingB--;
        }
        return letterIndex;
    }

    /** Legacy single-consume (kept for compatibility). */
    public void consumeHint(int group) {
        consumeHintAndGetLetterIndex(group);
    }

    public int getHintsRemaining(int group) {
        return group == GROUP_A ? hintsRemainingA : hintsRemainingB;
    }

    /** Called when a new card is loaded — resets letter-reveal counters. */
    public void resetHintLetterCounters() {
        hintsRevealedLettersA = 0;
        hintsRevealedLettersB = 0;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Round / game state helpers
    // ═════════════════════════════════════════════════════════════════════════

    public boolean isRoundComplete() {
        return turnGroupAFinish && turnGroupBFinish;
    }

    public boolean isGameOver() {
        return currentRound >= totalRounds && isRoundComplete();
    }

    public boolean canSkipRemainingRounds() {
        if (isFinished) return false;
        int roundsLeft = totalRounds - currentRound;
        if (!isRoundComplete()) roundsLeft++;
        int maxPossible = roundsLeft * 3;
        int a = getTotalScoreA(), b = getTotalScoreB();
        return (a > b + maxPossible) || (b > a + maxPossible);
    }

    public boolean isExactDraw() {
        return isGameOver() && getTotalScoreA() == getTotalScoreB();
    }

    public int getTimerDurationSeconds() {
        return minutePicker * 60 + secondPicker;
    }

    public String getCurrentGroupName() {
        return groupTurn == GROUP_A ? groupAName : groupBName;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Card tracking helpers
    // ═════════════════════════════════════════════════════════════════════════

    public boolean isCardUsed(String path) {
        if (usedCardPaths == null) usedCardPaths = new ArrayList<>();
        return usedCardPaths.contains(path);
    }

    public void markCardUsed(String path) {
        if (usedCardPaths == null) usedCardPaths = new ArrayList<>();
        if (!usedCardPaths.contains(path)) usedCardPaths.add(path);
    }

    public void clearUsedCards() {
        if (usedCardPaths == null) usedCardPaths = new ArrayList<>();
        usedCardPaths.clear();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Null-safety guard
    // ═════════════════════════════════════════════════════════════════════════

    public void ensureNonNullLists() {
        if (scoresA       == null) scoresA       = new ArrayList<>();
        if (scoresB       == null) scoresB       = new ArrayList<>();
        if (comboScoresA  == null) comboScoresA  = new ArrayList<>();
        if (comboScoresB  == null) comboScoresB  = new ArrayList<>();
        if (categories    == null) categories    = new ArrayList<>();
        if (usedCardPaths == null) usedCardPaths = new ArrayList<>();
        if (difficultyLevel == null) difficultyLevel = "medium";
        // Migrate old games that didn't have hintCount
        if (hintCount <= 0) hintCount = DEFAULT_HINT_COUNT;
    }
}