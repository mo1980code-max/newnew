package org.Allah_Clock_Live_Wallpaper.utils;

import android.text.SpannableStringBuilder;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import org.Allah_Clock_Live_Wallpaper.model.QuranAyah;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** One flowing page/surah fragment, with offsets for verse taps, bookmarks and resume. */
public final class QuranParagraph {
    public final SpannableStringBuilder text = new SpannableStringBuilder();
    public final List<QuranAyah> ayahs;
    private final int[] starts;
    private final int[] ends;

    public QuranParagraph(@NonNull List<QuranAyah> ayahs, float density,
                          @ColorInt int markerColor) {
        if (ayahs.isEmpty()) {
            throw new IllegalArgumentException("A Quran paragraph must contain ayahs");
        }
        this.ayahs = Collections.unmodifiableList(new ArrayList<>(ayahs));
        starts = new int[ayahs.size()];
        ends = new int[ayahs.size()];
        for (int i = 0; i < ayahs.size(); i++) {
            // The only inter-ayah separator is a space, never a newline or separate TextView.
            if (i > 0) {
                text.append(' ');
            }
            starts[i] = text.length();
            QuranAyah ayah = ayahs.get(i);
            QuranText.appendAyah(text, ayah.getText(), ayah.getAyahNumber(), density, markerColor);
            ends[i] = text.length();
        }
    }

    public int start(int index) {
        return starts[index];
    }

    public int end(int index) {
        return ends[index];
    }

    /** A wrapped line can begin halfway through a verse; keep that verse as the resume target. */
    @NonNull
    public QuranAyah ayahAtOffset(int offset) {
        for (int i = 0; i < ends.length; i++) {
            if (offset < ends[i]) {
                return ayahs.get(i);
            }
        }
        return ayahs.get(ayahs.size() - 1);
    }

    public int offsetOf(int surah, int ayah) {
        for (int i = 0; i < ayahs.size(); i++) {
            QuranAyah entry = ayahs.get(i);
            if (entry.getSurahNumber() == surah && entry.getAyahNumber() == ayah) {
                return starts[i];
            }
        }
        return -1;
    }
}
