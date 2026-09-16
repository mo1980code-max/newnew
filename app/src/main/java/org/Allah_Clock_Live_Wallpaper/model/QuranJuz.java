package org.Allah_Clock_Live_Wallpaper.model;

import androidx.annotation.NonNull;

/** One of the thirty traditional Quran juz (parts), with its canonical first ayah and page. */
public final class QuranJuz {

    private final int number;
    private final int pageNumber;
    @NonNull
    private final QuranAyah firstAyah;

    public QuranJuz(int number, int pageNumber, @NonNull QuranAyah firstAyah) {
        this.number = number;
        this.pageNumber = pageNumber;
        this.firstAyah = firstAyah;
    }

    public int getNumber() {
        return number;
    }

    /** The Madani-page containing the first ayah of this juz. */
    public int getPageNumber() {
        return pageNumber;
    }

    @NonNull
    public QuranAyah getFirstAyah() {
        return firstAyah;
    }
}
