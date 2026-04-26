package com.example.sadaguessgame.helper;

import android.content.Context;
import android.content.res.AssetManager;
import androidx.annotation.Nullable;
import com.example.sadaguessgame.data.GameState;
import com.example.sadaguessgame.data.ScoreStorage;
import com.example.sadaguessgame.enums.CategoryCards;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Selects a random asset image from the current game's selected categories.
 * Prevents duplicate card selection within the same game session.
 *
 * FIX: If no categories are saved, falls back to ALL categories automatically.
 */
public class FileSelectingRandom {

    private static FileSelectingRandom instance;
    private final Context context;
    private final Random random = new Random();

    /** All valid category folder names (English, lowercase) */
    private static final List<String> ALL_CATEGORIES = Arrays.asList(
            "animal", "behavior", "challenge", "equipment",
            "food", "general", "occupation", "people", "place"
    );

    private FileSelectingRandom(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized FileSelectingRandom getInstance(Context context) {
        if (instance == null) {
            instance = new FileSelectingRandom(context);
        }
        return instance;
    }

    /**
     * Returns a random asset image path that has NOT been used in this game.
     * Falls back to any random image if all cards have been used.
     * If no categories are selected, uses ALL categories.
     *
     * @return path like "animal/cat.jpg" or null if nothing found
     */
    @Nullable
    public String getRandomAssetImage() {
        GameState currentGame = ScoreStorage.getInstance(context).getCurrentGame();
        if (currentGame == null) return null;

        // ── FIX 1: if no categories saved, use ALL categories ──
        List<String> selectedCategories = currentGame.categories;
        if (selectedCategories == null || selectedCategories.isEmpty()) {
            selectedCategories = ALL_CATEGORIES;
            // Persist the fix so future calls are consistent
            currentGame.categories = new ArrayList<>(ALL_CATEGORIES);
            ScoreStorage.getInstance(context).saveCurrentGame(currentGame);
        }

        AssetManager assetManager = context.getAssets();

        // Collect ALL available paths from selected categories
        List<String> allPaths = new ArrayList<>();
        for (String englishCategoryName : selectedCategories) {
            CategoryCards categoryEnum = CategoryCards.fromEnglishName(englishCategoryName);
            if (categoryEnum == null) continue;
            String assetFolder = categoryEnum.getEnglishName().toLowerCase();
            try {
                String[] files = assetManager.list(assetFolder);
                if (files == null) continue;
                for (String file : files) {
                    allPaths.add(assetFolder + "/" + file);
                }
            } catch (Exception e) {
                // skip unavailable folder
            }
        }

        if (allPaths.isEmpty()) return null;

        // Filter out already-used paths
        List<String> unusedPaths = new ArrayList<>();
        for (String path : allPaths) {
            if (!currentGame.isCardUsed(path)) {
                unusedPaths.add(path);
            }
        }

        String chosen;
        if (unusedPaths.isEmpty()) {
            // All cards used — reset and pick from all
            currentGame.clearUsedCards();
            ScoreStorage.getInstance(context).saveCurrentGame(currentGame);
            Collections.shuffle(allPaths, random);
            chosen = allPaths.get(0);
        } else {
            Collections.shuffle(unusedPaths, random);
            chosen = unusedPaths.get(0);
        }

        // Mark as used
        currentGame.markCardUsed(chosen);
        ScoreStorage.getInstance(context).saveCurrentGame(currentGame);

        return chosen;
    }
}