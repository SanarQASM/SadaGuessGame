package com.example.sadaguessgame.activities;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.sadaguessgame.R;
import com.example.sadaguessgame.data.GameState;
import com.example.sadaguessgame.data.ScoreStorage;
import com.example.sadaguessgame.enums.CategoryCards;
import com.example.sadaguessgame.helper.FileSelectingRandom;
import com.example.sadaguessgame.manager.HapticManager;
import com.example.sadaguessgame.manager.SoundManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * Sudden-Death tiebreaker round.
 *
 * Rules:
 *  • 30 seconds for Group A to guess one card.
 *  • If they succeed → Group A wins.
 *  • If time expires → Group B gets a chance (another 30 s).
 *  • If Group B also fails → result stays as Draw.
 *
 * Entered from GameScoreActivity when isExactDraw() is true.
 * After resolution, navigates to WinnerActivity.
 */
public class SuddenDeathActivity extends BaseActivity {

    private static final int SUDDEN_DEATH_SECONDS = 30;

    private ShapeableImageView cardImage;
    private TextView           cardName;
    private LinearLayout       cardContainer;
    private TextView           timeDisplay;
    private TextView           groupTurnTv;
    private TextView           titleTv;
    private ProgressBar        progressBar;
    private MaterialButton     btnFound, btnNotFound;

    private GameState           game;
    private FileSelectingRandom fileRandom;
    private SoundManager        soundManager;
    private HapticManager       hapticManager;

    private CountDownTimer countDownTimer;
    private int            timeLeft;
    private int            cardState   = 0;   // 0 = back, 1 = front
    private boolean        groupATried = false;
    private String         assetPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sudden_death);

        game         = ScoreStorage.getInstance(this).getCurrentGame();
        if (game == null) { finish(); return; }

        fileRandom   = FileSelectingRandom.getInstance(this);
        soundManager = SoundManager.getInstance(this);
        hapticManager= HapticManager.getInstance(this);

        initViews();
        game.isSuddenDeath = true;
        ScoreStorage.getInstance(this).saveCurrentGame(game);

        soundManager.playSuddenDeath();
        startRoundFor(GameState.GROUP_A);
    }

    // ─── Init ────────────────────────────────────────────────────────────────

    private void initViews() {
        cardImage     = findViewById(R.id.sdCardImage);
        cardName      = findViewById(R.id.sdCardName);
        cardContainer = findViewById(R.id.sdCardContainer);
        timeDisplay   = findViewById(R.id.sdTimeDisplay);
        groupTurnTv   = findViewById(R.id.sdGroupTurn);
        titleTv       = findViewById(R.id.sdTitle);
        progressBar   = findViewById(R.id.sdProgressBar);
        btnFound      = findViewById(R.id.btnSdFound);
        btnNotFound   = findViewById(R.id.btnSdNotFound);

        titleTv.setText(R.string.sudden_death_title);

        cardContainer.setOnClickListener(v -> {
            if (cardState == 0) showFrontCard();
        });

        btnFound.setOnClickListener(v   -> onFound());
        btnNotFound.setOnClickListener(v-> onNotFound());
    }

    // ─── Round management ────────────────────────────────────────────────────

    private void startRoundFor(int group) {
        game.groupTurn = group;
        ScoreStorage.getInstance(this).saveCurrentGame(game);

        assetPath = fileRandom.getRandomAssetImage();
        cardState = 0;
        showBackCard();

        groupTurnTv.setText(group == GameState.GROUP_A
                ? game.groupAName : game.groupBName);

        timeLeft = SUDDEN_DEATH_SECONDS;
        progressBar.setMax(SUDDEN_DEATH_SECONDS);
        progressBar.setProgress(0);
        updateTimeText();
        startCountdown();
    }

    private void onFound() {
        stopCountdown();
        hapticManager.success();
        game.suddenDeathWinner = game.groupTurn;
        game.isFinished = true;
        ScoreStorage.getInstance(this).saveCurrentGame(game);
        navigateToWinner();
    }

    private void onNotFound() {
        stopCountdown();
        if (!groupATried && game.groupTurn == GameState.GROUP_A) {
            // Give Group B a chance
            groupATried = true;
            startRoundFor(GameState.GROUP_B);
        } else {
            // Both failed → draw stands
            game.suddenDeathWinner = GameState.NO_WINNER;
            game.isFinished = true;
            ScoreStorage.getInstance(this).saveCurrentGame(game);
            navigateToWinner();
        }
    }

    // ─── Card display ────────────────────────────────────────────────────────

    private void showBackCard() {
        if (assetPath == null) return;
        if (assetPath.startsWith(FileSelectingRandom.CUSTOM_PREFIX)) {
            cardImage.setImageResource(R.drawable.cards);
            cardName.setText("?");
            return;
        }
        String category = assetPath.split("/")[0];
        CategoryCards cat = CategoryCards.fromEnglishName(category);
        if (cat != null) cardImage.setImageResource(cat.getBackImageRes());
        cardName.setText(cat != null ? cat.getDisplayName(this) : category);
    }

    private void showFrontCard() {
        cardState = 1;
        if (assetPath == null) return;
        if (assetPath.startsWith(FileSelectingRandom.CUSTOM_PREFIX)) {
            cardName.setText(assetPath.substring(FileSelectingRandom.CUSTOM_PREFIX.length()));
            return;
        }
        try {
            InputStream is = getAssets().open(assetPath);
            Bitmap bmp = BitmapFactory.decodeStream(is);
            is.close();
            cardImage.setImageBitmap(bmp);
            String name = assetPath.substring(assetPath.lastIndexOf('/') + 1,
                    assetPath.lastIndexOf('.')).replace("_", " ");
            cardName.setText(name.isEmpty() ? "" :
                    name.substring(0, 1).toUpperCase() + name.substring(1));
        } catch (IOException ignored) {}
    }

    // ─── Countdown ───────────────────────────────────────────────────────────

    private void startCountdown() {
        countDownTimer = new CountDownTimer(timeLeft * 1000L, 1000) {
            @Override public void onTick(long ms) {
                timeLeft = (int)(ms / 1000);
                updateTimeText();
                progressBar.setProgress(SUDDEN_DEATH_SECONDS - timeLeft);
                if (timeLeft <= 10) hapticManager.tick();
                if (timeLeft <= 10) {
                    timeDisplay.setTextColor(Color.RED);
                    timeDisplay.startAnimation(
                            AnimationUtils.loadAnimation(SuddenDeathActivity.this,
                                    R.anim.shake));
                }
            }
            @Override public void onFinish() {
                timeLeft = 0; updateTimeText(); onNotFound();
            }
        }.start();
    }

    private void stopCountdown() {
        if (countDownTimer != null) { countDownTimer.cancel(); countDownTimer = null; }
        hapticManager.cancel();
        timeDisplay.setTextColor(Color.WHITE);
    }

    private void updateTimeText() {
        timeDisplay.setText(String.format(Locale.getDefault(),
                "%02d", timeLeft));
    }

    private void navigateToWinner() {
        startActivity(new Intent(this, WinnerActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        stopCountdown();
    }
}