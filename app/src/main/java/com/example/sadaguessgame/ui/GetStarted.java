package com.example.sadaguessgame.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.sadaguessgame.activities.BaseActivity;
import com.example.sadaguessgame.R;

public class GetStarted extends BaseActivity {
    Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started);

        startButton = findViewById(R.id.getStarted);

        startButton.setOnClickListener(view -> {
            Intent i = new Intent(GetStarted.this, NavigationActivity.class);
            startActivity(i);
            finish();
        });
    }
}