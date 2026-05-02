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

    private LinearLayout  scoreContainer;
    private ScoreStorage  scoreStorage;
    private MaterialButton btnDeleteAll;

    public RecentlyFragment() {}

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recently_activity, container, false);

        scoreContainer = view.findViewById(R.id.scoreContainer);
        btnDeleteAll   = view.findViewById(R.id.delete_all);
        scoreStorage   = ScoreStorage.getInstance(requireContext());

        btnDeleteAll.setOnClickListener(v -> showDeleteAllConfirmation());
        loadAllGames();
        return view;
    }

    private void loadAllGames() {
        scoreContainer.removeAllViews();

        List<GameState> finishedGames = scoreStorage.getAllGames();
        GameState currentGame         = scoreStorage.getCurrentGame();
        boolean hasUnfinished         = currentGame != null && !currentGame.isFinished;

        boolean isEmpty = (finishedGames == null || finishedGames.isEmpty()) && !hasUnfinished;

        if (isEmpty) {
            showEmptyState();
            btnDeleteAll.setVisibility(View.GONE);
            return;
        }

        btnDeleteAll.setVisibility(View.VISIBLE);

        int displayIndex = 1;

        if (hasUnfinished) {
            // Unfinished game gets row 1 but has no storage index — uses special click handler
            addUnfinishedGameView(currentGame, displayIndex);
            displayIndex++;
        }

        if (finishedGames != null) {
            for (int storageIndex = 0; storageIndex < finishedGames.size(); storageIndex++) {
                // FIX: pass the actual storage index for deletion, not the display index
                addFinishedGameView(finishedGames.get(storageIndex), displayIndex, storageIndex);
                displayIndex++;
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

    /**
     * Adds the in-progress (unfinished) game row.
     * No delete button — the user continues or it persists until a new game starts.
     */
    private void addUnfinishedGameView(GameState gameState, int displayIndex) {
        if (gameState == null) return;
        View itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.recently_game_item, scoreContainer, false);

        ((TextView) itemView.findViewById(R.id.tvIndex)).setText(
                getString(R.string.status_index_continue_format,
                        displayIndex, getString(R.string.continue_game)));
        ((TextView) itemView.findViewById(R.id.groupAName)).setText(gameState.groupAName);
        ((TextView) itemView.findViewById(R.id.groupBName)).setText(gameState.groupBName);
        ((TextView) itemView.findViewById(R.id.groupAScore))
                .setText(String.valueOf(calculateTotal(gameState.scoresA)));
        ((TextView) itemView.findViewById(R.id.groupBScore))
                .setText(String.valueOf(calculateTotal(gameState.scoresB)));

        itemView.setAlpha(0.9f);
        itemView.setOnClickListener(v -> continueUnfinishedGame());
        itemView.findViewById(R.id.delete_game).setVisibility(View.GONE);

        scoreContainer.addView(itemView);
    }

    /**
     * FIX: storageIndex is the true position in getAllGames() list.
     * Previously the display index was passed which was off-by-one when an
     * unfinished game row was prepended to the list.
     */
    private void addFinishedGameView(GameState gameState, int displayIndex, int storageIndex) {
        if (gameState == null) return;
        View itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.recently_game_item, scoreContainer, false);

        ((TextView) itemView.findViewById(R.id.tvIndex)).setText(
                getString(R.string.status_index_simple_format, displayIndex));
        ((TextView) itemView.findViewById(R.id.groupAName)).setText(gameState.groupAName);
        ((TextView) itemView.findViewById(R.id.groupBName)).setText(gameState.groupBName);
        ((TextView) itemView.findViewById(R.id.groupAScore))
                .setText(String.valueOf(calculateTotal(gameState.scoresA)));
        ((TextView) itemView.findViewById(R.id.groupBScore))
                .setText(String.valueOf(calculateTotal(gameState.scoresB)));

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
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteGame(storageIndex))
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
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteAllGames())
                .setNegativeButton(R.string.no_button, null)
                .show();
    }

    private void deleteGame(int storageIndex) {
        boolean success = scoreStorage.deleteGame(storageIndex);
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
        if (scores == null) return 0;
        int total = 0;
        for (int score : scores) total += score;
        return total;
    }
}