package com.example.sadaguessgame.data;

import java.util.ArrayList;
import java.util.List;

public class GameState {

    public static final int GROUP_A = 0;
    public static final int GROUP_B = 1;

    public String gameId = "";

    public List<Integer> scoresA = new ArrayList<>();
    public List<Integer> scoresB = new ArrayList<>();
    public List<String> categories = new ArrayList<>();

    // Track used card paths to prevent duplicates in same game
    public List<String> usedCardPaths = new ArrayList<>();

    public int totalRounds = 10;
    public int currentRound = 1;

    public int minutePicker = 1;
    public int secondPicker = 0;

    public String groupAName = "";
    public String groupBName = "";

    public int groupTurn = GROUP_A;

    public boolean isFinished = false;
    public boolean turnGroupAFinish = false;
    public boolean turnGroupBFinish = false;

    public int groupAColor = 0;
    public int groupBColor = 0;

    public int getTotalScoreA() {
        int total = 0;
        if (scoresA == null) return total;
        for (int s : scoresA) total += s;
        return total;
    }

    public int getTotalScoreB() {
        int total = 0;
        if (scoresB == null) return total;
        for (int s : scoresB) total += s;
        return total;
    }

    public boolean isRoundComplete() {
        return turnGroupAFinish && turnGroupBFinish;
    }

    public boolean isGameOver() {
        return currentRound >= totalRounds && isRoundComplete();
    }

    /**
     * Check if the game should skip remaining rounds because one group
     * cannot possibly catch up even if they score max on all remaining rounds.
     * Score dialog allows max 3 per round.
     */
    public boolean canSkipRemainingRounds() {
        if (isFinished) return false;
        int roundsLeft = totalRounds - currentRound;
        // +1 for current round if not complete
        if (!isRoundComplete()) roundsLeft++;
        int maxPossiblePerGroup = roundsLeft * 3;
        int scoreA = getTotalScoreA();
        int scoreB = getTotalScoreB();
        // A wins decisively
        if (scoreA > scoreB + maxPossiblePerGroup) return true;
        // B wins decisively
        if (scoreB > scoreA + maxPossiblePerGroup) return true;
        return false;
    }

    public int getTimerDurationSeconds() {
        return minutePicker * 60 + secondPicker;
    }

    public String getCurrentGroupName() {
        return groupTurn == GROUP_A ? groupAName : groupBName;
    }

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
}