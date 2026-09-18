package org.Allah_Clock_Live_Wallpaper.ads;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import org.Allah_Clock_Live_Wallpaper.R;

/**
 * The only entry point into the rewarded flow — for the golden clocks and for the premium
 * backgrounds alike.
 *
 * <p>Flow: the user taps a locked item → an explanatory dialog with a real cancel button →
 * only after an explicit “Watch ad &amp; unlock” tap is the rewarded video requested → and only
 * AdMob's own reward callback grants the item. Nothing here ever starts an ad on its own, which
 * is what AdMob's “unexpected rewarded” policy is about.</p>
 *
 * <p>What the reward <i>means</i> is passed in by the caller, because the two gates differ on
 * purpose: the clocks unlock in memory for the session ({@link PremiumManager}), while a
 * background is written to local storage immediately and stays unlocked for good
 * ({@link PremiumUnlocks}).</p>
 */
public final class RewardedUnlockHelper {

    public interface UnlockCallback {
        /** The premium content is available now — continue with the original action. */
        void onProceed();

        /** The user declined or closed the video early — do nothing. */
        void onDeclined();
    }

    /** What an earned reward does. Invoked on the main thread, after AdMob confirmed it. */
    private interface Reward {
        void grant(@NonNull Activity activity);
    }

    private RewardedUnlockHelper() {
    }

    // ═════════════════════════════ premium (golden) clocks ═════════════════════════════

    /**
     * @param activity hosts the dialog and the rewarded video
     * @param callback always invoked exactly once, on the main thread
     */
    public static void requestUnlock(@NonNull Activity activity, @NonNull UnlockCallback callback) {
        if (!PremiumManager.premiumEnabled() || PremiumManager.isUnlocked()) {
            callback.onProceed();
            return;
        }
        showExplanation(activity, R.string.premium_dialog_title, R.string.premium_dialog_message,
                R.string.premium_dialog_watch, R.string.premium_unlocked,
                // A lambda, not PremiumManager::unlock — the reward takes the Activity that hosts
                // the video, while unlock() is a static that takes nothing, so the method
                // reference has no signature to bind to and javac rejects it.
                R.string.premium_not_unlocked, callback, act -> PremiumManager.unlock());
    }

    // ═════════════════════════════ premium backgrounds ═════════════════════════════

    /**
     * Asks for the rewarded video that unlocks one premium background. The unlock is persisted
     * locally the moment the reward is confirmed, so the background stays the user's for good.
     *
     * @param drawableRes the wallpaper being unlocked
     */
    public static void requestBackgroundUnlock(@NonNull Activity activity, int drawableRes,
                                              @NonNull UnlockCallback callback) {
        if (!PremiumBackgroundHelper.isLocked(activity, drawableRes)) {
            callback.onProceed();
            return;
        }
        showExplanation(activity, R.string.premium_bg_title, R.string.premium_bg_message,
                R.string.premium_dialog_watch, R.string.premium_bg_unlocked,
                R.string.premium_bg_not_unlocked, callback,
                context -> PremiumUnlocks.unlock(context, drawableRes));
    }

    // ═════════════════════════════ shared dialog and video ═════════════════════════════

    private static void showExplanation(@NonNull Activity activity,
                                        @StringRes final int titleRes,
                                        @StringRes final int messageRes,
                                        @StringRes final int watchRes,
                                        @StringRes final int grantedRes,
                                        @StringRes final int deniedRes,
                                        @NonNull final UnlockCallback callback,
                                        @NonNull final Reward reward) {
        View content = LayoutInflater.from(activity)
                .inflate(R.layout.dialog_reward_unlock, null);
        ((TextView) content.findViewById(R.id.rewardTitle)).setText(titleRes);
        ((TextView) content.findViewById(R.id.rewardMessage)).setText(messageRes);
        ((TextView) content.findViewById(R.id.rewardWatch)).setText(watchRes);

        final AlertDialog dialog = new AlertDialog.Builder(activity)
                .setView(content)
                .setCancelable(true)
                .create();

        content.findViewById(R.id.rewardWatch).setOnClickListener(v -> {
            dialog.dismiss();
            if (activity.isFinishing() || activity.isDestroyed()) {
                callback.onDeclined();
                return;
            }
            AdManager.showRewarded(activity, activity.getString(R.string.ad_loading),
                    new AdManager.RewardedCallback() {
                        @Override
                        public void onRewardEarned() {
                            reward.grant(activity);
                            toast(activity, grantedRes);
                            callback.onProceed();
                        }

                        @Override
                        public void onRewardCancelled() {
                            toast(activity, deniedRes);
                            callback.onDeclined();
                        }
                    });
        });

        content.findViewById(R.id.rewardCancel).setOnClickListener(v -> {
            dialog.dismiss();
            callback.onDeclined();
        });

        dialog.setOnCancelListener(d -> callback.onDeclined());

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private static void toast(@NonNull Activity activity, @StringRes int messageRes) {
        if (activity.isFinishing() || activity.isDestroyed()) {
            return;
        }
        Toast.makeText(activity, messageRes, Toast.LENGTH_SHORT).show();
    }
}
