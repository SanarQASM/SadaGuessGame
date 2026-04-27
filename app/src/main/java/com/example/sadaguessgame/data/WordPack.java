package com.example.sadaguessgame.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user-created word pack.
 *
 * Stored as a JSON array in SharedPreferences under "wordpack_prefs".
 * Each pack has a unique auto-incremented id, a display name, and a list of words.
 *
 * The id is used as wordPackId in GameState to activate a custom pack.
 * id == -1L means built-in assets (default behaviour).
 */
public class WordPack {

    public long        id        = -1L;
    public String      name      = "";
    public String      category  = "custom";   // display category label
    public List<String> words    = new ArrayList<>();
    public long        createdAt = 0L;
    public int         timesPlayed = 0;

    public WordPack() {}

    public WordPack(String name, String category) {
        this.name      = name;
        this.category  = category;
        this.createdAt = System.currentTimeMillis();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    public boolean isValid() {
        return name != null && !name.trim().isEmpty()
                && words != null && words.size() >= 3;
    }

    public int getWordCount() {
        return words == null ? 0 : words.size();
    }

    /** Returns a displayable subtitle, e.g. "12 words · Custom". */
    public String getSubtitle() {
        return getWordCount() + " words · " + category;
    }

    public void addWord(String word) {
        if (words == null) words = new ArrayList<>();
        String trimmed = word.trim();
        if (!trimmed.isEmpty() && !words.contains(trimmed)) {
            words.add(trimmed);
        }
    }

    public void removeWord(String word) {
        if (words != null) words.remove(word);
    }
}