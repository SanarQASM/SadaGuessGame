package com.example.sadaguessgame.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.sadaguessgame.R;

public class TimerActivity extends BaseActivity {

    private NumberPicker minutePicker, secondPicker;
    private TextView timeDisplay;
    private ProgressBar circularProgressBar;
    private Button startTimer, stopTimer, restartTimer;
    private ImageView backHomeButton;

    private int totalTime = 0;
    private int timeLeft  = 0;
    private boolean isRunning = false;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timer_activity);

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
        updateTimeText();
        updateButtonState(totalTime > 0);
    }

    private void updateTimeText() {
        int minutes = timeLeft / 60;
        int seconds = timeLeft % 60;
        @SuppressLint("DefaultLocale") String timeStr = String.format("%02d : %02d", minutes, seconds);
        timeDisplay.setText(timeStr);

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
        startTimer.setOnClickListener(v -> startTimerAction());
        stopTimer.setOnClickListener(v -> stopTimerAction());
        restartTimer.setOnClickListener(v -> restartTimerAction());

        // Simply finish — the back stack returns naturally to MainActivity
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
            }

            @Override
            public void onFinish() {
                timeLeft = 0;
                updateTimeText();
                isRunning = false;
            }
        }.start();

        isRunning = true;
    }

    private void stopTimerAction() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        isRunning = false;
    }

    private void restartTimerAction() {
        stopTimerAction();
        timeLeft = totalTime;
        updateTimeText();
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