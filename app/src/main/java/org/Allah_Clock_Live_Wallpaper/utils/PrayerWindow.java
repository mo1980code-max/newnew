package org.Allah_Clock_Live_Wallpaper.utils;

import android.content.Context;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Decides whether "now" falls inside the morning or the evening athkar window.
 *
 * <p><b>No scheduler of any kind.</b> The answer is computed lazily at the moment something
 * wants to show the badge (activity resume, wallpaper redraw), exactly like the rotating
 * dhikr. A WorkManager job or a repeating alarm would wake the device to update a badge
 * nobody can see while it sleeps — pure battery waste for zero benefit.</p>
 *
 * <p>Two modes, chosen in the athkar settings dialog:</p>
 * <ul>
 *   <li><b>Astronomical (default)</b>: sunrise / fajr / asr / maghrib / isha from the classic
 *       solar declination + hour-angle formulas, fully offline, using the city the user
 *       already picked for the Qibla compass ({@code qiblaCityIndex}).</li>
 *   <li><b>Fixed slots</b>: four editable times for users who prefer their own schedule or
 *       live where high-latitude math degenerates.</li>
 * </ul>
 *
 * <p>Windows: morning = fajr → sunrise + 2 h grace; evening = asr → isha
 * (maghrib + 2 h when isha cannot be computed).</p>
 */
public final class PrayerWindow {

    public static final int NONE = 0;
    public static final int MORNING = 1;
    public static final int EVENING = 2;

    /** Sun altitude at sunrise / sunset, accounting for refraction. */
    private static final double SUNRISE_ALTITUDE = -0.833;
    private static final double MORNING_GRACE_HOURS = 2.0;
    private static final double EVENING_FALLBACK_HOURS = 2.0;

    private PrayerWindow() {
    }

    public static int currentWindow(Context context, TinyDB prefs, long millis) {
        if (prefs.getBoolean("athkarFixedTimes", false)) {
            int minute = minuteOfDay(millis);
            if (between(minute, prefs.getInt("morningStartMin", 270),
                    prefs.getInt("morningEndMin", 540))) {
                return MORNING;
            }
            if (between(minute, prefs.getInt("eveningStartMin", 960),
                    prefs.getInt("eveningEndMin", 1230))) {
                return EVENING;
            }
            return NONE;
        }

        double[] times = computeSolarTimes(context, prefs, millis);
        double fajr = times[0];
        double sunrise = times[1];
        double asr = times[2];
        double maghrib = times[3];
        double isha = times[4];
        double now = minuteOfDay(millis) / 60.0;

        // High latitudes: the sun may never reach the fajr / sunrise altitudes, which makes the
        // formulas return NaN. Rather than hiding the athkar for good, fall back to the same
        // default slots the fixed mode uses, so the reader is always reachable.
        if (Double.isNaN(fajr) || Double.isNaN(sunrise) || Double.isNaN(asr)
                || Double.isNaN(maghrib)) {
            int minute = minuteOfDay(millis);
            if (between(minute, 270, 540)) {
                return MORNING;
            }
            if (between(minute, 960, 1230)) {
                return EVENING;
            }
            return NONE;
        }

        if (now >= fajr && now <= sunrise + MORNING_GRACE_HOURS) {
            return MORNING;
        }
        double eveningEnd = Double.isNaN(isha) ? maghrib + EVENING_FALLBACK_HOURS : isha;
        if (now >= asr && now <= eveningEnd) {
            return EVENING;
        }
        return NONE;
    }

    /**
     * The window to open when the reader is asked for outside both windows.
     *
     * <p>The home screen carries a permanent athkar tile, so tapping it at 22:00 must not land
     * on a closing screen. Before the evening window opens (including the small hours after
     * midnight) the morning set is the one ahead; afterwards the evening set is the closest.</p>
     *
     * @return {@link #MORNING} or {@link #EVENING}, never {@link #NONE}
     */
    public static int windowOrUpcoming(Context context, TinyDB prefs, long millis) {
        int current = currentWindow(context, prefs, millis);
        if (current != NONE) {
            return current;
        }
        int eveningStart;
        if (prefs.getBoolean("athkarFixedTimes", false)) {
            eveningStart = prefs.getInt("eveningStartMin", 960);
        } else {
            double asr = computeSolarTimes(context, prefs, millis)[2];
            eveningStart = Double.isNaN(asr) ? 960 : (int) Math.round(asr * 60.0);
        }
        return minuteOfDay(millis) < eveningStart ? MORNING : EVENING;
    }

    /** @return hours-of-day of {fajr, sunrise, asr, maghrib, isha}; isha may be NaN. */
    private static double[] computeSolarTimes(Context context, TinyDB prefs, long millis) {
        int city = prefs.getInt("qiblaCityIndex", 0);
        if (city < 0 || city >= QiblaUtil.CITY_COORDS.length) {
            city = 0;
        }
        double lat = Math.toRadians(QiblaUtil.CITY_COORDS[city][0]);
        double lon = QiblaUtil.CITY_COORDS[city][1];
        double tzOffset = TimeZone.getDefault().getOffset(millis) / 3600000.0;

        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utc.setTimeInMillis(millis);
        int doy = utc.get(Calendar.DAY_OF_YEAR);

        double b = Math.toRadians(360.0 / 365.0 * (doy - 81));
        double declination = Math.toRadians(23.45 * Math.sin(b));
        double equationOfTime = 9.87 * Math.sin(2 * b) - 7.53 * Math.cos(b) - 1.5 * Math.sin(b);
        double solarNoon = 12.0 - lon / 15.0 - equationOfTime / 60.0 + tzOffset;

        double fajrAngle = prefs.getInt("fajrAngle", 15);
        double sunriseHours = hourAngleHours(SUNRISE_ALTITUDE, lat, declination);
        double fajrHours = hourAngleHours(-fajrAngle, lat, declination);
        double maghribHours = sunriseHours;
        double ishaHours = hourAngleHours(-fajrAngle, lat, declination);

        // Asr: sun altitude when an object's shadow equals its length plus the noon shadow.
        double asrAltitude = Math.toDegrees(
                Math.atan(1.0 / (1.0 + Math.tan(Math.abs(lat - declination)))));
        double asrHours = hourAngleHours(asrAltitude, lat, declination);

        return new double[]{
                solarNoon - fajrHours,
                solarNoon - sunriseHours,
                solarNoon + asrHours,
                solarNoon + maghribHours,
                Double.isNaN(ishaHours) ? Double.NaN : solarNoon + ishaHours,
        };
    }

    /** Hours between solar noon and the moment the sun reaches {@code altitudeDeg}. */
    private static double hourAngleHours(double altitudeDeg, double lat, double declination) {
        double cosH = (Math.sin(Math.toRadians(altitudeDeg))
                - Math.sin(lat) * Math.sin(declination))
                / (Math.cos(lat) * Math.cos(declination));
        if (cosH > 1.0 || cosH < -1.0) {
            // The sun never reaches this altitude today (polar latitudes).
            return Double.NaN;
        }
        return Math.toDegrees(Math.acos(cosH)) / 15.0;
    }

    private static int minuteOfDay(long millis) {
        Calendar local = Calendar.getInstance();
        local.setTimeInMillis(millis);
        return local.get(Calendar.HOUR_OF_DAY) * 60 + local.get(Calendar.MINUTE);
    }

    private static boolean between(int minute, int start, int end) {
        return minute >= start && minute <= end;
    }
}
