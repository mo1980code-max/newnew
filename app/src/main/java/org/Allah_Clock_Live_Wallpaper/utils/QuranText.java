package org.Allah_Clock_Live_Wallpaper.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Small, testable text helpers shared by the Quran surfaces.
 *
 * <p>The end-of-ayah number is printed in Arabic-Indic digits exactly like a Mushaf prints it,
 * and the surah headings are the official names of the companion metadata with the decorative
 * leading "سورة" dropped, so the header reads "البقرة" the way a printed Mushaf titles it.</p>
 */
public final class QuranText {

    private QuranText() {
    }

    /** 6236 → ٦٢٣٦, the way an ayah number is printed in its end-of-ayah glyph. */
    @NonNull
    public static String arabicIndic(int value) {
        String source = String.valueOf(value);
        StringBuilder out = new StringBuilder(source.length());
        for (int index = 0; index < source.length(); index++) {
            out.append((char) ('\u0660' + (source.charAt(index) - '0')));
        }
        return out.toString();
    }

    /**
     * Closes one raw Uthmanic text with its built-in end-of-ayah glyph: the ARABIC END OF AYAH
     * character (U+06DD) directly followed by the ayah number in Arabic-Indic digits. This is the
     * same shape the official edition's rendered text uses, so the reader prints the glyph and
     * the number as plain characters with no manual positioning.
     */
    @NonNull
    public static String withEndGlyph(@NonNull String rawText, int ayahNumber) {
        return rawText + '\u06DD' + arabicIndic(ayahNumber);
    }

    /**
     * The display form of an official surah name: the leading "سورة" (in any spelling, with or
     * without vowel marks) is dropped and the remaining vowel marks are removed, so the heading
     * shows the clean name a printed Mushaf uses. The Quran text itself is never touched.
     */
    @NonNull
    public static String displaySurahName(@Nullable String officialName) {
        if (officialName == null) {
            return "";
        }
        String value = officialName.trim();
        // Drop the leading "سورة" token in every spelling: the four letters in order, with any
        // diacritic or tatweel allowed between or after them (سُوْرَةُ, سُورَة, سورة, …).
        final String prefix = "سورة";
        int letters = 0;
        int cursor = 0;
        while (cursor < value.length() && letters < prefix.length()) {
            int codePoint = value.codePointAt(cursor);
            int advance = Character.charCount(codePoint);
            if (isMark(codePoint)) {
                cursor += advance; // a mark inside the prefix, e.g. the damma of سُوْرَةُ
                continue;
            }
            if (codePoint != prefix.charAt(letters)) {
                break;
            }
            letters++;
            cursor += advance;
        }
        if (letters == prefix.length()) {
            value = value.substring(cursor).trim();
        }
        if (value.isEmpty()) {
            return officialName.trim();
        }
        StringBuilder out = new StringBuilder(value.length());
        for (int offset = 0; offset < value.length();) {
            int codePoint = value.codePointAt(offset);
            offset += Character.charCount(codePoint);
            if (!isMark(codePoint)) {
                out.appendCodePoint(codePoint);
            }
        }
        String plain = out.toString().replace("\u0640", ""); // tatweel
        return plain.trim().isEmpty() ? officialName.trim() : plain.trim();
    }

    /** Arabic diacritics, the small waqf marks and the dagger alif are dropped from names. */
    private static boolean isMark(int codePoint) {
        return (codePoint >= 0x064B && codePoint <= 0x065F)   // fathatan … wasla
                || (codePoint >= 0x0670 && codePoint <= 0x0670) // dagger alif
                || (codePoint >= 0x06D6 && codePoint <= 0x06ED) // small high marks
                || codePoint == 0x0654; // hamza-above combining mark
    }
}
