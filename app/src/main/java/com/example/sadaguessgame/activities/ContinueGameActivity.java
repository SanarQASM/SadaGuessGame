package com.example.sadaguessgame.activities;

import android.content.Intent;
import android.os.Bundle;

import com.example.sadaguessgame.R;

public class ContinueGameActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(ContinueGameActivity.this, CardsActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

}