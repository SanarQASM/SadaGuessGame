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
import com.example.sadaguessgame.data.LeaderboardEntry;
import com.example.sadaguessgame.data.LeaderboardStorage;
import com.google.android.material.button.MaterialButton;
import java.util.List;

/**
 * Hall of Fame fragment displayed as a 4th tab in MainActivity.
 *
 * Shows the top 20 groups ranked by wins → win-rate → total points.
 * Each row shows: rank medal, group name, W/L/D record, best streak,
 * win rate, and total points scored.
 *
 * A "Clear leaderboard" button is available at the bottom.
 */
public class LeaderboardFragment extends BaseFragment {

    private LinearLayout        rowContainer;
    private TextView            emptyTv;
    private LeaderboardStorage  storage;

    public LeaderboardFragment() {}

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
        btnClear.setOnClickListener(v -> confirmClear());

        loadEntries();
        return root;
    }

    // ─── Load ────────────────────────────────────────────────────────────────

    private void loadEntries() {
        rowContainer.removeAllViews();
        List<LeaderboardEntry> entries = storage.getTopEntries(20);

        if (entries.isEmpty()) {
            emptyTv.setVisibility(View.VISIBLE);
            return;
        }
        emptyTv.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (int i = 0; i < entries.size(); i++) {
            LeaderboardEntry e   = entries.get(i);
            View             row = inflater.inflate(
                    R.layout.item_leaderboard_row, rowContainer, false);

            TextView tvRank      = row.findViewById(R.id.tvLbRank);
            TextView tvName      = row.findViewById(R.id.tvLbName);
            TextView tvRecord    = row.findViewById(R.id.tvLbRecord);
            TextView tvStreak    = row.findViewById(R.id.tvLbStreak);
            TextView tvWinRate   = row.findViewById(R.id.tvLbWinRate);
            TextView tvPoints    = row.findViewById(R.id.tvLbPoints);

            // Rank medal
            String rankLabel;
            switch (i) {
                case 0: rankLabel = "🥇"; break;
                case 1: rankLabel = "🥈"; break;
                case 2: rankLabel = "🥉"; break;
                default: rankLabel = String.valueOf(i + 1); break;
            }
            tvRank.setText(rankLabel);
            tvName.setText(e.groupName);
            tvRecord.setText(e.totalWins + "W  " + e.totalLosses + "L  " + e.totalDraws + "D");
            tvStreak.setText("🔥 " + e.maxWinStreak);
            tvWinRate.setText(e.getWinRateFormatted());
            tvPoints.setText(String.valueOf(e.totalPointsScored));

            rowContainer.addView(row);
        }
    }

    // ─── Clear ───────────────────────────────────────────────────────────────

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

    @Override
    public void onResume() {
        super.onResume();
        loadEntries();   // refresh when returning from a game
    }
}