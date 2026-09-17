package org.Allah_Clock_Live_Wallpaper.utils;

import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextDirectionHeuristics;
import android.text.TextPaint;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Cuts a laid-out run of verses into screen-sized pages, so the reader can <b>flip</b> through a
 * surah the way a printed Mushaf is flipped instead of scrolling one endless column.
 *
 * <p>The measurement is done once, on the very same {@link StaticLayout} parameters the page
 * views draw with (same paint, same width, same line spacing, same text direction), and the cut
 * points are read back from the measured line boxes. That is what makes a "page" here exactly
 * what fits on the screen: no guessed character counts, no measurement drift between the
 * paginator and the renderer.</p>
 *
 * <p>Verses may straddle a page boundary — that is how a Mushaf works; the ornament always stays
 * with its own verse.</p>
 */
public final class QuranPaginator {

    /** One screen-sized page: a slice of the built text plus the verses it covers. */
    public static final class Screen {
        public final int start;
        public final int end;
        public final int firstAyah;
        public final int lastAyah;

        Screen(int start, int end, int firstAyah, int lastAyah) {
            this.start = start;
            this.end = end;
            this.firstAyah = firstAyah;
            this.lastAyah = lastAyah;
        }
    }

    private QuranPaginator() {
    }

    /**
     * @param text            the built surah text ({@link QuranPageBuilder#getText()})
     * @param verses          the verse ranges inside it
     * @param paint           the shared text paint
     * @param width           usable text width in px
     * @param height          usable text height in px
     * @param lineSpacingExtra extra leading in px
     * @return one screen per fragment of the text; always at least one, never empty slices
     */
    @NonNull
    public static List<Screen> slice(@NonNull CharSequence text,
                                     @NonNull List<QuranPageBuilder.Verse> verses,
                                     @NonNull TextPaint paint, int width, int height,
                                     float lineSpacingExtra) {
        List<Screen> screens = new ArrayList<>();
        if (text.length() == 0 || width <= 0 || height <= 0) {
            screens.add(new Screen(0, Math.max(0, text.length()), 0, 0));
            return Collections.unmodifiableList(screens);
        }

        StaticLayout layout = build(text, 0, text.length(), paint, width, lineSpacingExtra);
        int lines = layout.getLineCount();
        int line = 0;
        while (line < lines) {
            int top = layout.getLineTop(line);
            int last = line;
            // Grow the page line by line while the next line still fits inside the viewport.
            while (last + 1 < lines && layout.getLineBottom(last + 1) - top <= height) {
                last++;
            }
            int start = layout.getLineStart(line);
            int end = layout.getLineEnd(last);
            while (end > start && text.charAt(end - 1) == '\n') {
                end--;
            }
            screens.add(new Screen(start, end, firstAyah(verses, start), lastAyah(verses, end)));
            line = last + 1;
        }
        if (screens.isEmpty()) {
            screens.add(new Screen(0, text.length(), 0, 0));
        }
        return Collections.unmodifiableList(screens);
    }

    /** The layout a page is drawn with; the same parameters {@link #slice} measured with. */
    @NonNull
    public static StaticLayout build(@NonNull CharSequence text, int start, int end,
                                     @NonNull TextPaint paint, int width,
                                     float lineSpacingExtra) {
        StaticLayout.Builder builder = StaticLayout.Builder
                .obtain(text, start, end, paint, Math.max(1, width))
                .setLineSpacing(lineSpacingExtra, 1f)
                .setIncludePad(false)
                // Quranic text is always right-to-left, whatever the UI language is.
                .setTextDirection(TextDirectionHeuristics.RTL)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // The inter-word justification a printed page uses; ignored where unsupported.
            builder.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
        }
        return builder.build();
    }

    private static int firstAyah(@NonNull List<QuranPageBuilder.Verse> verses, int offset) {
        for (QuranPageBuilder.Verse verse : verses) {
            if (verse.markerEnd > offset) {
                return verse.ayahNumber;
            }
        }
        return verses.isEmpty() ? 0 : verses.get(verses.size() - 1).ayahNumber;
    }

    private static int lastAyah(@NonNull List<QuranPageBuilder.Verse> verses, int offset) {
        int last = verses.isEmpty() ? 0 : verses.get(0).ayahNumber;
        for (QuranPageBuilder.Verse verse : verses) {
            if (verse.start < offset) {
                last = verse.ayahNumber;
            }
        }
        return last;
    }
}
