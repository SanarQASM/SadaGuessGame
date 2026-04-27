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

/**
 * "Time is up!" dialog — shown when the countdown reaches zero.
 *
 * Changes in v2:
 *  • "No" path calls game.recordMiss() to reset the streak counter.
 *  • Groups' names are shown in the dialog text dynamically.
 */
public class TimeUpDialog {

    private final Dialog  dialog;
    private final Context context;

    public TimeUpDialog(@NonNull Context context) {
        this.context = context;

        dialog = new Dialog(context);
        dialog.setContentView(R.layout.time_up_dialog);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        updateGroupText();
        setupButtons();
        setupOutsideShake();
    }

    // ─── Group text ──────────────────────────────────────────────────────────

    private void updateGroupText() {
        GameState game = ScoreStorage.getInstance(context).getCurrentGame();
        if (game == null) return;

        TextView dialogText  = dialog.findViewById(R.id.dialogText);
        TextView dialogTitle = dialog.findViewById(R.id.dialogTitle);

        if (dialogTitle != null) dialogTitle.setText(R.string.time_up);

        if (dialogText != null) {
            // Show group name + question
            String groupName = game.getCurrentGroupName();
            String question  = context.getString(
                    game.groupTurn == GameState.GROUP_A
                            ? R.string.group_turn_found_A
                            : R.string.group_turn_found_B);
            dialogText.setText(groupName + " — " + question);
        }
    }

    // ─── Buttons ─────────────────────────────────────────────────────────────

    private void setupButtons() {
        MaterialButton btnYes = dialog.findViewById(R.id.btn_yes);
        MaterialButton btnNo  = dialog.findViewById(R.id.btn_no);

        // YES → open score picker
        btnYes.setOnClickListener(v -> {
            dismiss();
            new ScoreDialog(context)
                    .setOnScoreSavedListener(() -> navigateToScore())
                    .show();
        });

        // NO → record miss (streak reset), score 0
        btnNo.setOnClickListener(v -> {
            addZeroScoreAndResetStreak();
            dismiss();
            navigateToScore();
        });
    }

    private void addZeroScoreAndResetStreak() {
        GameState game = ScoreStorage.getInstance(context).getCurrentGame();
        if (game == null) return;

        game.recordMiss(game.groupTurn);

        if (game.groupTurn == GameState.GROUP_A) {
            game.scoresA.add(0);
            game.comboScoresA.add(false);
        } else {
            game.scoresB.add(0);
            game.comboScoresB.add(false);
        }

        ScoreStorage.getInstance(context).saveCurrentGame(game);
    }

    private void navigateToScore() {
        Intent intent = new Intent(context, GameScoreActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    // ─── Outside shake ───────────────────────────────────────────────────────

    @SuppressLint("ClickableViewAccessibility")
    private void setupOutsideShake() {
        dialog.setOnShowListener(d -> {
            View decorView = Objects.requireNonNull(dialog.getWindow()).getDecorView();
            View content   = dialog.findViewById(R.id.dialog_root);
            if (content == null) return;
            decorView.setOnTouchListener((v, event) -> {
                int[] loc = new int[2];
                content.getLocationOnScreen(loc);
                float x = event.getRawX(), y = event.getRawY();
                if (x < loc[0] || x > loc[0] + content.getWidth()
                        || y < loc[1] || y > loc[1] + content.getHeight()) {
                    content.startAnimation(
                            AnimationUtils.loadAnimation(context, R.anim.shake));
                    return true;
                }
                return false;
            });
        });
    }

    public void show()    { dialog.show(); }
    public void dismiss() { if (dialog.isShowing()) dialog.dismiss(); }
}