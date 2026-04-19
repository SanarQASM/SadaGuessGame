package com.example.sadaguessgame.helper;

public class Category {
    public String title;          // Localized display name
    public String englishName;    // English name for logic
    public int imageRes;          // drawable resource id

    public Category(String title, String englishName, int imageRes) {
        this.title = title;
        this.englishName = englishName;
        this.imageRes = imageRes;
    }

    // Convenience constructor (if you only need display name)
    public Category(String title, int imageRes) {
        this.title = title;
        this.englishName = title.toLowerCase().replace(" ", "_");
        this.imageRes = imageRes;
    }
}