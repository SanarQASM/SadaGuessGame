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
    private LinearLayout scoreContainer;
    private ScoreStorage scoreStorage;
    private MaterialButton btnDeleteAll;

    public RecentlyFragment() {}

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.recently_activity, container, false);

        scoreContainer = view.findViewById(R.id.scoreContainer);
        btnDeleteAll = view.findViewById(R.id.delete_all);

        scoreStorage = ScoreStorage.getInstance(requireContext());

        btnDeleteAll.setOnClickListener(v -> showDeleteAllConfirmation());

        loadAllGames();
        return view;
    }

    private void loadAllGames() {
        scoreContainer.removeAllViews();

        List<GameState> finishedGames = scoreStorage.getAllGames();
        GameState currentGame = scoreStorage.getCurrentGame();
        boolean hasUnfinished = currentGame != null && !currentGame.isFinished;

        boolean isEmpty = (finishedGames == null || finishedGames.isEmpty()) && !hasUnfinished;

        if (isEmpty) {
            showEmptyState();
            btnDeleteAll.setVisibility(View.GONE);
            return;
        }

        btnDeleteAll.setVisibility(View.VISIBLE);

        int index = 1;

        if (hasUnfinished) {
            addGameView(currentGame, index - 1, true);
            index++;
        }

        if (finishedGames != null) {
            for (int i = 0; i < finishedGames.size(); i++) {
                addGameView(finishedGames.get(i), i, false);
            }
        }
    }

    private void showEmptyState() {
        TextView emptyView = new TextView(requireContext());
        emptyView.setText(R.string.no_recently_game);
        emptyView.setTextSize(16);
        emptyView.setPadding(32, 32, 32, 32);
        emptyView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        emptyView.setTextColor(requireContext().getColor(R.color.primary_text));
        scoreContainer.addView(emptyView);
    }

    private void addGameView(GameState gameState, int index, boolean isUnfinished) {
        if (gameState == null) return;

        View itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.recently_game_item, scoreContainer, false);

        TextView tvIndex    = itemView.findViewById(R.id.tvIndex);
        TextView groupAName = itemView.findViewById(R.id.groupAName);
        TextView groupAScore= itemView.findViewById(R.id.groupAScore);
        TextView groupBName = itemView.findViewById(R.id.groupBName);
        TextView groupBScore= itemView.findViewById(R.id.groupBScore);
        ImageView btnDelete = itemView.findViewById(R.id.delete_game);

        String statusLabel = isUnfinished
                ? getString(R.string.status_index_continue_format,
                index + 1, getString(R.string.continue_game))
                : getString(R.string.status_index_simple_format, index + 1);

        tvIndex.setText(statusLabel);
        groupAName.setText(gameState.groupAName);
        groupBName.setText(gameState.groupBName);
        groupAScore.setText(String.valueOf(calculateTotal(gameState.scoresA)));
        groupBScore.setText(String.valueOf(calculateTotal(gameState.scoresB)));

        if (isUnfinished) {
            itemView.setOnClickListener(v -> continueUnfinishedGame(gameState));
            itemView.setAlpha(0.9f);
            btnDelete.setVisibility(View.GONE);
        } else {
            btnDelete.setOnClickListener(v -> showDeleteConfirmation(index));
            btnDelete.setVisibility(View.VISIBLE);
        }

        scoreContainer.addView(itemView);
    }

    private void continueUnfinishedGame(GameState game) {
        startActivity(new Intent(requireContext(), CardsActivity.class));
        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void showDeleteConfirmation(int index) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_title)
                .setMessage(R.string.detele_disc)
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteGame(index))
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
                .setMessage(R.string.detele_disc)
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteAllGames())
                .setNegativeButton(R.string.no_button, null)
                .show();
    }

    private void deleteGame(int index) {
        boolean success = scoreStorage.deleteGame(index);
        if (success) {
            Toast.makeText(requireContext(), R.string.game_deleted, Toast.LENGTH_SHORT).show();
            loadAllGames();
        } else {
            Toast.makeText(requireContext(), R.string.failed_to_delete_game, Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteAllGames() {
        boolean success = scoreStorage.deleteAllGames();
        if (success) {
            Toast.makeText(requireContext(), R.string.all_game_deleted, Toast.LENGTH_SHORT).show();
            loadAllGames();
        } else {
            Toast.makeText(requireContext(), R.string.failed_to_delete_all, Toast.LENGTH_SHORT).show();
        }
    }

    private int calculateTotal(List<Integer> scores) {
        int total = 0;
        if (scores == null) return total;
        for (int score : scores) total += score;
        return total;
    }
}