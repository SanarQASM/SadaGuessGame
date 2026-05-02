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
 *
 * FIX: saveAndNavigate() now guards against calling saveFinishedGame() twice.
 * The first call marks the game finished and writes it to history. A second
 * call (e.g. from onBackPressed or a rapid double-tap) would duplicate the
 * entry in getAllGames(). The guard flag `saved` prevents this.
 */
public class DrawActivity extends BaseActivity {

    private GameState   currentGame;
    private MediaPlayer mediaPlayer;
    private boolean     isMediaPlayerReady = false;
    // FIX: guard against duplicate saveFinishedGame() calls
    private boolean     saved = false;

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
        // FIX: only save once — duplicate calls would append another entry to history
        if (!saved) {
            saved = true;
            ScoreStorage.getInstance(this).saveFinishedGame(currentGame);
        }
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

    @Override protected void onDestroy() { super.onDestroy(); stopSound(); }
    @Override protected void onStop()    { super.onStop();    stopSound(); }
}