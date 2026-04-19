package com.example.sadaguessgame.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

import com.example.sadaguessgame.activities.ContinueGameActivity;
import com.example.sadaguessgame.activities.CreateNewGameActivity;
import com.example.sadaguessgame.activities.DiceActivity;
import com.example.sadaguessgame.R;
import com.example.sadaguessgame.activities.ScoreBoardActivity;
import com.example.sadaguessgame.activities.TimerActivity;
import com.example.sadaguessgame.data.GameState;
import com.example.sadaguessgame.data.ScoreStorage;
import com.example.sadaguessgame.ui.NavigationActivity;
import com.google.android.material.button.MaterialButton;

// CHANGE THIS LINE: extends BaseFragment instead of Fragment
public class HomeFragment extends BaseFragment {

    private LinearLayout continueGameContainer;
    private TextSwitcher textSwitcher;
    private String[] texts;
    private final int delay = 3000;
    private int index = 0;
    private final Handler handler = new Handler();
    private GameState unfinishedGame;

    private boolean hasPreviousGame;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);

        // ------------------- INITIALIZE VIEWS -------------------
        LinearLayout newGameContainer = view.findViewById(R.id.NewGameContainer);
        continueGameContainer = view.findViewById(R.id.ContinueGameContainer);
        LinearLayout timerContainer = view.findViewById(R.id.Timer);
        LinearLayout diceContainer = view.findViewById(R.id.DiceContiner);
        LinearLayout scoreContainer = view.findViewById(R.id.scoreBoardContiner);
        MaterialButton learnPlay = view.findViewById(R.id.learnPlay);
        textSwitcher = view.findViewById(R.id.home_text_switcher);
        texts = getResources().getStringArray(R.array.home_texts);

        // ------------------- TEXT SWITCHER -------------------

        textSwitcher.setFactory(() -> {
            TextView textView = new TextView(requireContext());
            textView.setTextSize(26);
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_button_color));
            textView.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.secondary_font));
            textView.setGravity(Gravity.START);
            textView.setPaintFlags(textView.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
            return textView;
        });

        textSwitcher.setInAnimation(requireContext(), android.R.anim.slide_in_left);
        textSwitcher.setOutAnimation(requireContext(), android.R.anim.slide_out_right);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                textSwitcher.setText(texts[index]);
                index = (index + 1) % texts.length;
                handler.postDelayed(this, delay);
            }
        };
        handler.post(runnable);

        unfinishedGame = ScoreStorage.getInstance(getActivity()).getLastUnfinishedGame();
        hasPreviousGame = unfinishedGame != null;
        enablePreviousGameButton(hasPreviousGame);

        // ------------------- SET CLICK LISTENERS -------------------
        newGameContainer.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CreateNewGameActivity.class);
            startActivity(intent);
        });

        continueGameContainer.setOnClickListener(v -> {
            if (hasPreviousGame) {
                Intent intent = new Intent(requireContext(), ContinueGameActivity.class);
                startActivity(intent);
            }
        });

        learnPlay.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), NavigationActivity.class);
            startActivity(intent);
        });

        timerContainer.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), TimerActivity.class);
            startActivity(intent);
        });

        diceContainer.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), DiceActivity.class);
            startActivity(intent);
        });

        scoreContainer.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ScoreBoardActivity.class);
            startActivity(intent);
        });

        return view;
    }

    // Optional: dynamically update continue button state
    @Override
    public void onResume() {
        super.onResume();
        hasPreviousGame = unfinishedGame != null;
        enablePreviousGameButton(hasPreviousGame);
    }

    private void enablePreviousGameButton(boolean hasPreviousGame) {
        if (hasPreviousGame) {
            continueGameContainer.setEnabled(true);
            continueGameContainer.setClickable(true);
            continueGameContainer.setAlpha(1);
        }
        else{
            continueGameContainer.setEnabled(false);
            continueGameContainer.setClickable(false);
            continueGameContainer.setAlpha(0.5F);
        }
    }
}