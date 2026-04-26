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

import com.example.sadaguessgame.activities.CardsActivity;
import com.example.sadaguessgame.activities.CreateNewGameActivity;
import com.example.sadaguessgame.activities.DiceActivity;
import com.example.sadaguessgame.R;
import com.example.sadaguessgame.activities.ScoreBoardActivity;
import com.example.sadaguessgame.activities.TimerActivity;
import com.example.sadaguessgame.data.GameState;
import com.example.sadaguessgame.data.ScoreStorage;
import com.example.sadaguessgame.ui.NavigationActivity;
import com.google.android.material.button.MaterialButton;

public class HomeFragment extends BaseFragment {

    private LinearLayout continueGameContainer;
    private TextSwitcher textSwitcher;
    private String[] texts;
    private final int delay = 3000;
    private int index = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable textRunnable;
    private GameState unfinishedGame;
    private boolean hasPreviousGame;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);

        LinearLayout newGameContainer = view.findViewById(R.id.NewGameContainer);
        continueGameContainer = view.findViewById(R.id.ContinueGameContainer);
        LinearLayout timerContainer = view.findViewById(R.id.Timer);
        LinearLayout diceContainer = view.findViewById(R.id.DiceContiner);
        LinearLayout scoreContainer = view.findViewById(R.id.scoreBoardContiner);
        MaterialButton learnPlay = view.findViewById(R.id.learnPlay);
        textSwitcher = view.findViewById(R.id.home_text_switcher);
        texts = getResources().getStringArray(R.array.home_texts);

        setupTextSwitcher();

        unfinishedGame = ScoreStorage.getInstance(getActivity()).getLastUnfinishedGame();
        hasPreviousGame = unfinishedGame != null;
        enablePreviousGameButton(hasPreviousGame);

        newGameContainer.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CreateNewGameActivity.class)));

        continueGameContainer.setOnClickListener(v -> {
            if (hasPreviousGame) {
                startActivity(new Intent(requireContext(), CardsActivity.class));
                requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        learnPlay.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), NavigationActivity.class)));

        timerContainer.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), TimerActivity.class)));

        diceContainer.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), DiceActivity.class)));

        scoreContainer.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ScoreBoardActivity.class)));

        return view;
    }

    private void setupTextSwitcher() {
        textSwitcher.setFactory(() -> {
            TextView textView = new TextView(requireContext());
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimension(R.dimen.secondary_size));
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_button_color));
            textView.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.secondary_font));
            textView.setGravity(Gravity.START);
            textView.setPaintFlags(textView.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
            return textView;
        });

        textSwitcher.setInAnimation(requireContext(), android.R.anim.slide_in_left);
        textSwitcher.setOutAnimation(requireContext(), android.R.anim.slide_out_right);

        textRunnable = new Runnable() {
            @Override
            public void run() {
                if (textSwitcher == null) return;
                textSwitcher.setText(texts[index]);
                index = (index + 1) % texts.length;
                handler.postDelayed(this, delay);
            }
        };
        handler.post(textRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        unfinishedGame = ScoreStorage.getInstance(requireContext()).getLastUnfinishedGame();
        hasPreviousGame = unfinishedGame != null;
        enablePreviousGameButton(hasPreviousGame);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Stop the text switcher runnable to prevent memory leaks
        if (textRunnable != null) {
            handler.removeCallbacks(textRunnable);
        }
    }

    private void enablePreviousGameButton(boolean hasPreviousGame) {
        if (hasPreviousGame) {
            continueGameContainer.setEnabled(true);
            continueGameContainer.setClickable(true);
            continueGameContainer.setAlpha(1f);
        } else {
            continueGameContainer.setEnabled(false);
            continueGameContainer.setClickable(false);
            continueGameContainer.setAlpha(0.5f);
        }
    }
}