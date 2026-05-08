package com.example.sadaguessgame.activities;

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
        backButton.setOnClickListener(v -> finish());

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
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < items.size(); i++) {
            ScoreBoardActivity.HistoryItem item = items.get(i);
            View row = inflater.inflate(R.layout.item_score_linear, container, false);

            TextView tvIndex = row.findViewById(R.id.tvIndex);
            TextView tvGroup = row.findViewById(R.id.tvGroup);

            tvIndex.setText(getString(R.string.history_item_index_format, i + 1));
            tvGroup.setText(item.groupName != null ? item.groupName :
                    (item.isGroupA ? getString(R.string.group_a_color) : getString(R.string.group_b_color)));

            container.addView(row);
        }
    }
}