package com.example.sadaguessgame.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.sadaguessgame.R;
import com.example.sadaguessgame.data.LeaderboardEntry;
import com.example.sadaguessgame.data.LeaderboardStorage;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class LeaderboardFragment extends BaseFragment {

    private LinearLayout       rowContainer;
    private LinearLayout emptyTv;
    private LeaderboardStorage storage;

    public LeaderboardFragment() {}

    @SuppressLint("WrongViewCast")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_leaderboard, container, false);

        storage      = LeaderboardStorage.getInstance(requireContext());
        rowContainer = root.findViewById(R.id.leaderboardRowContainer);
        emptyTv      = root.findViewById(R.id.tvLeaderboardEmpty);

        MaterialButton btnClear = root.findViewById(R.id.btnClearLeaderboard);
        if (btnClear != null) btnClear.setOnClickListener(v -> confirmClear());

        loadEntries();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadEntries();
    }

    private void loadEntries() {
        if (rowContainer == null) return;
        rowContainer.removeAllViews();

        List<LeaderboardEntry> entries = storage.getTopEntries(20);

        if (entries.isEmpty()) {
            if (emptyTv != null) emptyTv.setVisibility(View.VISIBLE);
            return;
        }
        if (emptyTv != null) emptyTv.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (int i = 0; i < entries.size(); i++) {
            LeaderboardEntry e   = entries.get(i);
            View             row = inflater.inflate(
                    R.layout.item_leaderboard_row, rowContainer, false);

            TextView tvRank    = row.findViewById(R.id.tvLbRank);
            TextView tvName    = row.findViewById(R.id.tvLbName);
            TextView tvRecord  = row.findViewById(R.id.tvLbRecord);
            TextView tvStreak  = row.findViewById(R.id.tvLbStreak);
            TextView tvWinRate = row.findViewById(R.id.tvLbWinRate);
            TextView tvGames   = row.findViewById(R.id.tvLbGamesPlayed);
            TextView tvPoints  = row.findViewById(R.id.tvLbPoints);

            // Rank with medal emoji for top 3
            String rankLabel;
            switch (i) {
                case 0:  rankLabel = getString(R.string.leaderboard_rank_gold);   break;
                case 1:  rankLabel = getString(R.string.leaderboard_rank_silver); break;
                case 2:  rankLabel = getString(R.string.leaderboard_rank_bronze); break;
                default: rankLabel = String.valueOf(i + 1);                       break;
            }
            if (tvRank != null) tvRank.setText(rankLabel);

            // Group name
            if (tvName != null) tvName.setText(e.groupName);

            // W/L/D record — what it actually tracks: wins, losses, draws across games
            if (tvRecord != null) tvRecord.setText(
                    getString(R.string.leaderboard_record_format,
                            e.totalWins, e.totalLosses, e.totalDraws));

            // Best win streak — max consecutive wins
            if (tvStreak != null) tvStreak.setText(
                    getString(R.string.leaderboard_streak_format, e.maxWinStreak));

            // Win rate percentage
            if (tvWinRate != null) tvWinRate.setText(e.getWinRateFormatted());

            // Total games played
            if (tvGames != null) tvGames.setText(
                    getString(R.string.leaderboard_games_played_format, e.gamesPlayed));

            // Total points scored across all games
            if (tvPoints != null) {
                tvPoints.setText(getString(R.string.leaderboard_points_format, e.totalPointsScored));
                tvPoints.setVisibility(View.VISIBLE);
            }

            // Highlight top 3 rows
            if (i < 3) {
                row.setAlpha(1f);
                row.setElevation(getResources().getDimension(R.dimen.small_size_layout));
            }

            rowContainer.addView(row);
        }
    }

    private void confirmClear() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.leaderboard_clear_title)
                .setMessage(R.string.leaderboard_clear_message)
                .setPositiveButton(R.string.delete, (d, w) -> {
                    storage.clearAll();
                    loadEntries();
                    Toast.makeText(requireContext(),
                            R.string.leaderboard_cleared, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.no_button, null)
                .show();
    }
}