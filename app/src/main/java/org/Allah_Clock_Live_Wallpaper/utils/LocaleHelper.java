package org.Allah_Clock_Live_Wallpaper.utils;

import android.content.Context;
import android.os.Build;
import android.os.LocaleList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import java.util.Locale;

/**
 * In-app Arabic / English switching with full RTL support.
 *
 * <p>Delegates to {@link AppCompatDelegate#setApplicationLocales(LocaleListCompat)}: the
 * platform handles the activity recreation, the layout mirroring and the persistence across
 * restarts, so there is no hand-rolled locale code to rot.</p>
 */
public final class LocaleHelper {

    public static final String TAG_EN = "en";
    public static final String TAG_AR = "ar";

    private LocaleHelper() {
    }

    /** @return true when the UI is currently Arabic, i.e. layouts must read RTL. */
    public static boolean isArabic(@NonNull Context context) {
        Locale locale;
        if (Build.VERSION.SDK_INT >= 24) {
            LocaleList list = context.getResources().getConfiguration().getLocales();
            locale = list.isEmpty() ? Locale.getDefault() : list.get(0);
        } else {
            locale = context.getResources().getConfiguration().locale;
        }
        return locale != null && "ar".equals(locale.getLanguage());
    }

    /** Applies and persists the chosen language; the UI recreates itself. */
    public static void apply(@NonNull String languageTag) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTag));
    }

    @NonNull
    public static String currentTag(@NonNull Context context) {
        return isArabic(context) ? TAG_AR : TAG_EN;
    }
}
