package com.example.sadaguessgame.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Objects;

import com.example.sadaguessgame.R;
import com.example.sadaguessgame.activities.GameScoreActivity;
import com.example.sadaguessgame.data.GameState;
import com.example.sadaguessgame.data.ScoreStorage;
import com.google.android.material.button.MaterialButton;

public class ScoreDialog {

    private final Dialog dialog;
    private final Context context;
    private GameState currentGame;

    public ScoreDialog(@NonNull Context context) {
        this.context = context;
        this.currentGame = ScoreStorage.getInstance(context).getCurrentGame();
        // Initialize dialog
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.score_dialog);
        dialog.setCancelable(false); // cannot dismiss with back button
        dialog.setCanceledOnTouchOutside(false); // we'll handle outside taps

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        setupButtons();
        setupOutsideShake();
    }

    private void setupButtons() {
        MaterialButton btn1 = dialog.findViewById(R.id.btn_1);
        MaterialButton btn2 = dialog.findViewById(R.id.btn_2);
        MaterialButton btn3 = dialog.findViewById(R.id.btn_3);

        // Save selected score to SharedRepository and dismiss
        btn1.setOnClickListener(v -> saveAndDismiss(1));
        btn2.setOnClickListener(v -> saveAndDismiss(2));
        btn3.setOnClickListener(v -> saveAndDismiss(3));
    }

    private void saveAndDismiss(int score) {
        if (currentGame.groupTurn == 0) {
            List<Integer> scoresA = currentGame.scoresA; // get existing scores
            scoresA.add(score); // add new score
            currentGame.scoresA = scoresA; // save updated list
        } else {
            List<Integer> scoresB = currentGame.scoresB; // get existing scores
            scoresB.add(score); // add new score
            currentGame.scoresB = scoresB; // save updated list
        }
        ScoreStorage.getInstance(context).saveCurrentGame(currentGame);
        dismiss();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupOutsideShake() {
        dialog.setOnShowListener(d -> {
            View decorView = Objects.requireNonNull(dialog.getWindow()).getDecorView();
            View content = dialog.findViewById(R.id.dialog_root); // root layout

            decorView.setOnTouchListener((v, event) -> {
                // Detect outside touch
                int[] location = new int[2];
                content.getLocationOnScreen(location);

                float x = event.getRawX();
                float y = event.getRawY();

                if (x < location[0] || x > location[0] + content.getWidth() ||
                        y < location[1] || y > location[1] + content.getHeight()) {

                    // Play shake animation
                    Animation shake = AnimationUtils.loadAnimation(context, R.anim.shake);
                    content.startAnimation(shake);
                    return true; // consume outside touch
                }

                return false; // inside touch passes normally
            });
        });
    }

    // Show the dialog
    public void show() {
        dialog.show();
    }

    // Dismiss the dialog manually if needed
    public void dismiss() {
        Intent intent = new Intent(context, GameScoreActivity.class);
        context.startActivity(intent);
        dialog.dismiss();
    }
}
