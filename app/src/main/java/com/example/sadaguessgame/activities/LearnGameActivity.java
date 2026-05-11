package com.example.sadaguessgame.activities;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sadaguessgame.R;
import com.example.sadaguessgame.ui.LearnSectionAdapter;

/**
 * Comprehensive "How to Play" screen.
 * Launched from BOTH the Home page "Learn" button AND the Settings "How to Play" button,
 * so there is exactly one implementation for both entry points.
 */
public class LearnGameActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_game);

        ImageView backButton = findViewById(R.id.back_home_activity_button);
        if (backButton != null) backButton.setOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.learnRecyclerView);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new LearnSectionAdapter(this, buildSections()));
        }
    }

    private LearnSectionAdapter.Section[] buildSections() {
        return new LearnSectionAdapter.Section[]{
                section(R.string.learn_section_overview_title,
                        R.string.learn_section_overview_desc,
                        R.drawable.sada_guess_main_image),
                section(R.string.learn_section_new_game_title,
                        R.string.learn_section_new_game_desc,
                        R.drawable.create_new_game),
                section(R.string.learn_section_cards_title,
                        R.string.learn_section_cards_desc,
                        R.drawable.cards),
                section(R.string.learn_section_scoring_title,
                        R.string.learn_section_scoring_desc,
                        R.drawable.score),
                section(R.string.learn_section_timer_title,
                        R.string.learn_section_timer_desc,
                        R.drawable.timer),
                section(R.string.learn_section_dice_title,
                        R.string.learn_section_dice_desc,
                        R.drawable.dice),
                section(R.string.learn_section_scoreboard_title,
                        R.string.learn_section_scoreboard_desc,
                        R.drawable.score),
                section(R.string.learn_section_wordpacks_title,
                        R.string.learn_section_wordpacks_desc,
                        R.drawable.general),
                section(R.string.learn_section_difficulty_title,
                        R.string.learn_section_difficulty_desc,
                        R.drawable.challenge),
                section(R.string.learn_section_streak_title,
                        R.string.learn_section_streak_desc,
                        R.drawable.people),
                section(R.string.learn_section_hints_title,
                        R.string.learn_section_hints_desc,
                        R.drawable.animal),
                section(R.string.learn_section_leaderboard_title,
                        R.string.learn_section_leaderboard_desc,
                        R.drawable.general),
                section(R.string.learn_section_voice_title,
                        R.string.learn_section_voice_desc,
                        R.drawable.occupation),
                section(R.string.learn_section_sudden_death_title,
                        R.string.learn_section_sudden_death_desc,
                        R.drawable.challenge),
        };
    }

    private LearnSectionAdapter.Section section(int titleRes, int descRes, int imageRes) {
        return new LearnSectionAdapter.Section(
                getString(titleRes), getString(descRes), imageRes);
    }
}