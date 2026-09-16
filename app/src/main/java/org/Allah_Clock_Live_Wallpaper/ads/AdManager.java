package org.Allah_Clock_Live_Wallpaper.ads;

import android.app.Activity;
import android.app.Dialog;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

import org.Allah_Clock_Live_Wallpaper.utils.UiCompat;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Every interaction with the Google Mobile Ads SDK goes through this class.
 *
 * <p>Policy notes baked into the implementation:</p>
 * <ul>
 *   <li>Consent (UMP) is gathered before the SDK is initialised and before any ad is
 *       requested — mandatory for EEA/UK/Swiss traffic and for the Play "Data safety"
 *       declaration.</li>
 *   <li>{@link #canRequestAds()} gates every single ad request.</li>
 *   <li>Interstitials are <i>pre-loaded</i> and only shown at a natural transition
 *       (right after a wallpaper has been applied). Never on launch, never on exit.</li>
 *   <li>Rewarded ads are only ever started from an explicit user tap on an explanatory
 *       dialog, and always have a working cancel path.</li>
 *   <li>Native ads are rendered inside a {@code NativeAdView} with the SDK's AdChoices
 *       icon plus a visible "Ad" badge, so they can never be mistaken for app content.</li>
 * </ul>
 */
public final class AdManager {

    private static final String TAG = "AdManager";

    private static final AtomicBoolean CONSENT_FLOW_STARTED = new AtomicBoolean(false);
    private static final AtomicBoolean SDK_INITIALISED = new AtomicBoolean(false);

    /** Raised while an interstitial or rewarded ad owns the screen. */
    private static final AtomicBoolean FULL_SCREEN_ACTIVE = new AtomicBoolean(false);

    /** True while another full-screen ad is on screen; app-open ads must never stack. */
    public static boolean isFullScreenAdActive() {
        return FULL_SCREEN_ACTIVE.get();
    }

    @Nullable
    private static ConsentInformation consentInformation;

    @Nullable
    private static InterstitialAd cachedInterstitial;
    private static boolean interstitialLoading = false;

    private AdManager() {
    }

    // ══════════════════════════ Consent + initialisation ══════════════════════════

    /**
     * Call once from the launcher Activity, as early as possible.
     *
     * @param onFinished always invoked exactly once on the main thread, whether consent
     *                   was granted, refused, unavailable or already known. Check
     *                   {@link #canRequestAds()} afterwards.
     */
    public static void requestConsentAndInitialize(@NonNull Activity activity,
                                                   @Nullable Runnable onFinished) {
        ConsentInformation info = UserMessagingPlatform.getConsentInformation(activity);
        consentInformation = info;

        if (!CONSENT_FLOW_STARTED.compareAndSet(false, true)) {
            // A second Activity asked while the first flow is still running.
            if (info.canRequestAds()) {
                initializeSdk(activity);
            }
            if (onFinished != null) {
                onFinished.run();
            }
            return;
        }

        ConsentRequestParameters params = new ConsentRequestParameters.Builder()
                .setTagForUnderAgeOfConsent(false)
                .build();

        info.requestConsentInfoUpdate(
                activity,
                params,
                () -> UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity, formError -> {
                    if (formError != null) {
                        Log.w(TAG, "consent form: " + formError);
                    }
                    if (info.canRequestAds()) {
                        initializeSdk(activity);
                    }
                    if (onFinished != null) {
                        onFinished.run();
                    }
                }),
                requestError -> {
                    Log.w(TAG, "consent info update: " + requestError);
                    if (info.canRequestAds()) {
                        initializeSdk(activity);
                    }
                    if (onFinished != null) {
                        onFinished.run();
                    }
                });

        // Consent gathered in a previous session can already be usable.
        if (info.canRequestAds()) {
            initializeSdk(activity);
        }
    }

    private static void initializeSdk(@NonNull Activity activity) {
        if (!SDK_INITIALISED.compareAndSet(false, true)) {
            return;
        }

        List<String> testDevices = Arrays.asList(AdConfig.TEST_DEVICE_HASHED_IDS);
        RequestConfiguration configuration = MobileAds.getRequestConfiguration()
                .toBuilder()
                .setTagForChildDirectedTreatment(
                        RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE)
                .setMaxAdContentRating(AdConfig.MAX_AD_CONTENT_RATING)
                .setTestDeviceIds(testDevices)
                .build();
        MobileAds.setRequestConfiguration(configuration);

        MobileAds.initialize(activity, initializationStatus -> {
            Log.i(TAG, "ads sdk ready");
            AppOpenAdController.preload(activity);
        });
    }

    /** True when consent is in place and the SDK may be asked for ads. */
    public static boolean canRequestAds() {
        ConsentInformation info = consentInformation;
        return info != null && info.canRequestAds();
    }

    /**
     * True when a "Privacy options" entry point must be offered to the user.
     * Google requires a persistent, easily reachable control in that case.
     */
    public static boolean isPrivacyOptionsRequired() {
        ConsentInformation info = consentInformation;
        return info != null
                && info.getPrivacyOptionsRequirementStatus()
                == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED;
    }

    /** Re-opens the privacy message form so users can change their choices. */
    public static void showPrivacyOptions(@NonNull Activity activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity,
                formError -> {
                    if (formError != null) {
                        Log.w(TAG, "privacy options form: " + formError);
                    }
                });
    }

    // ═══════════════════════════════ Interstitial ═══════════════════════════════

    /**
     * Starts loading an interstitial in the background. Call it well before the moment
     * you intend to show it (for example in {@code onCreate} of the screen that leads to
     * "apply wallpaper"), so the ad is ready and the user never waits.
     */
    public static void preloadInterstitial(@NonNull Activity activity) {
        if (!canRequestAds() || cachedInterstitial != null || interstitialLoading) {
            return;
        }
        interstitialLoading = true;
        InterstitialAd.load(activity.getApplicationContext(), AdConfig.INTERSTITIAL_UNIT_ID,
                new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        interstitialLoading = false;
                        cachedInterstitial = interstitialAd;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        interstitialLoading = false;
                        cachedInterstitial = null;
                        Log.w(TAG, "interstitial load failed: " + loadAdError.getMessage());
                    }
                });
    }

    /**
     * Shows a pre-loaded interstitial.
     *
     * @param onFinished always invoked exactly once on the main thread — after the ad is
     *                   dismissed, after a failed show, or immediately when no ad is
     *                   available. Never blocks the caller's flow.
     */
    public static void showInterstitial(@NonNull Activity activity, @Nullable Runnable onFinished) {
        InterstitialAd ad = cachedInterstitial;
        cachedInterstitial = null;

        if (ad == null || activity.isFinishing() || activity.isDestroyed()) {
            preloadInterstitial(activity);
            runOnce(onFinished);
            return;
        }

        final AtomicBoolean finished = new AtomicBoolean(false);
        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                FULL_SCREEN_ACTIVE.set(false);
                if (finished.compareAndSet(false, true)) {
                    runOnce(onFinished);
                }
                preloadInterstitial(activity);
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                FULL_SCREEN_ACTIVE.set(false);
                Log.w(TAG, "interstitial show failed: " + adError.getMessage());
                if (finished.compareAndSet(false, true)) {
                    runOnce(onFinished);
                }
                preloadInterstitial(activity);
            }

            @Override
            public void onAdShowedFullScreenContent() {
                // nothing to do
            }
        });

        FULL_SCREEN_ACTIVE.set(true);
        try {
            ad.show(activity);
        } catch (Throwable t) {
            Log.w(TAG, "interstitial show threw", t);
            if (finished.compareAndSet(false, true)) {
                runOnce(onFinished);
            }
        }
    }

    // ═════════════════════════════════ Rewarded ═════════════════════════════════

    public interface RewardedCallback {
        /** The user watched the video and earned the reward. */
        void onRewardEarned();

        /** The user cancelled, or no ad could be shown. */
        void onRewardCancelled();
    }

    /**
     * Loads and shows a rewarded video. Only call this from a direct user action
     * (a tap on "Watch ad to unlock"), never automatically.
     */
    public static void showRewarded(@NonNull Activity activity,
                                    @NonNull String loadingMessage,
                                    @NonNull RewardedCallback callback) {
        if (!canRequestAds() || activity.isFinishing() || activity.isDestroyed()) {
            callback.onRewardCancelled();
            return;
        }

        final Dialog loading = UiCompat.showLoading(activity, loadingMessage);
        final AtomicBoolean rewarded = new AtomicBoolean(false);
        final AtomicBoolean shown = new AtomicBoolean(false);
        final AtomicBoolean settled = new AtomicBoolean(false);

        RewardedAd.load(activity.getApplicationContext(), AdConfig.REWARDED_UNIT_ID,
                new AdRequest.Builder().build(), new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        UiCompat.dismissSafely(loading);
                        if (activity.isFinishing() || activity.isDestroyed()) {
                            settle(settled, rewarded, shown, callback);
                            return;
                        }
                        rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                FULL_SCREEN_ACTIVE.set(false);
                                settle(settled, rewarded, shown, callback);
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                FULL_SCREEN_ACTIVE.set(false);
                                Log.w(TAG, "rewarded show failed: " + adError.getMessage());
                                settle(settled, rewarded, shown, callback);
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                shown.set(true);
                            }
                        });
                    FULL_SCREEN_ACTIVE.set(true);
                    rewardedAd.show(activity, rewardItem -> rewarded.set(true));
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.w(TAG, "rewarded load failed: " + loadAdError.getMessage());
                        UiCompat.dismissSafely(loading);
                        settle(settled, rewarded, shown, callback);
                    }
                });
    }

    /**
     * Grants the reward when the video was watched to the end. When the ad never made it
     * on screen (no fill, no network …) the fail-open switch in {@link AdConfig} decides
     * whether the user still gets the unlock instead of being trapped by a broken ad.
     * A user who closed a playing video early never gets it for free.
     */
    private static void settle(AtomicBoolean settled, AtomicBoolean rewarded,
                               AtomicBoolean shown, RewardedCallback callback) {
        if (!settled.compareAndSet(false, true)) {
            return;
        }
        if (rewarded.get() || (!shown.get() && AdConfig.UNLOCK_WHEN_REWARDED_UNAVAILABLE)) {
            callback.onRewardEarned();
        } else {
            callback.onRewardCancelled();
        }
    }

    // ══════════════════════════════════ Native ══════════════════════════════════

    public interface NativeAdCallback {
        void onNativeAdLoaded(@NonNull NativeAd nativeAd);

        void onNativeAdFailed();
    }

    /** Loads one native ad. The caller owns the returned {@link NativeAd} and must destroy it. */
    public static void loadNative(@NonNull Activity activity, @NonNull NativeAdCallback callback) {
        if (!canRequestAds() || activity.isFinishing() || activity.isDestroyed()) {
            callback.onNativeAdFailed();
            return;
        }
        AdLoader adLoader = new AdLoader.Builder(activity.getApplicationContext(),
                AdConfig.NATIVE_UNIT_ID)
                .forNativeAd(callback::onNativeAdLoaded)
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.w(TAG, "native load failed: " + loadAdError.getMessage());
                        callback.onNativeAdFailed();
                    }
                })
                .build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private static void runOnce(@Nullable Runnable runnable) {
        if (runnable != null) {
            runnable.run();
        }
    }
}
