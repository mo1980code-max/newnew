package org.Allah_Clock_Live_Wallpaper.model;

/** A locally saved bookmark location. Quran text remains in the bundled read-only asset. */
public final class QuranBookmark {

    private final int surahNumber;
    private final int ayahNumber;

    public QuranBookmark(int surahNumber, int ayahNumber) {
        this.surahNumber = surahNumber;
        this.ayahNumber = ayahNumber;
    }

    public int getSurahNumber() {
        return surahNumber;
    }

    public int getAyahNumber() {
        return ayahNumber;
    }

    public String getKey() {
        return surahNumber + ":" + ayahNumber;
    }
}
