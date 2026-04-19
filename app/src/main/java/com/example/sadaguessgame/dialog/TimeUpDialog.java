package com.example.sadaguessgame.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.example.sadaguessgame.R;
import com.example.sadaguessgame.activities.GameScoreActivity;
import com.example.sadaguessgame.data.GameState;
import com.example.sadaguessgame.data.ScoreStorage;
import com.google.android.material.button.MaterialButton;
import java.util.Objects;

public class TimeUpDialog {

    private final Dialog dialog;
    private final Context context;

    public TimeUpDialog(@NonNull Context context) {
        this.context = context;

        dialog = new Dialog(context);
        dialog.setContentView(R.layout.time_up_dialog);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        updateGroupText();
        setupButtons();
        setupOutsideShake();
    }

    /** Fix: dynamically set the correct group question text */
    private void updateGroupText() {
        GameState game = ScoreStorage.getInstance(context).getCurrentGame();
        if (game == null) return;

        TextView dialogText = dialog.findViewById(R.id.dialogText);
        if (dialogText != null) {
            dialogText.setText(
                    game.groupTurn == GameState.GROUP_A
                            ? R.string.group_turn_found_A
                            : R.string.group_turn_found_B
            );
        }
    }

    private void setupButtons() {
        MaterialButton btnYes = dialog.findViewById(R.id.btn_yes);
        MaterialButton btnNo = dialog.findViewById(R.id.btn_no);

        btnYes.setOnClickListener(v -> {
            dismiss();
            new ScoreDialog(context)
                    .setOnScoreSavedListener(() -> {
                        context.startActivity(new Intent(context, GameScoreActivity.class));
                    })
                    .show();
        });

        btnNo.setOnClickListener(v -> {
            addZeroScore();
            navigateToScoreActivity();
            dismiss();
        });
    }

    private void addZeroScore() {
        GameState game = ScoreStorage.getInstance(context).getCurrentGame();
        if (game == null) return;

        if (game.groupTurn == GameState.GROUP_A) {
            game.scoresA.add(0);
        } else {
            game.scoresB.add(0);
        }
        ScoreStorage.getInstance(context).saveCurrentGame(game);
    }

    private void navigateToScoreActivity() {
        context.startActivity(new Intent(context, GameScoreActivity.class));
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupOutsideShake() {
        dialog.setOnShowListener(d -> {
            View decorView = Objects.requireNonNull(dialog.getWindow()).getDecorView();
            View content = dialog.findViewById(R.id.dialog_root);
            if (content == null) return;

            decorView.setOnTouchListener((v, event) -> {
                int[] location = new int[2];
                content.getLocationOnScreen(location);

                float x = event.getRawX();
                float y = event.getRawY();

                boolean outsideX = x < location[0] || x > location[0] + content.getWidth();
                boolean outsideY = y < location[1] || y > location[1] + content.getHeight();

                if (outsideX || outsideY) {
                    content.startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake));
                    return true;
                }
                return false;
            });
        });
    }

    public void show() { dialog.show(); }
    public void dismiss() { dialog.dismiss(); }
}