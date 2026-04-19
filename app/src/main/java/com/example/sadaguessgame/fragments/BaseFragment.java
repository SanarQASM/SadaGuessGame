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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Apply dark mode before creating view
        applyDarkMode();
        // This will be overridden by child fragments
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Force layout direction on fragment views
        forceFragmentLayoutDirection(view);
    }

    private void forceFragmentLayoutDirection(View rootView) {
        if (rootView == null || getContext() == null) return;

        SharedPreferences prefs = requireContext().getSharedPreferences("settings_prefs", Context.MODE_PRIVATE);
        String langCode = prefs.getString("language", "en");

        int direction = (langCode.equals("ku") || langCode.equals("kmr"))
                ? View.LAYOUT_DIRECTION_RTL
                : View.LAYOUT_DIRECTION_LTR;

        // Force direction recursively on all views
        forceLayoutDirectionRecursive(rootView, direction);
    }

    private void forceLayoutDirectionRecursive(View view, int direction) {
        if (view == null) return;

        view.setLayoutDirection(direction);

        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                forceLayoutDirectionRecursive(viewGroup.getChildAt(i), direction);
            }
        }
    }

    // Apply saved dark mode preference
    private void applyDarkMode() {
        if (getContext() == null) return;

        SharedPreferences prefs = requireContext().getSharedPreferences("settings_prefs", Context.MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);

        AppCompatDelegate.setDefaultNightMode(
                isDarkMode
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}