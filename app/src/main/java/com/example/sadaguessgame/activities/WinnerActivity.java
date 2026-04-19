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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.winner_activity);
        // GameState instance
        currentGame = ScoreStorage.getInstance(this).getCurrentGame();
        // Views
        TextView groupWinnerName = findViewById(R.id.groupWinnerName);
        TextView groupOneName = findViewById(R.id.groupOneName);
        TextView groupTwoName = findViewById(R.id.groupTwoName);
        TextView groupOneScore = findViewById(R.id.groupOneScore);
        TextView groupTwoScore = findViewById(R.id.groupTwoScore);

        MaterialButton btnHomePage = findViewById(R.id.btnHomePage);
        MaterialButton btnPlayAgain = findViewById(R.id.btnPlayAgain);

        if (currentGame == null) {
            finish();
            return;
        }
        // Set group names
        groupOneName.setText(currentGame.groupAName);
        groupTwoName.setText(currentGame.groupBName);

        // Calculate total scores
        int totalScoreA = currentGame.scoresA.stream().mapToInt(Integer::intValue).sum();
        int totalScoreB = currentGame.scoresB.stream().mapToInt(Integer::intValue).sum();

        groupOneScore.setText(String.valueOf(totalScoreA));
        groupTwoScore.setText(String.valueOf(totalScoreB));

        // Determine winner
        if (totalScoreA > totalScoreB) {
            groupWinnerName.setText(currentGame.groupAName);

            playSound(R.raw.winner_sound);
        } else if (totalScoreB > totalScoreA) {
            playSound(R.raw.winner_sound);
            groupWinnerName.setText(currentGame.groupBName);
        } else {
            playSound(R.raw.draw_sound);
            groupWinnerName.setText(R.string.draw);
        }

        // Button listeners
        btnHomePage.setOnClickListener(v -> {
            ScoreStorage.getInstance(this).saveFinishedGame(currentGame);
            Intent intent = new Intent(WinnerActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });

        btnPlayAgain.setOnClickListener(v -> {
            ScoreStorage.getInstance(this).saveFinishedGame(currentGame);
            Intent intent = new Intent(WinnerActivity.this, CreateNewGameActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });
    }

    private void playSound(int winnerSound) {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, winnerSound);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(MediaPlayer::release);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Final save on destroy
        if (currentGame != null) {
            ScoreStorage.getInstance(this).saveFinishedGame(currentGame);
        }
    }
}