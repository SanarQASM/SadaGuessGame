package com.example.sadaguessgame.activities;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sadaguessgame.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import yuku.ambilwarna.AmbilWarnaDialog;

public class ScoreBoardActivity extends BaseActivity {

    private FrameLayout frameGroupOne, frameGroupTwo;
    private TextView tvGroupOneScore, tvGroupTwoScore, tvGroupOneName, tvGroupTwoName;
    private LinearLayout historyContainerLinear;

    private int scoreGroupA = 0, scoreGroupB = 0;
    private int colorGroupA = Color.RED, colorGroupB = Color.BLUE;

    // ✅ HISTORY DATA (not views)
    private final List<HistoryItem> historyList = new ArrayList<>();
    private int currentHistoryIndex = -1;

    static class HistoryItem {
        boolean isGroupA;

        HistoryItem(boolean isGroupA) {
            this.isGroupA = isGroupA;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.score_game_activity);

        frameGroupOne = findViewById(R.id.frameGroupOne);
        frameGroupTwo = findViewById(R.id.frameGroupTwo);
        tvGroupOneScore = findViewById(R.id.tvGroupOneScore);
        tvGroupTwoScore = findViewById(R.id.tvGroupTwoScore);
        tvGroupOneName = findViewById(R.id.tvGroupOneName);
        tvGroupTwoName = findViewById(R.id.tvGroupTwoName);
        historyContainerLinear = findViewById(R.id.historyContainerLinear);

        MaterialButton groupAColorBtn = findViewById(R.id.groupAColor);
        MaterialButton groupBColorBtn = findViewById(R.id.groupBColor);
        MaterialButton restartScoreBtn = findViewById(R.id.restartScore);
        MaterialButton undoButton = findViewById(R.id.undoButton);

        findViewById(R.id.back_home_activity_button).setOnClickListener(v -> finish());

        groupAColorBtn.setOnClickListener(v -> openColorPicker(true));
        groupBColorBtn.setOnClickListener(v -> openColorPicker(false));

        frameGroupOne.setOnClickListener(v -> addScore(true));
        frameGroupTwo.setOnClickListener(v -> addScore(false));

        restartScoreBtn.setOnClickListener(v -> restartScores());
        undoButton.setOnClickListener(v -> showPreviousHistory());
    }

    private void addScore(boolean isGroupA) {
        if (isGroupA) {
            scoreGroupA++;
            tvGroupOneScore.setText(String.valueOf(scoreGroupA));
        } else {
            scoreGroupB++;
            tvGroupTwoScore.setText(String.valueOf(scoreGroupB));
        }

        historyList.add(new HistoryItem(isGroupA));
        currentHistoryIndex = historyList.size() - 1;
        showCurrentHistory();
    }

    @SuppressLint("SetTextI18n")
    private void showCurrentHistory() {
        historyContainerLinear.removeAllViews();

        if (historyList.isEmpty() || currentHistoryIndex < 0) {
            return;
        }

        HistoryItem item = historyList.get(currentHistoryIndex);

        View view = LayoutInflater.from(this)
                .inflate(R.layout.item_score_linear, historyContainerLinear, false);

        TextView tvIndex = view.findViewById(R.id.tvIndex);
        TextView tvGroup = view.findViewById(R.id.tvGroup);

        tvIndex.setText((currentHistoryIndex + 1) + " -");
        tvGroup.setText(item.isGroupA
                ? getString(R.string.group_a_color)
                : getString(R.string.group_b_color));

        historyContainerLinear.addView(view);
    }

    // ✅ Updated Undo method
    private void showPreviousHistory() {
        if (currentHistoryIndex >= 0 && !historyList.isEmpty()) {

            // Get last history item
            HistoryItem item = historyList.get(currentHistoryIndex);

            // Undo score
            if (item.isGroupA) {
                scoreGroupA = Math.max(0, scoreGroupA - 1);
                tvGroupOneScore.setText(String.valueOf(scoreGroupA));
            } else {
                scoreGroupB = Math.max(0, scoreGroupB - 1);
                tvGroupTwoScore.setText(String.valueOf(scoreGroupB));
            }

            // Remove history item
            historyList.remove(currentHistoryIndex);
            currentHistoryIndex--;

            // Update UI
            if (currentHistoryIndex >= 0) {
                showCurrentHistory();
            } else {
                historyContainerLinear.removeAllViews();
            }

        } else {
            Toast.makeText(this, "No previous history", Toast.LENGTH_SHORT).show();
        }
    }

    private void restartScores() {
        scoreGroupA = 0;
        scoreGroupB = 0;
        tvGroupOneScore.setText("0");
        tvGroupTwoScore.setText("0");

        historyList.clear();
        currentHistoryIndex = -1;
        historyContainerLinear.removeAllViews();
    }

    private void openColorPicker(boolean isGroupA) {
        AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this, isGroupA ? colorGroupA : colorGroupB, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                if (isGroupA) {
                    colorGroupA = color;
                    setFrameColor(frameGroupOne, colorGroupA);
                    setTextColorForVisibility(tvGroupOneName, tvGroupOneScore, colorGroupA);
                } else {
                    colorGroupB = color;
                    setFrameColor(frameGroupTwo, colorGroupB);
                    setTextColorForVisibility(tvGroupTwoName, tvGroupTwoScore, colorGroupB);
                }
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) { }
        });
        colorPicker.show();
    }

    private void setFrameColor(FrameLayout frame, int color) {
        Drawable bg = frame.getBackground();
        if (bg instanceof GradientDrawable) {
            ((GradientDrawable) bg.mutate()).setColor(color);
        }
    }

    private void setTextColorForVisibility(TextView name, TextView score, int bgColor) {
        double darkness = 1 - (0.299 * Color.red(bgColor)
                + 0.587 * Color.green(bgColor)
                + 0.114 * Color.blue(bgColor)) / 255;
        int textColor = darkness < 0.5 ? Color.BLACK : Color.WHITE;
        name.setTextColor(textColor);
        score.setTextColor(textColor);
    }
}