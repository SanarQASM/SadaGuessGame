package com.example.sadaguessgame.enums;


import com.example.sadaguessgame.R;

public enum SliderImage {

    HOME_PAGE(R.drawable.home_page_tip),
    RECENTLY_PAGE(R.drawable.recently_page_tip),
    SETTING_PAGE(R.drawable.setting_page_tip);

    private final int drawableResId;

    SliderImage(int drawableResId) {
        this.drawableResId = drawableResId;
    }

    public int getDrawableResId() {
        return drawableResId;
    }
}
