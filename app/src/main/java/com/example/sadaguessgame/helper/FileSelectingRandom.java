package com.example.sadaguessgame.helper;

import android.content.Context;
import android.content.res.AssetManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.sadaguessgame.data.GameState;
import com.example.sadaguessgame.data.ScoreStorage;
import com.example.sadaguessgame.data.WordPack;
import com.example.sadaguessgame.data.WordPackStorage;
import com.example.sadaguessgame.enums.CategoryCards;
import com.example.sadaguessgame.enums.DifficultyLevel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Selects a random card path (or word) for the current game.
 *
 * Priority:
 *  1. If game.wordPackId >= 0 → use custom WordPack words.
 *  2. Otherwise → use asset images, respecting difficulty subfolder:
 *       assets/{category}/{difficulty}/{file}   ← preferred
 *       assets/{category}/{file}                ← fallback (flat layout)
 *
 * The result is either:
 *  • "animal/easy/cat.jpg"  (asset path)
 *  • "custom::Lion"         (custom word pack word, prefixed with "custom::")
 *
 * CardsActivity checks the prefix to decide how to display the card.
 */
public class FileSelectingRandom {

    public static final String CUSTOM_PREFIX = "custom::";

    private static FileSelectingRandom instance;
    private final Context appContext;
    private final Random  random = new Random();

    private FileSelectingRandom(Context context) {
        appContext = context.getApplicationContext();
    }

    public static synchronized FileSelectingRandom getInstance(Context context) {
        if (instance == null) instance = new FileSelectingRandom(context);
        return instance;
    }

    // ─── Public API ──────────────────────────────────────────────────────────

    /**
     * Returns the next unused card path/word for the current game.
     * Falls back to a random pick from all cards if every card has been used.
     */
    @Nullable
    public String getRandomAssetImage() {
        GameState game = ScoreStorage.getInstance(appContext).getCurrentGame();
        if (game == null) return null;

        // Custom word pack path
        if (game.wordPackId >= 0) {
            return pickFromCustomPack(game);
        }

        // Asset path
        return pickFromAssets(game);
    }

    // ─── Custom pack picker ──────────────────────────────────────────────────

    @Nullable
    private String pickFromCustomPack(@NonNull GameState game) {
        WordPack pack = WordPackStorage.getInstance(appContext)
                .getPackById(game.wordPackId);
        if (pack == null || pack.words == null || pack.words.isEmpty()) return null;

        List<String> unused = new ArrayList<>();
        for (String word : pack.words) {
            String path = CUSTOM_PREFIX + word;
            if (!game.isCardUsed(path)) unused.add(path);
        }

        if (unused.isEmpty()) {
            // All used → reset
            game.clearUsedCards();
            ScoreStorage.getInstance(appContext).saveCurrentGame(game);
            unused.addAll(pack.words);
            Collections.shuffle(unused, random);
            String chosen = CUSTOM_PREFIX + unused.get(0);
            game.markCardUsed(chosen);
            ScoreStorage.getInstance(appContext).saveCurrentGame(game);
            return chosen;
        }

        Collections.shuffle(unused, random);
        String chosen = unused.get(0);
        game.markCardUsed(chosen);
        ScoreStorage.getInstance(appContext).saveCurrentGame(game);
        return chosen;
    }

    // ─── Asset picker ────────────────────────────────────────────────────────

    @Nullable
    private String pickFromAssets(@NonNull GameState game) {
        List<String> categories = game.categories;
        if (categories == null || categories.isEmpty()) return null;

        DifficultyLevel difficulty = DifficultyLevel.fromString(game.difficultyLevel);
        AssetManager assetManager = appContext.getAssets();

        // Build full candidate list
        List<String> allPaths = new ArrayList<>();
        for (String englishName : categories) {
            CategoryCards cat = CategoryCards.fromEnglishName(englishName);
            if (cat == null) continue;
            String folder = cat.getEnglishName().toLowerCase();
            allPaths.addAll(listAssets(assetManager, folder, difficulty));
        }

        if (allPaths.isEmpty()) return null;

        // Filter unused
        List<String> unused = new ArrayList<>();
        for (String p : allPaths) {
            if (!game.isCardUsed(p)) unused.add(p);
        }

        String chosen;
        if (unused.isEmpty()) {
            game.clearUsedCards();
            ScoreStorage.getInstance(appContext).saveCurrentGame(game);
            Collections.shuffle(allPaths, random);
            chosen = allPaths.get(0);
        } else {
            Collections.shuffle(unused, random);
            chosen = unused.get(0);
        }

        game.markCardUsed(chosen);
        ScoreStorage.getInstance(appContext).saveCurrentGame(game);
        return chosen;
    }

    /**
     * Lists asset image paths from:
     *   {folder}/{difficulty}/   (preferred)
     *   {folder}/                (fallback if difficulty subfolder absent)
     */
    @NonNull
    private List<String> listAssets(@NonNull AssetManager am,
                                    @NonNull String folder,
                                    @NonNull DifficultyLevel difficulty) {
        List<String> paths = new ArrayList<>();

        // Try difficulty subfolder first
        String diffFolder = folder + "/" + difficulty.getFolderName();
        String[] diffFiles = safeList(am, diffFolder);
        if (diffFiles != null && diffFiles.length > 0) {
            for (String f : diffFiles) paths.add(diffFolder + "/" + f);
            return paths;
        }

        // Fallback: flat folder (existing layout)
        String[] files = safeList(am, folder);
        if (files != null) {
            for (String f : files) {
                // Skip sub-directory names (easy/medium/hard) that appear in listing
                if (!f.contains(".")) continue;
                paths.add(folder + "/" + f);
            }
        }

        return paths;
    }

    @Nullable
    private String[] safeList(@NonNull AssetManager am, @NonNull String path) {
        try { return am.list(path); } catch (Exception e) { return null; }
    }
}