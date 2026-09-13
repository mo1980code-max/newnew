package com.clock.livewallpaper.quran.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class QuranPreferences {

    private static final String PREF_NAME = "quran_prefs";
    private static final String KEY_BOOKMARK_PAGE = "bookmark_page";
    private static final String KEY_BOOKMARK_SURAH = "bookmark_surah";
    private static final String KEY_BOOKMARK_TIME = "bookmark_timestamp";
    private static final String KEY_LAST_READ_PAGE = "last_read_page";

    private SharedPreferences preferences;

    public QuranPreferences(Context context) {
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveBookmark(int page, String surahName) {
        preferences.edit()
                .putInt(KEY_BOOKMARK_PAGE, page)
                .putString(KEY_BOOKMARK_SURAH, surahName)
                .putLong(KEY_BOOKMARK_TIME, System.currentTimeMillis())
                .apply();
    }

    public int getBookmarkPage() {
        return preferences.getInt(KEY_BOOKMARK_PAGE, 1);
    }

    public String getBookmarkSurah(String defaultName) {
        return preferences.getString(KEY_BOOKMARK_SURAH, defaultName);
    }

    public boolean hasBookmark() {
        return preferences.contains(KEY_BOOKMARK_PAGE);
    }

    public void saveLastReadPage(int page) {
        preferences.edit()
                .putInt(KEY_LAST_READ_PAGE, page)
                .apply();
    }

    public int getLastReadPage() {
        return preferences.getInt(KEY_LAST_READ_PAGE, 1);
    }
}
