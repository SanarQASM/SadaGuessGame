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
 *  3. Add your real IDs to strings.xml (DO NOT hardcode them here):
 *       <string name="admob_app_id">ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX</string>
 *       <string name="ad_banner_home">ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX</string>
 *       <string name="ad_interstitial_game_end">ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX</string>
 *
 *  4. Un-comment the ADMOB_ENABLED flag below, and un-comment the import
 *     blocks + implementation bodies.
 *
 *  Until you complete the above steps the manager compiles and runs in
 *  "ads-disabled" mode (all public methods are no-ops).
 *
 *  PLACEMENT STRATEGY (following Google best practices):
 *  • Banner  → Home fragment, bottom of screen
 *  • Interstitial → After game ends (WinnerActivity / DrawActivity)
 *  • Show at most ONE interstitial per game session
 * ──────────────────────────────────────────────────────────────────────────────
 */

// ── Toggle this to true once AdMob SDK is added ───────────────────────────────
// private static final boolean ADMOB_ENABLED = true;
// ─────────────────────────────────────────────────────────────────────────────

// UNCOMMENT these imports when ADMOB_ENABLED = true:
// import com.google.android.gms.ads.AdListener;
// import com.google.android.gms.ads.AdRequest;
// import com.google.android.gms.ads.AdSize;
// import com.google.android.gms.ads.AdView;
// import com.google.android.gms.ads.FullScreenContentCallback;
// import com.google.android.gms.ads.LoadAdError;
// import com.google.android.gms.ads.MobileAds;
// import com.google.android.gms.ads.interstitial.InterstitialAd;
// import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import com.example.sadaguessgame.R;

/**
 * Centralised wrapper for all AdMob ad placements.
 *
 * All public methods are safe to call regardless of whether ads are enabled.
 * The manager handles null-safety, load retries and per-session frequency caps.
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
    private boolean       initialized    = false;
    private boolean       interstitialShownThisSession = false;

    // When AdMob is enabled, store the loaded ads here:
    // private AdView       bannerAdView;
    // private InterstitialAd interstitialAd;

    private AdsManager(@NonNull Context ctx) {
        this.appContext = ctx;
    }

    // ─── Initialisation ───────────────────────────────────────────────────────

    /**
     * Initialise the AdMob SDK.  Call once from MyApplication.onCreate().
     *
     * Replace the body with the real init once the SDK is added:
     *   MobileAds.initialize(appContext, initializationStatus -> {
     *       initialized = true;
     *       preloadInterstitial();
     *   });
     */
    public void initialize() {
        // STUB — replace with MobileAds.initialize(…) when SDK is present
        initialized = false;
        Log.d(TAG, "AdsManager: ads disabled (SDK not yet added). "
                + "Follow the setup instructions in AdsManager.java.");
    }

    // ─── Banner ads ───────────────────────────────────────────────────────────

    /**
     * Loads and attaches a banner ad to {@code container}.
     *
     * When ads are enabled replace the body with:
     *   AdView adView = new AdView(activity);
     *   adView.setAdSize(AdSize.BANNER);
     *   adView.setAdUnitId(activity.getString(R.string.ad_banner_home));
     *   container.addView(adView);
     *   adView.loadAd(new AdRequest.Builder().build());
     *
     * @param activity  The host Activity.
     * @param container The ViewGroup to inject the banner into.
     */
    public void showBanner(@NonNull Activity activity, @NonNull ViewGroup container) {
        if (!initialized) {
            Log.d(TAG, "showBanner: ads not initialised, skipping.");
            return;
        }
        // TODO: un-comment AdView code above when SDK is added
    }

    /** Hides and destroys a previously attached banner to free resources. */
    public void destroyBanner() {
        // if (bannerAdView != null) { bannerAdView.destroy(); bannerAdView = null; }
    }

    // ─── Interstitial ads ─────────────────────────────────────────────────────

    /**
     * Pre-loads an interstitial ad so it's ready when needed.
     * Call after initialization and after each display.
     *
     * When enabled replace body with:
     *   InterstitialAd.load(appContext,
     *       appContext.getString(R.string.ad_interstitial_game_end),
     *       new AdRequest.Builder().build(),
     *       new InterstitialAdLoadCallback() {
     *           @Override public void onAdLoaded(@NonNull InterstitialAd ad) {
     *               interstitialAd = ad;
     *           }
     *           @Override public void onAdFailedToLoad(@NonNull LoadAdError e) {
     *               interstitialAd = null;
     *           }
     *       });
     */
    public void preloadInterstitial() {
        if (!initialized) return;
        // TODO: un-comment load code above
    }

    /**
     * Shows the interstitial if loaded and not yet shown this session.
     * Call from WinnerActivity / DrawActivity after game ends.
     *
     * When enabled replace body with:
     *   if (!initialized || interstitialShownThisSession || interstitialAd == null) return;
     *   interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
     *       @Override public void onAdDismissedFullScreenContent() {
     *           interstitialAd = null;
     *           preloadInterstitial();
     *       }
     *   });
     *   interstitialAd.show(activity);
     *   interstitialShownThisSession = true;
     *   if (callback != null) callback.onAdShown();
     *
     * @param activity The host Activity.
     * @param callback Optional callback fired after the ad is dismissed.
     */
    public void showInterstitialIfReady(@NonNull Activity activity,
                                        @Nullable Runnable callback) {
        if (!initialized) {
            // Ads not enabled — just fire the callback immediately
            if (callback != null) callback.run();
            return;
        }
        // TODO: un-comment show code above; for now just execute callback
        if (callback != null) callback.run();
    }

    /** Resets the per-session flag so a new game can show one more interstitial. */
    public void resetSessionCap() {
        interstitialShownThisSession = false;
    }
}