package com.example.sadaguessgame.helper;

import android.content.Context;
import android.content.res.AssetManager;
import androidx.annotation.Nullable;
import com.example.sadaguessgame.data.GameState;
import com.example.sadaguessgame.data.ScoreStorage;
import com.example.sadaguessgame.enums.CategoryCards;
import java.util.List;
import java.util.Random;

/**
 * Selects a random asset image from the current game's selected categories.
 * NOTE: GameState is fetched fresh on each call — not cached — to avoid stale data.
 */
public class FileSelectingRandom {

    private static FileSelectingRandom instance;
    private final Context context;
    private final Random random = new Random();

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
     * Returns a random asset image path for the current game's selected categories.
     * Fetches GameState fresh on every call.
     *
     * @return path like "animal/cat.jpg" or null if nothing found
     */
    @Nullable
    public String getRandomAssetImage() {
        // Always fetch fresh — fixes stale singleton bug
        GameState currentGame = ScoreStorage.getInstance(context).getCurrentGame();

        if (currentGame == null) return null;

        List<String> selectedCategories = currentGame.categories;
        if (selectedCategories == null || selectedCategories.isEmpty()) return null;

        AssetManager assetManager = context.getAssets();

        try {
            String englishCategoryName = selectedCategories.get(
                    random.nextInt(selectedCategories.size())
            );

            CategoryCards categoryEnum = CategoryCards.fromEnglishName(englishCategoryName);
            if (categoryEnum == null) return null;

            String assetFolder = categoryEnum.getEnglishName().toLowerCase();
            String[] files = assetManager.list(assetFolder);

            if (files == null || files.length == 0) return null;

            return assetFolder + "/" + files[random.nextInt(files.length)];

        } catch (Exception e) {
            return null;
        }
    }
}