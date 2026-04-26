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
    private boolean isMediaPlayerReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.winner_activity);

        currentGame = ScoreStorage.getInstance(this).getCurrentGame();
        if (currentGame == null) {
            finish();
            return;
        }

        bindViews();
        setupButtons();
    }

    private void bindViews() {
        TextView groupWinnerName = findViewById(R.id.groupWinnerName);
        TextView groupOneName   = findViewById(R.id.groupOneName);
        TextView groupTwoName   = findViewById(R.id.groupTwoName);
        TextView groupOneScore  = findViewById(R.id.groupOneScore);
        TextView groupTwoScore  = findViewById(R.id.groupTwoScore);

        groupOneName.setText(currentGame.groupAName);
        groupTwoName.setText(currentGame.groupBName);

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

    private void playSound(int resId) {
        stopSound();
        try {
            mediaPlayer = MediaPlayer.create(this, resId);
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
            if (isMediaPlayerReady && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
        } catch (IllegalStateException ignored) {
            // MediaPlayer was in an invalid state — safe to ignore
        } finally {
            try {
                mediaPlayer.release();
            } catch (Exception ignored) { }
            mediaPlayer = null;
            isMediaPlayerReady = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSound();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopSound();
    }
}