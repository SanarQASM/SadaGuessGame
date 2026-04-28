package com.example.sadaguessgame.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import com.example.sadaguessgame.R;
import com.example.sadaguessgame.data.GameState;
import com.example.sadaguessgame.data.ScoreStorage;
import com.example.sadaguessgame.manager.ShareManager;
import com.example.sadaguessgame.manager.SoundManager;
import com.google.android.material.button.MaterialButton;

public class WinnerActivity extends BaseActivity {

    private GameState    currentGame;
    private SoundManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.winner_activity);

        currentGame  = ScoreStorage.getInstance(this).getCurrentGame();
        if (currentGame == null) { finish(); return; }

        soundManager = SoundManager.getInstance(this);

        bindViews();
        setupButtons();
    }

    private void bindViews() {
        TextView groupWinnerName = findViewById(R.id.groupWinnerName);
        TextView groupOneName    = findViewById(R.id.groupOneName);
        TextView groupTwoName    = findViewById(R.id.groupTwoName);
        TextView groupOneScore   = findViewById(R.id.groupOneScore);
        TextView groupTwoScore   = findViewById(R.id.groupTwoScore);

        TextView streakSummary = findViewById(R.id.streakSummary);
        if (streakSummary != null) {
            streakSummary.setText(getString(R.string.streak_summary_format,
                    currentGame.groupAName, currentGame.maxStreakA,
                    currentGame.groupBName, currentGame.maxStreakB));
        }

        groupOneName.setText(currentGame.groupAName);
        groupTwoName.setText(currentGame.groupBName);

        int totalA = currentGame.getTotalScoreA();
        int totalB = currentGame.getTotalScoreB();
        groupOneScore.setText(String.valueOf(totalA));
        groupTwoScore.setText(String.valueOf(totalB));

        if (currentGame.suddenDeathWinner != GameState.NO_WINNER) {
            String sdWinner = currentGame.suddenDeathWinner == GameState.GROUP_A
                    ? currentGame.groupAName : currentGame.groupBName;
            groupWinnerName.setText(getString(R.string.winner_sudden_death_format, sdWinner));
            soundManager.playWinner();
        } else if (totalA > totalB) {
            groupWinnerName.setText(currentGame.groupAName);
            soundManager.playWinner();
        } else if (totalB > totalA) {
            groupWinnerName.setText(currentGame.groupBName);
            soundManager.playWinner();
        } else {
            groupWinnerName.setText(R.string.draw);
            soundManager.playDraw();
        }
    }

    private void setupButtons() {
        MaterialButton btnHome      = findViewById(R.id.btnHomePage);
        MaterialButton btnPlayAgain = findViewById(R.id.btnPlayAgain);
        MaterialButton btnShare     = findViewById(R.id.btnShareWinner);

        btnHome.setOnClickListener(v -> {
            saveAndNavigate(MainActivity.class);
            finish();
        });

        btnPlayAgain.setOnClickListener(v -> {
            saveAndNavigate(CreateNewGameActivity.class);
            finish();
        });

        if (btnShare != null) {
            btnShare.setOnClickListener(v ->
                    ShareManager.shareGameSummary(this, currentGame));
        }
    }

    private void saveAndNavigate(Class<?> dest) {
        ScoreStorage.getInstance(this).saveFinishedGame(currentGame);
        Intent intent = new Intent(this, dest);
        if (dest == MainActivity.class)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        soundManager.stopCurrent();
    }
}