package com.example.sadaguessgame.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.sadaguessgame.R;
import com.example.sadaguessgame.activities.MainActivity;
import com.example.sadaguessgame.activities.PrivacyPolicyActivity;
import com.example.sadaguessgame.activities.WordPackActivity;
import com.example.sadaguessgame.enums.SoundTheme;
import com.example.sadaguessgame.manager.SoundManager;
import com.example.sadaguessgame.manager.VoiceClueManager;
import com.example.sadaguessgame.ui.NavigationActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * Settings screen.
 *
 * NEW in v2:
 *  • Sound theme selector (Classic / Dabke / Silent) via RadioGroup.
 *  • Global voice clue toggle (persisted in SharedPreferences).
 *  • "Manage Word Packs" button → WordPackActivity.
 */
public class SettingFragment extends BaseFragment {

    private SwitchMaterial   darkModeSwitch;
    private SwitchMaterial   voiceClueSwitch;
    private Spinner          languageSpinner;
    private RadioGroup       soundThemeGroup;
    private RadioButton      rbClassic, rbDabke, rbSilent;
    private TextView         appVersion;
    private MaterialButton   learnPlay, privacyPolicyBtn, manageWordPacksBtn;

    private SharedPreferences prefs;
    private boolean           spinnerInitialized = false;

    public SettingFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.setting_fragment, container, false);
        prefs = requireContext().getSharedPreferences("settings_prefs", Context.MODE_PRIVATE);

        bindViews(view);
        setupLearnPlay();
        setupDarkMode();
        setupLanguageSpinner();
        setupSoundTheme();
        setupVoiceClue();
        setupWordPacks();
        setupAppVersion();
        setupPrivacyPolicy();

        return view;
    }

    // ─── Bind ────────────────────────────────────────────────────────────────

    private void bindViews(View view) {
        darkModeSwitch    = view.findViewById(R.id.customSwitch);
        voiceClueSwitch   = view.findViewById(R.id.voiceClueSettingSwitch);
        languageSpinner   = view.findViewById(R.id.languageSpinner);
        soundThemeGroup   = view.findViewById(R.id.soundThemeGroup);
        rbClassic         = view.findViewById(R.id.rbClassic);
        rbDabke           = view.findViewById(R.id.rbDabke);
        rbSilent          = view.findViewById(R.id.rbSilent);
        appVersion        = view.findViewById(R.id.appVersion);
        learnPlay         = view.findViewById(R.id.learnPlay);
        privacyPolicyBtn  = view.findViewById(R.id.privacyPolicyBtn);
        manageWordPacksBtn= view.findViewById(R.id.btnManageWordPacks);
    }

    // ─── Learn / Privacy / Word packs ────────────────────────────────────────

    private void setupLearnPlay() {
        if (learnPlay != null)
            learnPlay.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), NavigationActivity.class)));
    }

    private void setupPrivacyPolicy() {
        if (privacyPolicyBtn != null)
            privacyPolicyBtn.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), PrivacyPolicyActivity.class)));
    }

    private void setupWordPacks() {
        if (manageWordPacksBtn != null)
            manageWordPacksBtn.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), WordPackActivity.class)));
    }

    // ─── Dark mode ───────────────────────────────────────────────────────────

    private void setupDarkMode() {
        if (darkModeSwitch == null) return;
        boolean isDark = prefs.getBoolean("dark_mode", false);
        darkModeSwitch.setChecked(isDark);
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        darkModeSwitch.setOnCheckedChangeListener((btn, checked) -> {
            prefs.edit().putBoolean("dark_mode", checked).apply();
            AppCompatDelegate.setDefaultNightMode(
                    checked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });
    }

    // ─── Language ────────────────────────────────────────────────────────────

    private void setupLanguageSpinner() {
        if (languageSpinner == null) return;
        String[] languages = getResources().getStringArray(R.array.language_txt);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);

        String saved = prefs.getString("language", "en");
        int pos = saved.equals("ku") ? 1 : saved.equals("kmr") ? 2 : 0;
        languageSpinner.setSelection(pos, false);

        languageSpinner.post(() -> {
            spinnerInitialized = true;
            languageSpinner.setOnItemSelectedListener(
                    new android.widget.AdapterView.OnItemSelectedListener() {
                        @Override public void onItemSelected(
                                android.widget.AdapterView<?> parent, View v, int position, long id) {
                            if (!spinnerInitialized) return;
                            String lang = position == 1 ? "ku" : position == 2 ? "kmr" : "en";
                            setLanguage(lang);
                        }
                        @Override public void onNothingSelected(android.widget.AdapterView<?> p) {}
                    });
        });
    }

    private void setLanguage(String langCode) {
        String current = prefs.getString("language", "en");
        if (current.equals(langCode)) return;
        prefs.edit().putString("language", langCode).apply();
        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    // ─── Sound theme (NEW) ───────────────────────────────────────────────────

    private void setupSoundTheme() {
        if (soundThemeGroup == null) return;
        SoundManager sm = SoundManager.getInstance(requireContext());
        SoundTheme current = sm.getCurrentTheme();

        switch (current) {
            case DABKE:  if (rbDabke  != null) rbDabke.setChecked(true);  break;
            case SILENT: if (rbSilent != null) rbSilent.setChecked(true); break;
            default:     if (rbClassic!= null) rbClassic.setChecked(true);break;
        }

        soundThemeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            SoundTheme theme;
            if (checkedId == R.id.rbDabke)       theme = SoundTheme.DABKE;
            else if (checkedId == R.id.rbSilent) theme = SoundTheme.SILENT;
            else                                  theme = SoundTheme.CLASSIC;
            sm.setTheme(theme);
        });
    }

    // ─── Voice clue global toggle (NEW) ──────────────────────────────────────

    private void setupVoiceClue() {
        if (voiceClueSwitch == null) return;
        VoiceClueManager vcm = VoiceClueManager.getInstance(requireContext());
        voiceClueSwitch.setChecked(vcm.isEnabled());
        voiceClueSwitch.setOnCheckedChangeListener((btn, checked) -> {
            vcm.setEnabled(checked);
            if (!checked) vcm.shutdown();
        });
    }

    // ─── App version ─────────────────────────────────────────────────────────

    @SuppressLint("SetTextI18n")
    private void setupAppVersion() {
        if (appVersion == null) return;
        try {
            String v = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0).versionName;
            appVersion.setText(getString(R.string.app_version_prefix) + " " + v);
        } catch (Exception e) {
            appVersion.setText(getString(R.string.app_version_prefix) + " 1.0");
        }
    }
}