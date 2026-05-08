package com.example.sadaguessgame.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sadaguessgame.R;
import com.example.sadaguessgame.ui.LearnSectionAdapter;

public class LearnGameActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_game);

        ImageView backButton = findViewById(R.id.back_home_activity_button);
        backButton.setOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.learnRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new LearnSectionAdapter(this, buildSections()));
    }

    private LearnSectionAdapter.Section[] buildSections() {
        return new LearnSectionAdapter.Section[]{
                new LearnSectionAdapter.Section(
                        getString(R.string.learn_section_overview_title),
                        getString(R.string.learn_section_overview_desc),
                        R.drawable.sada_guess_main_image
                ),
                new LearnSectionAdapter.Section(
                        getString(R.string.learn_section_new_game_title),
                        getString(R.string.learn_section_new_game_desc),
                        R.drawable.create_new_game
                ),
                new LearnSectionAdapter.Section(
                        getString(R.string.learn_section_cards_title),
                        getString(R.string.learn_section_cards_desc),
                        R.drawable.cards
                ),
                new LearnSectionAdapter.Section(
                        getString(R.string.learn_section_scoring_title),
                        getString(R.string.learn_section_scoring_desc),
                        R.drawable.score
                ),
                new LearnSectionAdapter.Section(
                        getString(R.string.learn_section_timer_title),
                        getString(R.string.learn_section_timer_desc),
                        R.drawable.timer
                ),
                new LearnSectionAdapter.Section(
                        getString(R.string.learn_section_dice_title),
                        getString(R.string.learn_section_dice_desc),
                        R.drawable.dice
                ),
                new LearnSectionAdapter.Section(
                        getString(R.string.learn_section_scoreboard_title),
                        getString(R.string.learn_section_scoreboard_desc),
                        R.drawable.score
                ),
                new LearnSectionAdapter.Section(
                        getString(R.string.learn_section_wordpacks_title),
                        getString(R.string.learn_section_wordpacks_desc),
                        R.drawable.general
                ),
                new LearnSectionAdapter.Section(
                        getString(R.string.learn_section_difficulty_title),
                        getString(R.string.learn_section_difficulty_desc),
                        R.drawable.challenge
                ),
                new LearnSectionAdapter.Section(
                        getString(R.string.learn_section_streak_title),
                        getString(R.string.learn_section_streak_desc),
                        R.drawable.people
                ),
                new LearnSectionAdapter.Section(
                        getString(R.string.learn_section_hints_title),
                        getString(R.string.learn_section_hints_desc),
                        R.drawable.animal
                ),
                new LearnSectionAdapter.Section(
                        getString(R.string.learn_section_leaderboard_title),
                        getString(R.string.learn_section_leaderboard_desc),
                        R.drawable.general
                ),
                new LearnSectionAdapter.Section(
                        getString(R.string.learn_section_voice_title),
                        getString(R.string.learn_section_voice_desc),
                        R.drawable.occupation
                ),
                new LearnSectionAdapter.Section(
                        getString(R.string.learn_section_sudden_death_title),
                        getString(R.string.learn_section_sudden_death_desc),
                        R.drawable.challenge
                ),
        };
    }
}