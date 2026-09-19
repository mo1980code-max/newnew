package org.Allah_Clock_Live_Wallpaper.model;

import androidx.annotation.NonNull;

/**
 * One immutable ayah of the bundled Uthmanic text.
 *
 * <p>{@link #getText()} is the exact upstream Uthmanic text, without presentation markers.
 * The display layer draws the ayah number in a replacement span, leaving this model and its
 * normalized search text independent of fonts and layout.</p>
 *
 * <p>The mushaf fields ({@code page}, {@code juz}, {@code line}) come from the official
 * companion metadata of the same repository: the 604 Madani pages, the 30 juzs and the position
 * of the ayah on its printed page. The same metadata flags the fifteen ayahs of prostration
 * (مواضع السجود) and whether each is obligatory or recommended; the reader draws that flag as the
 * mihrab marker a printed Mushaf carries in its margin.</p>
 */
public final class QuranAyah {

    /** The ayah is not one of the fifteen ayahs of prostration. */
    public static final int NO_SAJDAH = 0;

    private final int surahNumber;
    private final int ayahNumber;
    @NonNull
    private final String text;
    @NonNull
    private final String normalizedSearchText;
    private final int page;
    private final int juz;
    private final int line;
    private final int sajdahNumber;
    private final boolean sajdahObligatory;

    public QuranAyah(int surahNumber, int ayahNumber, @NonNull String text,
                     @NonNull String normalizedSearchText, int page, int juz, int line) {
        this(surahNumber, ayahNumber, text, normalizedSearchText, page, juz, line,
                NO_SAJDAH, false);
    }

    public QuranAyah(int surahNumber, int ayahNumber, @NonNull String text,
                     @NonNull String normalizedSearchText, int page, int juz, int line,
                     int sajdahNumber, boolean sajdahObligatory) {
        this.surahNumber = surahNumber;
        this.ayahNumber = ayahNumber;
        this.text = text;
        this.normalizedSearchText = normalizedSearchText;
        this.page = page;
        this.juz = juz;
        this.line = line;
        this.sajdahNumber = sajdahNumber;
        this.sajdahObligatory = sajdahObligatory;
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

    /** The Madani Mushaf page (1..604) on which this ayah is printed. */
    public int getMushafPage() {
        return page;
    }

    /** The traditional juz (1..30) this ayah belongs to. */
    public int getJuz() {
        return juz;
    }

    /**
     * The ayah's line number in the complete printed Mushaf layout (1..6236), as the source
     * metadata numbers it: one running line count through the whole book, not per page.
     */
    public int getLine() {
        return line;
    }

    /**
     * The prostration number (1..15) the bundled metadata assigns to this ayah, or
     * {@link #NO_SAJDAH} when the ayah carries no prostration.
     */
    public int getSajdahNumber() {
        return sajdahNumber;
    }

    /** True exactly when this ayah is one of the fifteen ayahs of prostration. */
    public boolean isSajdahAyah() {
        return sajdahNumber != NO_SAJDAH;
    }

    /**
     * Whether the prostration of this ayah is obligatory (عزيمة) rather than recommended:
     * four of the fifteen are obligatory, the other eleven are recommended.
     */
    public boolean isSajdahObligatory() {
        return sajdahObligatory;
    }

    /** Stable, compact key used by the local bookmark store. */
    @NonNull
    public String getKey() {
        return surahNumber + ":" + ayahNumber;
    }
}
