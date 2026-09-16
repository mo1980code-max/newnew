package org.Allah_Clock_Live_Wallpaper.ads;

import android.os.SystemClock;

/**
 * Holds the state of the rewarded "premium clocks" unlock.
 *
 * <p>With the default {@link AdConfig#PREMIUM_UNLOCK_DURATION_MS} of {@code 0} the unlock
 * lives only for the current app session: {@link #resetForNewSession()} is called from
 * {@code AppClass.onCreate()}, which the OS runs once per process.</p>
 */
public final class PremiumManager {

    private static final long NOT_UNLOCKED = 0L;

    private static long unlockDeadline = NOT_UNLOCKED;

    private PremiumManager() {
    }

    /** Called once per process start. */
    public static void resetForNewSession() {
        unlockDeadline = NOT_UNLOCKED;
    }

    public static boolean isUnlocked() {
        if (unlockDeadline == NOT_UNLOCKED) {
            return false;
        }
        if (unlockDeadline == Long.MAX_VALUE) {
            return true;
        }
        if (SystemClock.elapsedRealtime() >= unlockDeadline) {
            unlockDeadline = NOT_UNLOCKED;
            return false;
        }
        return true;
    }

    public static void unlock() {
        unlockDeadline = AdConfig.PREMIUM_UNLOCK_DURATION_MS <= 0L
                ? Long.MAX_VALUE
                : SystemClock.elapsedRealtime() + AdConfig.PREMIUM_UNLOCK_DURATION_MS;
    }

    public static boolean premiumEnabled() {
        return AdConfig.PREMIUM_ITEMS_PER_LIST > 0;
    }

    /**
     * @param position  zero based index inside the list
     * @param itemCount total number of entries of that list
     * @return true when this entry belongs to the golden/premium tail of the list
     */
    public static boolean isPremiumIndex(int position, int itemCount) {
        int premiumCount = AdConfig.PREMIUM_ITEMS_PER_LIST;
        if (premiumCount <= 0 || itemCount <= premiumCount) {
            // Never lock a list that is mostly premium — that would lock everything.
            return false;
        }
        return position >= itemCount - premiumCount;
    }

    /** Premium entry that still needs a rewarded video. */
    public static boolean isLocked(int position, int itemCount) {
        return isPremiumIndex(position, itemCount) && !isUnlocked();
    }
}
