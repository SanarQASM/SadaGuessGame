package com.example.sadaguessgame.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sadaguessgame.R;
import com.example.sadaguessgame.ui.NavigationActivity;
import com.example.sadaguessgame.ui.OnboardingPrefs;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends BaseActivity {

    private static final int SPLASH_TIME = 2500;

    Animation image_anim, text_anim;
    ImageView logo;
    TextView app_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        logo = findViewById(R.id.logo);
        app_name = findViewById(R.id.app_name);


        image_anim = AnimationUtils.loadAnimation(this, R.anim.image_anim);
        text_anim = AnimationUtils.loadAnimation(this, R.anim.text_anim);

        logo.setAnimation(image_anim);
        app_name.setAnimation(text_anim);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            Intent intent;
            if (OnboardingPrefs.isOnboardingDone(this)) {
                // Already seen onboarding
                intent = new Intent(this, MainActivity.class);
            } else {
                // First time only
                intent = new Intent(this, NavigationActivity.class);
            }

            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();

        }, SPLASH_TIME);
    }
}