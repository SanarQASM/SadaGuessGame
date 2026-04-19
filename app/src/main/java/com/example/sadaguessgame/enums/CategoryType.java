package com.example.sadaguessgame.enums;

import com.example.sadaguessgame.R;

public enum CategoryType {

    ANIMAL("Animal", R.drawable.animal),
    PEOPLE("People", R.drawable.people),
    FOOD("Food", R.drawable.food),
    CHALLENGE("Challenge", R.drawable.challenge),
    PLACE("Place", R.drawable.place),
    OCCUPATION("Occupation", R.drawable.occupation),
    BEHAVIOR("Behavior", R.drawable.behavior),
    EQUIPMENT("Equipment", R.drawable.equipment),
    GENERAL("General", R.drawable.general),
    ALL("All", R.drawable.all);

    public final String title;
    public final int imageRes;

    CategoryType(String title, int imageRes) {
        this.title = title;
        this.imageRes = imageRes;
    }
    public int getDrawableId() {
        return imageRes;
    }
}

