package com.example.sadaguessgame.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.TextView;

import com.example.sadaguessgame.R;
import com.example.sadaguessgame.data.GameState;
import com.example.sadaguessgame.data.ScoreStorage;
import com.google.android.material.button.MaterialButton;

/**
 * Shown when both groups finish the game with equal scores.
 * Provides the same navigation options as WinnerActivity.
 */
public class DrawActivity extends BaseActivity {

    private GameState currentGame;
    private MediaPlayer mediaPlayer;
    private boolean isMediaPlayerReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.draw_activity);

        currentGame = ScoreStorage.getInstance(this).getCurrentGame();
        if (currentGame == null) {
            finish();
            return;
        }

        bindViews();
        setupButtons();
        playDrawSound();
    }

    private void bindViews() {
        TextView groupOneName  = findViewById(R.id.groupOneName);
        TextView groupTwoName  = findViewById(R.id.groupTwoName);
        TextView groupOneScore = findViewById(R.id.groupOneScore);
        TextView groupTwoScore = findViewById(R.id.groupTwoScore);

        groupOneName.setText(currentGame.groupAName);
        groupTwoName.setText(currentGame.groupBName);
        groupOneScore.setText(String.valueOf(currentGame.getTotalScoreA()));
        groupTwoScore.setText(String.valueOf(currentGame.getTotalScoreB()));
    }

    private void setupButtons() {
        MaterialButton btnHome      = findViewById(R.id.btnHomePage);
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

    private void playDrawSound() {
        stopSound();
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.draw_sound);
            if (mediaPlayer != null) {
                isMediaPlayerReady = true;
                mediaPlayer.setOnCompletionListener(mp -> {
                    isMediaPlayerReady = false;
                    mp.release();
                    if (mediaPlayer == mp) mediaPlayer = null;
                });
                mediaPlayer.start();
            }
        } catch (Exception e) {
            isMediaPlayerReady = false;
            mediaPlayer = null;
        }
    }

    private void stopSound() {
        if (mediaPlayer == null) return;
        try {
            if (isMediaPlayerReady && mediaPlayer.isPlaying()) mediaPlayer.stop();
        } catch (IllegalStateException ignored) {
        } finally {
            try { mediaPlayer.release(); } catch (Exception ignored) {}
            mediaPlayer = null;
            isMediaPlayerReady = false;
        }
    }

    @Override
    protected void onDestroy() { super.onDestroy(); stopSound(); }

    @Override
    protected void onStop() { super.onStop(); stopSound(); }
}