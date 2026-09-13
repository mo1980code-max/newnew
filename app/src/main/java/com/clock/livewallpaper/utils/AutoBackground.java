package com.clock.livewallpaper.utils;

import com.clock.livewallpaper.R;

import java.util.Calendar;


/**
 * Resolves which built-in background the live wallpaper should show when
 * automatic background switching is enabled (daily rotation or day/night).
 */
public class AutoBackground {
    public static final int MODE_OFF = 0;
    public static final int MODE_DAILY = 1;
    public static final int MODE_DAY_NIGHT = 2;

    public static final int[] BACKGROUNDS = {
            R.drawable.background7, R.drawable.background1, R.drawable.background2,
            R.drawable.background3, R.drawable.background4, R.drawable.background5,
            R.drawable.background6, R.drawable.background8, R.drawable.background9,
            R.drawable.pattern_gradient_01, R.drawable.pattern_gradient_02,
            R.drawable.pattern_gradient_03, R.drawable.pattern_gradient_04,
            R.drawable.pattern_gradient_05, R.drawable.pattern_gradient_06,
            R.drawable.pattern_gradient_07, R.drawable.pattern_gradient_08,
            R.drawable.pattern_gradient_09, R.drawable.pattern_gradient_10,
            R.drawable.pattern_gradient_11, R.drawable.pattern_gradient_12,
            R.drawable.pattern_gradient_13, R.drawable.pattern_gradient_14,
            R.drawable.pattern_gradient_15, R.drawable.pattern_gradient_16,
            R.drawable.pattern_gradient_17, R.drawable.pattern_gradient_18,
            R.drawable.pattern_gradient_19, R.drawable.pattern_gradient_20,
            R.drawable.pattern_gradient_21, R.drawable.pattern_gradient_22,
            R.drawable.pattern_gradient_23, R.drawable.bg1, R.drawable.bg2,
            R.drawable.bg3, R.drawable.bg4, R.drawable.bg5
    };

    /** Bright gradient used during the day in day/night mode. */
    public static final int DAY_BG = R.drawable.bg3;
    /** Dark gradient used during the night in day/night mode. */
    public static final int NIGHT_BG = R.drawable.bg1;

    /** Picks the background for today (rotates once per day). */
    public static int resolveDaily() {
        long days = System.currentTimeMillis() / 86400000L;
        return BACKGROUNDS[(int) (days % BACKGROUNDS.length)];
    }

    public static boolean isDayTime() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return hour >= 6 && hour < 18;
    }

    /** Picks the day or night background depending on the current hour. */
    public static int resolveDayNight() {
        return isDayTime() ? DAY_BG : NIGHT_BG;
    }
}
