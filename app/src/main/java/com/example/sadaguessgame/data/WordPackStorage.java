package com.example.sadaguessgame.data;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton repository for user-created WordPacks.
 * Backed by SharedPreferences + Gson — no external DB dependency required.
 *
 * IDs are generated as System.currentTimeMillis() on first save.
 */
public class WordPackStorage {

    private static final String PREFS_NAME  = "wordpack_prefs";
    private static final String KEY_PACKS   = "packs";

    private static volatile WordPackStorage instance;

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    private WordPackStorage(@NonNull Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static WordPackStorage getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (WordPackStorage.class) {
                if (instance == null) instance = new WordPackStorage(context);
            }
        }
        return instance;
    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    @NonNull
    public synchronized List<WordPack> getAllPacks() {
        String json = prefs.getString(KEY_PACKS, null);
        if (json == null) return new ArrayList<>();
        try {
            Type type = new TypeToken<List<WordPack>>() {}.getType();
            List<WordPack> packs = gson.fromJson(json, type);
            return packs != null ? packs : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Nullable
    public synchronized WordPack getPackById(long id) {
        for (WordPack p : getAllPacks()) {
            if (p.id == id) return p;
        }
        return null;
    }

    // ─── Write ────────────────────────────────────────────────────────────────

    /**
     * Inserts or updates a pack.  Assigns an id if new (id == -1).
     * @return the saved pack (with id populated).
     */
    @NonNull
    public synchronized WordPack save(@NonNull WordPack pack) {
        List<WordPack> packs = getAllPacks();
        if (pack.id == -1L) {
            pack.id = System.currentTimeMillis();
            packs.add(pack);
        } else {
            boolean found = false;
            for (int i = 0; i < packs.size(); i++) {
                if (packs.get(i).id == pack.id) {
                    packs.set(i, pack);
                    found = true;
                    break;
                }
            }
            if (!found) packs.add(pack);
        }
        persist(packs);
        return pack;
    }

    public synchronized boolean delete(long id) {
        List<WordPack> packs = getAllPacks();
        boolean removed = packs.removeIf(p -> p.id == id);
        if (removed) persist(packs);
        return removed;
    }

    public synchronized void incrementTimesPlayed(long id) {
        List<WordPack> packs = getAllPacks();
        for (WordPack p : packs) {
            if (p.id == id) { p.timesPlayed++; break; }
        }
        persist(packs);
    }

    /** Export a single pack as JSON string (for sharing via WhatsApp/Telegram). */
    @NonNull
    public String exportAsJson(long id) {
        WordPack pack = getPackById(id);
        return pack != null ? gson.toJson(pack) : "{}";
    }

    /**
     * Import a pack from a JSON string received from another device.
     * The imported pack gets a new id to avoid conflicts.
     * @return the saved pack, or null if JSON was invalid.
     */
    @Nullable
    public synchronized WordPack importFromJson(@NonNull String json) {
        try {
            WordPack pack = gson.fromJson(json, WordPack.class);
            if (pack == null || !pack.isValid()) return null;
            pack.id = -1L; // force new ID
            return save(pack);
        } catch (Exception e) {
            return null;
        }
    }

    // ─── Private ──────────────────────────────────────────────────────────────

    private void persist(List<WordPack> packs) {
        prefs.edit().putString(KEY_PACKS, gson.toJson(packs)).apply();
    }
}