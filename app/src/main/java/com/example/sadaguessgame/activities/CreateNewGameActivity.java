package com.example.sadaguessgame.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sadaguessgame.R;
import com.example.sadaguessgame.enums.CategoryCards;
import com.example.sadaguessgame.enums.CategoryType;
import com.example.sadaguessgame.helper.Category;
import com.example.sadaguessgame.helper.CategoryAdapter;
import com.example.sadaguessgame.data.GameState;
import com.example.sadaguessgame.data.ScoreStorage;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class CreateNewGameActivity extends BaseActivity {

    private NumberPicker secondPicker, minutePicker;
    private TextInputEditText groupNameOne, groupNameTwo;
    private Spinner spinner;
    private CategoryAdapter categoryAdapter;
    private GameState newGame;

    // FIX: track spinner position explicitly to avoid null issues
    private int selectedRoundPosition = 2; // default index = 10 rounds
    private String[] roundsStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_new_game_activity);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        newGame = new GameState();

        initPickers();
        initSpinner();
        initRecycler();
        initGroupInputs();
        initBackButton();
    }

    private void initBackButton() {
        ImageView backButton = findViewById(R.id.back_home_activity_button);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
    }

    private void initPickers() {
        minutePicker = findViewById(R.id.minutePicker2);
        secondPicker = findViewById(R.id.secondPicker2);

        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        secondPicker.setMinValue(0);
        secondPicker.setMaxValue(59);

        minutePicker.setWrapSelectorWheel(true);
        secondPicker.setWrapSelectorWheel(true);

        // Default 1 minute
        minutePicker.setValue(1);
        secondPicker.setValue(0);

        // FIX: update game state live as pickers change
        NumberPicker.OnValueChangeListener pickerListener = (picker, oldVal, newVal) -> {
            newGame.minutePicker = minutePicker.getValue();
            newGame.secondPicker = secondPicker.getValue();
        };
        minutePicker.setOnValueChangedListener(pickerListener);
        secondPicker.setOnValueChangedListener(pickerListener);
    }

    private void initSpinner() {
        spinner = findViewById(R.id.item_spinner);
        roundsStr = getResources().getStringArray(R.array.round);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, roundsStr);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Default: index 2 = 10 rounds
        spinner.setSelection(selectedRoundPosition);
        newGame.totalRounds = Integer.parseInt(roundsStr[selectedRoundPosition]);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRoundPosition = position;
                newGame.totalRounds = Integer.parseInt(roundsStr[position]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void initGroupInputs() {
        groupNameOne = findViewById(R.id.groupNameOne);
        groupNameTwo = findViewById(R.id.groupNameTwo);
        MaterialButton btnNext = findViewById(R.id.btnNext);

        btnNext.setOnClickListener(v -> {
            if (validateGroupNames()) {
                buildGameAndStart();
            }
        });
    }

    private void buildGameAndStart() {
        String nameOne = groupNameOne.getText() != null
                ? groupNameOne.getText().toString().trim() : "";
        String nameTwo = groupNameTwo.getText() != null
                ? groupNameTwo.getText().toString().trim() : "";

        newGame.gameId      = "game_" + System.currentTimeMillis();
        newGame.groupAName  = nameOne;
        newGame.groupBName  = nameTwo;
        newGame.minutePicker = minutePicker.getValue();
        newGame.secondPicker = secondPicker.getValue();

        // FIX: ensure totalRounds is set from spinner, not stale default
        if (spinner.getSelectedItem() != null) {
            newGame.totalRounds = Integer.parseInt(spinner.getSelectedItem().toString());
        } else {
            newGame.totalRounds = 10;
        }

        // FIX: if no category selected, select ALL before saving
        List<String> selected = categoryAdapter.getSelectedCategoryNames();
        if (selected.isEmpty()) {
            categoryAdapter.selectAll();
            selected = categoryAdapter.getSelectedCategoryNames();
        }
        newGame.categories = selected;

        // FIX: ensure timer has a minimum of 1 second
        if (newGame.minutePicker == 0 && newGame.secondPicker == 0) {
            newGame.minutePicker = 1;
            newGame.secondPicker = 0;
        }

        // Reset game state fields for a fresh game
        newGame.currentRound       = 1;
        newGame.groupTurn          = GameState.GROUP_A;
        newGame.turnGroupAFinish   = false;
        newGame.turnGroupBFinish   = false;
        newGame.isFinished         = false;
        newGame.scoresA            = new ArrayList<>();
        newGame.scoresB            = new ArrayList<>();
        newGame.usedCardPaths      = new ArrayList<>();

        ScoreStorage.getInstance(this).saveCurrentGame(newGame);

        Intent intent = new Intent(this, CardsActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private boolean validateGroupNames() {
        String nameOne = groupNameOne.getText() != null
                ? groupNameOne.getText().toString().trim() : "";
        String nameTwo = groupNameTwo.getText() != null
                ? groupNameTwo.getText().toString().trim() : "";

        boolean valid = true;

        int nameOneLen = nameOne.codePointCount(0, nameOne.length());
        int nameTwoLen = nameTwo.codePointCount(0, nameTwo.length());

        if (nameOne.isEmpty() || nameOneLen > 20) {
            Toast.makeText(this, getString(R.string.error_group_name), Toast.LENGTH_SHORT).show();
            groupNameOne.setBackgroundResource(R.drawable.bg_edit_text_error);
            valid = false;
        } else {
            groupNameOne.setBackgroundResource(R.drawable.bg_edit_text_selector);
        }

        if (nameTwo.isEmpty() || nameTwoLen > 20) {
            Toast.makeText(this, getString(R.string.error_group_name), Toast.LENGTH_SHORT).show();
            groupNameTwo.setBackgroundResource(R.drawable.bg_edit_text_error);
            valid = false;
        } else {
            groupNameTwo.setBackgroundResource(R.drawable.bg_edit_text_selector);
        }

        return valid;
    }

    private void initRecycler() {
        RecyclerView recyclerGames = findViewById(R.id.recyclerCategory);

        CategoryCards[] categoriesEnum = CategoryCards.values();
        CategoryType[]  categoryTypes  = CategoryType.values();

        List<Category> categories = new ArrayList<>();
        for (int i = 0; i < categoriesEnum.length; i++) {
            CategoryCards categoryEnum = categoriesEnum[i];
            String displayName  = categoryEnum.getDisplayName(this);
            String englishName  = categoryEnum.getEnglishName();
            int    imageResId   = categoryTypes[i].getDrawableId();
            categories.add(new Category(displayName, englishName, imageResId));
        }

        categoryAdapter = new CategoryAdapter(this, categories);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 5);
        recyclerGames.setLayoutManager(layoutManager);
        recyclerGames.setAdapter(categoryAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}