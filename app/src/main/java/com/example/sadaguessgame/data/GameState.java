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

    // groupTurn tracks whose turn it currently is WITHIN the current round
    public int groupTurn = GROUP_A;

    public boolean isFinished = false;

    // FIX: These flags track whether each group has completed their turn in the CURRENT round
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

    /**
     * FIX: A round is complete when BOTH groups have played their turn.
     * We track this by checking if both finish flags are set.
     */
    public boolean isRoundComplete() {
        return turnGroupAFinish && turnGroupBFinish;
    }

    /**
     * FIX: The game is over when the current round equals totalRounds AND both groups have
     * completed their turn in that final round.
     */
    public boolean isGameOver() {
        return currentRound >= totalRounds && isRoundComplete();
    }

    /**
     * Check if the game should end early because one group has an insurmountable lead.
     * Max score per round per group = 3 points.
     */
    public boolean canSkipRemainingRounds() {
        if (isFinished) return false;

        // Rounds remaining AFTER the current one (current round is still in progress)
        int roundsRemaining = totalRounds - currentRound;
        // If the current round is still incomplete, the losing team still has their turn
        if (!isRoundComplete()) roundsRemaining++;

        int maxPossiblePerGroup = roundsRemaining * 3;
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