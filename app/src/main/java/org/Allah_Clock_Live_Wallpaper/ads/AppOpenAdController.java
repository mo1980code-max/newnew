package org.Allah_Clock_Live_Wallpaper.ads;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;

/**
 * App-open ads, implemented strictly inside Google's policy envelope:
 *
 * <ul>
 *   <li>Shown <b>only</b> when the user brings the app back to the foreground (or cold-starts
 *       it) and <b>only</b> on the home screen — never on top of a task such as applying a
 *       wallpaper, never on the way out, never over the UMP consent form.</li>
 *   <li>Never before consent: {@link AdManager#canRequestAds()} gates every load and show.</li>
 *   <li>A four-hour cooldown is persisted, so returning users are not greeted by an ad on
 *       every single visit.</li>
 *   <li>Never stacked with another full-screen ad: interstitials and rewarded ads raise a
 *       flag in {@link AdManager} that this class respects.</li>
 *   <li>The ad is pre-loaded in the background; if none is ready when the app opens, nothing
 *       is shown and a load for the next visit starts instead. The user is never kept
 *       waiting on an ad.</li>
 * </ul>
 */
public final class AppOpenAdController {

    private static final String TAG = "AppOpenAd";
    private static final String PREFS = "ad_prefs";
    private static final String KEY_LAST_SHOW = "app_open_last_show_ms";

    /** Google's recommended minimum gap between two app-open ads. */
    private static final long COOLDOWN_MS = 4L * 60L * 60L * 1000L;

    @Nullable
    private static AppOpenAd cachedAd;
    private static boolean loadInProgress;
    private static boolean showing;

    private AppOpenAdController() {
    }

    /** Starts a background load when there is nothing cached and consent is in place. */
    public static void preload(@NonNull Context context) {
        if (cachedAd != null || loadInProgress || !AdManager.canRequestAds()) {
            return;
        }
        loadInProgress = true;
        try {
            // The orientation argument is mandatory: the four-argument
            // load(Context, String, AdRequest, AppOpenAd.AppOpenAdLoadCallback) overload was
            // deprecated in Google Mobile Ads SDK 21 and removed in the next major release, which
            // is what made this file fail to compile with "cannot find symbol" on
            // play-services-ads 25.x. The app's home screen is portrait, so the ad is asked for
            // in portrait. The load callback is referenced through its parent class
            // ({@link AppOpenAd#AppOpenAdLoadCallback}), which is how this SDK version exposes
            // it — importing a top-level AppOpenAdLoadCallback does not resolve.
            AppOpenAd.load(context.getApplicationContext(), AdConfig.APP_OPEN_UNIT_ID,
                    new AdRequest.Builder().build(), AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                    new AppOpenAd.AppOpenAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull AppOpenAd appOpenAd) {
                            loadInProgress = false;
                            cachedAd = appOpenAd;
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            loadInProgress = false;
                            Log.w(TAG, "app open load failed: " + loadAdError.getMessage());
                        }
                    });
        } catch (Throwable t) {
            loadInProgress = false;
            Log.w(TAG, "app open load threw", t);
        }
    }

    /**
     * Called once per foreground return. Shows the cached ad when every policy guard passes;
     * otherwise quietly prepares the next opportunity.
     */
    public static void tryShow(@NonNull Activity activity) {
        if (showing || AdManager.isFullScreenAdActive() || !AdManager.canRequestAds()) {
            return;
        }
        if (!cooldownElapsed(activity)) {
            preload(activity);
            return;
        }
        AppOpenAd ad = cachedAd;
        cachedAd = null;
        if (ad == null || activity.isFinishing() || activity.isDestroyed()) {
            preload(activity);
            return;
        }

        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdShowedFullScreenContent() {
                showing = true;
                markShown(activity);
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                showing = false;
                preload(activity);
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                showing = false;
                Log.w(TAG, "app open show failed: " + adError.getMessage());
                preload(activity);
            }
        });

        try {
            ad.show(activity);
        } catch (Throwable t) {
            showing = false;
            Log.w(TAG, "app open show threw", t);
            preload(activity);
        }
    }

    private static boolean cooldownElapsed(Context context) {
        long last = prefs(context).getLong(KEY_LAST_SHOW, 0L);
        return System.currentTimeMillis() - last > COOLDOWN_MS;
    }

    private static void markShown(Context context) {
        prefs(context).edit().putLong(KEY_LAST_SHOW, System.currentTimeMillis()).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
