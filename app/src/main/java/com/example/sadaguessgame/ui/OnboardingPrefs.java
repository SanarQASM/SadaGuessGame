package com.example.sadaguessgame.ui;

import android.content.Context;

public class OnboardingPrefs {

    private static final String PREF_NAME = "onboarding_prefs";
    private static final String KEY_DONE = "onboarding_done";

    public static boolean isOnboardingDone(Context context) {
        return context
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_DONE, false);
    }

    public static void setOnboardingDone(Context context) {
        context
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_DONE, true)
                .apply();
    }
}
