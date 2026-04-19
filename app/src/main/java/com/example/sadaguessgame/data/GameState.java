package com.example.sadaguessgame.data;

import java.util.ArrayList;
import java.util.List;

public class GameState {

    public String gameId;

    public List<Integer> scoresA = new ArrayList<>();
    public List<Integer> scoresB = new ArrayList<>();
    public List<String> categories = new ArrayList<>();

    public int totalRounds;
    public int currentRound;

    public int minutePicker;
    public int secondPicker;

    public String groupAName;
    public String groupBName;

    public int groupTurn; // 0 = A, 1 = B

    public boolean isFinished;

    public boolean turnGroupAFinish;
    public boolean turnGroupBFinish;

}