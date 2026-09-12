package org.Allah_Clock_Live_Wallpaper.ads;

/**
 * Single place that holds every advertising / monetisation constant.
 *
 * <p><b>BEFORE YOU PUBLISH:</b> the four ad-unit ids and the app id below are Google's
 * official <i>sample</i> ids. They render real looking test ads, never earn money and
 * never count as invalid traffic — but shipping them to production breaks AdMob policy.
 * Replace all five values with your own from https://apps.admob.com and mirror the app id
 * into the {@code com.google.android.gms.ads.APPLICATION_ID} meta-data in
 * AndroidManifest.xml.</p>
 */
public final class AdConfig {

    private AdConfig() {
    }

    // ─────────────────────────── AdMob ids ───────────────────────────

    /** AdMob app id — keep in sync with AndroidManifest.xml. */
    public static final String ADMOB_APP_ID = "ca-app-pub-3940256099942544~3347511713";

    /** Adaptive banner shown at the bottom of the sub-screens. */
    public static final String BANNER_UNIT_ID = "ca-app-pub-3940256099942544/6300978111";

    /** Interstitial shown only after a wallpaper has been applied successfully. */
    public static final String INTERSTITIAL_UNIT_ID = "ca-app-pub-3940256099942544/1033173712";

    /** Rewarded video that unlocks the premium (golden) clocks. */
    public static final String REWARDED_UNIT_ID = "ca-app-pub-3940256099942544/5224354917";

    /** Native card injected in the middle of the clock / wallpaper grids. */
    public static final String NATIVE_UNIT_ID = "ca-app-pub-3940256099942544/2247696110";

    /**
     * Hashed device ids that must always receive test ads — put your own test phones here
     * once you switch to real ad units, so you never click a live ad by accident
     * (that is an AdMob policy violation). Read them from Logcat with the tag
     * "Ads" while the app runs. Emulators are test devices automatically.
     */
    public static final String[] TEST_DEVICE_HASHED_IDS = new String[0];

    /**
     * Highest ad maturity served to users. {@code G} matches an "Everyone" rated
     * wallpaper app and keeps the app clear of the AdMob "Inappropriate content"
     * policy findings.
     */
    public static final String MAX_AD_CONTENT_RATING = "G";

    // ─────────────────────── Premium (golden) clocks ───────────────────────

    /**
     * How many entries at the <i>end</i> of every clock list are premium.
     * Applies to the Analog, Digital and Smart clock lists. Set to 0 to disable
     * the rewarded gate completely.
     */
    public static final int PREMIUM_ITEMS_PER_LIST = 3;

    /**
     * Lifetime of a rewarded unlock.
     * {@code 0} (default) = session unlock: it is granted for as long as the process
     * lives and is cleared by {@link PremiumManager#resetForNewSession()} when the app
     * starts again. Use e.g. {@code 24 * 60 * 60 * 1000L} for a 24 hour unlock.
     */
    public static final long PREMIUM_UNLOCK_DURATION_MS = 0L;

    /**
     * If the rewarded video cannot be loaded (no network, no fill …) the premium item is
     * unlocked anyway instead of trapping the user behind a broken ad.
     */
    public static final boolean UNLOCK_WHEN_REWARDED_UNAVAILABLE = true;

    // ─────────────────────────── Native ad card ───────────────────────────

    /**
     * Where the native card is injected, as a fraction of the list length.
     * {@code 0.5f} = exactly in the middle.
     */
    public static final float NATIVE_AD_POSITION_RATIO = 0.5f;

    /** Minimum number of rows before a native card is worth inserting. */
    public static final int NATIVE_AD_MIN_LIST_SIZE = 4;
}
