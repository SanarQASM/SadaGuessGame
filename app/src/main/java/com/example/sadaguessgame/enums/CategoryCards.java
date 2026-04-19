package com.example.sadaguessgame.enums;

import android.annotation.SuppressLint;
import android.content.Context;

import com.example.sadaguessgame.R;

public enum CategoryCards {
    ANIMAL("animal"),
    PEOPLE("people"),
    FOOD("food"),
    CHALLENGE("challenge"),
    PLACE("place"),
    OCCUPATION("occupation"),
    BEHAVIOR("behavior"),
    EQUIPMENT("equipment"),
    GENERAL("general"),
    SELECT_ALL("select_all");

    private final String englishName;

    CategoryCards(String englishName) {
        this.englishName = englishName;
    }

    // Get English name for logic/storage/intents
    public String getEnglishName() {
        return englishName;
    }

    // Get localized display name from strings.xml
    public String getDisplayName(Context context) {
        if (context == null) {
            return capitalizeFirst(englishName);
        }

        String resourceName = "category_" + englishName;
        @SuppressLint("DiscouragedApi") int resId = context.getResources().getIdentifier(
                resourceName,
                "string",
                context.getPackageName()
        );

        if (resId == 0) {
            return capitalizeFirst(englishName);
        }

        String localized = context.getString(resId);
        return localized;
    }

    // Fallback: capitalize first letter if string resource not found
    private String capitalizeFirst(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    // Get enum from English name (useful for intents/database)
    public static CategoryCards fromEnglishName(String englishName) {
        for (CategoryCards category : values()) {
            if (category.englishName.equalsIgnoreCase(englishName)) {
                return category;
            }
        }
        return null;
    }
    public int getBackImageRes() {
        switch (this) {
            case ANIMAL:     return R.drawable.animal;
            case BEHAVIOR:   return R.drawable.behavior;
            case CHALLENGE:  return R.drawable.challenge;
            case EQUIPMENT:  return R.drawable.equipment;
            case FOOD:       return R.drawable.food;
            case GENERAL:    return R.drawable.general;
            case OCCUPATION: return R.drawable.occupation;
            case PEOPLE:     return R.drawable.people;
            case PLACE:      return R.drawable.place;
            default:         return 0;
        }
    }
}