package org.Allah_Clock_Live_Wallpaper.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextPaint;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import org.Allah_Clock_Live_Wallpaper.R;

/**
 * The reader's palette: warm paper for the day, true black and soft parchment for the night.
 *
 * <p><b>Why this is not {@code values-night}:</b> the night reading theme is an in-app switch,
 * so it has to work whatever the system dark-mode setting is — a reader who keeps their phone
 * light all day still wants a dark Mushaf at 2 a.m., and one who runs the whole phone dark may
 * still prefer paper for reading. The two palettes therefore live side by side in
 * {@code colors.xml} and this class is the only thing that chooses between them.</p>
 *
 * <p>Instances are deliberately <b>mutable</b>: {@link #apply(Context, boolean)} re-reads the
 * selected palette into the same object, so every painter and view that holds a reference to
 * this theme follows a night-mode toggle immediately, without the column being rebuilt.</p>
 */
public final class QuranTheme {

    private boolean night;

    @ColorInt
    public int paper;
    @ColorInt
    public int surface;
    @ColorInt
    public int ink;
    @ColorInt
    public int body;
    @ColorInt
    public int muted;
    @ColorInt
    public int line;
    @ColorInt
    public int green;
    @ColorInt
    public int greenDark;
    @ColorInt
    public int softGreen;
    @ColorInt
    public int gold;
    @ColorInt
    public int verseHighlight;

    public QuranTheme(@NonNull Context context, boolean night) {
        apply(context, night);
    }

    /** Loads the day or the night palette into this instance. */
    public void apply(@NonNull Context context, boolean night) {
        this.night = night;
        this.paper = color(context, night ? R.color.quranNightPaper : R.color.quranPaper);
        this.surface = color(context, night ? R.color.quranNightSurface : R.color.quranSurface);
        this.ink = color(context, night ? R.color.quranNightInk : R.color.quranInk);
        this.body = color(context, night ? R.color.quranNightBody : R.color.quranBody);
        this.muted = color(context, night ? R.color.quranNightMuted : R.color.quranMuted);
        this.line = color(context, night ? R.color.quranNightLine : R.color.quranLine);
        this.green = color(context, night ? R.color.quranNightGreen : R.color.quranGreen);
        this.greenDark = color(context, night ? R.color.quranNightGreenDark : R.color.quranGreenDark);
        this.softGreen = color(context, night ? R.color.quranNightSoftGreen : R.color.quranSoftGreen);
        this.gold = color(context, night ? R.color.quranNightGold : R.color.quranGold);
        this.verseHighlight = color(context, R.color.quranVerseHighlight);
    }

    public boolean isNight() {
        return this.night;
    }

    /**
     * The text paint a column is set in: serif, anti-aliased and linearly filtered, with the
     * colour of the active palette.
     */
    @NonNull
    public TextPaint newTextPaint(float textSizePx) {
        TextPaint paint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG | TextPaint.LINEAR_TEXT_FLAG);
        paint.setTypeface(Typeface.SERIF);
        paint.setTextSize(textSizePx);
        paint.setColor(this.ink);
        paint.setSubpixelText(true);
        return paint;
    }

    @ColorInt
    private static int color(@NonNull Context context, int resource) {
        return ContextCompat.getColor(context, resource);
    }
}
