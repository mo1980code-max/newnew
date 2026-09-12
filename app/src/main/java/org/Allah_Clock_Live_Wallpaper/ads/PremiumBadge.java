package org.Allah_Clock_Live_Wallpaper.ads;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.Allah_Clock_Live_Wallpaper.utils.UiCompat;

/**
 * Shared "golden clock" behaviour for the three clock adapters: paints the premium
 * markers and turns a tap on a locked entry into the rewarded-unlock flow.
 */
public final class PremiumBadge {

    private PremiumBadge() {
    }

    /**
     * Golden tint marks an entry as premium whether or not it is unlocked; the padlock is
     * only visible while it still needs a rewarded video.
     */
    public static void bind(@Nullable View overlay, @Nullable ImageView lock,
                            int position, int itemCount) {
        boolean premium = PremiumManager.isPremiumIndex(position, itemCount);
        boolean locked = premium && !PremiumManager.isUnlocked();
        if (overlay != null) {
            overlay.setVisibility(premium ? View.VISIBLE : View.GONE);
        }
        if (lock != null) {
            lock.setVisibility(locked ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Routes a tap on a list entry: locked entries ask for the rewarded video first,
     * everything else runs straight through.
     *
     * @param source   the clicked view, used to find the hosting Activity
     * @param position index inside the content list
     * @param proceed  the original click action
     * @param refresh  called after the unlock state changed so badges can be repainted
     */
    public static void onItemClick(@NonNull View source, int position, int itemCount,
                                   @NonNull Runnable proceed, @Nullable Runnable refresh) {
        if (!PremiumManager.isLocked(position, itemCount)) {
            proceed.run();
            return;
        }
        Activity activity = UiCompat.findActivity(source.getContext());
        if (activity == null) {
            proceed.run();
            return;
        }
        RewardedUnlockHelper.requestUnlock(activity, new RewardedUnlockHelper.UnlockCallback() {
            @Override
            public void onProceed() {
                if (refresh != null) {
                    refresh.run();
                }
                proceed.run();
            }

            @Override
            public void onDeclined() {
                if (refresh != null) {
                    refresh.run();
                }
            }
        });
    }
}
