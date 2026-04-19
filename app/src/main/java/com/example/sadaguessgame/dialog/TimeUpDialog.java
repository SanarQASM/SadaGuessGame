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

import com.example.sadaguessgame.R;
import com.example.sadaguessgame.activities.GameScoreActivity;
import com.example.sadaguessgame.data.GameState;
import com.example.sadaguessgame.data.ScoreStorage;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Objects;

public class TimeUpDialog {

    private final Dialog dialog;
    private final Context context;
    private GameState currentGame;

    public TimeUpDialog(Context context) {
        this.context = context;
        this.currentGame = ScoreStorage.getInstance(context).getCurrentGame();

        // Initialize dialog
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.time_up_dialog);
        dialog.setCancelable(false); // user cannot dismiss with back button
        dialog.setCanceledOnTouchOutside(false); // we handle outside touches ourselves

        // Make the window background transparent
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        setupButtons();
        setupOutsideShake();
    }

    private void setupButtons() {
        MaterialButton btnYes = dialog.findViewById(R.id.btn_yes);
        MaterialButton btnNo = dialog.findViewById(R.id.btn_no);

        // Yes → open score dialog
        btnYes.setOnClickListener(v -> {
            dismiss();
            ScoreDialog scoreDialog = new ScoreDialog(context);
            scoreDialog.show();
        });

        // No → open score activity
        btnNo.setOnClickListener(v -> {
            if (currentGame.groupTurn == 0) {
                List<Integer> scoresA = currentGame.scoresA; // get existing scores
                scoresA.add(0); // add new score
                currentGame.scoresA = scoresA; // save updated list
            } else {
                List<Integer> scoresB = currentGame.scoresB; // get existing scores
                scoresB.add(0); // add new score
                currentGame.scoresB = scoresB; // save updated list
            }
            ScoreStorage.getInstance(context).saveCurrentGame(currentGame);
            Intent intent = new Intent(context, GameScoreActivity.class);
            context.startActivity(intent);
            dismiss();
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupOutsideShake() {
        dialog.setOnShowListener(d -> {
            // Get the decor view (entire window) and root content layout
            View decorView = Objects.requireNonNull(dialog.getWindow()).getDecorView();
            View content = dialog.findViewById(R.id.dialog_root); // make sure your ConstraintLayout root has this ID

            decorView.setOnTouchListener((v, event) -> {
                // Detect touch outside the dialog content
                int[] location = new int[2];
                content.getLocationOnScreen(location);

                float x = event.getRawX();
                float y = event.getRawY();

                if (x < location[0] || x > location[0] + content.getWidth() ||
                        y < location[1] || y > location[1] + content.getHeight()) {

                    // Shake animation
                    Animation shake = AnimationUtils.loadAnimation(context, R.anim.shake);
                    content.startAnimation(shake);

                    return true; // consume outside touch
                }

                return false; // let clicks inside content work normally
            });
        });
    }

    // Show the dialog
    public void show() {
        dialog.show();
    }

    // Dismiss the dialog
    public void dismiss() {
        dialog.dismiss();
    }
}
