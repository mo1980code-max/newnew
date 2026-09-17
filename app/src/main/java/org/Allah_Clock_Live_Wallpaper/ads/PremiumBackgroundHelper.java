package org.Allah_Clock_Live_Wallpaper.ads;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.utils.UiCompat;
import org.Allah_Clock_Live_Wallpaper.utils.WallpaperCatalog;

/**
 * The rewarded gate in front of the premium backgrounds: which ones are locked, how the lock is
 * painted, and what a tap on one does.
 *
 * <p>The flow is the one AdMob's rewarded policy asks for: tap → an explanation with a real
 * opt-out → only then the video → and only AdMob's own reward callback unlocks the background.
 * A user who closes the video early, or who declines in the dialog, keeps the lock; the
 * fail-open switch in {@link AdConfig} still applies so a broken ad can never trap anyone.</p>
 */
public final class PremiumBackgroundHelper {

    private PremiumBackgroundHelper() {
    }

    /** True for the three wallpapers that ship behind the rewarded gate. */
    public static boolean isPremium(@NonNull Context context, int drawableRes) {
        for (int premiumRes : WallpaperCatalog.getPremiumBackgrounds()) {
            if (premiumRes == drawableRes) {
                return true;
            }
        }
        // The catalogue lives in code, so a mismatch can only happen for a drawable that is not a
        // wallpaper at all (an intent extra from an old version, for instance).
        return false;
    }

    /** Premium <i>and</i> not earned yet: this one needs a rewarded video. */
    public static boolean isLocked(@NonNull Context context, int drawableRes) {
        return isPremium(context, drawableRes)
                && !PremiumUnlocks.isUnlocked(context, drawableRes);
    }

    /**
     * Paints the lock badge of one grid cell from the stored state. Called while a cell is bound —
     * so a background the user has just unlocked comes back unbadged the next time the grid is
     * bound — rather than from a "was it clicked" callback, which is what used to leave stale
     * padlocks behind.
     */
    public static void bindLock(@Nullable View lock, @Nullable TextView badge, @NonNull Context context,
                                int drawableRes) {
        boolean premium = isPremium(context, drawableRes);
        boolean locked = premium && isLocked(context, drawableRes);
        if (lock != null) {
            lock.setVisibility(locked ? View.VISIBLE : View.GONE);
        }
        if (badge != null) {
            badge.setVisibility(premium ? View.VISIBLE : View.GONE);
            badge.setText(R.string.premium_bg_badge);
        }
    }

    /**
     * Routes a tap on one wallpaper cell.
     *
     * @param source   the clicked view, used to find the hosting Activity
     * @param proceed  what the tap does when the background is available
     * @param refresh  called after the unlock state changed, so the grid repaints its badges
     */
    public static void onBackgroundClick(@NonNull View source, int drawableRes,
                                         @NonNull Runnable proceed, @Nullable Runnable refresh) {
        Context context = source.getContext();
        if (!isLocked(context, drawableRes)) {
            proceed.run();
            return;
        }
        Activity activity = UiCompat.findActivity(context);
        if (activity == null) {
            proceed.run();
            return;
        }
        RewardedUnlockHelper.requestBackgroundUnlock(activity, drawableRes,
                new RewardedUnlockHelper.UnlockCallback() {
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
