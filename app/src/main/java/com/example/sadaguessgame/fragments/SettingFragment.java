package com.example.sadaguessgame.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.sadaguessgame.R;
import com.example.sadaguessgame.activities.PrivacyPolicyActivity;
import com.example.sadaguessgame.ui.NavigationActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Locale;

import yuku.ambilwarna.BuildConfig;

public class SettingFragment extends BaseFragment {

    private SwitchMaterial customSwitch;
    private Spinner languageSpinner;
    private TextView appVersion;
    private MaterialButton learnPlay, privacyPolicyBtn;

    private SharedPreferences prefs;

    public SettingFragment() {}

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.setting_fragment, container, false);

        prefs = requireContext().getSharedPreferences("settings_prefs", Context.MODE_PRIVATE);

        // ------------------- VIEWS -------------------
        customSwitch = view.findViewById(R.id.customSwitch);
        languageSpinner = view.findViewById(R.id.languageSpinner);
        appVersion = view.findViewById(R.id.appVersion);
        learnPlay = view.findViewById(R.id.learnPlay);
        privacyPolicyBtn = view.findViewById(R.id.privacyPolicyBtn);

        setupLearnPlay();
        setupDarkMode();
        setupLanguageSpinner();
        setupAppVersion();
        setupPrivacyPolicy();

        return view;
    }

    // --------------------------------------------------
    // Learn How To Play
    // --------------------------------------------------
    private void setupLearnPlay() {
        learnPlay.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), NavigationActivity.class);
            startActivity(intent);
        });
    }

    // --------------------------------------------------
    // Dark Mode
    // --------------------------------------------------
    private void setupDarkMode() {
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        customSwitch.setChecked(isDarkMode);

        AppCompatDelegate.setDefaultNightMode(
                isDarkMode
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );

        customSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("dark_mode", isChecked).apply();

            AppCompatDelegate.setDefaultNightMode(
                    isChecked
                            ? AppCompatDelegate.MODE_NIGHT_YES
                            : AppCompatDelegate.MODE_NIGHT_NO
            );
        });
    }

    // --------------------------------------------------
    // Language Spinner
    // --------------------------------------------------
    private void setupLanguageSpinner() {
        String[] languages = getResources().getStringArray(R.array.language_txt);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                languages
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);

        String savedLang = prefs.getString("language", "en");
        int position = savedLang.equals("ku") ? 1 :
                savedLang.equals("kmr") ? 2 : 0;

        languageSpinner.setSelection(position, false);

        languageSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int pos, long id) {
                switch (pos) {
                    case 0:
                        setLanguage("en");
                        break;
                    case 1:
                        setLanguage("ku");   // Sorani
                        break;
                    case 2:
                        setLanguage("kmr");  // Badini / Kurmanji
                        break;
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void setLanguage(String langCode) {
        String currentLang = prefs.getString("language", "en");
        if (currentLang.equals(langCode)) return;

        prefs.edit().putString("language", langCode).apply();

        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        requireContext().getResources().updateConfiguration(
                config,
                requireContext().getResources().getDisplayMetrics()
        );

        // Restart activity to apply language everywhere
        Intent intent = requireActivity().getIntent();
        requireActivity().finish();
        startActivity(intent);
    }

    // --------------------------------------------------
    // App Version
    // --------------------------------------------------
    @SuppressLint("SetTextI18n")
    private void setupAppVersion() {
        appVersion.setText("Version " + BuildConfig.VERSION_NAME);
    }

    // --------------------------------------------------
    // Privacy Policy
    // --------------------------------------------------
    private void setupPrivacyPolicy() {
        privacyPolicyBtn.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), PrivacyPolicyActivity.class);
            startActivity(intent);
        });
    }
}
