package org.Allah_Clock_Live_Wallpaper.utils;

import android.content.Context;
import android.content.res.Configuration;
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

    /**
     * The Locale the UI currently reads in. Every piece of text or number formatting in the app
     * goes through this, so the language picked inside the app wins over the device language.
     */
    @NonNull
    public static Locale uiLocale(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= 24) {
            LocaleList list = context.getResources().getConfiguration().getLocales();
            if (!list.isEmpty()) {
                return list.get(0);
            }
        } else {
            Locale locale = context.getResources().getConfiguration().locale;
            if (locale != null) {
                return locale;
            }
        }
        return Locale.getDefault();
    }

    /** @return true when the UI is currently Arabic, i.e. layouts must read RTL. */
    public static boolean isArabic(@NonNull Context context) {
        return "ar".equals(uiLocale(context).getLanguage());
    }

    /**
     * A context whose resources read in the language chosen inside the app.
     *
     * <p>From API 33 the platform applies per-app locales to every context by itself, so this
     * normally returns {@code context} unchanged. On API 32 and below AppCompat only overrides
     * {@code AppCompatActivity}, which would leave the live wallpaper, the home-screen widget and
     * the tasbeeh toasts speaking the <i>device</i> language while the app speaks Arabic. Wrapping
     * those few non-activity contexts closes that gap.</p>
     */
    @NonNull
    public static Context wrap(@NonNull Context context) {
        LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();
        if (locales.isEmpty()) {
            return context;
        }
        Locale wanted = locales.get(0);
        if (wanted == null || wanted.getLanguage().equals(uiLocale(context).getLanguage())) {
            return context;
        }
        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(wanted);
        config.setLayoutDirection(wanted);
        return context.createConfigurationContext(config);
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
