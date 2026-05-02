package com.example.sadaguessgame.data;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the WordPack model.
 * Place at:
 *   app/src/test/java/com/example/sadaguessgame/data/WordPackTest.java
 * Run with:
 *   ./gradlew :app:test
 */
public class WordPackTest {

    private WordPack pack;

    @Before
    public void setUp() {
        pack = new WordPack("Test Pack", "Custom");
        pack.words.add("Lion");
        pack.words.add("Tiger");
        pack.words.add("Bear");
    }

    // ── isValid ──────────────────────────────────────────────────────────────

    @Test
    public void isValid_trueWithNameAndThreeWords() {
        assertTrue(pack.isValid());
    }

    @Test
    public void isValid_falseWithEmptyName() {
        pack.name = "";
        assertFalse(pack.isValid());
    }

    @Test
    public void isValid_falseWithBlankName() {
        pack.name = "   ";
        assertFalse(pack.isValid());
    }

    @Test
    public void isValid_falseWithFewerThanThreeWords() {
        pack.words.clear();
        pack.words.add("Lion");
        pack.words.add("Tiger");
        assertFalse(pack.isValid());
    }

    @Test
    public void isValid_falseWithNullWords() {
        pack.words = null;
        assertFalse(pack.isValid());
    }

    // ── addWord ──────────────────────────────────────────────────────────────

    @Test
    public void addWord_addsUniqueWord() {
        pack.addWord("Elephant");
        assertTrue(pack.words.contains("Elephant"));
        assertEquals(4, pack.getWordCount());
    }

    @Test
    public void addWord_ignoresDuplicate() {
        pack.addWord("Lion"); // already in list
        assertEquals(3, pack.getWordCount());
    }

    @Test
    public void addWord_ignoresBlankString() {
        pack.addWord("   ");
        assertEquals(3, pack.getWordCount());
    }

    @Test
    public void addWord_trimsWhitespace() {
        pack.addWord("  Giraffe  ");
        assertTrue(pack.words.contains("Giraffe"));
    }

    // ── removeWord ───────────────────────────────────────────────────────────

    @Test
    public void removeWord_removesExistingWord() {
        pack.removeWord("Lion");
        assertFalse(pack.words.contains("Lion"));
        assertEquals(2, pack.getWordCount());
    }

    @Test
    public void removeWord_doesNothingForNonExistentWord() {
        pack.removeWord("Elephant");
        assertEquals(3, pack.getWordCount());
    }

    // ── getSubtitle ──────────────────────────────────────────────────────────

    @Test
    public void getSubtitle_containsWordCountAndCategory() {
        String subtitle = pack.getSubtitle();
        assertTrue(subtitle.contains("3"));
        assertTrue(subtitle.contains("Custom"));
    }

    // ── getWordCount ─────────────────────────────────────────────────────────

    @Test
    public void getWordCount_returnsZeroWhenNull() {
        pack.words = null;
        assertEquals(0, pack.getWordCount());
    }

    @Test
    public void getWordCount_returnsCorrectCount() {
        assertEquals(3, pack.getWordCount());
    }
}