package org.Allah_Clock_Live_Wallpaper.ads;

import android.content.Context;

import androidx.annotation.NonNull;

import org.Allah_Clock_Live_Wallpaper.utils.TinyDB;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * The <b>permanent</b> rewarded unlock: which premium backgrounds this device has earned.
 *
 * <p>Deliberately different from {@link PremiumManager}, which holds the clocks' unlock in memory
 * for one session. A background the user has watched a video for is theirs to keep, so the state
 * is written to {@code SharedPreferences} the moment AdMob's reward callback fires and read back
 * on every later launch — closing the app, reinstalling over it or rebooting never loses it.</p>
 *
 * <p>The stored shape is a plain {@code ";"}-separated list of resource names, not indexes: a
 * wallpaper added to or removed from the catalogue at a later version would shift every index,
 * while a name stays attached to the image it was earned for.</p>
 */
public final class PremiumUnlocks {

    /** Preference key. Versioned so a future change of shape can migrate instead of crash. */
    private static final String PREF_UNLOCKED = "premiumBackgroundsUnlockedV1";

    /** Per-process cache; the preference file is read once and written through. */
    private static Set<String> cache;

    private PremiumUnlocks() {
    }

    /** Stable key of one wallpaper: its resource name, never its array position. */
    @NonNull
    public static String key(@NonNull Context context, int drawableRes) {
        try {
            return context.getResources().getResourceEntryName(drawableRes);
        } catch (Throwable ignored) {
            return "drawable:" + drawableRes;
        }
    }

    public static boolean isUnlocked(@NonNull Context context, int drawableRes) {
        return unlocked(context).contains(key(context, drawableRes));
    }

    /** Records an earned unlock and writes it to local storage. */
    public static void unlock(@NonNull Context context, int drawableRes) {
        String key = key(context, drawableRes);
        Set<String> unlocked = new HashSet<>(unlocked(context));
        if (!unlocked.add(key)) {
            return;
        }
        cache = unlocked;
        new TinyDB(context.getApplicationContext()).putString(PREF_UNLOCKED, join(unlocked));
    }

    /** Test/debug seam: forgets every earned background (used by the preview tooling). */
    public static void clear(@NonNull Context context) {
        cache = new HashSet<>();
        new TinyDB(context.getApplicationContext()).putString(PREF_UNLOCKED, "");
    }

    @NonNull
    private static Set<String> unlocked(@NonNull Context context) {
        if (cache != null) {
            return cache;
        }
        String stored = new TinyDB(context.getApplicationContext()).getString(PREF_UNLOCKED);
        Set<String> parsed = new HashSet<>();
        if (stored != null && !stored.isEmpty()) {
            Collections.addAll(parsed, stored.split(";"));
        }
        cache = parsed;
        return cache;
    }

    @NonNull
    private static String join(@NonNull Set<String> values) {
        StringBuilder out = new StringBuilder();
        for (String value : values) {
            if (value == null || value.isEmpty()) {
                continue;
            }
            if (out.length() > 0) {
                out.append(';');
            }
            out.append(value);
        }
        return out.toString();
    }
}
