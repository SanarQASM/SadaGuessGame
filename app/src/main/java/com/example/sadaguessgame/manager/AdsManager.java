package com.example.sadaguessgame.manager;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/*
 * ──────────────────────────────────────────────────────────────────────────────
 *  AdsManager — Feature 4: Centralised AdMob integration
 * ──────────────────────────────────────────────────────────────────────────────
 *
 *  HOW TO ENABLE ADS IN YOUR PROJECT
 *  ──────────────────────────────────
 *  1. Add the dependency in app/build.gradle:
 *       implementation 'com.google.android.gms:play-services-ads:23.1.0'
 *
 *  2. Add your AdMob App ID in AndroidManifest.xml inside <application>:
 *       <meta-data
 *           android:name="com.google.android.gms.ads.APPLICATION_ID"
 *           android:value="@string/admob_app_id"/>
 *
 *  3. Add your real IDs to strings.xml:
 *       <string name="admob_app_id">ca-app-pub-5078144066640392~6274693528</string>
 *       <string name="ad_banner_home">ca-app-pub-5078144066640392/5807789662</string>
 *       <string name="ad_interstitial_game_end">ca-app-pub-5078144066640392/1673103539</string>
 *
 *  4. In your MyApplication.java, call once:
 *       AdsManager.getInstance(this).initialize();
 *
 *  PLACEMENT STRATEGY:
 *  • Banner       → Home fragment, bottom of screen
 *  • Interstitial → After game ends (WinnerActivity / DrawActivity)
 *  • Show at most ONE interstitial per game session
 * ──────────────────────────────────────────────────────────────────────────────
 */

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import com.example.sadaguessgame.R;

/**
 * Centralised wrapper for all AdMob ad placements.
 * Handles: Banner, Interstitial
 *
 * All public methods are safe to call — null-safe and session-capped.
 */
public class AdsManager {

    private static final String TAG = "AdsManager";

    // ─── Singleton ────────────────────────────────────────────────────────────
    private static volatile AdsManager instance;

    public static AdsManager getInstance(@NonNull Context ctx) {
        if (instance == null) {
            synchronized (AdsManager.class) {
                if (instance == null)
                    instance = new AdsManager(ctx.getApplicationContext());
            }
        }
        return instance;
    }

    // ─── State ────────────────────────────────────────────────────────────────
    private final Context appContext;
    private boolean       initialized                  = false;
    private boolean       interstitialShownThisSession = false;

    // Ad objects
    private AdView         bannerAdView;
    private InterstitialAd interstitialAd;

    private AdsManager(@NonNull Context ctx) {
        this.appContext = ctx;
    }

    // ─── Initialisation ───────────────────────────────────────────────────────

    /**
     * Initialise the AdMob SDK.
     * Call once from MyApplication.onCreate():
     *
     *   AdsManager.getInstance(this).initialize();
     */
    public void initialize() {

        // Block inappropriate ad categories
        MobileAds.setRequestConfiguration(
                new RequestConfiguration.Builder()
                        .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
                        .build()
        );

        MobileAds.initialize(appContext, initializationStatus -> {
            initialized = true;
            Log.d(TAG, "AdMob SDK initialised.");
            preloadInterstitial();
        });
    }

    // ─── Banner ads ───────────────────────────────────────────────────────────

    /**
     * Loads and attaches a banner ad to the given container.
     * Call from your HomeFragment / MainActivity after the layout is ready.
     *
     * @param activity  The host Activity.
     * @param container The ViewGroup (e.g. a FrameLayout at the bottom of screen).
     */
    public void showBanner(@NonNull Activity activity, @NonNull ViewGroup container) {
        if (!initialized) {
            Log.d(TAG, "showBanner: SDK not initialised yet, skipping.");
            return;
        }

        bannerAdView = new AdView(activity);
        bannerAdView.setAdSize(AdSize.BANNER);
        bannerAdView.setAdUnitId(activity.getString(R.string.ad_banner_home));
        bannerAdView.setAdListener(new AdListener() {
            @Override public void onAdFailedToLoad(@NonNull LoadAdError error) {
                Log.w(TAG, "Banner failed to load: " + error.getMessage());
            }
        });
        container.removeAllViews();
        container.addView(bannerAdView);
        bannerAdView.loadAd(new AdRequest.Builder().build());
        Log.d(TAG, "Banner ad loading...");
    }

    /** Call from onDestroy() of the host Activity/Fragment to free memory. */
    public void destroyBanner() {
        if (bannerAdView != null) {
            bannerAdView.destroy();
            bannerAdView = null;
            Log.d(TAG, "Banner destroyed.");
        }
    }

    // ─── Interstitial ads ─────────────────────────────────────────────────────

    /**
     * Pre-loads an interstitial ad in the background.
     * Called automatically after init and after each display.
     */
    public void preloadInterstitial() {
        if (!initialized) return;

        InterstitialAd.load(
                appContext,
                appContext.getString(R.string.ad_interstitial_game_end),
                new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback() {
                    @Override public void onAdLoaded(@NonNull InterstitialAd ad) {
                        interstitialAd = ad;
                        Log.d(TAG, "Interstitial loaded.");
                    }
                    @Override public void onAdFailedToLoad(@NonNull LoadAdError error) {
                        interstitialAd = null;
                        Log.w(TAG, "Interstitial failed: " + error.getMessage());
                    }
                }
        );
    }

    /**
     * Shows the interstitial if ready. Shows at most once per game session.
     * Call from WinnerActivity / DrawActivity after the game ends.
     *
     * @param activity The host Activity.
     * @param callback Runs after ad is dismissed (or immediately if not ready).
     */
    public void showInterstitialIfReady(@NonNull Activity activity,
                                        @Nullable Runnable callback) {
        if (!initialized || interstitialShownThisSession || interstitialAd == null) {
            Log.d(TAG, "Interstitial skipped (not ready or already shown).");
            if (callback != null) callback.run();
            return;
        }

        interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override public void onAdDismissedFullScreenContent() {
                interstitialAd = null;
                preloadInterstitial();
                if (callback != null) callback.run();
            }
            @Override public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError error) {
                interstitialAd = null;
                if (callback != null) callback.run();
            }
        });

        interstitialAd.show(activity);
        interstitialShownThisSession = true;
        Log.d(TAG, "Interstitial shown.");
    }

    /** Call at the start of each new game to allow one more interstitial. */
    public void resetSessionCap() {
        interstitialShownThisSession = false;
    }
}