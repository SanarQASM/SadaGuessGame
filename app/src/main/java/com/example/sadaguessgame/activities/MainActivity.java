package com.example.sadaguessgame.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.example.sadaguessgame.R;
import com.example.sadaguessgame.fragments.HomeFragment;
import com.example.sadaguessgame.fragments.LeaderboardFragment;
import com.example.sadaguessgame.fragments.RecentlyFragment;
import com.example.sadaguessgame.fragments.SettingFragment;
import com.example.sadaguessgame.manager.AdsManager;
import com.example.sadaguessgame.manager.NavigationStateManager;

/**
 * MainActivity (updated for Features 1, 2, 3, 4).
 *
 * Changes vs original:
 *  • onCreate restores the last-saved tab (Feature 3)
 *  • AdsManager.resetSessionCap() so a new game can show an ad (Feature 4)
 *  • Tab index is saved on every selection (Feature 3)
 */
public class MainActivity extends BaseActivity {

    private View       indicator;
    private FrameLayout frHome, frRecently, frLeaderboard, frSetting;
    private ImageView   icHome, icRecently, icLeaderboard, icSetting;
    private ImageView[] allIcons;

    // Maps tab-index → fragment factory
    private static final int TAB_HOME        = 0;
    private static final int TAB_RECENTLY    = 1;
    private static final int TAB_LEADERBOARD = 2;
    private static final int TAB_SETTING     = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupTabs();

        // Feature 3: restore the last tab the user was on
        int lastTab = NavigationStateManager.getInstance(this).getLastTab();
        selectTabByIndex(lastTab);

        // Feature 4: a new session means a new ad impression is allowed
        AdsManager.getInstance(this).resetSessionCap();
    }

    // ─── Init ─────────────────────────────────────────────────────────────────

    private void initViews() {
        indicator     = findViewById(R.id.indicator);
        frHome        = findViewById(R.id.frHomeFragment);
        frRecently    = findViewById(R.id.frRecentlyFragment);
        frLeaderboard = findViewById(R.id.frLeaderboardFragment);
        frSetting     = findViewById(R.id.frSettingFragment);

        icHome        = findViewById(R.id.btnHomeFragment);
        icRecently    = findViewById(R.id.btnRecentlyFragment);
        icLeaderboard = findViewById(R.id.btnLeaderboardFragment);
        icSetting     = findViewById(R.id.btnSettingFragment);

        allIcons = new ImageView[]{icHome, icRecently, icLeaderboard, icSetting};
    }

    // ─── Tab setup ────────────────────────────────────────────────────────────

    private void setupTabs() {
        if (frHome        != null) frHome.setOnClickListener(v        -> onTabClicked(TAB_HOME));
        if (frRecently    != null) frRecently.setOnClickListener(v    -> onTabClicked(TAB_RECENTLY));
        if (frLeaderboard != null) frLeaderboard.setOnClickListener(v -> onTabClicked(TAB_LEADERBOARD));
        if (frSetting     != null) frSetting.setOnClickListener(v     -> onTabClicked(TAB_SETTING));
    }

    private void onTabClicked(int tabIndex) {
        selectTabByIndex(tabIndex);
        // Feature 3: persist selection
        NavigationStateManager.getInstance(this).saveLastTab(tabIndex);
    }

    private void selectTabByIndex(int tabIndex) {
        Fragment fragment;
        ImageView icon;
        View      tabView;

        switch (tabIndex) {
            case TAB_RECENTLY:
                fragment = new RecentlyFragment();
                icon     = icRecently;
                tabView  = frRecently;
                break;
            case TAB_LEADERBOARD:
                fragment = new LeaderboardFragment();
                icon     = icLeaderboard;
                tabView  = frLeaderboard;
                break;
            case TAB_SETTING:
                fragment = new SettingFragment();
                icon     = icSetting;
                tabView  = frSetting;
                break;
            default: // TAB_HOME
                fragment = new HomeFragment();
                icon     = icHome;
                tabView  = frHome;
                break;
        }

        loadFragment(fragment);
        setIconSelected(icon);
        // Indicator animation requires the view to be laid out first
        if (tabView != null) {
            tabView.post(() -> moveIndicator(tabView));
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void setIconSelected(ImageView selected) {
        for (ImageView ic : allIcons) {
            if (ic != null) { ic.setSelected(false); ic.setAlpha(0.7f); }
        }
        if (selected != null) { selected.setSelected(true); selected.setAlpha(1.0f); }
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
        if (target == null || indicator == null) return;
        float x = target.getX() + (target.getWidth() - indicator.getWidth()) / 2f;
        indicator.animate().x(x).setDuration(300).start();
    }
}