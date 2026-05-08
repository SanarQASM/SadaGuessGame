package com.example.sadaguessgame.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.sadaguessgame.R;
import com.example.sadaguessgame.data.WordPack;
import com.example.sadaguessgame.data.WordPackStorage;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import java.util.List;

public class WordPackActivity extends BaseActivity {

    public static final String EXTRA_SELECT_MODE = "select_mode";
    public static final String RESULT_PACK_ID    = "pack_id";
    public static final int    REQ_SELECT_PACK   = 2001;

    private boolean         selectMode;
    private WordPackStorage storage;
    private LinearLayout    packListContainer;
    private LinearLayout    emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_pack);

        selectMode        = getIntent().getBooleanExtra(EXTRA_SELECT_MODE, false);
        storage           = WordPackStorage.getInstance(this);
        packListContainer = findViewById(R.id.packListContainer);
        emptyView         = findViewById(R.id.tvEmptyPacks);

        ImageView back = findViewById(R.id.back_home_activity_button);
        if (back != null) back.setOnClickListener(v -> finish());

        ExtendedFloatingActionButton fab = findViewById(R.id.fabNewPack);
        if (fab != null) fab.setOnClickListener(v -> showCreatePackDialog(null));

        View builtInRow = findViewById(R.id.rowBuiltInCards);
        if (selectMode && builtInRow != null) {
            builtInRow.setVisibility(View.VISIBLE);
            builtInRow.setOnClickListener(v -> {
                Intent result = new Intent();
                result.putExtra(RESULT_PACK_ID, -1L);
                setResult(RESULT_OK, result);
                finish();
            });
        }

        loadPacks();
    }

    // ─── List ─────────────────────────────────────────────────────────────────

    private void loadPacks() {
        packListContainer.removeAllViews();
        List<WordPack> packs = storage.getAllPacks();

        if (packs.isEmpty()) {
            if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
            return;
        }
        if (emptyView != null) emptyView.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (WordPack pack : packs) {
            View row = inflater.inflate(R.layout.item_word_pack, packListContainer, false);

            TextView tvName        = row.findViewById(R.id.tvPackName);
            TextView tvSubtitle    = row.findViewById(R.id.tvPackSubtitle);
            TextView tvCategory    = row.findViewById(R.id.tvPackCategory);
            TextView tvTimesPlayed = row.findViewById(R.id.tvPackTimesPlayed);
            ImageView btnEdit      = row.findViewById(R.id.btnEditPack);
            ImageView btnDelete    = row.findViewById(R.id.btnDeletePack);
            ImageView btnShare     = row.findViewById(R.id.btnSharePack);

            if (tvName != null) tvName.setText(pack.name);

            // Word count label
            if (tvSubtitle != null)
                tvSubtitle.setText(getString(R.string.word_count_format, pack.getWordCount()));

            // Category badge
            if (tvCategory != null) {
                tvCategory.setText(pack.category != null && !pack.category.isEmpty()
                        ? pack.category : getString(R.string.pack_category_hint));
            }

            // Times played
            if (tvTimesPlayed != null)
                tvTimesPlayed.setText(getString(R.string.pack_times_played_format, pack.timesPlayed));

            if (selectMode) {
                row.setOnClickListener(v -> returnPackId(pack.id));
                if (btnEdit   != null) btnEdit.setVisibility(View.GONE);
                if (btnDelete != null) btnDelete.setVisibility(View.GONE);
                if (btnShare  != null) btnShare.setVisibility(View.GONE);
            } else {
                if (btnEdit   != null) btnEdit.setOnClickListener(v -> showCreatePackDialog(pack));
                if (btnDelete != null) btnDelete.setOnClickListener(v -> confirmDelete(pack));
                if (btnShare  != null) btnShare.setOnClickListener(v -> sharePackJson(pack));
            }

            packListContainer.addView(row);
        }
    }

    // ─── Create / Edit dialog ──────────────────────────────────────────────────

    private void showCreatePackDialog(WordPack existing) {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_create_word_pack, null);

        EditText etName      = view.findViewById(R.id.etPackName);
        EditText etCategory  = view.findViewById(R.id.etPackCategory);
        EditText etWords     = view.findViewById(R.id.etPackWords);
        TextView tvWordCount = view.findViewById(R.id.tvWordCount);

        if (existing != null) {
            etName.setText(existing.name);
            etCategory.setText(existing.category);
            if (existing.words != null)
                etWords.setText(android.text.TextUtils.join("\n", existing.words));
        }

        etWords.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                String trimmed = s.toString().trim();
                int count = trimmed.isEmpty() ? 0 : trimmed.split("\\n").length;
                tvWordCount.setText(getString(R.string.word_count_format, count));
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        new AlertDialog.Builder(this)
                .setTitle(existing == null
                        ? getString(R.string.create_word_pack)
                        : getString(R.string.edit_word_pack))
                .setView(view)
                .setPositiveButton(R.string.save_label, (d, w) -> {
                    String name     = etName.getText().toString().trim();
                    String category = etCategory.getText().toString().trim();
                    String rawWords = etWords.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(this, R.string.pack_name_required, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    WordPack pack = (existing != null) ? existing : new WordPack();
                    pack.name     = name;
                    pack.category = category.isEmpty()
                            ? getString(R.string.pack_category_hint) : category;
                    pack.words.clear();

                    for (String line : rawWords.split("\\n")) {
                        String ww = line.trim();
                        if (!ww.isEmpty()) pack.words.add(ww);
                    }

                    if (!pack.isValid()) {
                        Toast.makeText(this, R.string.pack_min_words, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    storage.save(pack);
                    loadPacks();
                    Toast.makeText(this, R.string.pack_saved, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.no_button, null)
                .show();
    }

    // ─── Delete ───────────────────────────────────────────────────────────────

    private void confirmDelete(WordPack pack) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_title)
                .setMessage(R.string.detele_disc)
                .setPositiveButton(R.string.delete, (d, w) -> {
                    storage.delete(pack.id);
                    loadPacks();
                    Toast.makeText(this, R.string.game_deleted, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.no_button, null)
                .show();
    }

    // ─── Share ────────────────────────────────────────────────────────────────

    private void sharePackJson(WordPack pack) {
        String json = storage.exportAsJson(pack.id);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, json);
        intent.putExtra(Intent.EXTRA_SUBJECT,
                getString(R.string.share_pack_title) + ": " + pack.name);
        startActivity(Intent.createChooser(intent, getString(R.string.share_pack_title)));
    }

    // ─── Select mode ──────────────────────────────────────────────────────────

    private void returnPackId(long id) {
        Intent result = new Intent();
        result.putExtra(RESULT_PACK_ID, id);
        setResult(RESULT_OK, result);
        finish();
    }
}