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
        if (currentGame == null) { finish(); return; }

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

    private void setHeaderNames() {
        ((TextView) findViewById(R.id.groupAName)).setText(currentGame.groupAName);
        ((TextView) findViewById(R.id.groupBName)).setText(currentGame.groupBName);
    }

    private void populateScoreTable() {
        List<Integer> scoresA = currentGame.scoresA != null ? currentGame.scoresA : new ArrayList<>();
        List<Integer> scoresB = currentGame.scoresB != null ? currentGame.scoresB : new ArrayList<>();
        int totalRounds = Math.max(scoresA.size(), scoresB.size());

        for (int i = 0; i < totalRounds; i++) {
            TableRow row = new TableRow(this);
            row.setPadding(12, 12, 12, 12);
            row.addView(createCell(String.valueOf(i + 1)));
            row.addView(createCell(i < scoresA.size() ? String.valueOf(scoresA.get(i)) : "--"));
            row.addView(createCell(i < scoresB.size() ? String.valueOf(scoresB.get(i)) : "--"));
            tableLayoutScores.addView(row);
        }

        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private TextView createCell(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(getResources().getColor(R.color.primary_text, null));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.sub_font_size));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            tv.setTypeface(getResources().getFont(R.font.subtext_font));
        }
        int pad = (int) getResources().getDimension(R.dimen.primary_size_layout);
        tv.setPadding(pad, pad, pad, pad);
        tv.setGravity(Gravity.CENTER);

        TableRow.LayoutParams params = new TableRow.LayoutParams(0,
                TableRow.LayoutParams.WRAP_CONTENT, 1f);
        params.setMargins(pad, pad, pad, pad);
        tv.setLayoutParams(params);
        return tv;
    }

    private void setupButtons() {
        btnContinueGame.setOnClickListener(v -> {
            advanceGameTurn();
            if (currentGame.isFinished) {
                navigateToWinner();
            } else {
                navigateToCards();
            }
        });

        btnEndGame.setOnClickListener(v -> {
            currentGame.isFinished = true;
            ScoreStorage.getInstance(this).saveCurrentGame(currentGame);
            navigateToWinner();
        });
    }

    private void advanceGameTurn() {
        if (currentGame == null) return;

        if (currentGame.groupTurn == GameState.GROUP_A) {
            currentGame.turnGroupAFinish = true;
            currentGame.groupTurn = GameState.GROUP_B;
        } else {
            currentGame.turnGroupBFinish = true;
            currentGame.groupTurn = GameState.GROUP_A;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentGame != null) {
            ScoreStorage.getInstance(this).saveCurrentGame(currentGame);
        }
    }
}