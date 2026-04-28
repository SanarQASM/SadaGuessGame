package com.example.sadaguessgame.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.sadaguessgame.R;
import com.example.sadaguessgame.data.GameState;
import com.example.sadaguessgame.data.ScoreStorage;
import com.example.sadaguessgame.manager.SoundManager;
import com.google.android.material.button.MaterialButton;
import java.util.Objects;

public class ScoreDialog {

    public interface OnScoreSavedListener { void onScoreSaved(); }

    private final Dialog              dialog;
    private final Context             context;
    @Nullable private OnScoreSavedListener listener;

    public ScoreDialog(@NonNull Context context) {
        this.context = context;

        dialog = new Dialog(context);
        dialog.setContentView(R.layout.score_dialog);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        bindStreakBadge();
        setupButtons();
        setupOutsideShake();
    }

    public ScoreDialog setOnScoreSavedListener(@Nullable OnScoreSavedListener l) {
        this.listener = l;
        return this;
    }

    // ─── Streak badge ────────────────────────────────────────────────────────

    private void bindStreakBadge() {
        GameState game = ScoreStorage.getInstance(context).getCurrentGame();
        if (game == null) return;

        int streak = game.getCurrentStreak(game.groupTurn);
        TextView tvStreak = dialog.findViewById(R.id.tvStreakBadge);
        if (tvStreak == null) return;

        if (streak >= 3) {
            tvStreak.setVisibility(View.VISIBLE);
            tvStreak.setText(context.getString(R.string.streak_fire_format, streak));
        } else if (streak == 2) {
            tvStreak.setVisibility(View.VISIBLE);
            tvStreak.setText(context.getString(R.string.streak_two_in_row_format, streak));
        } else {
            tvStreak.setVisibility(View.GONE);
        }
    }

    // ─── Buttons ─────────────────────────────────────────────────────────────

    private void setupButtons() {
        MaterialButton btn1 = dialog.findViewById(R.id.btn_1);
        MaterialButton btn2 = dialog.findViewById(R.id.btn_2);
        MaterialButton btn3 = dialog.findViewById(R.id.btn_3);

        if (btn1 != null) btn1.setOnClickListener(v -> saveAndDismiss(1));
        if (btn2 != null) btn2.setOnClickListener(v -> saveAndDismiss(2));
        if (btn3 != null) btn3.setOnClickListener(v -> saveAndDismiss(3));
    }

    private void saveAndDismiss(int baseScore) {
        GameState game = ScoreStorage.getInstance(context).getCurrentGame();
        if (game == null) { dismiss(); return; }

        float multiplier = game.recordCorrectGuess(game.groupTurn);
        int finalScore = Math.min(3, Math.round(baseScore * multiplier));
        boolean comboApplied = (multiplier > 1.0f);

        if (game.groupTurn == GameState.GROUP_A) {
            game.scoresA.add(finalScore);
            game.comboScoresA.add(comboApplied);
        } else {
            game.scoresB.add(finalScore);
            game.comboScoresB.add(comboApplied);
        }

        ScoreStorage.getInstance(context).saveCurrentGame(game);

        SoundManager sm = SoundManager.getInstance(context);
        sm.playCorrectAnswer();
        int streak = game.getCurrentStreak(game.groupTurn);
        if (streak >= 3) sm.playCombo();

        dismiss();
        if (listener != null) listener.onScoreSaved();
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