package com.example.sadaguessgame.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.sadaguessgame.R;
import com.example.sadaguessgame.data.GameState;
import com.example.sadaguessgame.data.ScoreStorage;
import com.example.sadaguessgame.manager.AdsManager;
import com.example.sadaguessgame.manager.AppNotificationManager;
import com.example.sadaguessgame.manager.ShareManager;
import com.example.sadaguessgame.manager.SoundManager;
import com.google.android.material.button.MaterialButton;

/**
 * WinnerActivity (updated for Features 4 & 6).
 *
 * Changes vs original:
 *  • After displaying winner, shows an interstitial ad (Feature 4).
 *    The ad is shown BEFORE the user taps any button, so the gameplay
 *    result is always visible first.
 *  • If either group hit a streak ≥ 3 a notification is posted (Feature 6).
 */
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

        // Feature 6: notify streak milestone if earned this game
        postStreakNotificationIfNeeded();

        // Feature 4: show interstitial after a brief moment so the result
        // screen is visible first — the callback fires after the ad closes
        AdsManager.getInstance(this).showInterstitialIfReady(this, null);
    }

    // ─── Views ────────────────────────────────────────────────────────────────

    private void bindViews() {
        TextView groupWinnerName = findViewById(R.id.groupWinnerName);
        TextView groupOneName    = findViewById(R.id.groupOneName);
        TextView groupTwoName    = findViewById(R.id.groupTwoName);
        TextView groupOneScore   = findViewById(R.id.groupOneScore);
        TextView groupTwoScore   = findViewById(R.id.groupTwoScore);
        TextView streakSummary   = findViewById(R.id.streakSummary);

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

    // ─── Buttons ─────────────────────────────────────────────────────────────

    private void setupButtons() {
        MaterialButton btnHome      = findViewById(R.id.btnHomePage);
        MaterialButton btnPlayAgain = findViewById(R.id.btnPlayAgain);
        MaterialButton btnShare     = findViewById(R.id.btnShareWinner);

        if (btnHome != null) {
            btnHome.setOnClickListener(v -> {
                saveAndNavigate(MainActivity.class);
                finish();
            });
        }

        if (btnPlayAgain != null) {
            btnPlayAgain.setOnClickListener(v -> {
                saveAndNavigate(CreateNewGameActivity.class);
                finish();
            });
        }

        if (btnShare != null) {
            btnShare.setOnClickListener(v ->
                    ShareManager.shareGameSummary(this, currentGame));
        }
    }

    // ─── Feature 6: streak notification ──────────────────────────────────────

    private void postStreakNotificationIfNeeded() {
        int threshold = 3;
        if (currentGame.maxStreakA >= threshold) {
            AppNotificationManager.getInstance(this)
                    .notifyStreakMilestone(currentGame.maxStreakA);
        } else if (currentGame.maxStreakB >= threshold) {
            AppNotificationManager.getInstance(this)
                    .notifyStreakMilestone(currentGame.maxStreakB);
        }
    }

    // ─── Navigation ───────────────────────────────────────────────────────────

    private void saveAndNavigate(Class<?> dest) {
        ScoreStorage.getInstance(this).saveFinishedGame(currentGame);
        Intent intent = new Intent(this, dest);
        if (dest == MainActivity.class)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundManager.stopCurrent();
    }
}