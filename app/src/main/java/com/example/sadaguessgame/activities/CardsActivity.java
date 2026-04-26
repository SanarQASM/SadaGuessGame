package com.example.sadaguessgame.activities;

import android.app.AlertDialog;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.content.Intent;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;

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
    private ImageView backButton;

    private int cardImageState = 0; // 0 = back, 1 = front
    private String assetPath;

    private GameState currentGame;
    private FileSelectingRandom fileSelectingRandom;

    private CountDownTimer countDownTimer;
    private boolean isRunning = false;
    private int totalTime;
    private int timeLeft;
    private boolean warningPlayed = false;

    private MediaPlayer warningPlayer;
    private boolean isWarningPlayerReady = false;
    private boolean isDialogShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cards_activity);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackPress();
            }
        });

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
        cardImage           = findViewById(R.id.cardImage);
        cardName            = findViewById(R.id.cardName);
        groupTurn           = findViewById(R.id.groupTrun);
        timeDisplay         = findViewById(R.id.timeDisplay);
        circularProgressBar = findViewById(R.id.circularProgressBar);
        cards               = findViewById(R.id.cards);
        backButton          = findViewById(R.id.back_home_activity_button);

        startTimer   = findViewById(R.id.startTimer);
        stopTimer    = findViewById(R.id.stopTimer);
        restartTimer = findViewById(R.id.restartTimer);
        endTimer     = findViewById(R.id.endTimer);
    }

    private void initIntentData() {
        assetPath = fileSelectingRandom.getRandomAssetImage();

        totalTime = (currentGame.minutePicker * 60) + currentGame.secondPicker;
        if (totalTime <= 0) totalTime = 60;
        timeLeft = totalTime;

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

            if (bitmap != null) {
                cardImage.setImageBitmap(bitmap);
            } else {
                cardImage.setImageResource(R.drawable.nothing_selected);
            }

            String fileName = assetPath.substring(
                    assetPath.lastIndexOf("/") + 1,
                    assetPath.contains(".") ? assetPath.lastIndexOf(".") : assetPath.length()
            ).replace("_", " ");

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
        if (backButton != null) {
            backButton.setOnClickListener(v -> handleBackPress());
        }

        cards.setOnClickListener(v -> {
            if (cardImageState == 0) initFrontCard();
            else initBackCard();
        });

        startTimer.setOnClickListener(v -> startTimer());
        stopTimer.setOnClickListener(v -> stopTimerAction());
        restartTimer.setOnClickListener(v -> restartTimer());
        endTimer.setOnClickListener(v -> {
            stopTimerAction();
            showEndDialog();
        });
    }

    private void handleBackPress() {
        if (isRunning) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.back))
                    .setMessage(getString(R.string.confirm_leave_game))
                    .setPositiveButton(getString(R.string.yes_button), (d, w) -> {
                        stopTimerAction();
                        finish();
                    })
                    .setNegativeButton(getString(R.string.no_button), null)
                    .show();
        } else {
            finish();
        }
    }

    // ------------- TIMER -------------

    private void startTimer() {
        if (isRunning || timeLeft <= 0) return;

        // FIX: Update button states to reflect running state
        startTimer.setEnabled(false);
        startTimer.setAlpha(0.5f);
        stopTimer.setEnabled(true);
        stopTimer.setAlpha(1f);

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
                isRunning = false;
                cancelCountDownTimer();
                resetButtonStates();
                showEndDialog();
            }
        }.start();

        isRunning = true;
    }

    private void resetButtonStates() {
        if (startTimer != null) {
            startTimer.setEnabled(true);
            startTimer.setAlpha(1f);
        }
        if (stopTimer != null) {
            stopTimer.setEnabled(true);
            stopTimer.setAlpha(1f);
        }
    }

    private boolean shouldPlayWarning() {
        int totalMinutes = totalTime / 60;
        if (totalTime < 60) return timeLeft <= 10;
        if (totalMinutes == 1) return timeLeft <= 20;
        if (totalMinutes == 2) return timeLeft <= 25;
        int threshold = Math.min(10 + totalMinutes * 5, 60);
        return timeLeft <= threshold;
    }

    private void stopTimerAction() {
        cancelCountDownTimer();
        stopWarningSound();
        isRunning = false;
        warningPlayed = false;
        resetButtonStates();
    }

    private void cancelCountDownTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    private void restartTimer() {
        stopTimerAction();
        assetPath = fileSelectingRandom.getRandomAssetImage();
        timeLeft = totalTime;
        circularProgressBar.setProgress(0);
        updateTimeText();
        initBackCard();
    }

    // ------------- SOUND -------------

    private void playWarningSound() {
        stopWarningSound();
        try {
            warningPlayer = MediaPlayer.create(this, R.raw.countdown_boom);
            if (warningPlayer != null) {
                isWarningPlayerReady = true;
                warningPlayer.setOnCompletionListener(mp -> stopWarningSound());
                warningPlayer.start();
            }
        } catch (Exception e) {
            isWarningPlayerReady = false;
            warningPlayer = null;
        }
    }

    private void stopWarningSound() {
        if (warningPlayer == null) return;
        try {
            if (isWarningPlayerReady && warningPlayer.isPlaying()) {
                warningPlayer.stop();
            }
        } catch (IllegalStateException ignored) {
        } finally {
            try {
                warningPlayer.release();
            } catch (Exception ignored) { }
            warningPlayer = null;
            isWarningPlayerReady = false;
        }
    }

    // ------------- UI -------------

    private void updateTimeText() {
        if (timeDisplay == null) return;
        timeDisplay.setText(
                String.format(Locale.getDefault(),
                        "%02d : %02d", timeLeft / 60, timeLeft % 60)
        );
    }

    private void setGroupText() {
        if (groupTurn == null) return;
        GameState game = ScoreStorage.getInstance(this).getCurrentGame();
        if (game == null) return;

        // FIX: Show the actual group name (not just A/B label) for better UX
        String name = game.groupTurn == GameState.GROUP_A
                ? game.groupAName : game.groupBName;
        String turnText = name + " " + getString(R.string.turn_label);
        groupTurn.setText(turnText);
    }

    private void showEndDialog() {
        if (isDialogShown || isFinishing() || isDestroyed()) return;
        isDialogShown = true;
        new TimeUpDialog(this).show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Layout handles orientation via different layout files; no action needed here
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelCountDownTimer();
        stopWarningSound();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isRunning) stopTimerAction();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isDialogShown = false;
        setGroupText();
        // Refresh game state in case it changed
        currentGame = ScoreStorage.getInstance(this).getCurrentGame();
    }
}