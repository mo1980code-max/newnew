package org.Allah_Clock_Live_Wallpaper.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextPaint;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import org.Allah_Clock_Live_Wallpaper.R;

/**
 * The reader's palette: paper for the day, deep slate and warm cream for the night.
 *
 * <p><b>Why this is not {@code values-night}:</b> the night reading theme is an in-app switch,
 * so it has to work whatever the system dark-mode setting is — a reader who keeps their phone
 * light all day still wants a dark Mushaf at 2 a.m., and one who runs the whole phone dark may
 * still prefer paper for reading. The two palettes therefore live side by side in
 * {@code colors.xml} and this class is the only thing that chooses between them.</p>
 *
 * <p>Instances are deliberately <b>mutable</b>: {@link #apply(Context, boolean)} re-reads the
 * selected palette into the same object, so every painter, span and view that holds a reference
 * to this theme (including the end-of-ayah badges already laid out inside the page text) follows
 * a night-mode toggle immediately, without the page being rebuilt or repaginated.</p>
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
    /** Fill behind the ayah number inside the end-of-ayah ornament. */
    @ColorInt
    public int badgeFill;
    /** The digits themselves, inside the ornament. */
    @ColorInt
    public int badgeNumber;
    /** Top action bar tone; darker than the paper so the controls read as a separate strip. */
    @ColorInt
    public int iconBar;

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
        this.badgeFill = color(context,
                night ? R.color.ayahOrnamentFillNight : R.color.ayahOrnamentFill);
        this.badgeNumber = color(context,
                night ? R.color.ayahOrnamentNumberNight : R.color.ayahOrnamentNumber);
        this.iconBar = color(context, night ? R.color.quranNightIconBar : R.color.quranPaper);
    }

    public boolean isNight() {
        return this.night;
    }

    /**
     * The text paint the page is laid out with. One instance is shared by the layout, the
     * paginator and the page views, so nothing can drift out of sync: colours are re-read from
     * this theme and the size is set by the reader.
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
