package org.Allah_Clock_Live_Wallpaper.ads;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;

/**
 * Anchored-adaptive banner bound to one container view.
 *
 * <p>An {@link AdView} must be paused, resumed and destroyed with its host Activity, so
 * wire the three lifecycle methods into {@code onPause}, {@code onResume} and
 * {@code onDestroy}. Skipping that is one of the most common AdMob policy findings.</p>
 *
 * <p>The banner is only requested when the UMP consent state allows it
 * ({@link AdManager#canRequestAds()}); if consent arrives later the next
 * {@link #resume()} retries the load automatically.</p>
 */
public final class BannerAdController {

    private static final String TAG = "BannerAdController";

    @NonNull
    private final Activity activity;
    @Nullable
    private final ViewGroup container;

    @Nullable
    private AdView adView;
    private boolean requested;

    /** A controller without a container is inert, which keeps callers free of null checks. */
    public BannerAdController(@NonNull Activity activity, @Nullable ViewGroup container) {
        this.activity = activity;
        this.container = container;
    }

    /** Asks for a banner once the container has been measured. Safe to call repeatedly. */
    public void load() {
        final ViewGroup target = container;
        if (target == null || requested) {
            return;
        }
        target.setVisibility(View.GONE);
        target.post(() -> {
            if (!requested) {
                loadNow();
            }
        });
    }

    private void loadNow() {
        final ViewGroup target = container;
        if (target == null || requested) {
            return;
        }
        if (!AdManager.canRequestAds()) {
            return;
        }
        if (activity.isFinishing() || activity.isDestroyed()) {
            return;
        }
        requested = true;
        destroyAdView();

        AdView view = new AdView(activity);
        view.setAdSize(adaptiveSize());
        view.setAdUnitId(AdConfig.BANNER_UNIT_ID);
        view.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                target.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.w(TAG, "banner failed: " + loadAdError.getMessage());
                target.setVisibility(View.GONE);
            }

            @Override
            public void onAdClicked() {
                // Keep the container visible while the landing page is open.
                target.setVisibility(View.VISIBLE);
            }
        });

        adView = view;
        target.removeAllViews();
        target.addView(view, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.loadAd(new AdRequest.Builder().build());
    }

    /** Anchored-adaptive size for the current orientation and the real container width. */
    private AdSize adaptiveSize() {
        DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
        int widthPx = container != null ? container.getWidth() : 0;
        if (widthPx <= 0) {
            widthPx = metrics.widthPixels;
        }
        int widthDp = Math.max(1, (int) (widthPx / metrics.density));
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, widthDp);
    }

    public void pause() {
        if (adView != null) {
            adView.pause();
        }
    }

    public void resume() {
        if (adView != null) {
            adView.resume();
        } else if (!requested) {
            // Consent became available after onCreate, or the first attempt never ran.
            loadNow();
        }
    }

    public void destroy() {
        destroyAdView();
        if (container != null) {
            container.removeAllViews();
            container.setVisibility(View.GONE);
        }
    }

    private void destroyAdView() {
        if (adView != null) {
            try {
                adView.destroy();
            } catch (Throwable ignored) {
            }
            adView = null;
        }
    }
}
