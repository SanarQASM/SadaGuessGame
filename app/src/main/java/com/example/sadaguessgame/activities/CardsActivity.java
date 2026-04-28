package com.example.sadaguessgame.activities;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.example.sadaguessgame.R;
import com.example.sadaguessgame.data.GameState;
import com.example.sadaguessgame.data.ScoreStorage;
import com.example.sadaguessgame.dialog.TimeUpDialog;
import com.example.sadaguessgame.enums.CategoryCards;
import com.example.sadaguessgame.helper.FileSelectingRandom;
import com.example.sadaguessgame.manager.HapticManager;
import com.example.sadaguessgame.manager.SoundManager;
import com.example.sadaguessgame.manager.VoiceClueManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class CardsActivity extends BaseActivity {

    // ─── Views ───────────────────────────────────────────────────────────────
    private ShapeableImageView cardImage;
    private TextView           cardName;
    private LinearLayout       cards;
    private TextView           groupTurnTv;
    private TextView           timeDisplay;
    private TextView           streakBadge;
    private ProgressBar        circularProgressBar;
    private MaterialButton     startTimer, stopTimer, restartTimer, endTimer;
    private ImageButton        hintButton;
    private TextView           hintCountTv;

    // ─── State ───────────────────────────────────────────────────────────────
    private int     cardImageState = 0;
    private String  assetPath;
    private String  currentWord;

    private GameState            currentGame;
    private FileSelectingRandom  fileRandom;

    // ─── Timer ───────────────────────────────────────────────────────────────
    private CountDownTimer countDownTimer;
    private boolean        isRunning      = false;
    private int            totalTime;
    private int            timeLeft;
    private boolean        warningPlayed  = false;

    // ─── Managers ────────────────────────────────────────────────────────────
    private SoundManager    soundManager;
    private VoiceClueManager voiceManager;
    private HapticManager   hapticManager;

    // ─── Flash animator ──────────────────────────────────────────────────────
    @Nullable private ValueAnimator flashAnimator;

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cards_activity);

        currentGame = ScoreStorage.getInstance(this).getCurrentGame();
        if (currentGame == null) { finish(); return; }

        soundManager  = SoundManager.getInstance(this);
        voiceManager  = VoiceClueManager.getInstance(this);
        hapticManager = HapticManager.getInstance(this);
        fileRandom    = FileSelectingRandom.getInstance(this);

        if (currentGame.voiceClueModeEnabled) {
            voiceManager.init(null);
        }

        initViews();
        initIntentData();
        setupButtons();
    }

    // ─── Init ────────────────────────────────────────────────────────────────

    private void initViews() {
        cardImage            = findViewById(R.id.cardImage);
        cardName             = findViewById(R.id.cardName);
        groupTurnTv          = findViewById(R.id.groupTrun);
        timeDisplay          = findViewById(R.id.timeDisplay);
        circularProgressBar  = findViewById(R.id.circularProgressBar);
        cards                = findViewById(R.id.cards);
        startTimer           = findViewById(R.id.startTimer);
        stopTimer            = findViewById(R.id.stopTimer);
        restartTimer         = findViewById(R.id.restartTimer);
        endTimer             = findViewById(R.id.endTimer);
        streakBadge          = findViewById(R.id.streakBadge);
        hintButton           = findViewById(R.id.hintButton);
        hintCountTv          = findViewById(R.id.hintCountTv);
    }

    private void initIntentData() {
        assetPath = fileRandom.getRandomAssetImage();

        totalTime = currentGame.getTimerDurationSeconds();
        if (totalTime <= 0) totalTime = 60;
        timeLeft = totalTime;

        circularProgressBar.setMax(totalTime);
        circularProgressBar.setProgress(0);
        updateTimeText();
        setGroupText();
        updateStreakBadge();
        updateHintUI();
        showBackCard();
    }

    // ─── Card display ────────────────────────────────────────────────────────

    private void showBackCard() {
        cardImageState = 0;
        voiceManager.stop();

        if (assetPath == null) {
            cardName.setText(R.string.card_word);
            cardImage.setImageResource(R.drawable.nothing_selected);
            return;
        }

        if (assetPath.startsWith(FileSelectingRandom.CUSTOM_PREFIX)) {
            cardImage.setImageResource(R.drawable.cards);
            cardName.setText(R.string.card_back_placeholder);
            currentWord = assetPath.substring(FileSelectingRandom.CUSTOM_PREFIX.length());
            return;
        }

        String[] parts = assetPath.split("/");
        String category = parts[0];
        CategoryCards catEnum = CategoryCards.fromEnglishName(category);
        int backRes = (catEnum != null) ? catEnum.getBackImageRes() : 0;
        if (backRes != 0) cardImage.setImageResource(backRes);

        String displayName = (catEnum != null) ? catEnum.getDisplayName(this) : category;
        cardName.setText(displayName);

        String fileName = assetPath.substring(assetPath.lastIndexOf('/') + 1,
                        assetPath.lastIndexOf('.'))
                .replace("_", " ");
        currentWord = fileName.isEmpty() ? "" :
                fileName.substring(0, 1).toUpperCase() + fileName.substring(1);
    }

    private void showFrontCard() {
        if (assetPath == null) { cardName.setText(R.string.card_word); return; }
        cardImageState = 1;

        if (assetPath.startsWith(FileSelectingRandom.CUSTOM_PREFIX)) {
            cardImage.setImageResource(R.drawable.cards);
            cardName.setText(currentWord);
            speakCurrentWord();
            return;
        }

        try {
            InputStream is = getAssets().open(assetPath);
            Bitmap bmp = BitmapFactory.decodeStream(is);
            is.close();
            cardImage.setImageBitmap(bmp);
            cardName.setText(currentWord);
            speakCurrentWord();
        } catch (IOException e) {
            cardName.setText(R.string.card_word);
            cardImage.setImageResource(R.drawable.nothing_selected);
        }
    }

    private void speakCurrentWord() {
        if (currentWord != null && !currentWord.isEmpty()) {
            voiceManager.speakWord(currentWord);
        }
    }

    // ─── Hint system ─────────────────────────────────────────────────────────

    private void updateHintUI() {
        if (hintButton == null || hintCountTv == null) return;
        int hintsLeft = currentGame.getHintsRemaining(currentGame.groupTurn);
        hintCountTv.setText(String.valueOf(hintsLeft));
        hintButton.setEnabled(hintsLeft > 0 && cardImageState == 0);
        hintButton.setAlpha(hintsLeft > 0 ? 1f : 0.4f);
    }

    private void onHintClicked() {
        if (!currentGame.canUseHint(currentGame.groupTurn)) {
            Toast.makeText(this, R.string.no_hints_left, Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentWord == null || currentWord.isEmpty()) return;

        currentGame.consumeHint(currentGame.groupTurn);
        ScoreStorage.getInstance(this).saveCurrentGame(currentGame);
        updateHintUI();

        String firstLetter = currentWord.substring(0, 1).toUpperCase(Locale.getDefault());
        String hint = getString(R.string.hint_display_format, firstLetter);
        Toast.makeText(this, hint, Toast.LENGTH_LONG).show();
        cardName.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
    }

    // ─── Streak badge ────────────────────────────────────────────────────────

    private void updateStreakBadge() {
        if (streakBadge == null) return;
        int streak = currentGame.getCurrentStreak(currentGame.groupTurn);
        if (streak >= 2) {
            streakBadge.setVisibility(View.VISIBLE);
            streakBadge.setText(getString(R.string.streak_badge_format, streak));
        } else {
            streakBadge.setVisibility(View.GONE);
        }
    }

    // ─── Buttons ─────────────────────────────────────────────────────────────

    private void setupButtons() {
        cards.setOnClickListener(v -> {
            if (cardImageState == 0) showFrontCard();
            else showBackCard();
        });

        startTimer.setOnClickListener(v  -> startTimer());
        stopTimer.setOnClickListener(v   -> stopTimer());
        restartTimer.setOnClickListener(v-> restartTimer());
        endTimer.setOnClickListener(v    -> { stopTimer(); showEndDialog(); });

        if (hintButton != null) hintButton.setOnClickListener(v -> onHintClicked());
    }

    // ─── Timer ───────────────────────────────────────────────────────────────

    private void startTimer() {
        if (isRunning || timeLeft <= 0) return;
        countDownTimer = new CountDownTimer(timeLeft * 1000L, 1000) {
            @Override public void onTick(long ms) {
                timeLeft = (int)(ms / 1000);
                updateTimeText();
                circularProgressBar.setProgress(totalTime - timeLeft);

                if (!warningPlayed && shouldPlayWarning()) {
                    soundManager.playTimerWarning();
                    hapticManager.warning();
                    startFlashAnimation();
                    warningPlayed = true;
                }

                if (timeLeft <= 10) hapticManager.tick();
            }
            @Override public void onFinish() {
                timeLeft = 0;
                updateTimeText();
                circularProgressBar.setProgress(totalTime);
                stopTimer();
                showEndDialog();
            }
        }.start();
        isRunning = true;
    }

    private void stopTimer() {
        if (countDownTimer != null) { countDownTimer.cancel(); countDownTimer = null; }
        stopFlashAnimation();
        hapticManager.cancel();
        soundManager.stopCurrent();
        isRunning     = false;
        warningPlayed = false;
    }

    private void restartTimer() {
        stopTimer();
        assetPath   = fileRandom.getRandomAssetImage();
        currentWord = null;
        timeLeft    = totalTime;
        circularProgressBar.setProgress(0);
        updateTimeText();
        showBackCard();
        updateHintUI();
        updateStreakBadge();
    }

    // ─── Screen flash ─────────────────────────────────────────────────────────

    private void startFlashAnimation() {
        if (cards == null) return;
        stopFlashAnimation();
        flashAnimator = ValueAnimator.ofObject(
                new ArgbEvaluator(),
                Color.TRANSPARENT, 0x55FF0000, Color.TRANSPARENT);
        flashAnimator.setDuration(600);
        flashAnimator.setRepeatCount(ValueAnimator.INFINITE);
        flashAnimator.setRepeatMode(ValueAnimator.REVERSE);
        flashAnimator.addUpdateListener(anim ->
                cards.setBackgroundColor((int) anim.getAnimatedValue()));
        flashAnimator.start();
    }

    private void stopFlashAnimation() {
        if (flashAnimator != null) { flashAnimator.cancel(); flashAnimator = null; }
        if (cards != null) cards.setBackgroundColor(Color.TRANSPARENT);
    }

    private boolean shouldPlayWarning() {
        int mins = totalTime / 60;
        if (totalTime < 60)  return timeLeft <= 10;
        if (mins == 1)       return timeLeft <= 20;
        if (mins == 2)       return timeLeft <= 25;
        int threshold = Math.min(10 + mins * 5, 60);
        return timeLeft <= threshold;
    }

    // ─── UI helpers ──────────────────────────────────────────────────────────

    private void updateTimeText() {
        timeDisplay.setText(getString(R.string.time_format_mmss, timeLeft / 60, timeLeft % 60));
    }

    private void setGroupText() {
        GameState game = ScoreStorage.getInstance(this).getCurrentGame();
        if (game == null) return;
        groupTurnTv.setText(game.groupTurn == GameState.GROUP_A
                ? R.string.group_turn_A : R.string.group_turn_B);
    }

    private void showEndDialog() { new TimeUpDialog(this).show(); }

    // ─── Lifecycle ───────────────────────────────────────────────────────────

    @Override protected void onResume() {
        super.onResume();
        currentGame = ScoreStorage.getInstance(this).getCurrentGame();
        if (currentGame == null) return;
        setGroupText();
        updateStreakBadge();
        updateHintUI();
    }

    @Override protected void onStop() {
        super.onStop();
        if (isRunning) stopTimer();
        voiceManager.stop();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        stopTimer();
        if (currentGame != null && currentGame.voiceClueModeEnabled)
            voiceManager.shutdown();
    }

    @Override public void onConfigurationChanged(Configuration cfg) { super.onConfigurationChanged(cfg); }
}