package com.example.sadaguessgame.activities;

import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.animation.Animator;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.example.sadaguessgame.R;
import com.google.android.material.button.MaterialButton;

import java.util.Random;

public class DiceActivity extends BaseActivity {

    private MaterialButton btn1Dice, btn2Dice;
    private MaterialButton btn0, btn_1, btn_2, btn_3;
    private ImageView diceImage, dices1Image, dices2Image;
    private LinearLayout dicesContainer;
    private LottieAnimationView diceLottie;
    private LinearLayout btn_container;
    private CheckBox checkboxSkipAnimation;

    private int selectedDiceCount = 1;
    private int selectedProbability = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dice_activity);

        btn1Dice            = findViewById(R.id.btn1Dice);
        btn2Dice            = findViewById(R.id.btn2Dice);
        btn0                = findViewById(R.id.btn0);
        btn_1               = findViewById(R.id.btn_1);
        btn_2               = findViewById(R.id.btn_2);
        btn_3               = findViewById(R.id.btn_3);
        MaterialButton startRollDice = findViewById(R.id.startRollDice);
        diceImage           = findViewById(R.id.diceImage);
        dices1Image         = findViewById(R.id.dices1Image);
        dices2Image         = findViewById(R.id.dices2Image);
        dicesContainer      = findViewById(R.id.dicesContainer);
        diceLottie          = findViewById(R.id.diceLottie);
        ImageView backHomeButton = findViewById(R.id.back_home_activity_button);
        btn_container       = findViewById(R.id.btn_container);
        checkboxSkipAnimation = findViewById(R.id.checkboxSkipAnimation);

        selectedDiceCount   = 1;
        selectedProbability = 0;
        updateDiceButtonBackground();
        setProbability(selectedProbability, btn0);

        backHomeButton.setOnClickListener(v -> {
            Intent intent = new Intent(DiceActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });

        btn1Dice.setOnClickListener(v -> {
            selectedDiceCount = 1;
            updateDiceButtonBackground();
        });

        btn2Dice.setOnClickListener(v -> {
            selectedDiceCount = 2;
            updateDiceButtonBackground();
        });

        btn0.setOnClickListener(v -> setProbability(0, btn0));
        btn_1.setOnClickListener(v -> setProbability(25, btn_1));
        btn_2.setOnClickListener(v -> setProbability(50, btn_2));
        btn_3.setOnClickListener(v -> setProbability(75, btn_3));

        startRollDice.setOnClickListener(v -> {
            if (checkboxSkipAnimation.isChecked()) {
                rollDiceWithoutAnimation();
            } else {
                rollDiceWithAnimation();
            }
        });
    }

    private void updateDiceButtonBackground() {
        if (selectedDiceCount == 1) {
            btn_container.setEnabled(false);
            btn_container.setClickable(false);
            btn_container.setAlpha(0.5F);
            btn0.setEnabled(false);
            btn_1.setEnabled(false);
            btn_2.setEnabled(false);
            btn_3.setEnabled(false);
            btn1Dice.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary_color));
            btn2Dice.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.secondary_color));
        } else {
            btn_container.setEnabled(true);
            btn_container.setClickable(true);
            btn_container.setAlpha(1);
            btn0.setEnabled(true);
            btn_1.setEnabled(true);
            btn_2.setEnabled(true);
            btn_3.setEnabled(true);
            btn2Dice.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary_color));
            btn1Dice.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.secondary_color));
        }
    }

    private void setProbability(int probability, MaterialButton selectedButton) {
        selectedProbability = probability;
        btn0.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.secondary_color));
        btn_1.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.secondary_color));
        btn_2.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.secondary_color));
        btn_3.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.secondary_color));
        selectedButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary_color));
    }

    private void rollDiceWithoutAnimation() {
        diceImage.setVisibility(View.GONE);
        diceLottie.setVisibility(View.GONE);
        dicesContainer.setVisibility(View.GONE);

        if (selectedDiceCount == 1) {
            int diceNumber = rollSingleDie();
            diceImage.setImageResource(getDiceDrawable(diceNumber));
            diceImage.setVisibility(View.VISIBLE);
        } else {
            // FIX: roll both dice; if probability triggers, force both to same value
            int[] results = rollTwoDice();
            dices1Image.setImageResource(getDiceDrawable(results[0]));
            dices2Image.setImageResource(getDiceDrawable(results[1]));
            dicesContainer.setVisibility(View.VISIBLE);
        }
    }

    private void rollDiceWithAnimation() {
        diceImage.setVisibility(View.GONE);
        dicesContainer.setVisibility(View.GONE);

        diceLottie.setVisibility(View.VISIBLE);
        diceLottie.removeAllAnimatorListeners();
        diceLottie.cancelAnimation();

        if (selectedDiceCount == 1) {
            diceLottie.setAnimation(R.raw.dice);
        } else {
            diceLottie.setAnimation(R.raw.dices);
        }

        diceLottie.setProgress(0f);
        diceLottie.setRepeatCount(0);

        diceLottie.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                diceLottie.setVisibility(View.GONE);

                if (selectedDiceCount == 1) {
                    int diceNumber = rollSingleDie();
                    diceImage.setImageResource(getDiceDrawable(diceNumber));
                    diceImage.setVisibility(View.VISIBLE);
                } else {
                    // FIX: use rollTwoDice() for correct matched-pair logic
                    int[] results = rollTwoDice();
                    dices1Image.setImageResource(getDiceDrawable(results[0]));
                    dices2Image.setImageResource(getDiceDrawable(results[1]));
                    dicesContainer.setVisibility(View.VISIBLE);
                }
            }
        });

        diceLottie.playAnimation();
    }

    private int rollSingleDie() {
        return new Random().nextInt(6) + 1;
    }

    /**
     * FIX: Roll two dice. If the probability trigger fires, both dice get the
     * same value (true double). Previously, each die called rollWithProbability()
     * independently so they never actually matched.
     */
    private int[] rollTwoDice() {
        int d1 = rollSingleDie();
        int d2 = rollSingleDie();

        if (selectedProbability > 0) {
            int chance = new Random().nextInt(100);
            if (chance < selectedProbability) {
                // Force a double — both dice show the same face
                d2 = d1;
            }
        }

        return new int[]{d1, d2};
    }

    private int getDiceDrawable(int number) {
        switch (number) {
            case 1: return R.drawable.dice1;
            case 2: return R.drawable.dice2;
            case 3: return R.drawable.dice3;
            case 4: return R.drawable.dice4;
            case 5: return R.drawable.dice5;
            case 6:
            default: return R.drawable.dice6;
        }
    }
}