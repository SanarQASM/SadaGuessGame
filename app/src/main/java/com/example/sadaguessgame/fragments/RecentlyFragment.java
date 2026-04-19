package com.example.sadaguessgame.fragments;

import android.app.AlertDialog;
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
import com.example.sadaguessgame.data.GameState;
import com.example.sadaguessgame.data.ScoreStorage;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class RecentlyFragment extends BaseFragment {
    private LinearLayout scoreContainer;
    private ScoreStorage scoreStorage;
    private MaterialButton btnDeleteAll;

    public RecentlyFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.recently_activity, container, false);

        // ------------------- INITIALIZE VIEWS -------------------
        scoreContainer = view.findViewById(R.id.scoreContainer);
        btnDeleteAll = view.findViewById(R.id.delete_all);

        scoreStorage = ScoreStorage.getInstance(requireContext());

        // Setup delete all button
        btnDeleteAll.setOnClickListener(v -> showDeleteAllConfirmation());

        loadAllGames();
        return view;
    }

    private void loadAllGames() {
        scoreContainer.removeAllViews();
        List<GameState> games = scoreStorage.getAllGames();

        if (games == null || games.isEmpty()) {
            showEmptyState();
            btnDeleteAll.setVisibility(View.GONE);
            return;
        }

        btnDeleteAll.setVisibility(View.VISIBLE);

        // Add games in order from 1 to down (oldest first, newest last)
        for (int i = 0; i < games.size(); i++) {
            addGameView(games.get(i), i);
        }
    }

    private void showEmptyState() {
        TextView emptyView = new TextView(requireContext());
        emptyView.setText(R.string.no_recently_game);
        emptyView.setTextSize(16);
        emptyView.setPadding(32, 32, 32, 32);
        emptyView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        scoreContainer.addView(emptyView);
    }

    private void addGameView(GameState gameState, int index) {
        if (gameState == null) return;

        View itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.recently_game_item, scoreContainer, false);

        TextView tvIndex = itemView.findViewById(R.id.tvIndex);
        TextView groupAName = itemView.findViewById(R.id.groupAName);
        TextView groupAScore = itemView.findViewById(R.id.groupAScore);
        TextView groupBName = itemView.findViewById(R.id.groupBName);
        TextView groupBScore = itemView.findViewById(R.id.groupBScore);
        ImageView btnDelete = itemView.findViewById(R.id.delete_game);

        // Index
        tvIndex.setText((index + 1) + " - ");

        // Names
        groupAName.setText(gameState.groupAName);
        groupBName.setText(gameState.groupBName);

        // Total scores
        groupAScore.setText(String.valueOf(calculateTotal(gameState.scoresA)));
        groupBScore.setText(String.valueOf(calculateTotal(gameState.scoresB)));

        // Delete button click listener
        btnDelete.setOnClickListener(v -> showDeleteConfirmation(index));

        // Add games in order (append to bottom)
        scoreContainer.addView(itemView);
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
            Toast.makeText(requireContext(), "No games to delete", Toast.LENGTH_SHORT).show();
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
            loadAllGames(); // Refresh the list
        } else {
            Toast.makeText(requireContext(), "Failed to delete game", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteAllGames() {
        boolean success = scoreStorage.deleteAllGames();
        if (success) {
            Toast.makeText(requireContext(), R.string.all_game_deleted, Toast.LENGTH_SHORT).show();
            loadAllGames(); // Refresh the list
        } else {
            Toast.makeText(requireContext(), "Failed to delete games", Toast.LENGTH_SHORT).show();
        }
    }

    private int calculateTotal(List<Integer> scores) {
        int total = 0;
        if (scores == null) return total;
        for (int score : scores) {
            total += score;
        }
        return total;
    }
}