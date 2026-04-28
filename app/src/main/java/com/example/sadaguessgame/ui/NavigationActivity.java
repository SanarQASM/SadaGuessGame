package com.example.sadaguessgame.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.example.sadaguessgame.activities.BaseActivity;
import com.example.sadaguessgame.activities.MainActivity;
import com.example.sadaguessgame.R;

public class NavigationActivity extends BaseActivity {

    private ViewPager slideViewPager;
    private LinearLayout dotIndicator;
    private Button backButton;
    private Button nextButton;
    private ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        backButton = findViewById(R.id.backButton);
        nextButton = findViewById(R.id.nextButton);
        Button skipButton = findViewById(R.id.skipButton);
        slideViewPager = findViewById(R.id.slideViewPager);
        dotIndicator = findViewById(R.id.dotIndicator);

        slideViewPager.setContentDescription(getString(R.string.onboarding_viewpager_desc));

        viewPagerAdapter = new ViewPagerAdapter(this);
        slideViewPager.setAdapter(viewPagerAdapter);

        setDotIndicator(0);

        slideViewPager.addOnPageChangeListener(viewPagerListener);

        backButton.setOnClickListener(v -> {
            if (getItem(-1) >= 0) {
                slideViewPager.setCurrentItem(getItem(-1), true);
            }
        });

        nextButton.setOnClickListener(v -> {
            int current = getItem(0);
            if (current < viewPagerAdapter.getCount() - 1) {
                slideViewPager.setCurrentItem(getItem(1), true);
            } else {
                finishOnboarding();
            }
        });

        skipButton.setOnClickListener(v -> finishOnboarding());

        animatePage(0);
    }

    private void finishOnboarding() {
        OnboardingPrefs.setOnboardingDone(this);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private int getItem(int offset) {
        return slideViewPager.getCurrentItem() + offset;
    }

    private void animatePage(int position) {
        // Tag is an internal view lookup key — not user-visible, stays as-is
        View currentPage = slideViewPager.findViewWithTag("page_" + position);
        if (currentPage != null) {
            currentPage.startAnimation(
                    AnimationUtils.loadAnimation(this, R.anim.page_anim));
        }
    }

    private final ViewPager.OnPageChangeListener viewPagerListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

        @Override
        public void onPageSelected(int position) {
            setDotIndicator(position);
            backButton.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);

            if (position == viewPagerAdapter.getCount() - 1) {
                nextButton.setText(R.string.txt_finish);
            } else {
                nextButton.setText(R.string.txt_next);
            }

            animatePage(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {}
    };

    public void setDotIndicator(int position) {
        int count = viewPagerAdapter.getCount();
        TextView[] dots = new TextView[count];
        dotIndicator.removeAllViews();

        for (int i = 0; i < count; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;", Html.FROM_HTML_MODE_LEGACY));
            dots[i].setTextSize(35);
            dots[i].setTextColor(ContextCompat.getColor(this, R.color.primary_text));
            dotIndicator.addView(dots[i]);
        }

        if (dots.length > 0) {
            dots[position].setTextColor(ContextCompat.getColor(this, R.color.primary_button_color));
        }
    }
}