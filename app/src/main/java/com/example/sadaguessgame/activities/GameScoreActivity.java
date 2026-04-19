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
    private GameState currentGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.score_activity);

        currentGame = ScoreStorage.getInstance(this).getCurrentGame();

        initViews();
        setHeaderNames();
        populateScoreTable();
        setupButtons();
    }

    private void initViews() {
        tableLayoutScores = findViewById(R.id.tableLayoutScores);
        scrollView = findViewById(R.id.scrollLayoutScores);
        btnContinueGame = findViewById(R.id.btnContinueGame);
        btnEndGame = findViewById(R.id.btnEndGame);
    }

    /**
     * Set group names in the HEADER (already exists in XML)
     */
    private void setHeaderNames() {
        TextView groupAHeader = findViewById(R.id.groupAName);
        TextView groupBHeader = findViewById(R.id.groupBName);

        groupAHeader.setText(currentGame.groupAName);
        groupBHeader.setText(currentGame.groupBName);
    }

    /**
     * Add score rows dynamically
     */
    private void populateScoreTable() {
        List<Integer> scoresA =
                currentGame.scoresA != null
                        ? currentGame.scoresA
                        : new ArrayList<>();

        List<Integer> scoresB =
                currentGame.scoresB != null
                        ? currentGame.scoresB
                        : new ArrayList<>();

        int totalRounds = Math.max(scoresA.size(), scoresB.size());

        // Start from index 1 because index 0 is HEADER row
        for (int i = 0; i < totalRounds; i++) {
            TableRow row = new TableRow(this);
            row.setPadding(12, 12, 12, 12);

            // Round number
            row.addView(createCell(String.valueOf(i + 1)));

            // Group A score
            row.addView(createCell(
                    i < scoresA.size() ? String.valueOf(scoresA.get(i)) : "--"
            ));

            // Group B score
            row.addView(createCell(
                    i < scoresB.size() ? String.valueOf(scoresB.get(i)) : "--"
            ));

            tableLayoutScores.addView(row);
        }

        // Auto-scroll to bottom
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    /**
     * Creates a TextView cell with fixed column width using layout_weight
     * This ensures all columns have equal width regardless of content
     */
    private TextView createCell(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextColor(getResources().getColor(R.color.primary_text, null));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.sub_font_size));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            textView.setTypeface(getResources().getFont(R.font.subtext_font));
        }
        textView.setPadding(
                (int) getResources().getDimension(R.dimen.primary_size_layout),
                (int) getResources().getDimension(R.dimen.primary_size_layout),
                (int) getResources().getDimension(R.dimen.primary_size_layout),
                (int) getResources().getDimension(R.dimen.primary_size_layout)
        );
        textView.setGravity(Gravity.CENTER);

        // Create layout params with layout_weight for equal column widths
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                0,  // width = 0dp when using layout_weight
                TableRow.LayoutParams.WRAP_CONTENT,
                1f  // layout_weight = 1 (equal weight for all columns)
        );

        // Optional: Add margin if needed
        int margin = (int) getResources().getDimension(R.dimen.primary_size_layout);
        params.setMargins(margin, margin, margin, margin);

        textView.setLayoutParams(params);

        return textView;
    }

    /**
     * Buttons logic
     */
    private void setupButtons() {

        btnContinueGame.setOnClickListener(v -> {
            finishCurrentGroupTurn();
            continueGame();
        });

        btnEndGame.setOnClickListener(v -> {
            gameFinished();
            finish(); // or navigate to main menu
        });
    }
    private void continueGame() {
        Intent intent = new Intent(this, CardsActivity.class);
// Open next activity
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
    public void finishCurrentGroupTurn() {
        if (currentGame == null) return;

        if (currentGame.groupTurn == 0) { // Group A finished
            currentGame.turnGroupAFinish = true;
            currentGame.groupTurn = 1; // next is group B
        } else { // Group B finished
            currentGame.turnGroupBFinish = true;
            currentGame.groupTurn = 0; // next round or back to group A
        }

        // Save changes
        ScoreStorage.getInstance(this).saveCurrentGame(currentGame);

        checkRoundProgress(); // check if round is finished
    }

    private void checkRoundProgress() {
        if (currentGame.turnGroupAFinish && currentGame.turnGroupBFinish) {
            // Both groups finished this round
            if (currentGame.currentRound >= currentGame.totalRounds) {
                // Game finished
                gameFinished();
            } else {
                // Prepare next round
                currentGame.currentRound++;
                currentGame.turnGroupAFinish = false;
                currentGame.turnGroupBFinish = false;
                currentGame.groupTurn = 0; // start next round with group A
                ScoreStorage.getInstance(this).saveCurrentGame(currentGame);
            }
        }
    }
    private void gameFinished() {
        currentGame.isFinished = true;
        Intent intent = new Intent(this, WinnerActivity.class);
// Open next activity
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ScoreStorage.getInstance(this).saveCurrentGame(currentGame);
    }
}