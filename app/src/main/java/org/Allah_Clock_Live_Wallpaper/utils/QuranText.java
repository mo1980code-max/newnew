package org.Allah_Clock_Live_Wallpaper.utils;

import android.text.SpannableStringBuilder;
import android.text.Spanned;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.Allah_Clock_Live_Wallpaper.model.QuranAyah;

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
     * Appends an intact verse and one atomic marker to an RTL-compatible text run. The
     * non-breaking space keeps the marker with the verse's last word; the object replacement
     * character is bidi-neutral, so Android lays it out in the surrounding Arabic direction.
     * The span itself supplies both the border and digits, not a Unicode ornament + number.
     */
    public static void appendAyah(@NonNull SpannableStringBuilder out, @NonNull String rawText,
                                  int ayahNumber, float density, @ColorInt int markerColor) {
        appendAyah(out, rawText, ayahNumber, density, markerColor, QuranAyah.NO_SAJDAH,
                SajdahMarkerSpan.PRIMARY_GOLD);
    }

    /**
     * Appends an intact verse, its end-of-ayah number and — for the fifteen ayahs of
     * prostration — the mihrab marker that a printed Mushaf carries for them.
     *
     * <p>{@code sajdahNumber} is the prostration number of the bundled metadata, or
     * {@link QuranAyah#NO_SAJDAH} for the other 6,221 ayahs. The marker is a second atomic
     * replacement drawn by {@link SajdahMarkerSpan}, so it scales with the reader's text size and
     * never depends on the device font carrying U+06E9.</p>
     */
    public static void appendAyah(@NonNull SpannableStringBuilder out, @NonNull String rawText,
                                  int ayahNumber, float density, @ColorInt int markerColor,
                                  int sajdahNumber, @ColorInt int sajdahColor) {
        out.append(rawText).append('\u00A0');
        int markerStart = out.length();
        out.append('\uFFFC');
        out.setSpan(new AyahNumberSpan(ayahNumber, density, markerColor), markerStart,
                out.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (sajdahNumber != QuranAyah.NO_SAJDAH) {
            int sajdahStart = out.length();
            out.append('\uFFFC');
            out.setSpan(new SajdahMarkerSpan(density, sajdahColor), sajdahStart, out.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    @NonNull
    public static SpannableStringBuilder withAyahNumber(@NonNull String rawText, int ayahNumber,
                                                        float density, @ColorInt int markerColor) {
        SpannableStringBuilder out = new SpannableStringBuilder();
        appendAyah(out, rawText, ayahNumber, density, markerColor);
        return out;
    }

    /**
     * One ayah on its own — the way the prostration dialog quotes it — with its end-of-ayah
     * number and, for the fifteen ayahs of prostration, its mihrab marker.
     */
    @NonNull
    public static SpannableStringBuilder withAyahNumber(@NonNull String rawText, int ayahNumber,
                                                        float density, @ColorInt int markerColor,
                                                        int sajdahNumber,
                                                        @ColorInt int sajdahColor) {
        SpannableStringBuilder out = new SpannableStringBuilder();
        appendAyah(out, rawText, ayahNumber, density, markerColor, sajdahNumber, sajdahColor);
        return out;
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
