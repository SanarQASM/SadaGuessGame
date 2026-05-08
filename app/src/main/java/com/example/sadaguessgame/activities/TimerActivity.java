package com.example.sadaguessgame.activities;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.example.sadaguessgame.R;
import com.example.sadaguessgame.manager.HapticManager;
import com.example.sadaguessgame.manager.SoundManager;
import com.google.android.material.button.MaterialButton;

public class TimerActivity extends BaseActivity {

    private NumberPicker  minutePicker, secondPicker;
    private TextView      timeDisplay;
    private ProgressBar   circularProgressBar;
    private MaterialButton startTimer, stopTimer, restartTimer;
    private ImageView     backHomeButton;

    private int     totalTime    = 0;
    private int     timeLeft     = 0;
    private boolean isRunning    = false;
    private boolean warningPlayed = false;
    private CountDownTimer countDownTimer;

    private SoundManager   soundManager;
    private HapticManager  hapticManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timer_activity);

        soundManager  = SoundManager.getInstance(this);
        hapticManager = HapticManager.getInstance(this);

        initViews();
        setupPickers();
        setupButtons();
        updateButtonState(false);
    }

    private void initViews() {
        minutePicker        = findViewById(R.id.minutePicker2);
        secondPicker        = findViewById(R.id.secondPicker2);
        timeDisplay         = findViewById(R.id.timeDisplay);
        circularProgressBar = findViewById(R.id.circularProgressBar);
        startTimer          = findViewById(R.id.startTimer);
        stopTimer           = findViewById(R.id.stopTimer);
        restartTimer        = findViewById(R.id.restartTimer);
        backHomeButton      = findViewById(R.id.back_home_activity_button);
        circularProgressBar.setMax(100);
    }

    private void setupPickers() {
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        secondPicker.setMinValue(0);
        secondPicker.setMaxValue(59);

        NumberPicker.OnValueChangeListener listener = (picker, oldVal, newVal) -> updateTimeFromPickers();
        minutePicker.setOnValueChangedListener(listener);
        secondPicker.setOnValueChangedListener(listener);
    }

    private void updateTimeFromPickers() {
        int minutes = minutePicker.getValue();
        int seconds = secondPicker.getValue();
        totalTime = minutes * 60 + seconds;
        timeLeft  = totalTime;
        warningPlayed = false;
        updateTimeText();
        updateButtonState(totalTime > 0);
    }

    private void updateTimeText() {
        timeDisplay.setText(getString(R.string.time_format_mmss, timeLeft / 60, timeLeft % 60));

        if (totalTime > 0) {
            int progress = (int) ((totalTime - timeLeft) * 100.0 / totalTime);
            circularProgressBar.setProgress(progress);
        } else {
            circularProgressBar.setProgress(0);
        }
    }

    private void updateButtonState(boolean enabled) {
        startTimer.setEnabled(enabled);
        stopTimer.setEnabled(enabled);
        restartTimer.setEnabled(enabled);

        float alpha = enabled ? 1f : 0.5f;
        startTimer.setAlpha(alpha);
        stopTimer.setAlpha(alpha);
        restartTimer.setAlpha(alpha);
    }

    private void setupButtons() {
        startTimer.setOnClickListener(v   -> startTimerAction());
        stopTimer.setOnClickListener(v    -> stopTimerAction());
        restartTimer.setOnClickListener(v -> restartTimerAction());
        backHomeButton.setOnClickListener(v -> {
            stopTimerAction();
            finish();
        });
    }

    private void startTimerAction() {
        if (isRunning || timeLeft <= 0) return;

        countDownTimer = new CountDownTimer(timeLeft * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = (int) (millisUntilFinished / 1000);
                updateTimeText();

                // Warning sound — mirrors CardsActivity logic
                if (!warningPlayed && shouldPlayWarning()) {
                    soundManager.playTimerWarning();
                    hapticManager.warning();
                    warningPlayed = true;
                }

                // Tick haptic in last 10 seconds
                if (timeLeft <= 10) {
                    hapticManager.tick();
                }
            }

            @Override
            public void onFinish() {
                timeLeft = 0;
                updateTimeText();
                circularProgressBar.setProgress(100);
                isRunning     = false;
                warningPlayed = false;
                // Play winner sound to signal timer done
                soundManager.playTimerWarning();
                hapticManager.success();
            }
        }.start();

        isRunning = true;
    }

    private void stopTimerAction() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        soundManager.stopCurrent();
        hapticManager.cancel();
        isRunning     = false;
        warningPlayed = false;
    }

    private void restartTimerAction() {
        stopTimerAction();
        timeLeft      = totalTime;
        warningPlayed = false;
        updateTimeText();
    }

    /**
     * Mirrors CardsActivity.shouldPlayWarning() logic for consistency.
     */
    private boolean shouldPlayWarning() {
        int mins = totalTime / 60;
        if (totalTime < 60)  return timeLeft <= 10;
        if (mins == 1)       return timeLeft <= 20;
        if (mins == 2)       return timeLeft <= 25;
        int threshold = Math.min(10 + mins * 5, 60);
        return timeLeft <= threshold;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimerAction();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isRunning) stopTimerAction();
    }
}