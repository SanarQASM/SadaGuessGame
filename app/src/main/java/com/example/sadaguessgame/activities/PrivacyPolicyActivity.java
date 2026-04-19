package com.example.sadaguessgame.activities;

import android.os.Bundle;
import android.widget.ImageView;

import com.example.sadaguessgame.R;


public class PrivacyPolicyActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        // Initialize the back button
        ImageView backButton = findViewById(R.id.back_home_activity_button);

        // Set Click Listener to go back to the previous activity
        backButton.setOnClickListener(v -> finish());
    }

}
