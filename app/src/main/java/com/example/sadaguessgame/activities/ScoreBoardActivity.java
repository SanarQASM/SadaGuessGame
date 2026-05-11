package com.example.sadaguessgame.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import com.example.sadaguessgame.R;
import com.example.sadaguessgame.manager.SoundManager;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import yuku.ambilwarna.AmbilWarnaDialog;

public class ScoreBoardActivity extends BaseActivity {

    public static final String EXTRA_HISTORY = "history_json";

    private FrameLayout  frameGroupOne, frameGroupTwo;
    private TextView     tvGroupOneScore, tvGroupTwoScore;
    private TextView     tvGroupOneName, tvGroupTwoName;

    // Editable name inputs (in header)
    private EditText     etGroupOneName, etGroupTwoName;

    private int scoreGroupA = 0, scoreGroupB = 0;
    private int colorGroupA, colorGroupB;

    private SoundManager soundManager;

    final List<HistoryItem> historyList = new ArrayList<>();
    int currentHistoryIndex = -1;

    public static class HistoryItem {
        public boolean isGroupA;
        public int     scoreAdded;
        public String  groupName;

        public HistoryItem(boolean isGroupA, int scoreAdded, String groupName) {
            this.isGroupA   = isGroupA;
            this.scoreAdded = scoreAdded;
            this.groupName  = groupName;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.score_game_activity);

        soundManager = SoundManager.getInstance(this);

        colorGroupA = ContextCompat.getColor(this, R.color.score_card_a_start);
        colorGroupB = ContextCompat.getColor(this, R.color.score_card_b_start);

        initViews();
        setupButtons();
        applyGroupColors();
    }

    private void initViews() {
        frameGroupOne   = findViewById(R.id.frameGroupOne);
        frameGroupTwo   = findViewById(R.id.frameGroupTwo);
        tvGroupOneScore = findViewById(R.id.tvGroupOneScore);
        tvGroupTwoScore = findViewById(R.id.tvGroupTwoScore);
        tvGroupOneName  = findViewById(R.id.tvGroupOneName);
        tvGroupTwoName  = findViewById(R.id.tvGroupTwoName);
        etGroupOneName  = findViewById(R.id.etGroupOneName);
        etGroupTwoName  = findViewById(R.id.etGroupTwoName);

        tvGroupOneScore.setText(getString(R.string.score_zero));
        tvGroupTwoScore.setText(getString(R.string.score_zero));
    }

    private void setupButtons() {
        View backBtn = findViewById(R.id.back_home_activity_button);
        if (backBtn != null) backBtn.setOnClickListener(v -> finish());

        MaterialButton groupAColorBtn  = findViewById(R.id.groupAColor);
        MaterialButton groupBColorBtn  = findViewById(R.id.groupBColor);
        MaterialButton restartScoreBtn = findViewById(R.id.restartScore);
        MaterialButton undoButton      = findViewById(R.id.undoButton);
        MaterialButton historyButton   = findViewById(R.id.historyButton);

        if (groupAColorBtn  != null) groupAColorBtn.setOnClickListener(v -> openColorPicker(true));
        if (groupBColorBtn  != null) groupBColorBtn.setOnClickListener(v -> openColorPicker(false));
        if (restartScoreBtn != null) restartScoreBtn.setOnClickListener(v -> confirmRestart());
        if (undoButton      != null) undoButton.setOnClickListener(v -> undoLastScore());
        if (historyButton   != null) historyButton.setOnClickListener(v -> openHistory());

        frameGroupOne.setOnClickListener(v -> addScore(true));
        frameGroupTwo.setOnClickListener(v -> addScore(false));

        // Large touch target: contentDescription for accessibility
        frameGroupOne.setContentDescription(getString(R.string.score_card_accessibility_a));
        frameGroupTwo.setContentDescription(getString(R.string.score_card_accessibility_b));
    }

    // ─── Restart with confirmation ────────────────────────────────────────────

    private void confirmRestart() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.restart_confirm_title)
                .setMessage(R.string.restart_confirm_message)
                .setPositiveButton(R.string.restart_confirm_yes, (d, w) -> restartScores())
                .setNegativeButton(R.string.no_button, null)
                .show();
    }

    private void restartScores() {
        scoreGroupA = 0;
        scoreGroupB = 0;
        tvGroupOneScore.setText(getString(R.string.score_zero));
        tvGroupTwoScore.setText(getString(R.string.score_zero));
        historyList.clear();
        currentHistoryIndex = -1;
    }

    // ─── Score management ─────────────────────────────────────────────────────

    private void addScore(boolean isGroupA) {
        // Play click sound
        soundManager.playCorrectAnswer();

        if (isGroupA) {
            scoreGroupA++;
            tvGroupOneScore.setText(String.valueOf(scoreGroupA));
        } else {
            scoreGroupB++;
            tvGroupTwoScore.setText(String.valueOf(scoreGroupB));
        }

        // Sync display names from input fields
        syncGroupNames();

        String groupName = isGroupA ? getGroupAName() : getGroupBName();
        historyList.add(new HistoryItem(isGroupA, 1, groupName));
        currentHistoryIndex = historyList.size() - 1;

        // Animate score bump
        View scoreView = isGroupA ? tvGroupOneScore : tvGroupTwoScore;
        scoreView.animate().scaleX(1.3f).scaleY(1.3f).setDuration(120)
                .withEndAction(() ->
                        scoreView.animate().scaleX(1f).scaleY(1f).setDuration(120).start())
                .start();
    }

    private void undoLastScore() {
        if (currentHistoryIndex < 0 || historyList.isEmpty()) {
            Toast.makeText(this, getString(R.string.score_zero), Toast.LENGTH_SHORT).show();
            return;
        }

        HistoryItem item = historyList.get(currentHistoryIndex);
        if (item.isGroupA) {
            scoreGroupA = Math.max(0, scoreGroupA - item.scoreAdded);
            tvGroupOneScore.setText(String.valueOf(scoreGroupA));
        } else {
            scoreGroupB = Math.max(0, scoreGroupB - item.scoreAdded);
            tvGroupTwoScore.setText(String.valueOf(scoreGroupB));
        }

        historyList.remove(currentHistoryIndex);
        currentHistoryIndex--;
    }

    private void syncGroupNames() {
        if (tvGroupOneName != null && etGroupOneName != null) {
            String n = etGroupOneName.getText().toString().trim();
            if (!n.isEmpty()) tvGroupOneName.setText(n);
        }
        if (tvGroupTwoName != null && etGroupTwoName != null) {
            String n = etGroupTwoName.getText().toString().trim();
            if (!n.isEmpty()) tvGroupTwoName.setText(n);
        }
    }

    private String getGroupAName() {
        if (etGroupOneName == null) return getString(R.string.group_a_color);
        String name = etGroupOneName.getText().toString().trim();
        return TextUtils.isEmpty(name) ? getString(R.string.group_a_color) : name;
    }

    private String getGroupBName() {
        if (etGroupTwoName == null) return getString(R.string.group_b_color);
        String name = etGroupTwoName.getText().toString().trim();
        return TextUtils.isEmpty(name) ? getString(R.string.group_b_color) : name;
    }

    // ─── History ──────────────────────────────────────────────────────────────

    private void openHistory() {
        if (historyList.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_history_yet), Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, ScoreBoardHistoryActivity.class);
        intent.putExtra(EXTRA_HISTORY, new Gson().toJson(historyList));
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    // ─── Color picker ─────────────────────────────────────────────────────────

    private void openColorPicker(boolean isGroupA) {
        AmbilWarnaDialog picker = new AmbilWarnaDialog(this,
                isGroupA ? colorGroupA : colorGroupB,
                new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        if (isGroupA) {
                            colorGroupA = color;
                        } else {
                            colorGroupB = color;
                        }
                        applyGroupColors();
                    }
                    @Override public void onCancel(AmbilWarnaDialog dialog) {}
                });
        picker.show();
    }

    /**
     * Apply selected colors to both score-card frames and adjust text color
     * for readability (dark text on light bg, light text on dark bg).
     */
    private void applyGroupColors() {
        applyColorToFrame(frameGroupOne, colorGroupA, tvGroupOneName, tvGroupOneScore);
        applyColorToFrame(frameGroupTwo, colorGroupB, tvGroupTwoName, tvGroupTwoScore);
    }

    private void applyColorToFrame(FrameLayout frame, int color,
                                   TextView nameView, TextView scoreView) {
        Drawable bg = frame.getBackground();
        if (bg instanceof GradientDrawable) {
            ((GradientDrawable) bg.mutate()).setColor(color);
        } else {
            frame.setBackgroundColor(color);
        }

        // Luminance-based text color for accessibility
        int textColor = isLightColor(color) ? Color.BLACK : Color.WHITE;
        if (nameView  != null) nameView.setTextColor(textColor);
        if (scoreView != null) scoreView.setTextColor(textColor);
    }

    /**
     * Returns true if the color is light (should use dark text).
     * Uses relative luminance formula (WCAG).
     */
    private boolean isLightColor(int color) {
        double r = Color.red(color)   / 255.0;
        double g = Color.green(color) / 255.0;
        double b = Color.blue(color)  / 255.0;
        double luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b;
        return luminance > 0.5;
    }
}