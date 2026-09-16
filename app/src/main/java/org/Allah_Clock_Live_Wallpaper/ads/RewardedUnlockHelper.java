package org.Allah_Clock_Live_Wallpaper.ads;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import org.Allah_Clock_Live_Wallpaper.R;

/**
 * The only entry point into the rewarded flow.
 *
 * <p>Flow: user taps a locked golden clock → an explanatory dialog with a real cancel
 * button → only after an explicit "Watch ad &amp; unlock" tap is the rewarded video
 * requested. Nothing here ever starts an ad on its own, which is what AdMob's
 * "unexpected interstitial / rewarded" policy is about.</p>
 */
public final class RewardedUnlockHelper {

    public interface UnlockCallback {
        /** The premium content is available now — continue with the original action. */
        void onProceed();

        /** The user declined or closed the video early — do nothing. */
        void onDeclined();
    }

    private RewardedUnlockHelper() {
    }

    /**
     * @param activity hosts the dialog and the rewarded video
     * @param callback always invoked exactly once, on the main thread
     */
    public static void requestUnlock(@NonNull Activity activity, @NonNull UnlockCallback callback) {
        if (!PremiumManager.premiumEnabled() || PremiumManager.isUnlocked()) {
            callback.onProceed();
            return;
        }
        if (activity.isFinishing() || activity.isDestroyed()) {
            callback.onDeclined();
            return;
        }
        showExplanation(activity, callback);
    }

    private static void showExplanation(@NonNull Activity activity,
                                        @NonNull UnlockCallback callback) {
        View content = LayoutInflater.from(activity)
                .inflate(R.layout.dialog_reward_unlock, null);

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
                            PremiumManager.unlock();
                            toast(activity, R.string.premium_unlocked);
                            callback.onProceed();
                        }

                        @Override
                        public void onRewardCancelled() {
                            toast(activity, R.string.premium_not_unlocked);
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

    private static void toast(@NonNull Activity activity, int messageRes) {
        if (activity.isFinishing() || activity.isDestroyed()) {
            return;
        }
        Toast.makeText(activity, messageRes, Toast.LENGTH_SHORT).show();
    }
}
