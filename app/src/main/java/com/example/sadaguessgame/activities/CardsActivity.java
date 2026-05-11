package com.example.sadaguessgame.activities;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
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
import androidx.appcompat.app.AlertDialog;
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

    // ─── Views ────────────────────────────────────────────────────────────────
    private ShapeableImageView cardImage;
    private TextView           cardName;
    private LinearLayout       cards;
    private TextView           groupTurnTv;
    private TextView           timeDisplay;
    private TextView           streakBadge;
    private ProgressBar        circularProgressBar;
    private MaterialButton     startTimer, stopTimer, restartTimer, endTimer;
    private MaterialButton     changeCardButton;
    private MaterialButton     timerSoundToggleBtn;
    private ImageButton        hintButton;
    private TextView           hintCountTv;

    // ─── State ────────────────────────────────────────────────────────────────
    private int     cardImageState = 0;   // 0 = back, 1 = front
    private String  assetPath;
    private String  currentWord;

    private GameState           currentGame;
    private FileSelectingRandom fileRandom;

    // ─── Timer ────────────────────────────────────────────────────────────────
    private CountDownTimer countDownTimer;
    private boolean        isRunning     = false;
    private int            totalTime;
    private int            timeLeft;
    private boolean        warningPlayed = false;

    // ─── Managers ─────────────────────────────────────────────────────────────
    private SoundManager     soundManager;
    private VoiceClueManager voiceManager;
    private HapticManager    hapticManager;

    @Nullable
    private ValueAnimator flashAnimator;

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

        if (currentGame.voiceClueModeEnabled) voiceManager.init(null);

        initViews();
        initData();
        setupButtons();
        updateTimerSoundToggleUI();
    }

    // ─── Init ─────────────────────────────────────────────────────────────────

    private void initViews() {
        cardImage             = findViewById(R.id.cardImage);
        cardName              = findViewById(R.id.cardName);
        groupTurnTv           = findViewById(R.id.groupTrun);
        timeDisplay           = findViewById(R.id.timeDisplay);
        circularProgressBar   = findViewById(R.id.circularProgressBar);
        cards                 = findViewById(R.id.cards);
        startTimer            = findViewById(R.id.startTimer);
        stopTimer             = findViewById(R.id.stopTimer);
        restartTimer          = findViewById(R.id.restartTimer);
        endTimer              = findViewById(R.id.endTimer);
        changeCardButton      = findViewById(R.id.changeCardButton);
        streakBadge           = findViewById(R.id.streakBadge);
        hintButton            = findViewById(R.id.hintButton);
        hintCountTv           = findViewById(R.id.hintCountTv);
        timerSoundToggleBtn   = findViewById(R.id.timerSoundToggleBtn);
    }

    private void initData() {
        assetPath = fileRandom.getRandomAssetImage();
        totalTime = currentGame.getTimerDurationSeconds();
        if (totalTime <= 0) totalTime = 60;
        timeLeft  = totalTime;

        circularProgressBar.setMax(totalTime);
        circularProgressBar.setProgress(0);
        updateTimeText();
        setGroupText();
        updateStreakBadge();
        updateHintUI();
        showBackCard();
    }

    // ─── Card display ─────────────────────────────────────────────────────────

    /**
     * Shows the BACK of the card.
     * IMPORTANT: never changes the ImageView width/height — only its image content.
     * The card dimensions are fixed in XML; size must NOT change on state toggle.
     */
    private void showBackCard() {
        cardImageState = 0;
        voiceManager.stop();

        if (assetPath == null) {
            cardName.setText(R.string.card_word);
            cardImage.setImageResource(R.drawable.nothing_selected);
            updateHintUI();
            return;
        }

        if (assetPath.startsWith(FileSelectingRandom.CUSTOM_PREFIX)) {
            cardImage.setImageResource(R.drawable.cards);
            cardName.setText(R.string.card_back_placeholder);
            currentWord = assetPath.substring(FileSelectingRandom.CUSTOM_PREFIX.length());
            updateHintUI();
            return;
        }

        String category = assetPath.split("/")[0];
        CategoryCards catEnum = CategoryCards.fromEnglishName(category);
        int backRes = (catEnum != null) ? catEnum.getBackImageRes() : 0;
        if (backRes != 0) {
            cardImage.setImageResource(backRes);
        } else {
            cardImage.setImageResource(R.drawable.cards);
        }

        String displayName = (catEnum != null) ? catEnum.getDisplayName(this) : category;
        cardName.setText(displayName);

        // Pre-compute word from filename so hints work before reveal
        String fileName = assetPath
                .substring(assetPath.lastIndexOf('/') + 1, assetPath.lastIndexOf('.'))
                .replace("_", " ");
        currentWord = fileName.isEmpty() ? "" :
                fileName.substring(0, 1).toUpperCase(Locale.getDefault())
                        + fileName.substring(1).toLowerCase(Locale.getDefault());

        updateHintUI();
    }

    /**
     * Shows the FRONT of the card.
     * Uses setImageBitmap — the ShapeableImageView keeps its fixed dimensions
     * because android:scaleType="centerCrop" + fixed width/height in XML.
     */
    private void showFrontCard() {
        if (assetPath == null) { cardName.setText(R.string.card_word); return; }
        cardImageState = 1;

        if (assetPath.startsWith(FileSelectingRandom.CUSTOM_PREFIX)) {
            // Keep the generic card image; only update the text label
            cardImage.setImageResource(R.drawable.cards);
            cardName.setText(currentWord);
            speakCurrentWord();
            return;
        }

        try {
            InputStream is  = getAssets().open(assetPath);
            Bitmap      bmp = BitmapFactory.decodeStream(is);
            is.close();
            // setImageBitmap does NOT resize the view; scaleType=centerCrop fills the fixed area
            cardImage.setImageBitmap(bmp);
            cardName.setText(currentWord);
            speakCurrentWord();
        } catch (IOException e) {
            cardName.setText(R.string.card_word);
            cardImage.setImageResource(R.drawable.nothing_selected);
        }
    }

    /**
     * Loads a completely NEW card without touching the timer.
     * Resets hint letter counters for the new card.
     */
    private void changeCard() {
        assetPath   = fileRandom.getRandomAssetImage();
        currentWord = null;
        currentGame.resetHintLetterCounters();
        ScoreStorage.getInstance(this).saveCurrentGame(currentGame);
        showBackCard();
        updateHintUI();
        updateStreakBadge();
    }

    private void speakCurrentWord() {
        if (currentWord != null && !currentWord.isEmpty()) {
            voiceManager.speakWord(currentWord);
        }
    }

    // ─── Hint system (progressive letter reveal) ──────────────────────────────

    private void updateHintUI() {
        if (hintButton == null || hintCountTv == null) return;
        int hintsLeft = currentGame.getHintsRemaining(currentGame.groupTurn);
        hintCountTv.setText(String.valueOf(hintsLeft));

        // Enable only when on back-of-card AND hints remain AND we have a word
        boolean canHint = hintsLeft > 0 && cardImageState == 0
                && currentWord != null && !currentWord.isEmpty();
        hintButton.setEnabled(canHint);
        hintButton.setAlpha(canHint ? 1f : 0.4f);
    }

    private void onHintClicked() {
        if (currentWord == null || currentWord.isEmpty()) return;

        if (!currentGame.canUseHint(currentGame.groupTurn)) {
            Toast.makeText(this, R.string.no_hints_left, Toast.LENGTH_SHORT).show();
            return;
        }

        // Consume hint and get which letter index to reveal next
        int letterIndex = currentGame.consumeHintAndGetLetterIndex(currentGame.groupTurn);
        ScoreStorage.getInstance(this).saveCurrentGame(currentGame);
        updateHintUI();

        if (letterIndex < 0 || letterIndex >= currentWord.length()) {
            Toast.makeText(this, R.string.no_hints_left, Toast.LENGTH_SHORT).show();
            return;
        }

        char letter = Character.toUpperCase(currentWord.charAt(letterIndex));
        String hintMsg = getString(R.string.hint_letter_format, letterIndex + 1,
                String.valueOf(letter));
        Toast.makeText(this, hintMsg, Toast.LENGTH_LONG).show();
        cardName.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));

        // Warn user if this was their last hint
        int remaining = currentGame.getHintsRemaining(currentGame.groupTurn);
        if (remaining == 0) {
            Toast.makeText(this, R.string.hint_warning_last, Toast.LENGTH_SHORT).show();
        }
    }

    // ─── Streak badge ─────────────────────────────────────────────────────────

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

    // ─── Timer sound toggle ───────────────────────────────────────────────────

    private void updateTimerSoundToggleUI() {
        if (timerSoundToggleBtn == null) return;
        if (currentGame.timerSoundEnabled) {
            timerSoundToggleBtn.setText(R.string.timer_sound_enabled);
            timerSoundToggleBtn.setAlpha(1f);
        } else {
            timerSoundToggleBtn.setText(R.string.timer_sound_disabled);
            timerSoundToggleBtn.setAlpha(0.6f);
        }
    }

    private void toggleTimerSound() {
        currentGame.timerSoundEnabled = !currentGame.timerSoundEnabled;
        ScoreStorage.getInstance(this).saveCurrentGame(currentGame);
        updateTimerSoundToggleUI();
    }

    // ─── Buttons ──────────────────────────────────────────────────────────────

    private void setupButtons() {
        // Tapping the card toggles front/back
        cards.setOnClickListener(v -> {
            if (cardImageState == 0) showFrontCard();
            else                     showBackCard();
        });

        startTimer.setOnClickListener(v   -> startTimerAction());
        stopTimer.setOnClickListener(v    -> stopTimerAction());
        restartTimer.setOnClickListener(v -> restartTimerOnly());
        endTimer.setOnClickListener(v     -> { stopTimerAction(); showEndDialog(); });

        if (changeCardButton != null) {
            changeCardButton.setOnClickListener(v -> changeCard());
        }

        if (hintButton != null) {
            hintButton.setOnClickListener(v -> onHintClicked());
        }

        if (timerSoundToggleBtn != null) {
            timerSoundToggleBtn.setOnClickListener(v -> toggleTimerSound());
        }
    }

    // ─── Timer ────────────────────────────────────────────────────────────────

    private void startTimerAction() {
        if (isRunning || timeLeft <= 0) return;
        countDownTimer = new CountDownTimer(timeLeft * 1000L, 1000) {
            @Override public void onTick(long ms) {
                timeLeft = (int)(ms / 1000);
                updateTimeText();
                circularProgressBar.setProgress(totalTime - timeLeft);

                if (!warningPlayed && shouldPlayWarning(timeLeft, totalTime)) {
                    if (currentGame.timerSoundEnabled) {
                        soundManager.playTimerWarning();
                    }
                    hapticManager.warning();
                    startFlashAnimation();
                    warningPlayed = true;
                }
                if (timeLeft <= 10) {
                    hapticManager.tick();
                    timeDisplay.setTextColor(getResources().getColor(R.color.timer_warning_color, null));
                }
            }
            @Override public void onFinish() {
                timeLeft = 0;
                circularProgressBar.setProgress(totalTime);
                updateTimeText();
                stopTimerAction();
                showEndDialog();
            }
        }.start();
        isRunning = true;
    }

    private void stopTimerAction() {
        if (countDownTimer != null) { countDownTimer.cancel(); countDownTimer = null; }
        stopFlashAnimation();
        hapticManager.cancel();
        soundManager.stopCurrent();
        isRunning     = false;
        warningPlayed = false;
        timeDisplay.setTextColor(getResources().getColor(R.color.timer_normal_color, null));
    }

    /**
     * Resets ONLY the countdown — does NOT change the card.
     */
    private void restartTimerOnly() {
        stopTimerAction();
        timeLeft      = totalTime;
        warningPlayed = false;
        circularProgressBar.setProgress(0);
        updateTimeText();
        // Card is intentionally NOT changed here
    }

    /**
     * Timer warning logic:
     * - timer < 60s  → warn at 10s remaining
     * - timer == 60s → warn at 15s remaining (user has ~15s awareness window)
     * - timer 61–120s → warn at 20s remaining
     * - timer 121–180s → warn at 25s remaining
     * - timer > 180s → warn at 30s remaining (capped)
     *
     * Sound plays from the warning point to end (NOT at the very start).
     */
    public static boolean shouldPlayWarning(int timeLeft, int totalTime) {
        int warnAt;
        if (totalTime < 60) {
            warnAt = 10;
        } else if (totalTime == 60) {
            warnAt = 15;
        } else if (totalTime <= 120) {
            warnAt = 20;
        } else if (totalTime <= 180) {
            warnAt = 25;
        } else {
            warnAt = 30;
        }
        return timeLeft <= warnAt;
    }

    // ─── Flash ────────────────────────────────────────────────────────────────

    private void startFlashAnimation() {
        if (cards == null) return;
        stopFlashAnimation();
        flashAnimator = ValueAnimator.ofObject(
                new ArgbEvaluator(),
                Color.TRANSPARENT,
                getResources().getColor(R.color.timer_warning_color, null) & 0x55FFFFFF,
                Color.TRANSPARENT);
        flashAnimator.setDuration(600);
        flashAnimator.setRepeatCount(ValueAnimator.INFINITE);
        flashAnimator.setRepeatMode(ValueAnimator.REVERSE);
        flashAnimator.addUpdateListener(a ->
                cards.setBackgroundColor((int) a.getAnimatedValue()));
        flashAnimator.start();
    }

    private void stopFlashAnimation() {
        if (flashAnimator != null) { flashAnimator.cancel(); flashAnimator = null; }
        if (cards != null) cards.setBackgroundColor(Color.TRANSPARENT);
    }

    // ─── UI helpers ───────────────────────────────────────────────────────────

    private void updateTimeText() {
        // Always LTR for time display regardless of app language
        timeDisplay.setTextDirection(View.TEXT_DIRECTION_LTR);
        timeDisplay.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        timeDisplay.setText(getString(R.string.time_format_mmss, timeLeft / 60, timeLeft % 60));
    }

    private void setGroupText() {
        GameState game = ScoreStorage.getInstance(this).getCurrentGame();
        if (game == null) return;
        if (game.groupTurn == GameState.GROUP_A) {
            groupTurnTv.setText(getString(R.string.group_turn_A));
        } else {
            groupTurnTv.setText(getString(R.string.group_turn_B));
        }
    }

    private void showEndDialog() {
        // When timer expires naturally: non-dismissible (user MUST answer)
        // When user manually taps End: dismissible (maybe accidental)
        TimeUpDialog d = new TimeUpDialog(this);
        d.show();
    }

    // ─── Back-press confirmation ───────────────────────────────────────────────

    @Override
    public void onBackPressed() {
        showLeaveConfirmation();
    }

    private void showLeaveConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_leave_game_title)
                .setMessage(R.string.confirm_leave_game)
                .setPositiveButton(R.string.confirm_leave_yes, (d, w) -> {
                    stopTimerAction();
                    finish();
                })
                .setNegativeButton(R.string.confirm_leave_no, null)
                .setCancelable(true)
                .show();
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    @Override protected void onResume() {
        super.onResume();
        currentGame = ScoreStorage.getInstance(this).getCurrentGame();
        if (currentGame == null) return;
        setGroupText();
        updateStreakBadge();
        updateHintUI();
        updateTimerSoundToggleUI();
    }

    @Override protected void onStop() {
        super.onStop();
        if (isRunning) stopTimerAction();
        voiceManager.stop();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        stopTimerAction();
        if (currentGame != null && currentGame.voiceClueModeEnabled)
            voiceManager.shutdown();
    }

    @Override
    public void onConfigurationChanged(Configuration cfg) {
        super.onConfigurationChanged(cfg);
        // Re-apply LTR direction for time display after config change
        if (timeDisplay != null) {
            timeDisplay.setTextDirection(View.TEXT_DIRECTION_LTR);
            timeDisplay.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
    }
}