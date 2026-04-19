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

    private int selectedDiceCount = 1; // 1 or 2
    private int selectedProbability = 0; // 0, 25, 50, 75

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dice_activity);

        // -------------------- INIT VIEWS --------------------
        btn1Dice = findViewById(R.id.btn1Dice);
        btn2Dice = findViewById(R.id.btn2Dice);
        btn0 = findViewById(R.id.btn0);
        btn_1 = findViewById(R.id.btn_1);
        btn_2 = findViewById(R.id.btn_2);
        btn_3 = findViewById(R.id.btn_3);
        MaterialButton startRollDice = findViewById(R.id.startRollDice);
        diceImage = findViewById(R.id.diceImage);
        dices1Image = findViewById(R.id.dices1Image);
        dices2Image = findViewById(R.id.dices2Image);
        dicesContainer = findViewById(R.id.dicesContainer);
        diceLottie = findViewById(R.id.diceLottie);
        ImageView backHomeButton = findViewById(R.id.back_home_activity_button);
        btn_container = findViewById(R.id.btn_container);
        checkboxSkipAnimation = findViewById(R.id.checkboxSkipAnimation);


        // -------------------- SET DEFAULT SELECTION --------------------
        selectedDiceCount = 1; // default to 1 dice
        selectedProbability = 0; // default probability 0
        updateDiceButtonBackground(); // sets btn1Dice selected
        setProbability(selectedProbability, btn0); // sets btn0 selected

        // -------------------- BACK BUTTON --------------------
        backHomeButton.setOnClickListener(v -> {
            Intent intent = new Intent(DiceActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });

        // -------------------- DICE SELECTION --------------------
        btn1Dice.setOnClickListener(v -> {
            selectedDiceCount = 1;
            updateDiceButtonBackground();
        });

        btn2Dice.setOnClickListener(v -> {
            selectedDiceCount = 2;
            updateDiceButtonBackground();
        });

        // -------------------- PROBABILITY BUTTONS --------------------
        btn0.setOnClickListener(v -> setProbability(0, btn0));
        btn_1.setOnClickListener(v -> setProbability(25, btn_1));
        btn_2.setOnClickListener(v -> setProbability(50, btn_2));
        btn_3.setOnClickListener(v -> setProbability(75, btn_3));

        // -------------------- START ROLL DICE --------------------
        startRollDice.setOnClickListener(v -> {
            if(checkboxSkipAnimation.isChecked()){
                rollDiceWithoutAnimation();
            }else rollDiceWithAnimation();
        });
    }

    // -------------------- HELPER METHODS --------------------
    private void updateDiceButtonBackground() {
        if (selectedDiceCount == 1) {
            btn_container.setEnabled(false);
            btn_container.setClickable(false);
            btn_container.setAlpha(0.5F);
            btn0.setEnabled(false);
            btn_1.setEnabled(false);
            btn_2.setEnabled(false);
            btn_3.setEnabled(false);
            btn1Dice.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary_color)); // PrimaryButtonStyleSelected
            btn2Dice.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.secondary_color)); // PrimaryButtonStyleSelected
        } else {
            btn_container.setEnabled(true);
            btn_container.setClickable(true);
            btn_container.setAlpha(1);
            btn0.setEnabled(true);
            btn_1.setEnabled(true);
            btn_2.setEnabled(true);
            btn_3.setEnabled(true);
            btn2Dice.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary_color)); // PrimaryButtonStyleSelected
            btn1Dice.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.secondary_color)); // PrimaryButtonStyleSelected
        }
    }

    private void setProbability(int probability, MaterialButton selectedButton) {
        selectedProbability = probability;

        // Reset all buttons first
        btn0.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.secondary_color));
        btn_1.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.secondary_color));
        btn_2.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.secondary_color));
        btn_3.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.secondary_color));

        // Highlight selected
        selectedButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary_color));
    }

    private void rollDiceWithoutAnimation() {
        // 1. Hide results
        diceImage.setVisibility(View.GONE);
        diceLottie.setVisibility(View.GONE);
        dicesContainer.setVisibility(View.GONE);

        if (selectedDiceCount == 1) {
            int diceNumber = rollWithProbability();
            diceImage.setImageResource(getDiceDrawable(diceNumber));
            diceImage.setVisibility(View.VISIBLE);
        } else {
            int d1 = rollWithProbability();
            int d2 = rollWithProbability();
            dices1Image.setImageResource(getDiceDrawable(d1));
            dices2Image.setImageResource(getDiceDrawable(d2));
            dicesContainer.setVisibility(View.VISIBLE);
        }


    }

    private void rollDiceWithAnimation() {
        // 1. Hide results
        diceImage.setVisibility(View.GONE);
        dicesContainer.setVisibility(View.GONE);

        // 2. Prepare Lottie
        diceLottie.setVisibility(View.VISIBLE);
        diceLottie.removeAllAnimatorListeners();
        diceLottie.cancelAnimation();

        // 3. Set animation FIRST
        if (selectedDiceCount == 1) {
            diceLottie.setAnimation(R.raw.dice);
        } else {
            diceLottie.setAnimation(R.raw.dices);
        }

        // 4. Reset progress AFTER setting animation
        diceLottie.setProgress(0f);
        diceLottie.setRepeatCount(0);

        // 5. Add listener
        diceLottie.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Hide animation
                diceLottie.setVisibility(View.GONE);

                // Show result
                if (selectedDiceCount == 1) {
                    int diceNumber = rollWithProbability();
                    diceImage.setImageResource(getDiceDrawable(diceNumber));
                    diceImage.setVisibility(View.VISIBLE);
                } else {
                    int d1 = rollWithProbability();
                    int d2 = rollWithProbability();
                    dices1Image.setImageResource(getDiceDrawable(d1));
                    dices2Image.setImageResource(getDiceDrawable(d2));
                    dicesContainer.setVisibility(View.VISIBLE);
                }
            }
        });

        // 6. Play
        diceLottie.playAnimation();
    }




    private int rollSingleDie() {
        return new Random().nextInt(6) + 1;
    }

    private int rollWithProbability() {
        int result = rollSingleDie();
        if (selectedDiceCount == 2 && selectedProbability > 0) {
            int chance = new Random().nextInt(100);
            if (chance < selectedProbability) {
                // Force a matching pair — both dice show the same value
                return result; // caller stores this for BOTH dice
            }
        }
        return result;
    }


    private int getDiceDrawable(int number) {
        switch (number) {
            case 1:
                return R.drawable.dice1;
            case 2:
                return R.drawable.dice2;
            case 3:
                return R.drawable.dice3;
            case 4:
                return R.drawable.dice4;
            case 5:
                return R.drawable.dice5;
            case 6:
            default:
                return R.drawable.dice6;
        }
    }
}