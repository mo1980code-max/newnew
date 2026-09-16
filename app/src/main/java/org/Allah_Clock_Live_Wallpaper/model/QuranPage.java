package org.Allah_Clock_Live_Wallpaper.model;

import androidx.annotation.NonNull;

/**
 * One canonical Madani Mushaf page boundary.
 *
 * <p>The reader reflows the bundled Uthmani text for the device instead of claiming an exact
 * printed-page glyph layout. These boundaries still let readers browse the traditional 604-page
 * partition and return to a meaningful page after reopening the app.</p>
 */
public final class QuranPage {

    private final int number;
    private final int startIndex;
    private final int endIndex;
    @NonNull
    private final QuranAyah firstAyah;
    @NonNull
    private final QuranAyah lastAyah;

    public QuranPage(int number, int startIndex, int endIndex, @NonNull QuranAyah firstAyah,
                     @NonNull QuranAyah lastAyah) {
        this.number = number;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.firstAyah = firstAyah;
        this.lastAyah = lastAyah;
    }

    public int getNumber() {
        return number;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    @NonNull
    public QuranAyah getFirstAyah() {
        return firstAyah;
    }

    @NonNull
    public QuranAyah getLastAyah() {
        return lastAyah;
    }
}
