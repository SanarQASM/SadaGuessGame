package com.example.sadaguessgame.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.fragment.app.Fragment;

import com.example.sadaguessgame.fragments.HomeFragment;
import com.example.sadaguessgame.fragments.RecentlyFragment;
import com.example.sadaguessgame.fragments.SettingFragment;
import com.example.sadaguessgame.R;
import com.example.sadaguessgame.data.GameState;
import com.example.sadaguessgame.data.ScoreStorage;

import java.util.List;

public class MainActivity extends BaseActivity {

    private View indicator;
    private FrameLayout frHome, frRecently, frSetting;
    private ImageView icHome, icRecently, icSetting;
    private ImageView[] allIcons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupTabs();
        findViewById(android.R.id.content).post(this::setDefaultTab);
    }

    private void initViews() {
        indicator = findViewById(R.id.indicator);

        frHome = findViewById(R.id.frHomeFragment);
        frRecently = findViewById(R.id.frRecentlyFragment);
        frSetting = findViewById(R.id.frSettingFragment);

        icHome = findViewById(R.id.btnHomeFragment);
        icRecently = findViewById(R.id.btnRecentlyFragment);
        icSetting = findViewById(R.id.btnSettingFragment);

        allIcons = new ImageView[]{icHome, icRecently, icSetting};
    }

    private void setupTabs() {
        if (frHome != null) {
            frHome.setOnClickListener(v -> selectTab(v, icHome, new HomeFragment()));
        }

        if (frRecently != null) {
            frRecently.setOnClickListener(v -> {
                // Show recently fragment if there are any games (finished OR unfinished)
                ScoreStorage storage = ScoreStorage.getInstance(this);
                List<GameState> games = storage.getAllGames();
                GameState current = storage.getCurrentGame();
                boolean hasAnyGame = (games != null && !games.isEmpty())
                        || (current != null && !current.isFinished);

                if (hasAnyGame) {
                    selectTab(v, icRecently, new RecentlyFragment());
                } else {
                    // Show empty recently state
                    selectTab(v, icRecently, new RecentlyFragment());
                }
            });
        }

        if (frSetting != null) {
            frSetting.setOnClickListener(v -> selectTab(v, icSetting, new SettingFragment()));
        }
    }

    private void setDefaultTab() {
        loadFragment(new HomeFragment());
        setIconSelected(icHome);
        moveIndicator(frHome);
    }

    private void selectTab(View tabView, ImageView icon, Fragment fragment) {
        loadFragment(fragment);
        setIconSelected(icon);
        moveIndicator(tabView);
    }

    private void setIconSelected(ImageView selectedIcon) {
        for (ImageView icon : allIcons) {
            if (icon != null) {
                icon.setSelected(false);
                icon.setAlpha(0.7f);
            }
        }
        if (selectedIcon != null) {
            selectedIcon.setSelected(true);
            selectedIcon.setAlpha(1.0f);
        }
    }

    private void loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
        }
    }

    private void moveIndicator(View target) {
        if (target != null && indicator != null) {
            float x = target.getX() + (target.getWidth() - indicator.getWidth()) / 2f;
            indicator.animate()
                    .x(x)
                    .setDuration(300)
                    .start();
        }
    }
}