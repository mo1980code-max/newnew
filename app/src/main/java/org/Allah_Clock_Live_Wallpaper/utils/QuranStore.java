package org.Allah_Clock_Live_Wallpaper.utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.Allah_Clock_Live_Wallpaper.model.QuranBookmark;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Private, offline persistence for reader state.
 *
 * <p>Bookmarks and the last visible ayah stay only in the application SharedPreferences.
 * They are restored at the next launch and are never uploaded or sent to an ad provider.</p>
 */
public final class QuranStore {

    private static final String PREF_BOOKMARKS = "quranBookmarksV1";
    private static final String PREF_LAST_SURAH = "quranLastSurahV1";
    private static final String PREF_LAST_AYAH = "quranLastAyahV1";

    private final TinyDB tinyDB;
    @Nullable
    private String cachedSource;
    @NonNull
    private List<QuranBookmark> cachedBookmarks = new ArrayList<>();

    public QuranStore(@NonNull Context context) {
        this.tinyDB = new TinyDB(context.getApplicationContext());
    }

    /** Saves or removes a mark and returns true exactly when a mark was added. */
    public boolean toggleBookmark(int surahNumber, int ayahNumber) {
        if (!isPlausible(surahNumber, ayahNumber)) {
            return false;
        }
        List<QuranBookmark> bookmarks = readBookmarks();
        int found = indexOf(bookmarks, surahNumber, ayahNumber);
        if (found >= 0) {
            bookmarks.remove(found);
            writeBookmarks(bookmarks);
            return false;
        }
        // Most recently added marks appear first in the restored list.
        bookmarks.add(0, new QuranBookmark(surahNumber, ayahNumber));
        writeBookmarks(bookmarks);
        return true;
    }

    public boolean removeBookmark(int surahNumber, int ayahNumber) {
        List<QuranBookmark> bookmarks = readBookmarks();
        int found = indexOf(bookmarks, surahNumber, ayahNumber);
        if (found < 0) {
            return false;
        }
        bookmarks.remove(found);
        writeBookmarks(bookmarks);
        return true;
    }

    public boolean isBookmarked(int surahNumber, int ayahNumber) {
        return indexOf(readBookmarks(), surahNumber, ayahNumber) >= 0;
    }

    /** A defensive list so adapters cannot accidentally mutate persisted reader state. */
    @NonNull
    public List<QuranBookmark> getBookmarks() {
        return new ArrayList<>(readBookmarks());
    }

    public void saveLastReading(int surahNumber, int ayahNumber) {
        if (!isPlausible(surahNumber, ayahNumber)) {
            return;
        }
        this.tinyDB.putInt(PREF_LAST_SURAH, surahNumber);
        this.tinyDB.putInt(PREF_LAST_AYAH, ayahNumber);
    }

    @Nullable
    public QuranBookmark getLastReading() {
        int surah = this.tinyDB.getInt(PREF_LAST_SURAH, -1);
        int ayah = this.tinyDB.getInt(PREF_LAST_AYAH, -1);
        return isPlausible(surah, ayah) ? new QuranBookmark(surah, ayah) : null;
    }

    @NonNull
    private List<QuranBookmark> readBookmarks() {
        String source = this.tinyDB.getString(PREF_BOOKMARKS);
        if (source.equals(this.cachedSource)) {
            return new ArrayList<>(this.cachedBookmarks);
        }
        List<QuranBookmark> parsed = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        if (!source.isEmpty()) {
            String[] rows = source.split(";");
            for (String row : rows) {
                int separator = row.indexOf(':');
                if (separator <= 0 || separator == row.length() - 1) {
                    continue;
                }
                try {
                    int surah = Integer.parseInt(row.substring(0, separator));
                    int ayah = Integer.parseInt(row.substring(separator + 1));
                    QuranBookmark bookmark = new QuranBookmark(surah, ayah);
                    if (isPlausible(surah, ayah) && seen.add(bookmark.getKey())) {
                        parsed.add(bookmark);
                    }
                } catch (NumberFormatException ignored) {
                    // Ignore old or malformed private preference data instead of breaking the reader.
                }
            }
        }
        this.cachedSource = source;
        this.cachedBookmarks = parsed;
        return new ArrayList<>(parsed);
    }

    private void writeBookmarks(@NonNull List<QuranBookmark> bookmarks) {
        StringBuilder serialized = new StringBuilder();
        for (QuranBookmark bookmark : bookmarks) {
            if (serialized.length() > 0) {
                serialized.append(';');
            }
            serialized.append(bookmark.getKey());
        }
        String source = serialized.toString();
        this.cachedSource = source;
        this.cachedBookmarks = new ArrayList<>(bookmarks);
        this.tinyDB.putString(PREF_BOOKMARKS, source);
    }

    private static int indexOf(@NonNull List<QuranBookmark> bookmarks, int surahNumber,
                               int ayahNumber) {
        for (int index = 0; index < bookmarks.size(); index++) {
            QuranBookmark bookmark = bookmarks.get(index);
            if (bookmark.getSurahNumber() == surahNumber
                    && bookmark.getAyahNumber() == ayahNumber) {
                return index;
            }
        }
        return -1;
    }

    /** Validates the compact stored shape; exact ayah counts are validated by QuranRepository. */
    private static boolean isPlausible(int surahNumber, int ayahNumber) {
        return surahNumber >= 1 && surahNumber <= 114 && ayahNumber >= 1 && ayahNumber <= 286;
    }
}
