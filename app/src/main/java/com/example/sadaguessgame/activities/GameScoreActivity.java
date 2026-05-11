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
    private TextView       tvTotalScoreA, tvTotalScoreB;
    private TextView       tvRoundInfo;
    private GameState      currentGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.score_activity);

        currentGame = ScoreStorage.getInstance(this).getCurrentGame();
        if (currentGame == null) { finish(); return; }

        initViews();
        setHeaderNames();
        updateLiveScoreTotals();
        updateRoundInfo();
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
        tvTotalScoreA     = findViewById(R.id.tvTotalScoreA);
        tvTotalScoreB     = findViewById(R.id.tvTotalScoreB);
        tvRoundInfo       = findViewById(R.id.tvRoundInfo);
    }

    private void setHeaderNames() {
        TextView nameA = findViewById(R.id.groupAName);
        TextView nameB = findViewById(R.id.groupBName);
        if (nameA != null) nameA.setText(currentGame.groupAName);
        if (nameB != null) nameB.setText(currentGame.groupBName);
    }

    private void updateLiveScoreTotals() {
        if (tvTotalScoreA != null)
            tvTotalScoreA.setText(String.valueOf(currentGame.getTotalScoreA()));
        if (tvTotalScoreB != null)
            tvTotalScoreB.setText(String.valueOf(currentGame.getTotalScoreB()));
    }

    private void updateRoundInfo() {
        if (tvRoundInfo == null) return;
        tvRoundInfo.setText(getString(R.string.round_of_format,
                currentGame.currentRound, currentGame.totalRounds));
    }

    // ─── Score table ─────────────────────────────────────────────────────────

    private void populateScoreTable() {
        List<Integer> scoresA = safe(currentGame.scoresA);
        List<Integer> scoresB = safe(currentGame.scoresB);
        List<Boolean> comboA  = safe(currentGame.comboScoresA);
        List<Boolean> comboB  = safe(currentGame.comboScoresB);
        int rounds = Math.max(scoresA.size(), scoresB.size());

        tableLayoutScores.removeAllViews();

        // Header row
        TableRow header = new TableRow(this);
        header.setPadding(0, 4, 0, 8);
        header.addView(createCell(getString(R.string.round), false, true, true));
        header.addView(createCell(currentGame.groupAName, false, true, true));
        header.addView(createCell(currentGame.groupBName, false, true, true));
        tableLayoutScores.addView(header);

        // Divider under header
        View hdiv = new View(this);
        hdiv.setBackgroundColor(getResources().getColor(R.color.divider_color, null));
        TableLayout.LayoutParams lp =
                new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, 1);
        lp.setMargins(8, 0, 8, 4);
        hdiv.setLayoutParams(lp);
        tableLayoutScores.addView(hdiv);

        String comboStar   = getString(R.string.combo_star_char);
        String placeholder = getString(R.string.score_placeholder);

        for (int i = 0; i < rounds; i++) {
            TableRow row = new TableRow(this);
            row.setPadding(0, 6, 0, 6);

            // Alternate background
            if (i % 2 == 0) {
                row.setBackgroundColor(
                        getResources().getColor(R.color.surface_elevated, null));
            }

            row.addView(createCell(String.valueOf(i + 1), false, false, false));

            boolean aCombo = i < comboA.size() && Boolean.TRUE.equals(comboA.get(i));
            String  aText  = i < scoresA.size()
                    ? scoresA.get(i) + (aCombo ? comboStar : "") : placeholder;
            row.addView(createCell(aText, aCombo, false, false));

            boolean bCombo = i < comboB.size() && Boolean.TRUE.equals(comboB.get(i));
            String  bText  = i < scoresB.size()
                    ? scoresB.get(i) + (bCombo ? comboStar : "") : placeholder;
            row.addView(createCell(bText, bCombo, false, false));

            tableLayoutScores.addView(row);
        }

        // Divider before summary rows
        View bdiv = new View(this);
        bdiv.setBackgroundColor(getResources().getColor(R.color.divider_color, null));
        TableLayout.LayoutParams lp2 =
                new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, 2);
        lp2.setMargins(8, 8, 8, 4);
        bdiv.setLayoutParams(lp2);
        tableLayoutScores.addView(bdiv);

        // Best streak row
        TableRow streakRow = new TableRow(this);
        streakRow.setPadding(0, 8, 0, 4);
        streakRow.addView(createCell(
                getString(R.string.score_table_streak_icon), false, true, false));
        streakRow.addView(createCell(
                getString(R.string.streak_best_format, currentGame.maxStreakA),
                false, true, false));
        streakRow.addView(createCell(
                getString(R.string.streak_best_format, currentGame.maxStreakB),
                false, true, false));
        tableLayoutScores.addView(streakRow);

        // Total row
        TableRow totalRow = new TableRow(this);
        totalRow.setPadding(0, 8, 0, 12);
        totalRow.setBackgroundColor(
                getResources().getColor(R.color.surface_card, null));
        totalRow.addView(createCell(getString(R.string.total_label), false, true, true));
        totalRow.addView(createCell(
                String.valueOf(currentGame.getTotalScoreA()), false, true, true));
        totalRow.addView(createCell(
                String.valueOf(currentGame.getTotalScoreB()), false, true, true));
        tableLayoutScores.addView(totalRow);

        // Combo legend (only if any combo was applied)
        boolean anyCombo = comboA.contains(true) || comboB.contains(true);
        if (anyCombo) {
            TextView legend = new TextView(this);
            legend.setText(getString(R.string.combo_star_tip));
            legend.setTextColor(getResources().getColor(R.color.combo_color, null));
            legend.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimension(R.dimen.normal_fond_size));
            legend.setPadding(16, 8, 16, 8);
            tableLayoutScores.addView(legend);
        }

        // Auto-scroll to bottom so user sees latest scores
        if (scrollView != null) {
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
        }
    }

    private TextView createCell(String text, boolean isCombo,
                                boolean isBold, boolean isHeader) {
        TextView tv = new TextView(this);
        tv.setText(text);

        int textColor;
        if (isHeader) {
            textColor = getResources().getColor(R.color.primary_button_color, null);
        } else if (isCombo) {
            textColor = getResources().getColor(R.color.combo_color, null);
        } else {
            textColor = getResources().getColor(R.color.primary_text, null);
        }
        tv.setTextColor(textColor);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.sub_font_size));

        if (isBold || isHeader) {
            tv.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                tv.setTypeface(getResources().getFont(R.font.subtext_font));
                if (isBold || isHeader) {
                    tv.setTypeface(tv.getTypeface(), android.graphics.Typeface.BOLD);
                }
            } catch (Exception ignored) {}
        }

        int pad = (int) getResources().getDimension(R.dimen.primary_size_layout);
        tv.setPadding(pad, pad / 2, pad, pad / 2);
        tv.setGravity(Gravity.CENTER);

        TableRow.LayoutParams params = new TableRow.LayoutParams(
                0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
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
            if (btnContinueGame != null) btnContinueGame.setEnabled(false);
        }
    }

    // ─── Buttons ─────────────────────────────────────────────────────────────

    private void setupButtons() {
        if (btnContinueGame != null) {
            btnContinueGame.setOnClickListener(v -> {
                advanceGameTurn();
                if (currentGame.isFinished) resolveFinish();
                else navigateToCards();
            });
        }

        if (btnEndGame != null) {
            btnEndGame.setOnClickListener(v -> {
                currentGame.isFinished = true;
                ScoreStorage.getInstance(this).saveCurrentGame(currentGame);
                resolveFinish();
            });
        }

        if (btnShare != null) {
            btnShare.setOnClickListener(v -> {
                boolean ok = ShareManager.shareGameSummary(this, currentGame);
                if (!ok) Toast.makeText(this,
                        R.string.share_failed, Toast.LENGTH_SHORT).show();
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

    /**
     * Advance game turn correctly:
     * - Mark current group's turn as finished
     * - Switch to other group
     * - If round complete: advance round counter, reset turn flags
     * - Check game-over and skip conditions
     */
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
        // Refresh display after state change
        updateLiveScoreTotals();
        updateRoundInfo();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentGame != null)
            ScoreStorage.getInstance(this).saveCurrentGame(currentGame);
    }
}