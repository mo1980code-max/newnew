package org.Allah_Clock_Live_Wallpaper.model;

import androidx.annotation.NonNull;

/**
 * One immutable ayah of the bundled Uthmanic text.
 *
 * <p>{@link #getText()} is the exact upstream Uthmanic text closed by its built-in end-of-ayah
 * glyph: the ARABIC END OF AYAH character (U+06DD) immediately followed by the ayah's number in
 * Arabic-Indic digits, the way the official edition's rendered text carries it. The glyph and the
 * digits are ordinary text characters, so the system font draws them — no ornament is painted at
 * guessed coordinates.</p>
 *
 * <p>The mushaf fields ({@code page}, {@code juz}, {@code line}) come from the official
 * companion metadata of the same repository: the 604 Madani pages, the 30 juzs and the position
 * of the ayah on its printed page.</p>
 */
public final class QuranAyah {

    private final int surahNumber;
    private final int ayahNumber;
    @NonNull
    private final String text;
    @NonNull
    private final String normalizedSearchText;
    private final int page;
    private final int juz;
    private final int line;

    public QuranAyah(int surahNumber, int ayahNumber, @NonNull String text,
                     @NonNull String normalizedSearchText, int page, int juz, int line) {
        this.surahNumber = surahNumber;
        this.ayahNumber = ayahNumber;
        this.text = text;
        this.normalizedSearchText = normalizedSearchText;
        this.page = page;
        this.juz = juz;
        this.line = line;
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

    /** Stable, compact key used by the local bookmark store. */
    @NonNull
    public String getKey() {
        return surahNumber + ":" + ayahNumber;
    }
}
