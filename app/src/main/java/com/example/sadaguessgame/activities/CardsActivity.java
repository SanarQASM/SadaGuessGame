package com.example.sadaguessgame.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.sadaguessgame.R;
import com.example.sadaguessgame.dialog.TimeUpDialog;
import com.example.sadaguessgame.helper.FileSelectingRandom;
import com.example.sadaguessgame.data.GameState;
import com.example.sadaguessgame.helper.GetCardBackImage;
import com.example.sadaguessgame.data.ScoreStorage;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class CardsActivity extends BaseActivity {

    // ---------- UI ----------
    private ShapeableImageView cardImage;
    private TextView cardName;
    private LinearLayout cards;
    private TextView groupTurn, timeDisplay;
    private ProgressBar circularProgressBar;
    private MaterialButton startTimer, stopTimer, restartTimer, endTimer;

    private int cardImageState = 0;

    private int currentGroup;
    private String assetPath;

    private GameState currentGame;
    private FileSelectingRandom fileSelectingRandom;

    // ---------- TIMER ----------
    private CountDownTimer countDownTimer;
    private boolean isRunning = false;
    private int totalTime; // seconds
    private int timeLeft;  // seconds
    private boolean warningPlayed = false;

    // ---------- SOUND ----------
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

    // ---------------- INIT ----------------

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
        timeLeft = totalTime;
        currentGroup = currentGame.groupTurn;

        circularProgressBar.setMax(totalTime);
        circularProgressBar.setProgress(0);
        updateTimeText();
        setGroupText();
        initBackCard();
    }

    // ---------------- CARD ----------------

    private void initBackCard() {
        cardImageState = 0;

        // Add safety check
        if (assetPath == null || !assetPath.contains("/")) {
            cardName.setText(R.string.card_word);
            cardImage.setImageResource(R.drawable.nothing_selected);
            return;
        }

        String[] pathParts = assetPath.split("/");
        if (pathParts.length == 0) {
            return;
        }

        String category = pathParts[0];
        int backImageRes = GetCardBackImage.getImage(category);

        if (backImageRes != 0) {
            cardImage.setImageResource(backImageRes);
            cardName.setText(category);
        }
    }

    private void initFrontCard() {
        try {
            InputStream inputStream = getAssets().open(assetPath);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            cardImage.setImageBitmap(bitmap);
            cardName.setText(
                    assetPath.substring(
                            assetPath.lastIndexOf("/") + 1,
                            assetPath.lastIndexOf(".")
                    ).replace("_", " ")
            );
            cardImageState = 1;

        } catch (IOException e) {
            cardName.setText(R.string.card_word);
            cardImage.setImageResource(R.drawable.nothing_selected);
        }
    }

    // ---------------- BUTTONS ----------------

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

    // ---------------- TIMER ----------------

    private void startTimer() {
        if (isRunning || timeLeft <= 0) return;

        countDownTimer = new CountDownTimer(timeLeft * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = (int) (millisUntilFinished / 1000);
                updateTimeText();
                circularProgressBar.setProgress(totalTime - timeLeft);

                // 🔊 PLAY SOUND ONLY WHEN CLOSE TO END
                if (!warningPlayed && shouldPlayWarning()) {
                    playWarningSound();
                    warningPlayed = true;
                }
            }

            @Override
            public void onFinish() {
                stopTimer();
                showEndDialog();
            }
        }.start();

        isRunning = true;
    }

    private boolean shouldPlayWarning() {
        if (totalTime < 60) return timeLeft <= 10;
        if (totalTime < 180) return timeLeft <= 20;
        return timeLeft <= 30;
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
        timeLeft = totalTime;
        circularProgressBar.setProgress(0);
        updateTimeText();
    }

    // ---------------- SOUND ----------------

    private void playWarningSound() {
        stopWarningSound();
        warningPlayer = MediaPlayer.create(this, R.raw.countdown_boom);
        if (warningPlayer != null) {
            warningPlayer.start();
        }
    }

    private void stopWarningSound() {
        if (warningPlayer != null) {
            if (warningPlayer.isPlaying()) warningPlayer.stop();
            warningPlayer.release();
            warningPlayer = null;
        }
    }

    // ---------------- UI ----------------

    private void updateTimeText() {
        timeDisplay.setText(
                String.format(Locale.getDefault(),
                        "%02d : %02d", timeLeft / 60, timeLeft % 60)
        );
    }

    private void setGroupText() {
        groupTurn.setText(
                currentGroup == 0 ? R.string.group_turn_A : R.string.group_turn_B
        );
    }

    private void showEndDialog() {
        new TimeUpDialog(this).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }
}