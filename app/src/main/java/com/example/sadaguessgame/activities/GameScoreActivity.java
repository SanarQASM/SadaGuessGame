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

import androidx.core.content.ContextCompat;

import com.example.sadaguessgame.R;
import com.example.sadaguessgame.data.GameState;
import com.example.sadaguessgame.data.ScoreStorage;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class GameScoreActivity extends BaseActivity {

    private TableLayout tableLayoutScores;
    private ScrollView scrollView;
    private MaterialButton btnContinueGame, btnEndGame;
    private TextView tvTotalA, tvTotalB, tvRoundInfo;
    private GameState currentGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.score_activity);

        currentGame = ScoreStorage.getInstance(this).getCurrentGame();
        if (currentGame == null) {
            finish();
            return;
        }

        if (currentGame.scoresA == null) currentGame.scoresA = new ArrayList<>();
        if (currentGame.scoresB == null) currentGame.scoresB = new ArrayList<>();

        initViews();
        setHeaderNames();
        populateScoreTable();
        updateRoundInfo();
        setupButtons();
        checkSkipCondition();
    }

    private void initViews() {
        tableLayoutScores = findViewById(R.id.tableLayoutScores);
        scrollView        = findViewById(R.id.scrollLayoutScores);
        btnContinueGame   = findViewById(R.id.btnContinueGame);
        btnEndGame        = findViewById(R.id.btnEndGame);
        tvTotalA          = findViewById(R.id.tvTotalScoreA);
        tvTotalB          = findViewById(R.id.tvTotalScoreB);
        tvRoundInfo       = findViewById(R.id.tvRoundInfo);
    }

    private void setHeaderNames() {
        TextView tvA = findViewById(R.id.groupAName);
        TextView tvB = findViewById(R.id.groupBName);
        if (tvA != null) tvA.setText(currentGame.groupAName);
        if (tvB != null) tvB.setText(currentGame.groupBName);
    }

    private void updateRoundInfo() {
        if (tvRoundInfo == null) return;
        String info = getString(R.string.round) + " " + currentGame.currentRound
                + " / " + currentGame.totalRounds;
        tvRoundInfo.setText(info);

        if (tvTotalA != null) tvTotalA.setText(String.valueOf(currentGame.getTotalScoreA()));
        if (tvTotalB != null) tvTotalB.setText(String.valueOf(currentGame.getTotalScoreB()));
    }

    private void populateScoreTable() {
        List<Integer> scoresA = currentGame.scoresA;
        List<Integer> scoresB = currentGame.scoresB;

        // FIX: Scores are added per-turn (each group plays once per round).
        // scoresA.size() and scoresB.size() reflect how many turns each group has completed.
        // We display them paired by round index.
        int totalRows = Math.max(scoresA.size(), scoresB.size());

        tableLayoutScores.removeAllViews();

        // Add header row
        addHeaderRow();

        int runningA = 0;
        int runningB = 0;

        for (int i = 0; i < totalRows; i++) {
            int scoreA = i < scoresA.size() ? scoresA.get(i) : -1;
            int scoreB = i < scoresB.size() ? scoresB.get(i) : -1;

            if (scoreA >= 0) runningA += scoreA;
            if (scoreB >= 0) runningB += scoreB;

            TableRow row = new TableRow(this);
            row.setPadding(8, 10, 8, 10);

            // Alternate row background for readability
            if (i % 2 == 0) {
                row.setBackgroundColor(ContextCompat.getColor(this, R.color.divider_color));
            }

            row.addView(createCell(String.valueOf(i + 1), false));
            row.addView(createCell(scoreA >= 0 ? String.valueOf(scoreA) : "—", false));
            row.addView(createCell(scoreB >= 0 ? String.valueOf(scoreB) : "—", false));
            tableLayoutScores.addView(row);
        }

        // Total row
        addTotalRow(runningA, runningB);

        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void addHeaderRow() {
        TableRow headerRow = new TableRow(this);
        headerRow.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_button_color));
        headerRow.setPadding(8, 12, 8, 12);

        TextView roundHeader = createCell(getString(R.string.round), true);
        roundHeader.setTextColor(ContextCompat.getColor(this, R.color.white));

        TextView groupAHeader = createCell(currentGame.groupAName, true);
        groupAHeader.setTextColor(ContextCompat.getColor(this, R.color.white));

        TextView groupBHeader = createCell(currentGame.groupBName, true);
        groupBHeader.setTextColor(ContextCompat.getColor(this, R.color.white));

        headerRow.addView(roundHeader);
        headerRow.addView(groupAHeader);
        headerRow.addView(groupBHeader);
        tableLayoutScores.addView(headerRow);
    }

    private void addTotalRow(int totalA, int totalB) {
        TableRow totalRow = new TableRow(this);
        totalRow.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_color));
        totalRow.setPadding(8, 14, 8, 14);

        TextView totalLabel = createCell(getString(R.string.total_label), true);
        totalLabel.setTextColor(ContextCompat.getColor(this, R.color.primary_text));

        TextView totalAView = createCell(String.valueOf(totalA), true);
        totalAView.setTextColor(ContextCompat.getColor(this, R.color.primary_text));

        TextView totalBView = createCell(String.valueOf(totalB), true);
        totalBView.setTextColor(ContextCompat.getColor(this, R.color.primary_text));

        totalRow.addView(totalLabel);
        totalRow.addView(totalAView);
        totalRow.addView(totalBView);
        tableLayoutScores.addView(totalRow);
    }

    private TextView createCell(String text, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(ContextCompat.getColor(this, R.color.primary_text));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.sub_font_size));
        if (bold) {
            tv.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        int pad = (int) getResources().getDimension(R.dimen.primary_size_layout);
        tv.setPadding(pad, pad, pad, pad);
        tv.setGravity(Gravity.CENTER);
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        params.setMargins(2, 2, 2, 2);
        tv.setLayoutParams(params);
        return tv;
    }

    private void checkSkipCondition() {
        if (currentGame.canSkipRemainingRounds()) {
            String leader = currentGame.getTotalScoreA() >= currentGame.getTotalScoreB()
                    ? currentGame.groupAName : currentGame.groupBName;
            Toast.makeText(this,
                    leader + " " + getString(R.string.winner_desc),
                    Toast.LENGTH_LONG).show();
            btnContinueGame.setEnabled(false);
            btnContinueGame.setAlpha(0.5f);
        }
    }

    private void setupButtons() {
        btnContinueGame.setOnClickListener(v -> {
            advanceGameTurn();
            if (currentGame.isFinished) {
                navigateToResult();
            } else {
                navigateToCards();
            }
        });

        btnEndGame.setOnClickListener(v -> {
            currentGame.isFinished = true;
            ScoreStorage.getInstance(this).saveCurrentGame(currentGame);
            navigateToResult();
        });
    }

    /**
     * FIX: Advance the game turn correctly.
     *
     * Turn flow per round:
     *   GROUP_A plays → score added to scoresA → come here → switch to GROUP_B
     *   GROUP_B plays → score added to scoresB → come here → round complete → advance round
     *
     * We do NOT add scores here. Scores are already added in TimeUpDialog/ScoreDialog.
     * We only advance the turn pointer and round counter.
     */
    private void advanceGameTurn() {
        if (currentGame == null) return;

        if (currentGame.groupTurn == GameState.GROUP_A) {
            // Group A just finished its turn → switch to Group B
            currentGame.turnGroupAFinish = true;
            currentGame.groupTurn = GameState.GROUP_B;
        } else {
            // Group B just finished its turn → round is complete
            currentGame.turnGroupBFinish = true;
            currentGame.groupTurn = GameState.GROUP_A;

            // Both groups played: check if game is over
            if (currentGame.isRoundComplete()) {
                if (currentGame.isGameOver()) {
                    currentGame.isFinished = true;
                } else {
                    // Advance to next round, reset per-round flags
                    currentGame.currentRound++;
                    currentGame.turnGroupAFinish = false;
                    currentGame.turnGroupBFinish = false;
                }
            }
        }

        // Check decisive win condition
        if (!currentGame.isFinished && currentGame.canSkipRemainingRounds()) {
            currentGame.isFinished = true;
        }

        ScoreStorage.getInstance(this).saveCurrentGame(currentGame);
    }

    private void navigateToResult() {
        Intent intent = new Intent(this, WinnerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    private void navigateToCards() {
        Intent intent = new Intent(this, CardsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentGame != null) {
            ScoreStorage.getInstance(this).saveCurrentGame(currentGame);
        }
    }
}