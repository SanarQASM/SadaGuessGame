package com.example.sadaguessgame.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.fragment.app.Fragment;
import com.example.sadaguessgame.R;
import com.example.sadaguessgame.data.GameState;
import com.example.sadaguessgame.data.ScoreStorage;
import com.example.sadaguessgame.fragments.HomeFragment;
import com.example.sadaguessgame.fragments.LeaderboardFragment;
import com.example.sadaguessgame.fragments.RecentlyFragment;
import com.example.sadaguessgame.fragments.SettingFragment;
import java.util.List;

/**
 * Host activity for all bottom-nav fragments.
 *
 * v2 changes:
 *  • Added 4th tab: Leaderboard (Hall of Fame).
 *  • Indicator animation updated for 4 tabs.
 */
public class MainActivity extends BaseActivity {

    private View       indicator;
    private FrameLayout frHome, frRecently, frLeaderboard, frSetting;
    private ImageView   icHome, icRecently, icLeaderboard, icSetting;
    private ImageView[] allIcons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupTabs();
        findViewById(android.R.id.content).post(this::setDefaultTab);
    }

    // ─── Init ────────────────────────────────────────────────────────────────

    private void initViews() {
        indicator      = findViewById(R.id.indicator);

        frHome         = findViewById(R.id.frHomeFragment);
        frRecently     = findViewById(R.id.frRecentlyFragment);
        frLeaderboard  = findViewById(R.id.frLeaderboardFragment);
        frSetting      = findViewById(R.id.frSettingFragment);

        icHome         = findViewById(R.id.btnHomeFragment);
        icRecently     = findViewById(R.id.btnRecentlyFragment);
        icLeaderboard  = findViewById(R.id.btnLeaderboardFragment);
        icSetting      = findViewById(R.id.btnSettingFragment);

        allIcons = new ImageView[]{icHome, icRecently, icLeaderboard, icSetting};
    }

    // ─── Tab setup ───────────────────────────────────────────────────────────

    private void setupTabs() {
        if (frHome != null)
            frHome.setOnClickListener(v ->
                    selectTab(v, icHome, new HomeFragment()));

        if (frRecently != null)
            frRecently.setOnClickListener(v ->
                    selectTab(v, icRecently, new RecentlyFragment()));

        if (frLeaderboard != null)
            frLeaderboard.setOnClickListener(v ->
                    selectTab(v, icLeaderboard, new LeaderboardFragment()));

        if (frSetting != null)
            frSetting.setOnClickListener(v ->
                    selectTab(v, icSetting, new SettingFragment()));
    }

    private void setDefaultTab() {
        loadFragment(new HomeFragment());
        setIconSelected(icHome);
        moveIndicator(frHome);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void selectTab(View tabView, ImageView icon, Fragment fragment) {
        loadFragment(fragment);
        setIconSelected(icon);
        moveIndicator(tabView);
    }

    private void setIconSelected(ImageView selected) {
        for (ImageView ic : allIcons) {
            if (ic != null) { ic.setSelected(false); ic.setAlpha(0.7f); }
        }
        if (selected != null) { selected.setSelected(true); selected.setAlpha(1.0f); }
    }

    private void loadFragment(Fragment fragment) {
        if (fragment != null)
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
    }

    private void moveIndicator(View target) {
        if (target == null || indicator == null) return;
        float x = target.getX() + (target.getWidth() - indicator.getWidth()) / 2f;
        indicator.animate().x(x).setDuration(300).start();
    }
}