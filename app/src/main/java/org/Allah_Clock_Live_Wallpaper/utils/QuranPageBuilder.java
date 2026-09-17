package org.Allah_Clock_Live_Wallpaper.utils;

import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.Allah_Clock_Live_Wallpaper.model.QuranAyah;
import org.Allah_Clock_Live_Wallpaper.viewUtils.AyahBadgeSpan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Turns ayahs into the continuous run of text a Mushaf page is printed as, and remembers where
 * every verse sits inside it.
 *
 * <p>One builder instance produces one laid-out text: either a whole surah (the reader's
 * screen-by-screen paging) or one canonical Madani page (the Mushaf mode). Verses are separated
 * by the end-of-ayah ornament, which carries its own number, instead of by a box per ayah.</p>
 *
 * <p>The text is a {@link SpannableStringBuilder} shared by the paginator, the layouts and the
 * page views, so a bookmark tint added after the fact is picked up by every screen that shows
 * that verse without anything being rebuilt.</p>
 */
public final class QuranPageBuilder {

    /** Where one verse lives inside the built text. */
    public static final class Verse {
        public final int ayahNumber;
        public final int start;
        public final int markerEnd;

        Verse(int ayahNumber, int start, int markerEnd) {
            this.ayahNumber = ayahNumber;
            this.start = start;
            this.markerEnd = markerEnd;
        }

        public boolean contains(int offset) {
            return offset >= start && offset < markerEnd;
        }
    }

    /** Leading/trailing line spacing the reader has always used, in dp. */
    public static final float LINE_SPACING_EXTRA_DP = 12f;

    private final QuranTheme theme;
    private final SpannableStringBuilder text = new SpannableStringBuilder();
    private final List<Verse> verses = new ArrayList<>();
    private final Map<Integer, BackgroundColorSpan> tints = new LinkedHashMap<>();

    public QuranPageBuilder(@NonNull QuranTheme theme) {
        this.theme = theme;
    }

    /**
     * The Basmalah opening, centred like the first line of a printed page. It is always the
     * exact Uthmani text of 1:1 taken from the bundled asset — never typed by hand.
     */
    public void appendBasmalah(@NonNull QuranAyah basmalah) {
        appendCentred(basmalah.getText(), this.theme.gold);
    }

    /** A surah name heading, printed above the Basmalah on a page that opens a surah. */
    public void appendHeading(@NonNull CharSequence heading) {
        appendCentred(heading, this.theme.gold);
    }

    private void appendCentred(@NonNull CharSequence value, int colour) {
        if (this.text.length() > 0) {
            this.text.append("\n\n");
        }
        int from = this.text.length();
        this.text.append(value);
        int to = this.text.length();
        this.text.setSpan(new ForegroundColorSpan(colour), from, to,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        this.text.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), from, to,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        this.text.append("\n\n");
    }

    /** Appends one verse followed by its numbered end-of-ayah ornament. */
    public void appendAyah(@NonNull QuranAyah ayah) {
        int start = this.text.length();
        this.text.append(ayah.getText());
        this.text.append(' ');

        int markerStart = this.text.length();
        this.text.append('\u06DD'); // ARABIC END OF AYAH — drawn by the badge, not by the font
        this.text.setSpan(new AyahBadgeSpan(ayah.getAyahNumber(), this.theme), markerStart,
                markerStart + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        int markerEnd = this.text.length();
        this.text.append(' ');

        this.verses.add(new Verse(ayah.getAyahNumber(), start, markerEnd));
    }

    /** The text as it should be laid out. Never null; safe to span into. */
    @NonNull
    public SpannableStringBuilder getText() {
        return this.text;
    }

    @NonNull
    public List<Verse> getVerses() {
        return Collections.unmodifiableList(this.verses);
    }

    /** Paints the saved-verse tint over one verse, as the reader's bookmark state demands. */
    public void highlight(int ayahNumber, int colour) {
        Verse target = verse(ayahNumber);
        if (target == null || this.tints.containsKey(ayahNumber)) {
            return;
        }
        BackgroundColorSpan span = new BackgroundColorSpan(colour);
        this.text.setSpan(span, target.start, target.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        this.tints.put(ayahNumber, span);
    }

    /** Removes the tint of one verse, so clearing a save only repaints that verse. */
    public void clearHighlight(int ayahNumber) {
        BackgroundColorSpan span = this.tints.remove(ayahNumber);
        if (span != null) {
            this.text.removeSpan(span);
        }
    }

    /** Removes every bookmark tint, so the caller can repaint the saved set from scratch. */
    public void clearHighlights() {
        for (BackgroundColorSpan span : this.tints.values()) {
            this.text.removeSpan(span);
        }
        this.tints.clear();
    }

    @Nullable
    public Verse verse(int ayahNumber) {
        for (Verse verse : this.verses) {
            if (verse.ayahNumber == ayahNumber) {
                return verse;
            }
        }
        return null;
    }

    /** The verse whose text or ornament contains {@code offset}. */
    @Nullable
    public Verse verseAt(int offset) {
        for (Verse verse : this.verses) {
            if (verse.contains(offset)) {
                return verse;
            }
        }
        return null;
    }

    /**
     * The text paint every page is laid out with, at the given size in px. Built once per reader
     * and shared by the paginator and the views, so layout and drawing can never disagree.
     */
    @NonNull
    public static TextPaint textPaint(@NonNull QuranTheme theme, float textSizePx) {
        return theme.newTextPaint(textSizePx);
    }
}
