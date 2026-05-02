package com.example.sadaguessgame.enums;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for DifficultyLevel enum.
 *
 * Place at:
 *   app/src/test/java/com/example/sadaguessgame/enums/DifficultyLevelTest.java
 *
 * Run with:
 *   ./gradlew :app:test
 */
public class DifficultyLevelTest {

    @Test
    public void fromString_returnsEasy() {
        assertEquals(DifficultyLevel.EASY, DifficultyLevel.fromString("easy"));
    }

    @Test
    public void fromString_returnsHard() {
        assertEquals(DifficultyLevel.HARD, DifficultyLevel.fromString("hard"));
    }

    @Test
    public void fromString_returnsMediumByDefault() {
        assertEquals(DifficultyLevel.MEDIUM, DifficultyLevel.fromString("garbage"));
    }

    @Test
    public void fromString_returnsMediumForNull() {
        assertEquals(DifficultyLevel.MEDIUM, DifficultyLevel.fromString(null));
    }

    @Test
    public void fromString_caseInsensitive() {
        assertEquals(DifficultyLevel.EASY, DifficultyLevel.fromString("EASY"));
    }

    @Test
    public void getFolderName_matchesStringInput() {
        assertEquals("easy",   DifficultyLevel.EASY.getFolderName());
        assertEquals("medium", DifficultyLevel.MEDIUM.getFolderName());
        assertEquals("hard",   DifficultyLevel.HARD.getFolderName());
    }

    @Test
    public void getStringKey_hasCorrectPrefix() {
        assertTrue(DifficultyLevel.EASY.getStringKey().startsWith("difficulty_"));
        assertEquals("difficulty_easy", DifficultyLevel.EASY.getStringKey());
    }
}