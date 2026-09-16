package org.Allah_Clock_Live_Wallpaper.model;

import androidx.annotation.NonNull;

/** One immutable ayah from the bundled Uthmani Quran text. */
public final class QuranAyah {

    private final int surahNumber;
    private final int ayahNumber;
    @NonNull
    private final String text;
    @NonNull
    private final String normalizedSearchText;

    public QuranAyah(int surahNumber, int ayahNumber, @NonNull String text,
                     @NonNull String normalizedSearchText) {
        this.surahNumber = surahNumber;
        this.ayahNumber = ayahNumber;
        this.text = text;
        this.normalizedSearchText = normalizedSearchText;
    }

    public int getSurahNumber() {
        return surahNumber;
    }

    public int getAyahNumber() {
        return ayahNumber;
    }

    @NonNull
    public String getText() {
        return text;
    }

    /** Normalized only for local search; it is never displayed in place of {@link #getText()}. */
    @NonNull
    public String getNormalizedSearchText() {
        return normalizedSearchText;
    }

    /** Stable, compact key used by the local bookmark store. */
    @NonNull
    public String getKey() {
        return surahNumber + ":" + ayahNumber;
    }
}
