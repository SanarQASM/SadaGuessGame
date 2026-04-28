package com.example.sadaguessgame.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import com.example.sadaguessgame.R;
import com.example.sadaguessgame.data.GameState;
import com.example.sadaguessgame.data.ScoreStorage;
import com.example.sadaguessgame.manager.ShareManager;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class GameScoreActivity extends BaseActivity {

    private TableLayout    tableLayoutScores;
    private ScrollView     scrollView;
    private MaterialButton btnContinueGame, btnEndGame, btnShare;
    private GameState      currentGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.score_activity);

        currentGame = ScoreStorage.getInstance(this).getCurrentGame();
        if (currentGame == null) { finish(); return; }

        initViews();
        setHeaderNames();
        populateScoreTable();
        setupButtons();
        checkSkipCondition();
    }

    private void initViews() {
        tableLayoutScores = findViewById(R.id.tableLayoutScores);
        scrollView        = findViewById(R.id.scrollLayoutScores);
        btnContinueGame   = findViewById(R.id.btnContinueGame);
        btnEndGame        = findViewById(R.id.btnEndGame);
        btnShare          = findViewById(R.id.btnShareResult);
    }

    private void setHeaderNames() {
        ((TextView) findViewById(R.id.groupAName)).setText(currentGame.groupAName);
        ((TextView) findViewById(R.id.groupBName)).setText(currentGame.groupBName);
    }

    // ─── Score table ─────────────────────────────────────────────────────────

    private void populateScoreTable() {
        List<Integer> scoresA  = safe(currentGame.scoresA);
        List<Integer> scoresB  = safe(currentGame.scoresB);
        List<Boolean> comboA   = safe(currentGame.comboScoresA);
        List<Boolean> comboB   = safe(currentGame.comboScoresB);
        int rounds = Math.max(scoresA.size(), scoresB.size());

        tableLayoutScores.removeAllViews();

        String comboStar    = getString(R.string.combo_star_char);
        String placeholder  = getString(R.string.score_placeholder);

        for (int i = 0; i < rounds; i++) {
            TableRow row = new TableRow(this);
            row.setPadding(12, 12, 12, 12);

            row.addView(createCell(String.valueOf(i + 1), false, false));

            boolean aCombo = i < comboA.size() && Boolean.TRUE.equals(comboA.get(i));
            String  aText  = i < scoresA.size()
                    ? scoresA.get(i) + (aCombo ? comboStar : "") : placeholder;
            row.addView(createCell(aText, aCombo, false));

            boolean bCombo = i < comboB.size() && Boolean.TRUE.equals(comboB.get(i));
            String  bText  = i < scoresB.size()
                    ? scoresB.get(i) + (bCombo ? comboStar : "") : placeholder;
            row.addView(createCell(bText, bCombo, false));

            tableLayoutScores.addView(row);
        }

        // Streak summary row
        TableRow streakRow = new TableRow(this);
        streakRow.setPadding(12, 8, 12, 8);
        streakRow.addView(createCell(getString(R.string.score_table_streak_icon), false, true));
        streakRow.addView(createCell(getString(R.string.streak_best_format, currentGame.maxStreakA), false, true));
        streakRow.addView(createCell(getString(R.string.streak_best_format, currentGame.maxStreakB), false, true));
        tableLayoutScores.addView(streakRow);

        // Total row
        TableRow totalRow = new TableRow(this);
        totalRow.setPadding(12, 16, 12, 12);
        totalRow.addView(createCell(getString(R.string.total_label), false, true));
        totalRow.addView(createCell(String.valueOf(currentGame.getTotalScoreA()), false, true));
        totalRow.addView(createCell(String.valueOf(currentGame.getTotalScoreB()), false, true));
        tableLayoutScores.addView(totalRow);

        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private TextView createCell(String text, boolean isCombo, boolean isBold) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(isCombo
                ? 0xFFFFAA00
                : getResources().getColor(R.color.primary_text, null));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.sub_font_size));
        if (isBold) tv.setTypeface(null, android.graphics.Typeface.BOLD);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            tv.setTypeface(getResources().getFont(R.font.subtext_font));
        }
        int pad = (int) getResources().getDimension(R.dimen.primary_size_layout);
        tv.setPadding(pad, pad, pad, pad);
        tv.setGravity(Gravity.CENTER);
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        params.setMargins(pad, pad, pad, pad);
        tv.setLayoutParams(params);
        return tv;
    }

    // ─── Skip check ──────────────────────────────────────────────────────────

    private void checkSkipCondition() {
        if (currentGame.canSkipRemainingRounds()) {
            String leader = currentGame.getTotalScoreA() > currentGame.getTotalScoreB()
                    ? currentGame.groupAName : currentGame.groupBName;
            Toast.makeText(this,
                    leader + " " + getString(R.string.winner_desc),
                    Toast.LENGTH_LONG).show();
            btnContinueGame.setEnabled(false);
        }
    }

    // ─── Buttons ─────────────────────────────────────────────────────────────

    private void setupButtons() {
        btnContinueGame.setOnClickListener(v -> {
            advanceGameTurn();
            if (currentGame.isFinished) resolveFinish();
            else navigateToCards();
        });

        btnEndGame.setOnClickListener(v -> {
            currentGame.isFinished = true;
            ScoreStorage.getInstance(this).saveCurrentGame(currentGame);
            resolveFinish();
        });

        if (btnShare != null) {
            btnShare.setOnClickListener(v -> {
                boolean ok = ShareManager.shareGameSummary(this, currentGame);
                if (!ok) Toast.makeText(this, R.string.share_failed, Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void resolveFinish() {
        if (currentGame.isExactDraw() && !currentGame.isSuddenDeath) {
            startActivity(new Intent(this, SuddenDeathActivity.class));
        } else {
            navigateToWinner();
        }
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void advanceGameTurn() {
        if (currentGame == null) return;

        if (currentGame.groupTurn == GameState.GROUP_A) {
            currentGame.turnGroupAFinish = true;
            currentGame.groupTurn        = GameState.GROUP_B;
        } else {
            currentGame.turnGroupBFinish = true;
            currentGame.groupTurn        = GameState.GROUP_A;
        }

        if (currentGame.isRoundComplete()) {
            if (currentGame.isGameOver()) {
                currentGame.isFinished = true;
            } else {
                currentGame.currentRound++;
                currentGame.turnGroupAFinish = false;
                currentGame.turnGroupBFinish = false;
            }
        }

        if (!currentGame.isFinished && currentGame.canSkipRemainingRounds()) {
            currentGame.isFinished = true;
        }

        ScoreStorage.getInstance(this).saveCurrentGame(currentGame);
    }

    private void navigateToCards() {
        startActivity(new Intent(this, CardsActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void navigateToWinner() {
        startActivity(new Intent(this, WinnerActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private <T> List<T> safe(List<T> list) {
        return list != null ? list : new ArrayList<>();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (currentGame != null)
            ScoreStorage.getInstance(this).saveCurrentGame(currentGame);
    }
}