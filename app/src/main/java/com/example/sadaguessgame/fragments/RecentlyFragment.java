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

        scoreContainer  = view.findViewById(R.id.scoreContainer);
        btnDeleteAll    = view.findViewById(R.id.delete_all);
        btnContinueGame = view.findViewById(R.id.btnContinueGame);
        continueGameCard = view.findViewById(R.id.continueGameCard);
        scoreStorage    = ScoreStorage.getInstance(requireContext());

        btnDeleteAll.setOnClickListener(v -> showDeleteAllConfirmation());
        if (btnContinueGame != null) {
            btnContinueGame.setOnClickListener(v -> continueUnfinishedGame());
        }

        loadAllGames();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAllGames();
    }

    private void loadAllGames() {
        scoreContainer.removeAllViews();

        List<GameState> finishedGames = scoreStorage.getAllGames();
        GameState currentGame         = scoreStorage.getCurrentGame();
        boolean   hasUnfinished       = currentGame != null && !currentGame.isFinished;

        // Show/hide continue card
        if (continueGameCard != null) {
            continueGameCard.setVisibility(hasUnfinished ? View.VISIBLE : View.GONE);
        }

        if (hasUnfinished && continueGameCard != null) {
            // Populate continue card with game details
            TextView tvGroupA = continueGameCard.findViewById(R.id.tvContinueGroupA);
            TextView tvGroupB = continueGameCard.findViewById(R.id.tvContinueGroupB);
            TextView tvScoreA = continueGameCard.findViewById(R.id.tvContinueScoreA);
            TextView tvScoreB = continueGameCard.findViewById(R.id.tvContinueScoreB);
            TextView tvRound  = continueGameCard.findViewById(R.id.tvContinueRound);

            if (tvGroupA != null) tvGroupA.setText(currentGame.groupAName);
            if (tvGroupB != null) tvGroupB.setText(currentGame.groupBName);
            if (tvScoreA != null) tvScoreA.setText(String.valueOf(currentGame.getTotalScoreA()));
            if (tvScoreB != null) tvScoreB.setText(String.valueOf(currentGame.getTotalScoreB()));
            if (tvRound  != null) tvRound.setText(getString(R.string.round_of_format,
                    currentGame.currentRound, currentGame.totalRounds));
        }

        boolean isEmpty = (finishedGames == null || finishedGames.isEmpty()) && !hasUnfinished;
        if (isEmpty) {
            showEmptyState();
            btnDeleteAll.setVisibility(View.GONE);
            return;
        }

        btnDeleteAll.setVisibility(
                (finishedGames != null && !finishedGames.isEmpty()) ? View.VISIBLE : View.GONE);

        if (finishedGames != null) {
            for (int i = 0; i < finishedGames.size(); i++) {
                addFinishedGameView(finishedGames.get(i), i + 1, i);
            }
        }
    }

    private void showEmptyState() {
        View emptyView = LayoutInflater.from(requireContext())
                .inflate(R.layout.layout_empty_state, scoreContainer, false);
        scoreContainer.addView(emptyView);
    }

    private void addFinishedGameView(GameState gameState, int displayIndex, int storageIndex) {
        if (gameState == null) return;
        View itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.recently_game_item, scoreContainer, false);

        ((TextView) itemView.findViewById(R.id.tvIndex))
                .setText(getString(R.string.status_index_simple_format, displayIndex));
        ((TextView) itemView.findViewById(R.id.groupAName)).setText(gameState.groupAName);
        ((TextView) itemView.findViewById(R.id.groupBName)).setText(gameState.groupBName);
        ((TextView) itemView.findViewById(R.id.groupAScore))
                .setText(String.valueOf(calculateTotal(gameState.scoresA)));
        ((TextView) itemView.findViewById(R.id.groupBScore))
                .setText(String.valueOf(calculateTotal(gameState.scoresB)));

        // Winner indicator
        TextView tvWinner = itemView.findViewById(R.id.tvWinnerBadge);
        if (tvWinner != null) {
            int a = calculateTotal(gameState.scoresA);
            int b = calculateTotal(gameState.scoresB);
            if (a > b) {
                tvWinner.setText(gameState.groupAName);
                tvWinner.setVisibility(View.VISIBLE);
            } else if (b > a) {
                tvWinner.setText(gameState.groupBName);
                tvWinner.setVisibility(View.VISIBLE);
            } else {
                tvWinner.setText(getString(R.string.draw));
                tvWinner.setVisibility(View.VISIBLE);
            }
        }

        ImageView btnDelete = itemView.findViewById(R.id.delete_game);
        btnDelete.setVisibility(View.VISIBLE);
        btnDelete.setOnClickListener(v -> showDeleteConfirmation(storageIndex));

        scoreContainer.addView(itemView);
    }

    private void continueUnfinishedGame() {
        startActivity(new Intent(requireContext(), CardsActivity.class));
        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

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
            Toast.makeText(requireContext(), R.string.no_games_to_delete, Toast.LENGTH_SHORT).show();
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

    private int calculateTotal(List<Integer> scores) {
        if (scores == null) return 0;
        int total = 0;
        for (int s : scores) total += s;
        return total;
    }
}