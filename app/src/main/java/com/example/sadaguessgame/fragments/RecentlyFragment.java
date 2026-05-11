package com.example.sadaguessgame.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.sadaguessgame.R;
import com.example.sadaguessgame.activities.CardsActivity;
import com.example.sadaguessgame.data.GameState;
import com.example.sadaguessgame.data.ScoreStorage;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class RecentlyFragment extends BaseFragment {

    private LinearLayout   scoreContainer;
    private ScoreStorage   scoreStorage;
    private MaterialButton btnDeleteAll;
    private MaterialButton btnContinueGame;
    private View           continueGameCard;

    public RecentlyFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recently_activity, container, false);

        scoreContainer   = view.findViewById(R.id.scoreContainer);
        btnDeleteAll     = view.findViewById(R.id.delete_all);
        btnContinueGame  = view.findViewById(R.id.btnContinueGame);
        continueGameCard = view.findViewById(R.id.continueGameCard);
        scoreStorage     = ScoreStorage.getInstance(requireContext());

        if (btnDeleteAll != null)
            btnDeleteAll.setOnClickListener(v -> showDeleteAllConfirmation());

        if (btnContinueGame != null)
            btnContinueGame.setOnClickListener(v -> continueUnfinishedGame());

        loadAllGames();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAllGames();
    }

    // ─── Load ────────────────────────────────────────────────────────────────

    private void loadAllGames() {
        if (scoreContainer == null) return;
        scoreContainer.removeAllViews();

        List<GameState> finishedGames = scoreStorage.getAllGames();
        GameState       currentGame   = scoreStorage.getCurrentGame();
        boolean         hasUnfinished = currentGame != null && !currentGame.isFinished;

        // Show / hide continue card
        if (continueGameCard != null) {
            continueGameCard.setVisibility(hasUnfinished ? View.VISIBLE : View.GONE);
        }

        if (hasUnfinished && continueGameCard != null) {
            populateContinueCard(currentGame);
        }

        boolean isEmpty = (finishedGames == null || finishedGames.isEmpty()) && !hasUnfinished;
        if (isEmpty) {
            showEmptyState();
            if (btnDeleteAll != null) btnDeleteAll.setVisibility(View.GONE);
            return;
        }

        if (btnDeleteAll != null) {
            btnDeleteAll.setVisibility(
                    (finishedGames != null && !finishedGames.isEmpty())
                            ? View.VISIBLE : View.GONE);
        }

        if (finishedGames != null) {
            for (int i = 0; i < finishedGames.size(); i++) {
                addFinishedGameView(finishedGames.get(i), i + 1, i);
            }
        }
    }

    // ─── Continue card ───────────────────────────────────────────────────────

    private void populateContinueCard(GameState game) {
        TextView tvGroupA = continueGameCard.findViewById(R.id.tvContinueGroupA);
        TextView tvGroupB = continueGameCard.findViewById(R.id.tvContinueGroupB);
        TextView tvScoreA = continueGameCard.findViewById(R.id.tvContinueScoreA);
        TextView tvScoreB = continueGameCard.findViewById(R.id.tvContinueScoreB);
        TextView tvRound  = continueGameCard.findViewById(R.id.tvContinueRound);

        if (tvGroupA != null) tvGroupA.setText(game.groupAName);
        if (tvGroupB != null) tvGroupB.setText(game.groupBName);
        if (tvScoreA != null) tvScoreA.setText(String.valueOf(game.getTotalScoreA()));
        if (tvScoreB != null) tvScoreB.setText(String.valueOf(game.getTotalScoreB()));
        if (tvRound  != null) tvRound.setText(getString(R.string.round_of_format,
                game.currentRound, game.totalRounds));
    }

    // ─── Empty state ─────────────────────────────────────────────────────────

    private void showEmptyState() {
        View emptyView = LayoutInflater.from(requireContext())
                .inflate(R.layout.layout_empty_state, scoreContainer, false);
        scoreContainer.addView(emptyView);
    }

    // ─── Finished game card ───────────────────────────────────────────────────

    private void addFinishedGameView(GameState game, int displayIndex, int storageIndex) {
        if (game == null) return;
        View itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.recently_game_item, scoreContainer, false);

        // Index
        TextView tvIndex = itemView.findViewById(R.id.tvIndex);
        if (tvIndex != null)
            tvIndex.setText(getString(R.string.status_index_simple_format, displayIndex));

        // Group names
        TextView tvGroupA = itemView.findViewById(R.id.groupAName);
        TextView tvGroupB = itemView.findViewById(R.id.groupBName);
        if (tvGroupA != null) tvGroupA.setText(safe(game.groupAName));
        if (tvGroupB != null) tvGroupB.setText(safe(game.groupBName));

        // Scores
        int totalA = calculateTotal(game.scoresA);
        int totalB = calculateTotal(game.scoresB);
        TextView tvScoreA = itemView.findViewById(R.id.groupAScore);
        TextView tvScoreB = itemView.findViewById(R.id.groupBScore);
        if (tvScoreA != null) tvScoreA.setText(String.valueOf(totalA));
        if (tvScoreB != null) tvScoreB.setText(String.valueOf(totalB));

        // Round info
        TextView tvRoundInfo = itemView.findViewById(R.id.tvGameRoundInfo);
        if (tvRoundInfo != null) {
            tvRoundInfo.setText(getString(R.string.round_of_format,
                    game.totalRounds, game.totalRounds));
            tvRoundInfo.setVisibility(View.VISIBLE);
        }

        // Best streaks
        TextView tvStreaks = itemView.findViewById(R.id.tvGameStreaks);
        if (tvStreaks != null && (game.maxStreakA > 0 || game.maxStreakB > 0)) {
            tvStreaks.setText(getString(R.string.streak_summary_format,
                    safe(game.groupAName), game.maxStreakA,
                    safe(game.groupBName), game.maxStreakB));
            tvStreaks.setVisibility(View.VISIBLE);
        }

        // Difficulty badge
        TextView tvDiff = itemView.findViewById(R.id.tvGameDifficulty);
        if (tvDiff != null && game.difficultyLevel != null) {
            String diffLabel;
            switch (game.difficultyLevel.toLowerCase()) {
                case "easy":  diffLabel = getString(R.string.difficulty_easy);   break;
                case "hard":  diffLabel = getString(R.string.difficulty_hard);   break;
                default:      diffLabel = getString(R.string.difficulty_medium); break;
            }
            tvDiff.setText(diffLabel);
            tvDiff.setVisibility(View.VISIBLE);
        }

        // Winner badge
        TextView tvWinner = itemView.findViewById(R.id.tvWinnerBadge);
        if (tvWinner != null) {
            if (game.suddenDeathWinner != GameState.NO_WINNER) {
                String sdWinner = game.suddenDeathWinner == GameState.GROUP_A
                        ? safe(game.groupAName) : safe(game.groupBName);
                tvWinner.setText("⚡ " + sdWinner);
                tvWinner.setVisibility(View.VISIBLE);
            } else if (totalA > totalB) {
                tvWinner.setText(safe(game.groupAName));
                tvWinner.setVisibility(View.VISIBLE);
            } else if (totalB > totalA) {
                tvWinner.setText(safe(game.groupBName));
                tvWinner.setVisibility(View.VISIBLE);
            } else {
                tvWinner.setText(getString(R.string.draw));
                tvWinner.setVisibility(View.VISIBLE);
            }
        }

        // Delete button
        ImageView btnDelete = itemView.findViewById(R.id.delete_game);
        if (btnDelete != null) {
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(v -> showDeleteConfirmation(storageIndex));
        }

        scoreContainer.addView(itemView);
    }

    // ─── Navigation ──────────────────────────────────────────────────────────

    private void continueUnfinishedGame() {
        startActivity(new Intent(requireContext(), CardsActivity.class));
        requireActivity().overridePendingTransition(
                R.anim.slide_in_right, R.anim.slide_out_left);
    }

    // ─── Delete ──────────────────────────────────────────────────────────────

    private void showDeleteConfirmation(int storageIndex) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_title)
                .setMessage(R.string.detele_disc)
                .setPositiveButton(R.string.delete, (d, w) -> deleteGame(storageIndex))
                .setNegativeButton(R.string.no_button, null)
                .show();
    }

    private void showDeleteAllConfirmation() {
        List<GameState> games = scoreStorage.getAllGames();
        if (games == null || games.isEmpty()) {
            Toast.makeText(requireContext(),
                    R.string.no_games_to_delete, Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.deletes_title)
                .setMessage(R.string.deteles_disc)
                .setPositiveButton(R.string.delete, (d, w) -> deleteAllGames())
                .setNegativeButton(R.string.no_button, null)
                .show();
    }

    private void deleteGame(int storageIndex) {
        boolean ok = scoreStorage.deleteGame(storageIndex);
        Toast.makeText(requireContext(),
                ok ? R.string.game_deleted : R.string.failed_to_delete_game,
                Toast.LENGTH_SHORT).show();
        if (ok) loadAllGames();
    }

    private void deleteAllGames() {
        boolean ok = scoreStorage.deleteAllGames();
        Toast.makeText(requireContext(),
                ok ? R.string.all_game_deleted : R.string.failed_to_delete_all,
                Toast.LENGTH_SHORT).show();
        if (ok) loadAllGames();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private int calculateTotal(List<Integer> scores) {
        if (scores == null) return 0;
        int t = 0;
        for (int s : scores) t += s;
        return t;
    }

    @NonNull
    private String safe(@Nullable String s) {
        return s != null ? s : "";
    }
}