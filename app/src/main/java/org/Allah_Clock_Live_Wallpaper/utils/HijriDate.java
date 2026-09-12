package org.Allah_Clock_Live_Wallpaper.utils;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;

import org.Allah_Clock_Live_Wallpaper.R;

/**
 * Hijri (Islamic) calendar date, formatted for display on the wallpaper.
 *
 * <p>On API 24+ the platform ICU implementation is used with the official
 * <i>Umm al-Qura</i> calculation, which is what Saudi Arabia and most Muslim-majority
 * countries publish. On API 23 (the last platform without {@code android.icu}) a standard
 * tabular civil conversion is used instead; it can differ from Umm al-Qura by one day,
 * which is acceptable for a decorative date line.</p>
 */
public final class HijriDate {

    private HijriDate() {
    }

    /** @return {@code {day, monthIndex0, year}} for the given UTC millis. */
    @NonNull
    public static int[] compute(long millis) {
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                android.icu.util.Calendar cal = android.icu.util.Calendar.getInstance(
                        new android.icu.util.ULocale("en-u-ca-islamic-umalqura"));
                cal.setTimeInMillis(millis);
                return new int[]{
                        cal.get(android.icu.util.Calendar.DAY_OF_MONTH),
                        cal.get(android.icu.util.Calendar.MONTH),
                        cal.get(android.icu.util.Calendar.YEAR),
                };
            } catch (Throwable ignored) {
                // fall through to the arithmetic conversion
            }
        }
        return tabular(millis);
    }

    /**
     * Tabular Islamic civil calendar (Kuwaiti algorithm). Epoch: 622-07-16 CE.
     * Deterministic, allocation free and identical on every device.
     */
    private static int[] tabular(long millis) {
        long jd = millis / 86400000L + 2440588L;
        long l = jd - 1948440L + 10632L;
        long n = (l - 1) / 10631L;
        l = l - 10631L * n + 354L;
        long j = ((10985L - l) / 5316L) * ((50L * l) / 17719L)
                + (l / 5670L) * ((43L * l) / 15238L);
        l = l - ((30L - j) / 15L) * ((17719L * j) / 50L)
                - (j / 16L) * ((15238L * j) / 43L) + 29L;
        long month = (24L * l) / 709L;
        long day = l - (709L * month) / 24L;
        long year = 30L * n + j - 30L;
        int monthIndex = (int) month - 1;
        if (monthIndex < 0) {
            monthIndex = 0;
        } else if (monthIndex > 11) {
            monthIndex = 11;
        }
        return new int[]{(int) day, monthIndex, (int) year};
    }

    /** e.g. "12 Ramadan 1447 AH", with month names taken from {@code strings.xml}. */
    @NonNull
    public static String format(@NonNull Context context, long millis) {
        int[] parts = compute(millis);
        String[] months = context.getResources().getStringArray(R.array.hijri_months);
        String month = parts[1] >= 0 && parts[1] < months.length ? months[parts[1]] : "";
        return context.getString(R.string.hijri_format, parts[0], month, parts[2]);
    }
}
