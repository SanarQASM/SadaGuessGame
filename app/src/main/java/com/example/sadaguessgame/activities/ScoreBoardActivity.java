package com.example.sadaguessgame.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import com.example.sadaguessgame.R;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import yuku.ambilwarna.AmbilWarnaDialog;

public class ScoreBoardActivity extends BaseActivity {

    public static final String EXTRA_HISTORY = "history_json";

    private FrameLayout  frameGroupOne, frameGroupTwo;
    private TextView     tvGroupOneScore, tvGroupTwoScore;
    private EditText     etGroupOneName, etGroupTwoName;
    private TextView     tvGroupOneLabelDisplay, tvGroupTwoLabelDisplay;

    private int scoreGroupA = 0, scoreGroupB = 0;
    private int colorGroupA, colorGroupB;

    final List<HistoryItem> historyList  = new ArrayList<>();
    int currentHistoryIndex = -1;

    public static class HistoryItem {
        public boolean isGroupA;
        public int     scoreAdded;
        public String  groupName;

        public HistoryItem(boolean isGroupA, int scoreAdded, String groupName) {
            this.isGroupA  = isGroupA;
            this.scoreAdded = scoreAdded;
            this.groupName = groupName;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.score_game_activity);

        colorGroupA = ContextCompat.getColor(this, R.color.primary_color);
        colorGroupB = ContextCompat.getColor(this, R.color.secondary_color);

        initViews();
        setupButtons();
    }

    private void initViews() {
        frameGroupOne         = findViewById(R.id.frameGroupOne);
        frameGroupTwo         = findViewById(R.id.frameGroupTwo);
        tvGroupOneScore       = findViewById(R.id.tvGroupOneScore);
        tvGroupTwoScore       = findViewById(R.id.tvGroupTwoScore);
        etGroupOneName        = findViewById(R.id.etGroupOneName);
        etGroupTwoName        = findViewById(R.id.etGroupTwoName);
        tvGroupOneLabelDisplay = findViewById(R.id.tvGroupOneName);
        tvGroupTwoLabelDisplay = findViewById(R.id.tvGroupTwoName);

        // Init display labels from hints
        if (tvGroupOneLabelDisplay != null)
            tvGroupOneLabelDisplay.setText(getString(R.string.group_a_color));
        if (tvGroupTwoLabelDisplay != null)
            tvGroupTwoLabelDisplay.setText(getString(R.string.group_a_color));
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
        if (restartScoreBtn != null) restartScoreBtn.setOnClickListener(v -> restartScores());
        if (undoButton      != null) undoButton.setOnClickListener(v -> undoLastScore());
        if (historyButton   != null) historyButton.setOnClickListener(v -> openHistory());

        frameGroupOne.setOnClickListener(v -> addScore(true));
        frameGroupTwo.setOnClickListener(v -> addScore(false));
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

    private void addScore(boolean isGroupA) {
        if (isGroupA) {
            scoreGroupA++;
            tvGroupOneScore.setText(String.valueOf(scoreGroupA));
        } else {
            scoreGroupB++;
            tvGroupTwoScore.setText(String.valueOf(scoreGroupB));
        }

        String groupName = isGroupA ? getGroupAName() : getGroupBName();
        historyList.add(new HistoryItem(isGroupA, 1, groupName));
        currentHistoryIndex = historyList.size() - 1;

        // Update display labels
        if (tvGroupOneLabelDisplay != null) tvGroupOneLabelDisplay.setText(getGroupAName());
        if (tvGroupTwoLabelDisplay != null) tvGroupTwoLabelDisplay.setText(getGroupBName());
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

    private void restartScores() {
        scoreGroupA = 0;
        scoreGroupB = 0;
        tvGroupOneScore.setText(getString(R.string.score_zero));
        tvGroupTwoScore.setText(getString(R.string.score_zero));
        historyList.clear();
        currentHistoryIndex = -1;
    }

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

    private void openColorPicker(boolean isGroupA) {
        AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this,
                isGroupA ? colorGroupA : colorGroupB,
                new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        if (isGroupA) {
                            colorGroupA = color;
                            setFrameColor(frameGroupOne, colorGroupA);
                            setTextColorForVisibility(tvGroupOneLabelDisplay, tvGroupOneScore, colorGroupA);
                        } else {
                            colorGroupB = color;
                            setFrameColor(frameGroupTwo, colorGroupB);
                            setTextColorForVisibility(tvGroupTwoLabelDisplay, tvGroupTwoScore, colorGroupB);
                        }
                    }
                    @Override public void onCancel(AmbilWarnaDialog dialog) {}
                });
        colorPicker.show();
    }

    private void setFrameColor(FrameLayout frame, int color) {
        Drawable bg = frame.getBackground();
        if (bg instanceof GradientDrawable) {
            ((GradientDrawable) bg.mutate()).setColor(color);
        } else {
            frame.setBackgroundColor(color);
        }
    }

    private void setTextColorForVisibility(TextView name, TextView score, int bgColor) {
        double darkness = 1 - (0.299 * Color.red(bgColor)
                + 0.587 * Color.green(bgColor)
                + 0.114 * Color.blue(bgColor)) / 255;
        int textColor = darkness < 0.5 ? Color.BLACK : Color.WHITE;
        if (name  != null) name.setTextColor(textColor);
        if (score != null) score.setTextColor(textColor);
    }
}