package com.example.sadaguessgame.enums;

import com.example.sadaguessgame.R;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for CategoryCards enum.
 *
 * Place at:
 *   app/src/test/java/com/example/sadaguessgame/enums/CategoryCardsTest.java
 *
 * Run with:
 *   ./gradlew :app:test
 */
public class CategoryCardsTest {

    // ── fromEnglishName ──────────────────────────────────────────────────────

    @Test
    public void fromEnglishName_returnsCorrectEnum() {
        assertEquals(CategoryCards.ANIMAL, CategoryCards.fromEnglishName("animal"));
    }

    @Test
    public void fromEnglishName_caseInsensitive() {
        assertEquals(CategoryCards.FOOD, CategoryCards.fromEnglishName("FOOD"));
    }

    @Test
    public void fromEnglishName_returnsNullForUnknown() {
        assertNull(CategoryCards.fromEnglishName("unknown_category"));
    }

    @Test
    public void fromEnglishName_returnsNullForNull() {
        assertNull(CategoryCards.fromEnglishName(null));
    }

    // ── getEnglishName ───────────────────────────────────────────────────────

    @Test
    public void getEnglishName_returnsLowercaseString() {
        assertEquals("animal", CategoryCards.ANIMAL.getEnglishName());
        assertEquals("people", CategoryCards.PEOPLE.getEnglishName());
        assertEquals("select_all", CategoryCards.SELECT_ALL.getEnglishName());
    }

    // ── getBackImageRes ──────────────────────────────────────────────────────

    @Test
    public void getBackImageRes_returnsNonZeroForKnownCategories() {
        assertNotEquals(0, CategoryCards.ANIMAL.getBackImageRes());
        assertNotEquals(0, CategoryCards.FOOD.getBackImageRes());
        assertNotEquals(0, CategoryCards.CHALLENGE.getBackImageRes());
        assertNotEquals(0, CategoryCards.PLACE.getBackImageRes());
    }

    @Test
    public void getBackImageRes_returnsZeroForSelectAll() {
        assertEquals(0, CategoryCards.SELECT_ALL.getBackImageRes());
    }

    // ── roundtrip ────────────────────────────────────────────────────────────

    @Test
    public void allCategoriesRoundtripThroughEnglishName() {
        for (CategoryCards cat : CategoryCards.values()) {
            if (cat == CategoryCards.SELECT_ALL) continue;
            assertEquals(cat, CategoryCards.fromEnglishName(cat.getEnglishName()));
        }
    }
}