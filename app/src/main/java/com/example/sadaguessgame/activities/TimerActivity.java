package com.example.sadaguessgame.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
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

    private int totalTime = 0; // in seconds
    private int timeLeft = 0; // in seconds
    private boolean isRunning = false;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timer_activity);

        initViews();
        setupPickers();
        setupButtons();
        updateButtonState(false); // initially disabled
    }

    private void initViews() {
        minutePicker = findViewById(R.id.minutePicker2);
        secondPicker = findViewById(R.id.secondPicker2);
        timeDisplay = findViewById(R.id.timeDisplay);
        circularProgressBar = findViewById(R.id.circularProgressBar);
        startTimer = findViewById(R.id.startTimer);
        stopTimer = findViewById(R.id.stopTimer);
        restartTimer = findViewById(R.id.restartTimer);
        backHomeButton = findViewById(R.id.back_home_activity_button);
        circularProgressBar.setMax(100);
    }

    private void setupPickers() {
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        secondPicker.setMinValue(0);
        secondPicker.setMaxValue(59);

        // Live update when value changes
        NumberPicker.OnValueChangeListener listener = (picker, oldVal, newVal) -> updateTimeFromPickers();
        minutePicker.setOnValueChangedListener(listener);
        secondPicker.setOnValueChangedListener(listener);
    }

    private void updateTimeFromPickers() {
        int minutes = minutePicker.getValue();
        int seconds = secondPicker.getValue();
        totalTime = minutes * 60 + seconds;
        timeLeft = totalTime;

        // update display immediately
        updateTimeText();

        // enable buttons if time > 0
        updateButtonState(totalTime > 0);
    }

    private void updateTimeText() {
        int minutes = timeLeft / 60;
        int seconds = timeLeft % 60;
        @SuppressLint("DefaultLocale") String timeStr = String.format("%02d : %02d", minutes, seconds);
        timeDisplay.setText(timeStr);

        // update circular progress bar
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
        startTimer.setOnClickListener(v -> startTimer());
        stopTimer.setOnClickListener(v -> stopTimer());
        restartTimer.setOnClickListener(v -> restartTimer());
        backHomeButton.setOnClickListener(v ->{
            Intent intent = new Intent(this, MainActivity.class);
            stopTimer();
            minutePicker.setValue(0);
            secondPicker.setValue(0);
            totalTime = 0;
            timeLeft = 0;
            updateTimeText();
            circularProgressBar.setProgress(0);
            updateButtonState(false);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // finish current activity
        });
    }

    // ---------------- TIMER LOGIC ----------------

    private void startTimer() {
        if (isRunning || timeLeft <= 0) return;

        countDownTimer = new CountDownTimer(timeLeft * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = (int) (millisUntilFinished / 1000);
                updateTimeText();
            }

            @Override
            public void onFinish() {
                stopTimer();
            }
        }.start();

        isRunning = true;
    }


    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        isRunning = false;
    }

    private void restartTimer() {
        stopTimer();
        timeLeft = totalTime;
        updateTimeText();
    }

}