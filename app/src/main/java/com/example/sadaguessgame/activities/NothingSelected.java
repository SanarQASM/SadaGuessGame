package com.example.sadaguessgame.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sadaguessgame.R;
import com.google.android.material.button.MaterialButton;

public class NothingSelected extends Fragment {

    public NothingSelected() {}

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.cards_nothing_fragment,
                container,
                false
        );

        MaterialButton btnNewGame = view.findViewById(R.id.btnNewGame);

        btnNewGame.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CreateNewGameActivity.class);
            startActivity(intent);
        });

        return view;
    }
}
