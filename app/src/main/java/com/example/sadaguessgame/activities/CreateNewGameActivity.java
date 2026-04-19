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

import java.util.ArrayList;
import java.util.List;

public class CreateNewGameActivity extends BaseActivity {

    private NumberPicker secondPicker, minutePicker;
    private EditText groupNameOne, groupNameTwo;
    private Spinner spinner;
    private CategoryAdapter categoryAdapter;
    private GameState newGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_new_game_activity);

        // ------------------- ACTION BAR BACK ARROW (OPTIONAL) -------------------
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // ------------------- CREATE NEW GAME WITH FACTORY METHOD -------------------
        newGame = new GameState();

        // ------------------- INITIALIZE UI -------------------
        initPickers();
        initSpinner();
        initRecycler();
        initGroupInputs();
        initBackButton();
    }

    // ------------------- BACK BUTTON -------------------
    private void initBackButton() {
        ImageView backButton = findViewById(R.id.back_home_activity_button);
        backButton.setOnClickListener(v -> finish());
    }

    // ------------------- NUMBER PICKERS -------------------
    private void initPickers() {
        minutePicker = findViewById(R.id.minutePicker2);
        secondPicker = findViewById(R.id.secondPicker2);

        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        secondPicker.setMinValue(0);
        secondPicker.setMaxValue(59);

        minutePicker.setWrapSelectorWheel(true);
        secondPicker.setWrapSelectorWheel(true);

        minutePicker.setValue(1);
        secondPicker.setValue(0);
    }

    // ------------------- SPINNER -------------------
    private void initSpinner() {
        spinner = findViewById(R.id.item_spinner);

        // Set adapter with custom layout
        String[] roundsStr = getResources().getStringArray(R.array.round);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                roundsStr);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Set default selection
        spinner.setSelection(2); // default = 10

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                newGame.totalRounds = Integer.parseInt(roundsStr[position]); // save to game
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // ------------------- GROUP INPUTS -------------------
    private void initGroupInputs() {
        groupNameOne = findViewById(R.id.groupNameOne);
        groupNameTwo = findViewById(R.id.groupNameTwo);
        Button btnNext = findViewById(R.id.btnNext);

        btnNext.setOnClickListener(v -> {
            if (validateGroupNames()) {
                // Set all fields before saving
                newGame.gameId = "game_" + System.currentTimeMillis();
                newGame.groupAName = groupNameOne.getText().toString().trim();
                newGame.groupBName = groupNameTwo.getText().toString().trim();
                newGame.minutePicker = minutePicker.getValue();
                newGame.secondPicker = secondPicker.getValue();
                newGame.totalRounds = Integer.parseInt(spinner.getSelectedItem().toString());
                newGame.categories = categoryAdapter.getSelectedCategoryNames();

                // Save game
                ScoreStorage.getInstance(this).saveCurrentGame(newGame);
                openCardActivity();
            }
        });
    }

    private void openCardActivity() {
        applyDefaultsIfNeeded();
        Intent intent = new Intent(CreateNewGameActivity.this, CardsActivity.class);
        // Open next activity
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    // ------------------- VALIDATION -------------------
    private boolean validateGroupNames() {
        String nameOne = groupNameOne.getText().toString().trim();
        String nameTwo = groupNameTwo.getText().toString().trim();

        boolean valid = true;

        // Count actual visible characters, not bytes
        int nameOneLength = nameOne.codePointCount(0, nameOne.length());
        int nameTwoLength = nameTwo.codePointCount(0, nameTwo.length());

        if (nameOne.isEmpty() || nameOneLength > 20) {
            Toast.makeText(this, getString(R.string.error_group_name), Toast.LENGTH_SHORT).show();
            groupNameOne.setBackgroundResource(R.drawable.bg_edit_text_error);
            valid = false;
        } else {
            groupNameOne.setBackgroundResource(R.drawable.bg_edit_text_selector);
        }

        if (nameTwo.isEmpty() || nameTwoLength > 20) {
            Toast.makeText(this, getString(R.string.error_group_name), Toast.LENGTH_SHORT).show();
            groupNameTwo.setBackgroundResource(R.drawable.bg_edit_text_error);
            valid = false;
        } else {
            groupNameTwo.setBackgroundResource(R.drawable.bg_edit_text_selector);
        }

        return valid;
    }

    // ------------------- APPLY DEFAULTS -------------------
    private void applyDefaultsIfNeeded() {
        // Categories
        if (categoryAdapter.getSelectedCategoryNames().isEmpty()) {
            categoryAdapter.selectAll();
        }

        // Spinner
        if (spinner.getSelectedItem() == null) {
            spinner.setSelection(2);
        }

        // Timer
        if (minutePicker.getValue() == 0 && secondPicker.getValue() == 0) {
            minutePicker.setValue(1);
            secondPicker.setValue(0);
        }
    }

    // ------------------- RECYCLER VIEW -------------------
    private void initRecycler() {
        RecyclerView recyclerGames = findViewById(R.id.recyclerCategory);

        CategoryCards[] categoriesEnum = CategoryCards.values();
        CategoryType[] categoryTypes = CategoryType.values();

        List<Category> categories = new ArrayList<>();

        for (int i = 0; i < categoriesEnum.length; i++) {
            CategoryCards categoryEnum = categoriesEnum[i];

            String displayName = categoryEnum.getDisplayName(this);  // Localized
            String englishName = categoryEnum.getEnglishName();      // English only
            int imageResId = categoryTypes[i].getDrawableId();

            categories.add(new Category(displayName, englishName, imageResId));
        }

        categoryAdapter = new CategoryAdapter(this, categories);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 5);
        recyclerGames.setLayoutManager(layoutManager);
        recyclerGames.setAdapter(categoryAdapter);
    }

    // ------------------- TOOLBAR BACK ARROW -------------------
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}