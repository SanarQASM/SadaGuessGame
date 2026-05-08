package com.example.sadaguessgame.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

public class BaseFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        applyDarkMode();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        forceFragmentLayoutDirection(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null) forceFragmentLayoutDirection(getView());
    }

    // ─── Direction ────────────────────────────────────────────────────────────

    protected void forceFragmentLayoutDirection(View rootView) {
        if (rootView == null || getContext() == null) return;
        int direction = isRtlLanguage()
                ? View.LAYOUT_DIRECTION_RTL
                : View.LAYOUT_DIRECTION_LTR;
        applyDirectionRecursive(rootView, direction);
    }

    private void applyDirectionRecursive(View view, int direction) {
        if (view == null) return;
        view.setLayoutDirection(direction);
        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                applyDirectionRecursive(vg.getChildAt(i), direction);
            }
        }
    }

    protected boolean isRtlLanguage() {
        if (getContext() == null) return false;
        String lang = getContext()
                .getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
                .getString("language", "en");
        return lang.equals("ku") || lang.equals("kmr");
    }

    // ─── Dark Mode ────────────────────────────────────────────────────────────

    private void applyDarkMode() {
        if (getContext() == null) return;
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("settings_prefs", Context.MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }
}