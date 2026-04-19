package com.example.sadaguessgame.data;

public class GameManager {

    // Single instance of current game
    private static GameState currentGame;

    // Get the current game
    public static GameState getCurrentGame() {
        return currentGame;
    }

    // Set or update the current game
    public static void setCurrentGame(GameState game) {
        currentGame = game;
    }
}
