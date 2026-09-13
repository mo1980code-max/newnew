package com.clock.livewallpaper.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;

import java.util.Locale;


/**
 * Applies the in-app language (Arabic by default, English switchable)
 * on all API levels supported by the app (19+).
 */
public class LocaleHelper {
    public static final String LANG_AR = "ar";
    public static final String LANG_EN = "en";
    private static final String KEY_APP_LANG = "appLang";

    public static String getLanguage(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String lang = prefs.getString(KEY_APP_LANG, "");
        if (lang == null || lang.isEmpty()) {
            return LANG_AR;
        }
        return lang;
    }

    public static void setLanguage(Context context, String lang) {
        if (!LANG_AR.equals(lang) && !LANG_EN.equals(lang)) {
            lang = LANG_AR;
        }
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putString(KEY_APP_LANG, lang).apply();
    }

    public static boolean isArabic(Context context) {
        return LANG_AR.equals(getLanguage(context));
    }

    public static Context wrap(Context context) {
        Locale locale = new Locale(getLanguage(context));
        Locale.setDefault(locale);
        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
            return context.createConfigurationContext(config);
        } else {
            config.locale = locale;
            res.updateConfiguration(config, res.getDisplayMetrics());
            return context;
        }
    }
}
