package com.example.sadaguessgame.activities;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import com.example.sadaguessgame.R;
import com.example.sadaguessgame.manager.HapticManager;
import com.example.sadaguessgame.manager.SoundManager;
import com.google.android.material.button.MaterialButton;

public class TimerActivity extends BaseActivity {

    private NumberPicker   minutePicker, secondPicker;
    private TextView       timeDisplay;
    private ProgressBar    circularProgressBar;
    private MaterialButton startTimer, stopTimer, restartTimer;
    private MaterialButton timerSoundToggleBtn;
    private ImageView      backHomeButton;

    private int     totalTime     = 0;
    private int     timeLeft      = 0;
    private boolean isRunning     = false;
    private boolean warningPlayed = false;
    private boolean timerSoundOn  = true;   // standalone timer sound toggle
    private CountDownTimer countDownTimer;

    private SoundManager  soundManager;
    private HapticManager hapticManager;

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
        updateTimerSoundToggleUI();
    }

    private void initViews() {
        minutePicker        = findViewById(R.id.minutePicker2);
        secondPicker        = findViewById(R.id.secondPicker2);
        timeDisplay         = findViewById(R.id.timeDisplay);
        circularProgressBar = findViewById(R.id.circularProgressBar);
        startTimer          = findViewById(R.id.startTimer);
        stopTimer           = findViewById(R.id.stopTimer);
        restartTimer        = findViewById(R.id.restartTimer);
        timerSoundToggleBtn = findViewById(R.id.timerSoundToggleBtn);
        backHomeButton      = findViewById(R.id.back_home_activity_button);
        circularProgressBar.setMax(100);

        // Time display must always be LTR regardless of app locale
        timeDisplay.setTextDirection(View.TEXT_DIRECTION_LTR);
        timeDisplay.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);

        // Number pickers always LTR
        minutePicker.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        secondPicker.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
    }

    private void setupPickers() {
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        secondPicker.setMinValue(0);
        secondPicker.setMaxValue(59);
        minutePicker.setWrapSelectorWheel(true);
        secondPicker.setWrapSelectorWheel(true);
        minutePicker.setValue(1);
        secondPicker.setValue(0);

        NumberPicker.OnValueChangeListener listener =
                (picker, oldVal, newVal) -> updateTimeFromPickers();
        minutePicker.setOnValueChangedListener(listener);
        secondPicker.setOnValueChangedListener(listener);

        // Set initial value
        updateTimeFromPickers();
    }

    private void updateTimeFromPickers() {
        int minutes = minutePicker.getValue();
        int seconds = secondPicker.getValue();
        totalTime     = minutes * 60 + seconds;
        timeLeft      = totalTime;
        warningPlayed = false;
        updateTimeText();
        updateButtonState(totalTime > 0);
    }

    private void updateTimeText() {
        timeDisplay.setText(getString(R.string.time_format_mmss, timeLeft / 60, timeLeft % 60));

        if (totalTime > 0) {
            int progress = (int)((totalTime - timeLeft) * 100.0 / totalTime);
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

    private void updateTimerSoundToggleUI() {
        if (timerSoundToggleBtn == null) return;
        if (timerSoundOn) {
            timerSoundToggleBtn.setText(R.string.timer_sound_enabled);
            timerSoundToggleBtn.setAlpha(1f);
        } else {
            timerSoundToggleBtn.setText(R.string.timer_sound_disabled);
            timerSoundToggleBtn.setAlpha(0.6f);
        }
    }

    private void setupButtons() {
        startTimer.setOnClickListener(v   -> startTimerAction());
        stopTimer.setOnClickListener(v    -> stopTimerAction());
        restartTimer.setOnClickListener(v -> restartTimerAction());

        if (timerSoundToggleBtn != null) {
            timerSoundToggleBtn.setOnClickListener(v -> {
                timerSoundOn = !timerSoundOn;
                updateTimerSoundToggleUI();
            });
        }

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
                timeLeft = (int)(millisUntilFinished / 1000);
                updateTimeText();

                // Warning: plays once when threshold is crossed, not at every tick
                if (!warningPlayed && CardsActivity.shouldPlayWarning(timeLeft, totalTime)) {
                    if (timerSoundOn) {
                        soundManager.playTimerWarning();
                    }
                    hapticManager.warning();
                    warningPlayed = true;
                }

                // Tick haptic in last 10 seconds
                if (timeLeft <= 10) {
                    hapticManager.tick();
                    timeDisplay.setTextColor(
                            getResources().getColor(R.color.timer_warning_color, null));
                }
            }

            @Override
            public void onFinish() {
                timeLeft = 0;
                updateTimeText();
                circularProgressBar.setProgress(100);
                isRunning     = false;
                warningPlayed = false;
                timeDisplay.setTextColor(
                        getResources().getColor(R.color.timer_normal_color, null));
                hapticManager.success();
                showTimerFinishedDialog();
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
        timeDisplay.setTextColor(
                getResources().getColor(R.color.timer_normal_color, null));
    }

    private void restartTimerAction() {
        stopTimerAction();
        timeLeft      = totalTime;
        warningPlayed = false;
        updateTimeText();
    }

    /**
     * Dialog shown when the timer naturally reaches zero.
     * Offers: Restart | Set New Time | Close
     */
    private void showTimerFinishedDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.timer_finished_title)
                .setMessage(R.string.timer_finished_message)
                .setPositiveButton(R.string.timer_restart_action, (d, w) -> {
                    restartTimerAction();
                    startTimerAction();
                })
                .setNeutralButton(R.string.timer_set_new_action, (d, w) -> {
                    // Allow user to pick new time — dismiss and interact with pickers
                    timeLeft  = totalTime;
                    updateTimeText();
                })
                .setNegativeButton(R.string.timer_close_action, null)
                .setCancelable(true)
                .show();
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