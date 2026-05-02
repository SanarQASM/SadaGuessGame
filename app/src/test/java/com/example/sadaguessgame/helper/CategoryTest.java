package com.example.sadaguessgame.helper;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Pure-Java unit tests for the Category model and the selection-logic parts
 * of CategoryAdapter (the parts that don't need Android context).
 *
 * Full CategoryAdapter tests (with RecyclerView) belong in androidTest/.
 *
 * Place at:
 *   app/src/test/java/com/example/sadaguessgame/helper/CategoryTest.java
 *
 * Run with:
 *   ./gradlew :app:test
 */
public class CategoryTest {

    // ── Category model ───────────────────────────────────────────────────────

    @Test
    public void constructor_twoArg_setsEnglishNameFromTitle() {
        Category c = new Category("Animal", 0);
        assertEquals("animal", c.englishName);
    }

    @Test
    public void constructor_twoArg_replacesSpaces() {
        Category c = new Category("Select All", 0);
        assertEquals("select_all", c.englishName);
    }

    @Test
    public void constructor_threeArg_usesProvidedEnglishName() {
        Category c = new Category("Hayvan", "animal", 0);
        assertEquals("animal", c.englishName);
        assertEquals("Hayvan", c.title);
    }

    @Test
    public void imageRes_storedCorrectly() {
        Category c = new Category("Animal", "animal", 42);
        assertEquals(42, c.imageRes);
    }
}