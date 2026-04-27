package com.example.sadaguessgame.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Central model representing the complete state of one game session.
 * Serialized to JSON via Gson and persisted in SharedPreferences.
 *
 * NEW FIELDS (v2):
 *  - streakA / streakB           → current consecutive-correct streak per group
 *  - maxStreakA / maxStreakB      → best streak achieved this game
 *  - hintsRemainingA/B           → hint tokens left (default 2 each)
 *  - comboScoresA/B              → parallel list: whether each score had combo applied
 *  - difficultyLevel             → "easy" | "medium" | "hard"
 *  - voiceClueModeEnabled        → TTS toggle
 *  - isSuddenDeath               → flag for tie-breaker round
 *  - suddenDeathWinner           → GROUP_A | GROUP_B | -1
 *  - wordPackId                  → -1 = built-in assets, else custom pack Room ID
 */
public class GameState {

    // ─── Group constants ──────────────────────────────────────────────────────
    public static final int GROUP_A    = 0;
    public static final int GROUP_B    = 1;
    public static final int NO_WINNER  = -1;

    // ─── Identity ─────────────────────────────────────────────────────────────
    public String gameId = "";

    // ─── Scores ───────────────────────────────────────────────────────────────
    public List<Integer> scoresA        = new ArrayList<>();
    public List<Integer> scoresB        = new ArrayList<>();
    public List<Boolean> comboScoresA   = new ArrayList<>();   // NEW: was combo applied?
    public List<Boolean> comboScoresB   = new ArrayList<>();   // NEW

    // ─── Streak (NEW) ─────────────────────────────────────────────────────────
    public int streakA    = 0;
    public int streakB    = 0;
    public int maxStreakA = 0;
    public int maxStreakB = 0;

    // ─── Hints (NEW) ──────────────────────────────────────────────────────────
    public int hintsRemainingA = 2;
    public int hintsRemainingB = 2;

    // ─── Difficulty (NEW) ─────────────────────────────────────────────────────
    public String difficultyLevel = "medium";   // "easy" | "medium" | "hard"

    // ─── Voice clue (NEW) ─────────────────────────────────────────────────────
    public boolean voiceClueModeEnabled = false;

    // ─── Sudden death (NEW) ───────────────────────────────────────────────────
    public boolean isSuddenDeath      = false;
    public int     suddenDeathWinner  = NO_WINNER;

    // ─── Custom word pack (NEW) ───────────────────────────────────────────────
    public long wordPackId = -1L;   // -1 = use built-in assets

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
    public boolean isFinished        = false;
    public boolean turnGroupAFinish  = false;
    public boolean turnGroupBFinish  = false;

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
    // Streak helpers (NEW)
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Call after a correct guess.  Returns the combo multiplier to apply:
     * streak 1-2 → 1.0x,  streak 3-4 → 1.5x,  streak 5+ → 2.0x
     */
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

    /** Call after a miss (score = 0). */
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
    // Hint helpers (NEW)
    // ═════════════════════════════════════════════════════════════════════════

    public boolean canUseHint(int group) {
        return group == GROUP_A ? hintsRemainingA > 0 : hintsRemainingB > 0;
    }

    public void consumeHint(int group) {
        if (group == GROUP_A && hintsRemainingA > 0) hintsRemainingA--;
        else if (group == GROUP_B && hintsRemainingB > 0) hintsRemainingB--;
    }

    public int getHintsRemaining(int group) {
        return group == GROUP_A ? hintsRemainingA : hintsRemainingB;
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

    /**
     * True if the trailing group cannot catch up even with max score (3) per remaining round.
     */
    public boolean canSkipRemainingRounds() {
        if (isFinished) return false;
        int roundsLeft = totalRounds - currentRound;
        if (!isRoundComplete()) roundsLeft++;
        int maxPossible = roundsLeft * 3;
        int a = getTotalScoreA(), b = getTotalScoreB();
        return (a > b + maxPossible) || (b > a + maxPossible);
    }

    /**
     * True when the game ends in an exact draw — triggers sudden death.
     */
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
    // Null-safety guard — call after Gson deserialization
    // ═════════════════════════════════════════════════════════════════════════

    public void ensureNonNullLists() {
        if (scoresA      == null) scoresA      = new ArrayList<>();
        if (scoresB      == null) scoresB      = new ArrayList<>();
        if (comboScoresA == null) comboScoresA = new ArrayList<>();
        if (comboScoresB == null) comboScoresB = new ArrayList<>();
        if (categories   == null) categories   = new ArrayList<>();
        if (usedCardPaths== null) usedCardPaths= new ArrayList<>();
        if (difficultyLevel == null) difficultyLevel = "medium";
    }
}