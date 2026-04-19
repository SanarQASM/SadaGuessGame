package com.example.sadaguessgame.helper;

import android.content.Context;
import android.content.res.AssetManager;

import com.example.sadaguessgame.data.GameState;
import com.example.sadaguessgame.data.ScoreStorage;
import com.example.sadaguessgame.enums.CategoryCards;

import java.util.List;
import java.util.Random;

public class FileSelectingRandom {

    private static FileSelectingRandom instance;

    private final GameState currentGame;
    private final Context context;

    // ---------------- CONSTRUCTOR ----------------
    private FileSelectingRandom(Context context) {
        // Use application context to avoid memory leaks
        this.context = context.getApplicationContext();
        // Initialize ScoreStorage and GameState safely
        this.currentGame = ScoreStorage.getInstance(context).getCurrentGame();
    }

    // ---------------- GET INSTANCE ----------------
    public static synchronized FileSelectingRandom getInstance(Context context) {
        if (instance == null) {
            instance = new FileSelectingRandom(context);
        }
        return instance;
    }

    // ---------------- GET RANDOM ASSET IMAGE ----------------
    public String getRandomAssetImage() {
        List<String> selectedCategories = currentGame.categories;

        if (selectedCategories == null || selectedCategories.isEmpty()) {
            return null;
        }

        AssetManager assetManager = context.getAssets();
        Random random = new Random();

        try {
            // Pick a random category (English name from GameState)
            String englishCategoryName = selectedCategories.get(random.nextInt(selectedCategories.size()));

            // Convert to CategoryCards enum to get proper English name
            CategoryCards categoryEnum = CategoryCards.fromEnglishName(englishCategoryName);

            if (categoryEnum == null) {
                return null;
            }

            // Use the English name directly as the asset folder name
            String assetFolder = categoryEnum.getEnglishName().toLowerCase();


            // Get list of files in that category folder
            String[] files = assetManager.list(assetFolder);

            if (files == null || files.length == 0) {
                return null;
            }

            // Pick a random file
            String file = files[random.nextInt(files.length)];

            return assetFolder + "/" + file;

        } catch (Exception e) {
            return null;
        }
    }
}