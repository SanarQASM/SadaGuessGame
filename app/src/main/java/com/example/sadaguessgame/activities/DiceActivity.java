package com.example.sadaguessgame.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.airbnb.lottie.LottieAnimationView;
import com.example.sadaguessgame.R;
import com.example.sadaguessgame.manager.HapticManager;
import com.example.sadaguessgame.manager.SoundManager;
import com.google.android.material.button.MaterialButton;
import java.util.Random;

public class DiceActivity extends BaseActivity {

    private MaterialButton btn1Dice, btn2Dice;
    private MaterialButton btn0, btn_1, btn_2, btn_3;
    private ImageView      diceImage, dices1Image, dices2Image;
    private LinearLayout   dicesContainer, btn_container;
    private LottieAnimationView diceLottie;
    private CheckBox       checkboxSkipAnimation;
    private TextView       tvDiceResult;
    private MaterialButton startRollDice;

    private int selectedDiceCount   = 1;
    private int selectedProbability = 0;

    private SoundManager  soundManager;
    private HapticManager hapticManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dice_activity);

        soundManager  = SoundManager.getInstance(this);
        hapticManager = HapticManager.getInstance(this);

        initViews();
        setupListeners();

        selectedDiceCount   = 1;
        selectedProbability = 0;
        updateDiceButtonBackground();
        setProbability(selectedProbability, btn0);
    }

    private void initViews() {
        btn1Dice              = findViewById(R.id.btn1Dice);
        btn2Dice              = findViewById(R.id.btn2Dice);
        btn0                  = findViewById(R.id.btn0);
        btn_1                 = findViewById(R.id.btn_1);
        btn_2                 = findViewById(R.id.btn_2);
        btn_3                 = findViewById(R.id.btn_3);
        startRollDice         = findViewById(R.id.startRollDice);
        diceImage             = findViewById(R.id.diceImage);
        dices1Image           = findViewById(R.id.dices1Image);
        dices2Image           = findViewById(R.id.dices2Image);
        dicesContainer        = findViewById(R.id.dicesContainer);
        diceLottie            = findViewById(R.id.diceLottie);
        btn_container         = findViewById(R.id.btn_container);
        checkboxSkipAnimation = findViewById(R.id.checkboxSkipAnimation);
        tvDiceResult          = findViewById(R.id.tvDiceResult);

        ImageView backHomeButton = findViewById(R.id.back_home_activity_button);
        backHomeButton.setOnClickListener(v -> {
            startActivity(new Intent(DiceActivity.this, MainActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });
    }

    private void setupListeners() {
        btn1Dice.setOnClickListener(v -> {
            selectedDiceCount = 1;
            updateDiceButtonBackground();
        });

        btn2Dice.setOnClickListener(v -> {
            selectedDiceCount = 2;
            updateDiceButtonBackground();
        });

        btn0.setOnClickListener(v  -> setProbability(0, btn0));
        btn_1.setOnClickListener(v -> setProbability(25, btn_1));
        btn_2.setOnClickListener(v -> setProbability(50, btn_2));
        btn_3.setOnClickListener(v -> setProbability(75, btn_3));

        startRollDice.setOnClickListener(v -> {
            startRollDice.setEnabled(false);
            if (checkboxSkipAnimation.isChecked()) {
                rollDiceWithoutAnimation();
            } else {
                rollDiceWithAnimation();
            }
        });
    }

    private void updateDiceButtonBackground() {
        boolean isSingle = selectedDiceCount == 1;
        btn_container.setEnabled(!isSingle);
        btn_container.setClickable(!isSingle);
        btn_container.setAlpha(isSingle ? 0.5f : 1f);
        btn0.setEnabled(!isSingle);
        btn_1.setEnabled(!isSingle);
        btn_2.setEnabled(!isSingle);
        btn_3.setEnabled(!isSingle);

        if (isSingle) {
            btn1Dice.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary_color));
            btn2Dice.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.secondary_color));
        } else {
            btn2Dice.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary_color));
            btn1Dice.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.secondary_color));
        }

        if (tvDiceResult != null) tvDiceResult.setText("");
    }

    private void setProbability(int probability, MaterialButton selectedButton) {
        selectedProbability = probability;
        MaterialButton[] buttons = {btn0, btn_1, btn_2, btn_3};
        for (MaterialButton btn : buttons) {
            btn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.secondary_color));
        }
        selectedButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary_color));
    }

    private void rollDiceWithoutAnimation() {
        diceImage.setVisibility(View.GONE);
        diceLottie.setVisibility(View.GONE);
        dicesContainer.setVisibility(View.GONE);

        int[] results = rollDice();
        showDiceResults(results);
        playRollSound(results);
        startRollDice.setEnabled(true);
    }

    private void rollDiceWithAnimation() {
        diceImage.setVisibility(View.GONE);
        dicesContainer.setVisibility(View.GONE);
        if (tvDiceResult != null) tvDiceResult.setText("");

        diceLottie.setVisibility(View.VISIBLE);
        diceLottie.removeAllAnimatorListeners();
        diceLottie.cancelAnimation();
        diceLottie.setAnimation(selectedDiceCount == 1 ? R.raw.dice : R.raw.dices);
        diceLottie.setProgress(0f);
        diceLottie.setRepeatCount(0);

        diceLottie.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                diceLottie.setVisibility(View.GONE);
                int[] results = rollDice();
                showDiceResults(results);
                playRollSound(results);
                startRollDice.setEnabled(true);
            }
        });

        diceLottie.playAnimation();
    }

    private int[] rollDice() {
        if (selectedDiceCount == 1) {
            return new int[]{rollSingleDie()};
        }
        int d1 = rollSingleDie();
        int d2 = rollSingleDie();
        if (selectedProbability > 0 && new Random().nextInt(100) < selectedProbability) {
            d2 = d1; // Force double
        }
        return new int[]{d1, d2};
    }

    private void showDiceResults(int[] results) {
        if (results.length == 1) {
            diceImage.setImageResource(getDiceDrawable(results[0]));
            diceImage.setVisibility(View.VISIBLE);
            if (tvDiceResult != null) {
                tvDiceResult.setText(getString(R.string.dice_result_single, results[0]));
                tvDiceResult.setVisibility(View.VISIBLE);
            }
        } else {
            dices1Image.setImageResource(getDiceDrawable(results[0]));
            dices2Image.setImageResource(getDiceDrawable(results[1]));
            dicesContainer.setVisibility(View.VISIBLE);
            if (tvDiceResult != null) {
                boolean isDouble = results[0] == results[1];
                String resultText = isDouble
                        ? getString(R.string.dice_result_double, results[0])
                        : getString(R.string.dice_result_two, results[0], results[1], results[0] + results[1]);
                tvDiceResult.setText(resultText);
                tvDiceResult.setVisibility(View.VISIBLE);
            }
        }
    }

    private void playRollSound(int[] results) {
        hapticManager.success();
        // Play combo sound for doubles, correct sound otherwise
        if (results.length == 2 && results[0] == results[1]) {
            soundManager.playCombo();
        } else {
            soundManager.playCorrectAnswer();
        }
    }

    private int rollSingleDie() {
        return new Random().nextInt(6) + 1;
    }

    private int getDiceDrawable(int number) {
        switch (number) {
            case 1: return R.drawable.dice1;
            case 2: return R.drawable.dice2;
            case 3: return R.drawable.dice3;
            case 4: return R.drawable.dice4;
            case 5: return R.drawable.dice5;
            default: return R.drawable.dice6;
        }
    }
}