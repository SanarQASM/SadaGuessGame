package com.example.sadaguessgame.activities;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.sadaguessgame.R;
import com.example.sadaguessgame.dialog.TimeUpDialog;
import com.example.sadaguessgame.enums.CategoryCards;
import com.example.sadaguessgame.helper.FileSelectingRandom;
import com.example.sadaguessgame.data.GameState;
import com.example.sadaguessgame.data.ScoreStorage;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class CardsActivity extends BaseActivity {

    private ShapeableImageView cardImage;
    private TextView cardName;
    private LinearLayout cards;
    private TextView groupTurn, timeDisplay;
    private ProgressBar circularProgressBar;
    private MaterialButton startTimer, stopTimer, restartTimer, endTimer;

    private int cardImageState = 0; // 0 = back, 1 = front

    private int currentGroup;
    private String assetPath;

    private GameState currentGame;
    private FileSelectingRandom fileSelectingRandom;

    private CountDownTimer countDownTimer;
    private boolean isRunning = false;
    private int totalTime;
    private int timeLeft;
    private boolean warningPlayed = false;

    private MediaPlayer warningPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cards_activity);

        currentGame = ScoreStorage.getInstance(this).getCurrentGame();
        if (currentGame == null) {
            finish();
            return;
        }

        fileSelectingRandom = FileSelectingRandom.getInstance(this);

        initViews();
        initIntentData();
        setupButtons();
    }

    private void initViews() {
        cardImage = findViewById(R.id.cardImage);
        cardName = findViewById(R.id.cardName);
        groupTurn = findViewById(R.id.groupTrun);
        timeDisplay = findViewById(R.id.timeDisplay);
        circularProgressBar = findViewById(R.id.circularProgressBar);
        cards = findViewById(R.id.cards);

        startTimer = findViewById(R.id.startTimer);
        stopTimer = findViewById(R.id.stopTimer);
        restartTimer = findViewById(R.id.restartTimer);
        endTimer = findViewById(R.id.endTimer);
    }

    private void initIntentData() {
        assetPath = fileSelectingRandom.getRandomAssetImage();

        totalTime = (currentGame.minutePicker * 60) + currentGame.secondPicker;
        // Ensure minimum timer of 10s so warning can fire
        if (totalTime <= 0) totalTime = 60;
        timeLeft = totalTime;
        currentGroup = currentGame.groupTurn;

        circularProgressBar.setMax(totalTime);
        circularProgressBar.setProgress(0);
        updateTimeText();
        setGroupText();
        initBackCard();
    }

    // ------------- CARD -------------

    private void initBackCard() {
        cardImageState = 0;

        if (assetPath == null || !assetPath.contains("/")) {
            cardName.setText(R.string.card_word);
            cardImage.setImageResource(R.drawable.nothing_selected);
            return;
        }

        String[] pathParts = assetPath.split("/");
        if (pathParts.length == 0) return;

        String category = pathParts[0];
        CategoryCards categoryEnum = CategoryCards.fromEnglishName(category);
        int backImageRes = (categoryEnum != null) ? categoryEnum.getBackImageRes() : 0;

        if (backImageRes != 0) {
            cardImage.setImageResource(backImageRes);
        }
        // Show category name on back of card
        String displayName = (categoryEnum != null)
                ? categoryEnum.getDisplayName(this)
                : category;
        cardName.setText(displayName);
    }

    private void initFrontCard() {
        if (assetPath == null) {
            cardName.setText(R.string.card_word);
            cardImage.setImageResource(R.drawable.nothing_selected);
            return;
        }
        try {
            InputStream inputStream = getAssets().open(assetPath);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            cardImage.setImageBitmap(bitmap);

            String fileName = assetPath.substring(
                    assetPath.lastIndexOf("/") + 1,
                    assetPath.lastIndexOf(".")
            ).replace("_", " ");
            // Capitalize first letter
            if (!fileName.isEmpty()) {
                fileName = fileName.substring(0, 1).toUpperCase() + fileName.substring(1);
            }
            cardName.setText(fileName);
            cardImageState = 1;

        } catch (IOException e) {
            cardName.setText(R.string.card_word);
            cardImage.setImageResource(R.drawable.nothing_selected);
        }
    }

    // ------------- BUTTONS -------------

    private void setupButtons() {
        cards.setOnClickListener(v -> {
            if (cardImageState == 0) initFrontCard();
            else initBackCard();
        });

        startTimer.setOnClickListener(v -> startTimer());
        stopTimer.setOnClickListener(v -> stopTimer());
        restartTimer.setOnClickListener(v -> restartTimer());
        endTimer.setOnClickListener(v -> {
            stopTimer();
            showEndDialog();
        });
    }

    // ------------- TIMER -------------

    private void startTimer() {
        if (isRunning || timeLeft <= 0) return;

        countDownTimer = new CountDownTimer(timeLeft * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = (int) (millisUntilFinished / 1000);
                updateTimeText();
                circularProgressBar.setProgress(totalTime - timeLeft);

                if (!warningPlayed && shouldPlayWarning()) {
                    playWarningSound();
                    warningPlayed = true;
                }
            }

            @Override
            public void onFinish() {
                timeLeft = 0;
                updateTimeText();
                circularProgressBar.setProgress(totalTime);
                stopTimer();
                showEndDialog();
            }
        }.start();

        isRunning = true;
    }

    /**
     * Sound warning thresholds:
     * < 60s total  → warn at 10s remaining
     * < 120s total → warn at 20s remaining  (1 min)
     * < 180s total → warn at 25s remaining  (2 min = requirement)
     * >= 180s      → warn at 30s remaining
     * Pattern: for each additional minute, add ~5s warning
     */
    private boolean shouldPlayWarning() {
        int totalMinutes = totalTime / 60;
        if (totalTime < 60) return timeLeft <= 10;
        if (totalMinutes == 1) return timeLeft <= 20;
        if (totalMinutes == 2) return timeLeft <= 25;
        // General rule: 10 + (minutes * 5) capped at 60
        int threshold = Math.min(10 + totalMinutes * 5, 60);
        return timeLeft <= threshold;
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        stopWarningSound();
        isRunning = false;
        warningPlayed = false;
    }

    private void restartTimer() {
        stopTimer();
        // Get fresh card
        assetPath = fileSelectingRandom.getRandomAssetImage();
        timeLeft = totalTime;
        circularProgressBar.setProgress(0);
        updateTimeText();
        initBackCard();
    }

    // ------------- SOUND -------------

    private void playWarningSound() {
        stopWarningSound();
        warningPlayer = MediaPlayer.create(this, R.raw.countdown_boom);
        if (warningPlayer != null) {
            warningPlayer.start();
            warningPlayer.setOnCompletionListener(mp -> stopWarningSound());
        }
    }

    private void stopWarningSound() {
        if (warningPlayer != null) {
            try {
                if (warningPlayer.isPlaying()) warningPlayer.stop();
                warningPlayer.release();
            } catch (IllegalStateException ignored) { }
            warningPlayer = null;
        }
    }

    // ------------- UI -------------

    private void updateTimeText() {
        timeDisplay.setText(
                String.format(Locale.getDefault(),
                        "%02d : %02d", timeLeft / 60, timeLeft % 60)
        );
    }

    private void setGroupText() {
        // Re-read from storage to get latest groupTurn
        GameState game = ScoreStorage.getInstance(this).getCurrentGame();
        if (game == null) return;
        groupTurn.setText(
                game.groupTurn == GameState.GROUP_A
                        ? R.string.group_turn_A
                        : R.string.group_turn_B
        );
    }

    private void showEndDialog() {
        new TimeUpDialog(this).show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Layout handles orientation via ScrollView — no action needed
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isRunning) stopTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh group text in case turn changed
        setGroupText();
    }
}