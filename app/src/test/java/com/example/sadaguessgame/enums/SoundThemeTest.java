package com.example.sadaguessgame.enums;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for SoundTheme enum.
 *
 * Place at:
 *   app/src/test/java/com/example/sadaguessgame/enums/SoundThemeTest.java
 *
 * Run with:
 *   ./gradlew :app:test
 */
public class SoundThemeTest {

    @Test
    public void fromKey_returnsClassicForClassicString() {
        assertEquals(SoundTheme.CLASSIC, SoundTheme.fromKey("classic"));
    }

    @Test
    public void fromKey_returnsDabke() {
        assertEquals(SoundTheme.DABKE, SoundTheme.fromKey("dabke"));
    }

    @Test
    public void fromKey_returnsSilent() {
        assertEquals(SoundTheme.SILENT, SoundTheme.fromKey("silent"));
    }

    @Test
    public void fromKey_returnsClassicForNull() {
        assertEquals(SoundTheme.CLASSIC, SoundTheme.fromKey(null));
    }

    @Test
    public void fromKey_returnsClassicForUnknownKey() {
        assertEquals(SoundTheme.CLASSIC, SoundTheme.fromKey("anything_else"));
    }

    @Test
    public void getKey_matchesFromKey() {
        for (SoundTheme theme : SoundTheme.values()) {
            assertEquals(theme, SoundTheme.fromKey(theme.getKey()));
        }
    }
}