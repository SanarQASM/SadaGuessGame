package com.example.sadaguessgame.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.TextView;
import com.example.sadaguessgame.R;
import com.example.sadaguessgame.data.GameState;
import com.example.sadaguessgame.data.ScoreStorage;
import com.google.android.material.button.MaterialButton;

public class WinnerActivity extends BaseActivity {

    private GameState currentGame;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.winner_activity);

        currentGame = ScoreStorage.getInstance(this).getCurrentGame();
        if (currentGame == null) { finish(); return; }

        bindViews();
        setupButtons();
    }

    private void bindViews() {
        TextView groupWinnerName = findViewById(R.id.groupWinnerName);
        TextView groupOneName = findViewById(R.id.groupOneName);
        TextView groupTwoName = findViewById(R.id.groupTwoName);
        TextView groupOneScore = findViewById(R.id.groupOneScore);
        TextView groupTwoScore = findViewById(R.id.groupTwoScore);

        groupOneName.setText(currentGame.groupAName);
        groupTwoName.setText(currentGame.groupBName);

        // Use helper methods from GameState
        int totalA = currentGame.getTotalScoreA();
        int totalB = currentGame.getTotalScoreB();

        groupOneScore.setText(String.valueOf(totalA));
        groupTwoScore.setText(String.valueOf(totalB));

        if (totalA > totalB) {
            groupWinnerName.setText(currentGame.groupAName);
            playSound(R.raw.winner_sound);
        } else if (totalB > totalA) {
            groupWinnerName.setText(currentGame.groupBName);
            playSound(R.raw.winner_sound);
        } else {
            groupWinnerName.setText(R.string.draw);
            playSound(R.raw.draw_sound);
        }
    }

    private void setupButtons() {
        MaterialButton btnHome = findViewById(R.id.btnHomePage);
        MaterialButton btnPlayAgain = findViewById(R.id.btnPlayAgain);

        btnHome.setOnClickListener(v -> {
            saveAndNavigate(MainActivity.class);
            finish();
        });

        btnPlayAgain.setOnClickListener(v -> {
            saveAndNavigate(CreateNewGameActivity.class);
            finish();
        });
    }

    private void saveAndNavigate(Class<?> dest) {
        ScoreStorage.getInstance(this).saveFinishedGame(currentGame);
        Intent intent = new Intent(this, dest);
        if (dest == MainActivity.class) {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void playSound(int resId) {
        stopSound();
        mediaPlayer = MediaPlayer.create(this, resId);
        if (mediaPlayer != null) {
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(MediaPlayer::release);
        }
    }

    private void stopSound() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSound();
        if (currentGame != null) {
            ScoreStorage.getInstance(this).saveFinishedGame(currentGame);
        }
    }
}