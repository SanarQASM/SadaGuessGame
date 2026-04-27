package com.example.sadaguessgame.enums;

/**
 * Word difficulty level used in CreateNewGameActivity and FileSelectingRandom.
 *
 * Asset folder convention:
 *   assets/animal/easy/cat.jpg
 *   assets/animal/medium/elephant.jpg
 *   assets/animal/hard/axolotl.jpg
 *
 * If a difficulty subfolder does not exist the helper falls back to the
 * root category folder (backwards-compatible with the existing flat layout).
 */
public enum DifficultyLevel {

    EASY("easy"),
    MEDIUM("medium"),
    HARD("hard");

    private final String folderName;

    DifficultyLevel(String folderName) {
        this.folderName = folderName;
    }

    public String getFolderName() {
        return folderName;
    }

    /** Safe parse – returns MEDIUM if the string is unrecognised. */
    public static DifficultyLevel fromString(String value) {
        if (value == null) return MEDIUM;
        switch (value.toLowerCase()) {
            case "easy":   return EASY;
            case "hard":   return HARD;
            default:       return MEDIUM;
        }
    }

    /** Returns the string resource name suffix used in strings.xml. */
    public String getStringKey() {
        return "difficulty_" + folderName;   // e.g. "difficulty_easy"
    }
}