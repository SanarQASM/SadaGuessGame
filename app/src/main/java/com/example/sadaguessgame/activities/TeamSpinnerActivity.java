package com.example.sadaguessgame.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.sadaguessgame.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Team Spinner / Group Splitter Activity.
 *
 * Lets the user enter any number of member names and then randomly
 * shuffles and divides them into two balanced teams.  Accessible from:
 *  • Home screen Tools section
 *  • CreateNewGameActivity "Split my team" helper button
 *
 * Design:
 *  • Entry field + Add button (or press Enter) to add names as chips
 *  • "Spin!" FAB triggers the wheel animation then reveals the two teams
 *  • "Use Teams" button pre-fills group names in CreateNewGameActivity
 */
public class TeamSpinnerActivity extends BaseActivity {

    // ── Views ─────────────────────────────────────────────────────────────────
    private EditText    etMemberName;
    private ChipGroup   chipGroupMembers;
    private LinearLayout layoutGroupA, layoutGroupB;
    private TextView    tvGroupATitle, tvGroupBTitle;
    private TextView    tvSpinInstruction;
    private View        resultCard;
    private MaterialButton btnSpin, btnUseTeams, btnAddMember;
    private ImageView   ivSpinner;

    // ── State ─────────────────────────────────────────────────────────────────
    private final List<String> members = new ArrayList<>();
    private String lastGroupA = "";
    private String lastGroupB = "";

    // ── Constants ─────────────────────────────────────────────────────────────
    private static final int  MIN_MEMBERS        = 2;
    private static final long SPIN_DURATION_MS   = 1800L;
    private static final int  SPIN_ROTATIONS     = 5;

    // ── Result extras (used by CreateNewGameActivity) ─────────────────────────
    public static final String EXTRA_GROUP_A = "spinner_group_a";
    public static final String EXTRA_GROUP_B = "spinner_group_b";
    /** Pass true when launched from CreateNewGameActivity so "Use Teams" is shown */
    public static final String EXTRA_SELECT_MODE = "select_mode";

    private boolean selectMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_spinner);

        selectMode = getIntent().getBooleanExtra(EXTRA_SELECT_MODE, false);

        initViews();
        setupButtons();
        updateSpinButtonState();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Init
    // ─────────────────────────────────────────────────────────────────────────

    private void initViews() {
        etMemberName    = findViewById(R.id.etMemberName);
        chipGroupMembers= findViewById(R.id.chipGroupMembers);
        layoutGroupA    = findViewById(R.id.layoutGroupA);
        layoutGroupB    = findViewById(R.id.layoutGroupB);
        tvGroupATitle   = findViewById(R.id.tvGroupATitle);
        tvGroupBTitle   = findViewById(R.id.tvGroupBTitle);
        tvSpinInstruction = findViewById(R.id.tvSpinInstruction);
        resultCard      = findViewById(R.id.resultCard);
        btnSpin         = findViewById(R.id.btnSpin);
        btnUseTeams     = findViewById(R.id.btnUseTeams);
        btnAddMember    = findViewById(R.id.btnAddMember);
        ivSpinner       = findViewById(R.id.ivSpinner);

        resultCard.setVisibility(View.GONE);
        if (btnUseTeams != null) {
            btnUseTeams.setVisibility(selectMode ? View.VISIBLE : View.GONE);
        }

        ImageView backBtn = findViewById(R.id.back_home_activity_button);
        if (backBtn != null) backBtn.setOnClickListener(v -> finish());
    }

    private void setupButtons() {
        btnAddMember.setOnClickListener(v -> addMember());

        // Support pressing "Done" / Enter on keyboard
        etMemberName.setOnEditorActionListener((v, actionId, event) -> {
            addMember();
            return true;
        });

        btnSpin.setOnClickListener(v -> startSpin());

        if (btnUseTeams != null) {
            btnUseTeams.setOnClickListener(v -> returnTeams());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Member management
    // ─────────────────────────────────────────────────────────────────────────

    private void addMember() {
        String name = etMemberName.getText().toString().trim();
        if (name.isEmpty()) {
            etMemberName.startAnimation(
                    android.view.animation.AnimationUtils.loadAnimation(this, R.anim.shake));
            return;
        }
        if (name.length() > 20) {
            Toast.makeText(this, R.string.error_group_name, Toast.LENGTH_SHORT).show();
            return;
        }
        // Duplicate check
        for (String m : members) {
            if (m.equalsIgnoreCase(name)) {
                Toast.makeText(this, R.string.spinner_member_duplicate, Toast.LENGTH_SHORT).show();
                etMemberName.selectAll();
                return;
            }
        }

        members.add(name);
        addChip(name);
        etMemberName.setText("");
        etMemberName.requestFocus();
        updateSpinButtonState();
    }

    private void addChip(@NonNull String name) {
        Chip chip = new Chip(this);
        chip.setText(name);
        chip.setCloseIconVisible(true);
        chip.setCheckable(false);
        chip.setChipBackgroundColorResource(R.color.primary_button_color);
        chip.setTextColor(getResources().getColor(R.color.primary_color, null));
        chip.setCloseIconTintResource(R.color.primary_color);
        chip.setOnCloseIconClickListener(v -> {
            members.remove(name);
            chipGroupMembers.removeView(chip);
            updateSpinButtonState();
        });
        chipGroupMembers.addView(chip);
    }

    private void updateSpinButtonState() {
        boolean canSpin = members.size() >= MIN_MEMBERS;
        btnSpin.setEnabled(canSpin);
        btnSpin.setAlpha(canSpin ? 1f : 0.5f);

        String instruction = canSpin
                ? getString(R.string.spinner_ready, members.size())
                : getString(R.string.spinner_add_min, MIN_MEMBERS);
        if (tvSpinInstruction != null) tvSpinInstruction.setText(instruction);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Spin animation + team split
    // ─────────────────────────────────────────────────────────────────────────

    private void startSpin() {
        if (members.size() < MIN_MEMBERS) return;

        btnSpin.setEnabled(false);
        resultCard.setVisibility(View.GONE);

        // Spin animation on the wheel icon
        if (ivSpinner != null) {
            ObjectAnimator spin = ObjectAnimator.ofFloat(
                    ivSpinner, View.ROTATION, 0f, 360f * SPIN_ROTATIONS);
            spin.setDuration(SPIN_DURATION_MS);
            spin.setInterpolator(new DecelerateInterpolator());
            spin.addListener(new AnimatorListenerAdapter() {
                @Override public void onAnimationEnd(Animator animation) {
                    splitAndReveal();
                }
            });
            spin.start();
        } else {
            // No wheel icon in layout — just delay then reveal
            new Handler(Looper.getMainLooper()).postDelayed(this::splitAndReveal, 600);
        }
    }

    private void splitAndReveal() {
        // Shuffle a copy so original list order is preserved for re-use
        List<String> shuffled = new ArrayList<>(members);
        Collections.shuffle(shuffled);

        List<String> teamA = new ArrayList<>();
        List<String> teamB = new ArrayList<>();
        for (int i = 0; i < shuffled.size(); i++) {
            if (i % 2 == 0) teamA.add(shuffled.get(i));
            else             teamB.add(shuffled.get(i));
        }

        lastGroupA = teamA.isEmpty() ? "" : teamA.get(0);
        lastGroupB = teamB.isEmpty() ? "" : teamB.get(0);

        // Update group title text (first member or generic)
        if (tvGroupATitle != null) tvGroupATitle.setText(
                getString(R.string.spinner_team_a_title, lastGroupA));
        if (tvGroupBTitle != null) tvGroupBTitle.setText(
                getString(R.string.spinner_team_b_title,
                        teamB.isEmpty() ? getString(R.string.group_b_color) : teamB.get(0)));

        // Populate member rows
        populateTeamLayout(layoutGroupA, teamA);
        populateTeamLayout(layoutGroupB, teamB);

        // Reveal card with fade-in
        resultCard.setAlpha(0f);
        resultCard.setVisibility(View.VISIBLE);
        resultCard.animate().alpha(1f).setDuration(350).start();

        btnSpin.setEnabled(true);
        if (btnUseTeams != null) btnUseTeams.setVisibility(selectMode ? View.VISIBLE : View.GONE);
    }

    private void populateTeamLayout(@NonNull LinearLayout layout,
                                    @NonNull List<String> team) {
        layout.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < team.size(); i++) {
            View row = inflater.inflate(R.layout.item_team_member, layout, false);
            TextView tvNum  = row.findViewById(R.id.tvMemberNumber);
            TextView tvName = row.findViewById(R.id.tvMemberNameResult);
            if (tvNum  != null) tvNum.setText(String.valueOf(i + 1));
            if (tvName != null) tvName.setText(team.get(i));
            layout.addView(row);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Return to CreateNewGameActivity (select mode)
    // ─────────────────────────────────────────────────────────────────────────

    private void returnTeams() {
        Intent result = new Intent();
        result.putExtra(EXTRA_GROUP_A, lastGroupA);
        result.putExtra(EXTRA_GROUP_B, lastGroupB);
        setResult(RESULT_OK, result);
        finish();
    }
}