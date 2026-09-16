package org.Allah_Clock_Live_Wallpaper.model;

import androidx.annotation.NonNull;

/** Immutable metadata for one of the 114 Quran surahs. */
public final class QuranSurah {

    private final int number;
    private final int ayahCount;
    @NonNull
    private final String arabicName;
    @NonNull
    private final String transliteration;
    @NonNull
    private final String englishMeaning;
    private final boolean meccan;

    public QuranSurah(int number, int ayahCount, @NonNull String arabicName,
                      @NonNull String transliteration, @NonNull String englishMeaning,
                      boolean meccan) {
        this.number = number;
        this.ayahCount = ayahCount;
        this.arabicName = arabicName;
        this.transliteration = transliteration;
        this.englishMeaning = englishMeaning;
        this.meccan = meccan;
    }

    public int getNumber() {
        return number;
    }

    public int getAyahCount() {
        return ayahCount;
    }

    @NonNull
    public String getArabicName() {
        return arabicName;
    }

    @NonNull
    public String getTransliteration() {
        return transliteration;
    }

    @NonNull
    public String getEnglishMeaning() {
        return englishMeaning;
    }

    public boolean isMeccan() {
        return meccan;
    }

    /** The primary name follows the language selected for the application UI. */
    @NonNull
    public String getDisplayName(boolean arabicUi) {
        return arabicUi ? arabicName : transliteration;
    }

    /** A helpful secondary label for the surah index. */
    @NonNull
    public String getSecondaryName(boolean arabicUi) {
        return arabicUi ? transliteration + " · " + englishMeaning : arabicName;
    }
}
