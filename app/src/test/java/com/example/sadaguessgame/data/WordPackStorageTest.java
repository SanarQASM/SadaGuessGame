package com.example.sadaguessgame.data;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Robolectric tests for WordPackStorage.
 *
 * Place at:
 *   app/src/test/java/com/example/sadaguessgame/data/WordPackStorageTest.java
 *
 * Run with:
 *   ./gradlew :app:test
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class WordPackStorageTest {

    private WordPackStorage storage;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        try {
            java.lang.reflect.Field f = WordPackStorage.class.getDeclaredField("instance");
            f.setAccessible(true);
            f.set(null, null);
        } catch (Exception ignored) {}
        storage = WordPackStorage.getInstance(context);
        // Delete all existing packs
        for (WordPack p : storage.getAllPacks()) storage.delete(p.id);
    }

    // ── save / getAllPacks ────────────────────────────────────────────────────

    @Test
    public void save_assignsIdToNewPack() {
        WordPack pack = buildPack("Animals");
        WordPack saved = storage.save(pack);
        assertNotEquals(-1L, saved.id);
    }

    @Test
    public void save_appendsToList() {
        storage.save(buildPack("Animals"));
        storage.save(buildPack("Food"));
        assertEquals(2, storage.getAllPacks().size());
    }

    @Test
    public void save_updatesExistingPack() {
        WordPack pack = storage.save(buildPack("Animals"));
        pack.name = "Updated";
        storage.save(pack);
        assertEquals(1, storage.getAllPacks().size());
        assertEquals("Updated", storage.getAllPacks().get(0).name);
    }

    // ── getPackById ──────────────────────────────────────────────────────────

    @Test
    public void getPackById_returnsCorrectPack() {
        WordPack saved = storage.save(buildPack("Animals"));
        WordPack found = storage.getPackById(saved.id);
        assertNotNull(found);
        assertEquals("Animals", found.name);
    }

    @Test
    public void getPackById_returnsNullForUnknownId() {
        assertNull(storage.getPackById(99999L));
    }

    // ── delete ───────────────────────────────────────────────────────────────

    @Test
    public void delete_removesPackFromList() {
        WordPack saved = storage.save(buildPack("Animals"));
        boolean result = storage.delete(saved.id);
        assertTrue(result);
        assertTrue(storage.getAllPacks().isEmpty());
    }

    @Test
    public void delete_returnsFalseForNonExistentId() {
        assertFalse(storage.delete(99999L));
    }

    // ── importFromJson / exportAsJson ────────────────────────────────────────

    @Test
    public void exportAndImportRoundtrip_preservesWords() {
        WordPack original = storage.save(buildPack("Animals"));
        String json = storage.exportAsJson(original.id);

        // delete original to get a fresh ID on import
        storage.delete(original.id);
        WordPack imported = storage.importFromJson(json);

        assertNotNull(imported);
        assertEquals(original.name, imported.name);
        assertEquals(original.words.size(), imported.words.size());
    }

    @Test
    public void importFromJson_returnsNullForInvalidJson() {
        assertNull(storage.importFromJson("{not valid json}"));
    }

    @Test
    public void importFromJson_returnsNullForPackWithTooFewWords() {
        // Create JSON with fewer than 3 words
        String tooFewJson = "{\"id\":1,\"name\":\"Test\",\"category\":\"c\",\"words\":[\"one\"]}";
        assertNull(storage.importFromJson(tooFewJson));
    }

    // ── incrementTimesPlayed ─────────────────────────────────────────────────

    @Test
    public void incrementTimesPlayed_incrementsCorrectly() {
        WordPack saved = storage.save(buildPack("Animals"));
        storage.incrementTimesPlayed(saved.id);
        storage.incrementTimesPlayed(saved.id);
        WordPack updated = storage.getPackById(saved.id);
        assertNotNull(updated);
        assertEquals(2, updated.timesPlayed);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private WordPack buildPack(String name) {
        WordPack p = new WordPack(name, "Test");
        p.words.add("Lion");
        p.words.add("Tiger");
        p.words.add("Bear");
        return p;
    }
}