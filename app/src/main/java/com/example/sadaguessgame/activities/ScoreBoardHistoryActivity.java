package com.example.sadaguessgame.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.sadaguessgame.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

public class ScoreBoardHistoryActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoreboard_history);

        ImageView backButton = findViewById(R.id.back_home_activity_button);
        if (backButton != null) backButton.setOnClickListener(v -> finish());

        String json = getIntent().getStringExtra(ScoreBoardActivity.EXTRA_HISTORY);
        if (json == null) { finish(); return; }

        try {
            Type type = new TypeToken<List<ScoreBoardActivity.HistoryItem>>() {}.getType();
            List<ScoreBoardActivity.HistoryItem> items = new Gson().fromJson(json, type);
            populateHistory(items);
        } catch (Exception e) {
            finish();
        }
    }

    private void populateHistory(List<ScoreBoardActivity.HistoryItem> items) {
        LinearLayout container = findViewById(R.id.historyListContainer);
        if (container == null || items == null) return;

        LayoutInflater inflater = LayoutInflater.from(this);

        // Running totals for context
        int totalA = 0, totalB = 0;

        for (int i = 0; i < items.size(); i++) {
            ScoreBoardActivity.HistoryItem item = items.get(i);

            // Update running totals
            if (item.isGroupA) totalA += item.scoreAdded;
            else               totalB += item.scoreAdded;

            View row = inflater.inflate(R.layout.item_history_row, container, false);

            TextView tvIndex     = row.findViewById(R.id.tvHistoryIndex);
            TextView tvGroupName = row.findViewById(R.id.tvHistoryGroupName);
            TextView tvScore     = row.findViewById(R.id.tvHistoryScore);
            TextView tvRunning   = row.findViewById(R.id.tvHistoryRunningTotal);
            View     colorDot    = row.findViewById(R.id.viewHistoryColorDot);

            if (tvIndex != null) {
                tvIndex.setText(getString(R.string.history_item_index_format, i + 1));
            }

            String name = item.groupName != null ? item.groupName :
                    (item.isGroupA ? getString(R.string.group_a_color)
                            : getString(R.string.group_b_color));
            if (tvGroupName != null) tvGroupName.setText(name);

            if (tvScore != null) {
                tvScore.setText(getString(R.string.score_added_format, item.scoreAdded));
            }

            // Running total shows live score at this point
            if (tvRunning != null) {
                tvRunning.setText(getString(R.string.group_a_color) + ": " + totalA
                        + "  |  " + getString(R.string.group_b_color) + ": " + totalB);
            }

            // Color dot: blue-ish for group A, teal for group B
            if (colorDot != null) {
                int dotColor = item.isGroupA
                        ? getResources().getColor(R.color.score_card_a_start, null)
                        : getResources().getColor(R.color.score_card_b_start, null);
                colorDot.setBackgroundColor(dotColor);
            }

            // Alternate row backgrounds for readability
            if (i % 2 == 0) {
                row.setBackgroundColor(
                        getResources().getColor(R.color.surface_elevated, null));
            } else {
                row.setBackgroundColor(Color.TRANSPARENT);
            }

            container.addView(row);
        }

        // Summary footer
        View footer = inflater.inflate(R.layout.item_history_footer, container, false);
        if (footer != null) {
            TextView tvFinalA = footer.findViewById(R.id.tvFinalScoreA);
            TextView tvFinalB = footer.findViewById(R.id.tvFinalScoreB);
            if (tvFinalA != null) tvFinalA.setText(String.valueOf(totalA));
            if (tvFinalB != null) tvFinalB.setText(String.valueOf(totalB));
            container.addView(footer);
        }
    }
}