package com.example.sadaguessgame.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.sadaguessgame.R;
import com.example.sadaguessgame.activities.CardsActivity;
import com.example.sadaguessgame.activities.CreateNewGameActivity;
import com.example.sadaguessgame.activities.DiceActivity;
import com.example.sadaguessgame.activities.LearnGameActivity;
import com.example.sadaguessgame.activities.ScoreBoardActivity;
import com.example.sadaguessgame.activities.TeamSpinnerActivity;
import com.example.sadaguessgame.activities.TimerActivity;
import com.example.sadaguessgame.data.GameState;
import com.example.sadaguessgame.data.ScoreStorage;
import com.example.sadaguessgame.manager.LiveUserCountManager;
import com.google.android.material.button.MaterialButton;

/**
 * HomeFragment (updated for Features 1 & 2).
 *
 * Changes vs original:
 *  • Spinner tile added in the Tools section (Feature 1)
 *  • Live user count shown below the app title (Feature 2)
 */
public class HomeFragment extends BaseFragment {

    private LinearLayout  continueGameContainer;
    private TextSwitcher  textSwitcher;
    private TextView      tvLiveCount;
    private String[]      texts;

    private static final int  SWITCHER_DELAY_MS = 3000;
    private int      textIndex   = 0;
    private final Handler  handler     = new Handler(Looper.getMainLooper());
    private Runnable textRunnable;
    private boolean  hasPreviousGame;

    // Feature 2
    private LiveUserCountManager liveCountManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);

        LinearLayout newGameContainer  = view.findViewById(R.id.NewGameContainer);
        continueGameContainer          = view.findViewById(R.id.ContinueGameContainer);
        LinearLayout timerContainer    = view.findViewById(R.id.Timer);
        LinearLayout diceContainer     = view.findViewById(R.id.DiceContiner);
        LinearLayout scoreContainer    = view.findViewById(R.id.scoreBoardContiner);
        LinearLayout spinnerContainer  = view.findViewById(R.id.SpinnerContiner);   // Feature 1
        MaterialButton learnPlay       = view.findViewById(R.id.learnPlay);
        textSwitcher                   = view.findViewById(R.id.home_text_switcher);
        tvLiveCount                    = view.findViewById(R.id.tvHomeLiveCount);   // Feature 2
        texts = getResources().getStringArray(R.array.home_texts);

        setupTextSwitcher();
        refreshContinueButton();
        setupLiveCount(); // Feature 2

        if (newGameContainer != null) {
            newGameContainer.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), CreateNewGameActivity.class)));
        }

        if (continueGameContainer != null) {
            continueGameContainer.setOnClickListener(v -> {
                if (hasPreviousGame) {
                    startActivity(new Intent(requireContext(), CardsActivity.class));
                    requireActivity().overridePendingTransition(
                            R.anim.slide_in_right, R.anim.slide_out_left);
                }
            });
        }

        if (learnPlay != null) {
            learnPlay.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), LearnGameActivity.class)));
        }

        if (timerContainer != null) {
            timerContainer.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), TimerActivity.class)));
        }

        if (diceContainer != null) {
            diceContainer.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), DiceActivity.class)));
        }

        if (scoreContainer != null) {
            scoreContainer.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), ScoreBoardActivity.class)));
        }

        // Feature 1: Team Spinner tile
        if (spinnerContainer != null) {
            spinnerContainer.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), TeamSpinnerActivity.class)));
        }

        return view;
    }

    // ─── Feature 2: Live user count ───────────────────────────────────────────

    private void setupLiveCount() {
        liveCountManager = LiveUserCountManager.getInstance(requireContext());
        if (tvLiveCount != null) {
            tvLiveCount.setText(
                    getString(R.string.live_users_format, liveCountManager.getCurrentCount()));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshContinueButton();
        if (liveCountManager != null) {
            liveCountManager.start(count -> {
                if (tvLiveCount != null && isAdded()) {
                    tvLiveCount.setText(getString(R.string.live_users_format, count));
                }
            });
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (liveCountManager != null) liveCountManager.stop();
    }

    // ─── Text switcher ────────────────────────────────────────────────────────

    private void setupTextSwitcher() {
        if (textSwitcher == null || !isAdded()) return;

        textSwitcher.setFactory(() -> {
            TextView tv = new TextView(requireContext());
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimension(R.dimen.secondary_size));
            tv.setTextColor(ContextCompat.getColor(requireContext(),
                    R.color.primary_button_color));
            try {
                tv.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.secondary_font));
            } catch (Exception ignored) {}
            tv.setGravity(Gravity.START);
            tv.setPaintFlags(tv.getPaintFlags()
                    | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
            return tv;
        });

        textSwitcher.setInAnimation(requireContext(), android.R.anim.slide_in_left);
        textSwitcher.setOutAnimation(requireContext(), android.R.anim.slide_out_right);

        textRunnable = new Runnable() {
            @Override public void run() {
                if (textSwitcher == null || !isAdded()) return;
                textSwitcher.setText(texts[textIndex]);
                textIndex = (textIndex + 1) % texts.length;
                handler.postDelayed(this, SWITCHER_DELAY_MS);
            }
        };
        handler.post(textRunnable);
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        if (textRunnable != null) {
            handler.removeCallbacks(textRunnable);
            textRunnable = null;
        }
        textSwitcher = null;
        tvLiveCount  = null;
    }

    private void refreshContinueButton() {
        if (continueGameContainer == null || !isAdded()) return;
        GameState unfinished =
                ScoreStorage.getInstance(requireContext()).getLastUnfinishedGame();
        hasPreviousGame = unfinished != null;
        setContinueButtonEnabled(hasPreviousGame);
    }

    private void setContinueButtonEnabled(boolean enabled) {
        continueGameContainer.setEnabled(enabled);
        continueGameContainer.setClickable(enabled);
        continueGameContainer.setAlpha(enabled ? 1f : 0.5f);
    }
}