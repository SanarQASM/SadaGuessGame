package com.example.sadaguessgame.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sadaguessgame.R;
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
 * Game setup screen.
 *
 * NEW in v2:
 *  • Difficulty level spinner (Easy / Medium / Hard).
 *  • Voice clue toggle switch.
 *  • "Use Word Pack" button that opens WordPackActivity.
 *    If a pack is selected its id is stored in GameState.wordPackId
 *    and the category RecyclerView is hidden (not needed for custom packs).
 */
public class CreateNewGameActivity extends BaseActivity {

    // ─── Views ───────────────────────────────────────────────────────────────
    private NumberPicker      secondPicker, minutePicker;
    private EditText          groupNameOne, groupNameTwo;
    private Spinner           roundSpinner, difficultySpinner;
    @SuppressWarnings("deprecation")
    private Switch            voiceClueSwitch;
    private CategoryAdapter   categoryAdapter;
    private MaterialButton    btnWordPack;
    private View              categorySection;

    // ─── State ───────────────────────────────────────────────────────────────
    private GameState newGame;
    private long      selectedWordPackId = -1L;   // -1 = use built-in assets

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_new_game_activity);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        newGame = new GameState();

        initPickers();
        initRoundSpinner();
        initDifficultySpinner();
        initRecycler();
        initGroupInputs();
        initVoiceClueSwitch();
        initWordPackButton();
        initBackButton();
    }

    // ─── Back ────────────────────────────────────────────────────────────────

    private void initBackButton() {
        ImageView btn = findViewById(R.id.back_home_activity_button);
        btn.setOnClickListener(v -> finish());
    }

    // ─── Pickers ─────────────────────────────────────────────────────────────

    private void initPickers() {
        minutePicker = findViewById(R.id.minutePicker2);
        secondPicker = findViewById(R.id.secondPicker2);
        minutePicker.setMinValue(0); minutePicker.setMaxValue(59);
        secondPicker.setMinValue(0); secondPicker.setMaxValue(59);
        minutePicker.setWrapSelectorWheel(true);
        secondPicker.setWrapSelectorWheel(true);
        minutePicker.setValue(1);
        secondPicker.setValue(0);
    }

    // ─── Round spinner ───────────────────────────────────────────────────────

    private void initRoundSpinner() {
        roundSpinner = findViewById(R.id.item_spinner);
        String[] roundsStr = getResources().getStringArray(R.array.round);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, roundsStr);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roundSpinner.setAdapter(adapter);
        roundSpinner.setSelection(2);   // default 10
        roundSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                newGame.totalRounds = Integer.parseInt(roundsStr[pos]);
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
    }

    // ─── Difficulty spinner (NEW) ─────────────────────────────────────────────

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
        difficultySpinner.setSelection(1);   // default Medium
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

    // ─── Voice clue switch (NEW) ──────────────────────────────────────────────

    @SuppressWarnings("deprecation")
    private void initVoiceClueSwitch() {
        voiceClueSwitch = findViewById(R.id.voiceClueSwitch);
        if (voiceClueSwitch == null) return;
        voiceClueSwitch.setOnCheckedChangeListener((btn, checked) ->
                newGame.voiceClueModeEnabled = checked);
    }

    // ─── Word pack button (NEW) ───────────────────────────────────────────────

    private void initWordPackButton() {
        btnWordPack    = findViewById(R.id.btnSelectWordPack);
        categorySection = findViewById(R.id.categorySection);
        if (btnWordPack == null) return;

        btnWordPack.setOnClickListener(v -> {
            // Open WordPackActivity for selection; result comes back in onActivityResult
            Intent intent = new Intent(this, WordPackActivity.class);
            intent.putExtra(WordPackActivity.EXTRA_SELECT_MODE, true);
            startActivityForResult(intent, WordPackActivity.REQ_SELECT_PACK);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == WordPackActivity.REQ_SELECT_PACK
                && resultCode == RESULT_OK && data != null) {
            selectedWordPackId = data.getLongExtra(WordPackActivity.RESULT_PACK_ID, -1L);
            if (selectedWordPackId >= 0) {
                WordPack pack = WordPackStorage.getInstance(this)
                        .getPackById(selectedWordPackId);
                if (pack != null) {
                    btnWordPack.setText(pack.name);
                    // Hide category grid — not needed for custom packs
                    if (categorySection != null)
                        categorySection.setVisibility(View.GONE);
                }
            } else {
                // User deselected
                selectedWordPackId = -1L;
                btnWordPack.setText(R.string.btn_select_word_pack);
                if (categorySection != null)
                    categorySection.setVisibility(View.VISIBLE);
            }
        }
    }

    // ─── Group name inputs ────────────────────────────────────────────────────

    private void initGroupInputs() {
        groupNameOne = findViewById(R.id.groupNameOne);
        groupNameTwo = findViewById(R.id.groupNameTwo);
        Button btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(v -> {
            if (validateGroupNames()) {
                buildAndSaveGame();
                openCardActivity();
            }
        });
    }

    private void buildAndSaveGame() {
        newGame.gameId        = "game_" + System.currentTimeMillis();
        newGame.groupAName    = groupNameOne.getText().toString().trim();
        newGame.groupBName    = groupNameTwo.getText().toString().trim();
        newGame.minutePicker  = minutePicker.getValue();
        newGame.secondPicker  = secondPicker.getValue();
        newGame.totalRounds   = Integer.parseInt(roundSpinner.getSelectedItem().toString());
        newGame.wordPackId    = selectedWordPackId;

        if (selectedWordPackId < 0) {
            // Use selected categories
            newGame.categories = categoryAdapter.getSelectedCategoryNames();
        }

        applyDefaultsIfNeeded();
        ScoreStorage.getInstance(this).saveCurrentGame(newGame);
    }

    private void openCardActivity() {
        startActivity(new Intent(this, CardsActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    // ─── Validation ──────────────────────────────────────────────────────────

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

    // ─── Defaults ────────────────────────────────────────────────────────────

    private void applyDefaultsIfNeeded() {
        if (newGame.wordPackId < 0 && categoryAdapter.getSelectedCategoryNames().isEmpty())
            categoryAdapter.selectAll();
        if (minutePicker.getValue() == 0 && secondPicker.getValue() == 0)
            minutePicker.setValue(1);
    }

    // ─── Recycler ────────────────────────────────────────────────────────────

    private void initRecycler() {
        RecyclerView recycler = findViewById(R.id.recyclerCategory);
        CategoryCards[]  cats  = CategoryCards.values();
        CategoryType[]   types = CategoryType.values();
        List<Category>   list  = new ArrayList<>();
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