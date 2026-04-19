package com.example.sadaguessgame.data;

import java.util.ArrayList;
import java.util.List;

public class GameState {

    // Group turn constants
    public static final int GROUP_A = 0;
    public static final int GROUP_B = 1;

    public String gameId = "";

    public List<Integer> scoresA = new ArrayList<>();
    public List<Integer> scoresB = new ArrayList<>();
    public List<String> categories = new ArrayList<>();

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

    /** Returns total score for Group A */
    public int getTotalScoreA() {
        int total = 0;
        for (int s : scoresA) total += s;
        return total;
    }

    /** Returns total score for Group B */
    public int getTotalScoreB() {
        int total = 0;
        for (int s : scoresB) total += s;
        return total;
    }

    /** Returns true if both groups have finished the current round */
    public boolean isRoundComplete() {
        return turnGroupAFinish && turnGroupBFinish;
    }

    /** Returns true if game has reached max rounds */
    public boolean isGameOver() {
        return currentRound >= totalRounds && isRoundComplete();
    }

    /** Returns the timer duration in seconds */
    public int getTimerDurationSeconds() {
        return minutePicker * 60 + secondPicker;
    }

    /** Returns the name of the current group's turn */
    public String getCurrentGroupName() {
        return groupTurn == GROUP_A ? groupAName : groupBName;
    }
}