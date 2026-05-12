package com.example.sadaguessgame.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sadaguessgame.R;
import com.example.sadaguessgame.activities.TeamSpinnerActivity;
import com.example.sadaguessgame.data.GameState;
import com.example.sadaguessgame.data.ScoreStorage;
import com.example.sadaguessgame.data.WordPack;
import com.example.sadaguessgame.data.WordPackStorage;
import com.example.sadaguessgame.enums.CategoryCards;
import com.example.sadaguessgame.enums.CategoryType;
import com.example.sadaguessgame.enums.DifficultyLevel;
import com.example.sadaguessgame.helper.Category;
import com.example.sadaguessgame.helper.CategoryAdapter;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * CreateNewGameActivity (updated for Feature 1).
 *
 * Change vs original:
 *  • "Split my team" button launches TeamSpinnerActivity in SELECT_MODE.
 *    When the user picks teams there the names are pre-filled here.
 */
public class CreateNewGameActivity extends BaseActivity {

    private static final int REQ_SPINNER = 5001;

    // ─── Views ────────────────────────────────────────────────────────────────
    private NumberPicker    secondPicker, minutePicker;
    private EditText        groupNameOne, groupNameTwo;
    private Spinner         roundSpinner, difficultySpinner, hintCountSpinner;
    @SuppressWarnings("deprecation")
    private Switch          voiceClueSwitch;
    private CategoryAdapter categoryAdapter;
    private MaterialButton  btnWordPack;
    private MaterialButton  btnSplitTeam;   // Feature 1
    private View            categorySection;

    // ─── State ────────────────────────────────────────────────────────────────
    private GameState newGame;
    private long      selectedWordPackId = -1L;
    private int       selectedHintCount  = GameState.DEFAULT_HINT_COUNT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_new_game_activity);

        newGame = new GameState();

        initPickers();
        initRoundSpinner();
        initDifficultySpinner();
        initHintCountSpinner();
        initRecycler();
        initGroupInputs();
        initVoiceClueSwitch();
        initWordPackButton();
        initSplitTeamButton();   // Feature 1
        initBackButton();
    }

    // ─── Back ─────────────────────────────────────────────────────────────────

    private void initBackButton() {
        ImageView btn = findViewById(R.id.back_home_activity_button);
        if (btn != null) btn.setOnClickListener(v -> finish());
    }

    // ─── Feature 1: Split-team helper ─────────────────────────────────────────

    private void initSplitTeamButton() {
        btnSplitTeam = findViewById(R.id.btnSplitTeam);
        if (btnSplitTeam == null) return;
        btnSplitTeam.setOnClickListener(v -> {
            Intent intent = new Intent(this, TeamSpinnerActivity.class);
            intent.putExtra(TeamSpinnerActivity.EXTRA_SELECT_MODE, true);
            startActivityForResult(intent, REQ_SPINNER);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Feature 1: pre-fill group names from spinner result
        if (requestCode == REQ_SPINNER && resultCode == RESULT_OK && data != null) {
            String teamA = data.getStringExtra(TeamSpinnerActivity.EXTRA_GROUP_A);
            String teamB = data.getStringExtra(TeamSpinnerActivity.EXTRA_GROUP_B);
            if (groupNameOne != null && teamA != null && !teamA.isEmpty())
                groupNameOne.setText(teamA);
            if (groupNameTwo != null && teamB != null && !teamB.isEmpty())
                groupNameTwo.setText(teamB);
            return;
        }

        // Word pack selection (unchanged)
        if (requestCode == WordPackActivity.REQ_SELECT_PACK
                && resultCode == RESULT_OK && data != null) {
            selectedWordPackId = data.getLongExtra(WordPackActivity.RESULT_PACK_ID, -1L);
            if (selectedWordPackId >= 0) {
                WordPack pack = WordPackStorage.getInstance(this).getPackById(selectedWordPackId);
                if (pack != null) {
                    btnWordPack.setText(pack.name);
                    if (categorySection != null)
                        categorySection.setVisibility(View.GONE);
                }
            } else {
                selectedWordPackId = -1L;
                btnWordPack.setText(R.string.btn_select_word_pack);
                if (categorySection != null)
                    categorySection.setVisibility(View.VISIBLE);
            }
        }
    }

    // ─── Pickers ──────────────────────────────────────────────────────────────

    private void initPickers() {
        minutePicker = findViewById(R.id.minutePicker2);
        secondPicker = findViewById(R.id.secondPicker2);

        minutePicker.setMinValue(0); minutePicker.setMaxValue(59);
        secondPicker.setMinValue(0); secondPicker.setMaxValue(59);
        minutePicker.setWrapSelectorWheel(true);
        secondPicker.setWrapSelectorWheel(true);
        minutePicker.setValue(1);
        secondPicker.setValue(0);

        minutePicker.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        secondPicker.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
    }

    // ─── Round spinner ────────────────────────────────────────────────────────

    private void initRoundSpinner() {
        roundSpinner = findViewById(R.id.item_spinner);
        String[] roundsStr = getResources().getStringArray(R.array.round);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, roundsStr);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roundSpinner.setAdapter(adapter);
        roundSpinner.setSelection(2);
        roundSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                try { newGame.totalRounds = Integer.parseInt(roundsStr[pos]); }
                catch (NumberFormatException ignored) {}
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
    }

    // ─── Difficulty spinner ───────────────────────────────────────────────────

    private void initDifficultySpinner() {
        difficultySpinner = findViewById(R.id.difficultySpinner);
        if (difficultySpinner == null) return;

        String[] labels = {
                getString(R.string.difficulty_easy),
                getString(R.string.difficulty_medium),
                getString(R.string.difficulty_hard)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(adapter);
        difficultySpinner.setSelection(1);
        difficultySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                switch (pos) {
                    case 0: newGame.difficultyLevel = DifficultyLevel.EASY.getFolderName();   break;
                    case 2: newGame.difficultyLevel = DifficultyLevel.HARD.getFolderName();   break;
                    default: newGame.difficultyLevel = DifficultyLevel.MEDIUM.getFolderName();
                }
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
    }

    // ─── Hint count spinner ───────────────────────────────────────────────────

    private void initHintCountSpinner() {
        hintCountSpinner = findViewById(R.id.hintCountSpinner);
        if (hintCountSpinner == null) return;

        String[] options = getResources().getStringArray(R.array.hint_count_options);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hintCountSpinner.setAdapter(adapter);
        hintCountSpinner.setSelection(GameState.DEFAULT_HINT_COUNT);
        selectedHintCount = GameState.DEFAULT_HINT_COUNT;

        hintCountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                try { selectedHintCount = Integer.parseInt(options[pos]); }
                catch (NumberFormatException ignored) {
                    selectedHintCount = GameState.DEFAULT_HINT_COUNT;
                }
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
    }

    // ─── Voice clue switch ────────────────────────────────────────────────────

    @SuppressWarnings("deprecation")
    private void initVoiceClueSwitch() {
        voiceClueSwitch = findViewById(R.id.voiceClueSwitch);
        if (voiceClueSwitch == null) return;
        voiceClueSwitch.setOnCheckedChangeListener((btn, checked) ->
                newGame.voiceClueModeEnabled = checked);
    }

    // ─── Word pack button ─────────────────────────────────────────────────────

    private void initWordPackButton() {
        btnWordPack     = findViewById(R.id.btnSelectWordPack);
        categorySection = findViewById(R.id.categorySection);
        if (btnWordPack == null) return;

        btnWordPack.setOnClickListener(v -> {
            Intent intent = new Intent(this, WordPackActivity.class);
            intent.putExtra(WordPackActivity.EXTRA_SELECT_MODE, true);
            startActivityForResult(intent, WordPackActivity.REQ_SELECT_PACK);
        });
    }

    // ─── Group name inputs ────────────────────────────────────────────────────

    private void initGroupInputs() {
        groupNameOne = findViewById(R.id.groupNameOne);
        groupNameTwo = findViewById(R.id.groupNameTwo);

        MaterialButton btnNext = findViewById(R.id.btnNext);
        if (btnNext != null) {
            btnNext.setOnClickListener(v -> {
                if (validateGroupNames()) {
                    buildAndSaveGame();
                    openCardActivity();
                }
            });
        }
    }

    private void buildAndSaveGame() {
        newGame.gameId       = "game_" + System.currentTimeMillis();
        newGame.groupAName   = groupNameOne.getText().toString().trim();
        newGame.groupBName   = groupNameTwo.getText().toString().trim();
        newGame.minutePicker = minutePicker.getValue();
        newGame.secondPicker = secondPicker.getValue();
        newGame.totalRounds  = Integer.parseInt(
                roundSpinner.getSelectedItem().toString());
        newGame.wordPackId   = selectedWordPackId;

        newGame.hintCount       = selectedHintCount;
        newGame.hintsRemainingA = selectedHintCount;
        newGame.hintsRemainingB = selectedHintCount;

        if (selectedWordPackId < 0) {
            newGame.categories = categoryAdapter.getSelectedCategoryNames();
        }

        applyDefaultsIfNeeded();
        ScoreStorage.getInstance(this).saveCurrentGame(newGame);
    }

    private void openCardActivity() {
        startActivity(new Intent(this, CardsActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    // ─── Validation ───────────────────────────────────────────────────────────

    private boolean validateGroupNames() {
        String n1 = groupNameOne.getText().toString().trim();
        String n2 = groupNameTwo.getText().toString().trim();
        boolean valid = true;

        if (n1.isEmpty() || n1.codePointCount(0, n1.length()) > 20) {
            Toast.makeText(this, R.string.error_group_name, Toast.LENGTH_SHORT).show();
            groupNameOne.setBackgroundResource(R.drawable.bg_edit_text_error);
            valid = false;
        } else {
            groupNameOne.setBackgroundResource(R.drawable.bg_edit_text_selector);
        }

        if (n2.isEmpty() || n2.codePointCount(0, n2.length()) > 20) {
            Toast.makeText(this, R.string.error_group_name, Toast.LENGTH_SHORT).show();
            groupNameTwo.setBackgroundResource(R.drawable.bg_edit_text_error);
            valid = false;
        } else {
            groupNameTwo.setBackgroundResource(R.drawable.bg_edit_text_selector);
        }

        return valid;
    }

    // ─── Defaults ─────────────────────────────────────────────────────────────

    private void applyDefaultsIfNeeded() {
        if (newGame.wordPackId < 0 &&
                (newGame.categories == null || newGame.categories.isEmpty())) {
            categoryAdapter.selectAll();
            newGame.categories = categoryAdapter.getSelectedCategoryNames();
        }
        if (minutePicker.getValue() == 0 && secondPicker.getValue() == 0) {
            minutePicker.setValue(1);
            newGame.minutePicker = 1;
        }
    }

    // ─── Recycler ─────────────────────────────────────────────────────────────

    private void initRecycler() {
        RecyclerView recycler = findViewById(R.id.recyclerCategory);
        if (recycler == null) return;

        CategoryCards[] cats  = CategoryCards.values();
        CategoryType[]  types = CategoryType.values();
        List<Category>  list  = new ArrayList<>();
        for (int i = 0; i < cats.length; i++) {
            list.add(new Category(
                    cats[i].getDisplayName(this),
                    cats[i].getEnglishName(),
                    types[i].getDrawableId()));
        }
        categoryAdapter = new CategoryAdapter(this, list);
        recycler.setLayoutManager(new GridLayoutManager(this, 5));
        recycler.setAdapter(categoryAdapter);
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }
}