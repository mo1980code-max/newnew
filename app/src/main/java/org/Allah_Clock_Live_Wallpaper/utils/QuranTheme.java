package org.Allah_Clock_Live_Wallpaper.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextPaint;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import org.Allah_Clock_Live_Wallpaper.R;

/**
 * The reader's palette and its background: three paper washes for the day (ورق · رَقّ · زيتوني)
 * and three deep washes for the night (أخضر · ليل · أزرق).
 *
 * <p><b>Why this is not {@code values-night}:</b> the reading theme is an in-app switch, so it has
 * to work whatever the system dark-mode setting is — a reader who keeps their phone light all day
 * still wants a dark Mushaf at 2 a.m., and one who runs the whole phone dark may still prefer
 * paper for reading. Every palette therefore lives side by side in {@code colors.xml}, every
 * background is a drawable in {@code res/drawable}, and this class is the only thing that chooses
 * between them.</p>
 *
 * <p>Instances are deliberately <b>mutable</b>: {@link #apply(Context, int)} re-reads the selected
 * palette into the same object, so every painter and view that holds a reference to this theme
 * follows a background change immediately, without the column being rebuilt and without losing
 * the reader's place.</p>
 */
public final class QuranTheme {

    /** Near-white paper: the default reading wash. */
    public static final int STYLE_PAPER = 1;
    /** Warm sepia parchment. */
    public static final int STYLE_PARCHMENT = 2;
    /** Soft olive paper. */
    public static final int STYLE_OLIVE = 3;
    /** The deep green of a printed Mushaf. */
    public static final int STYLE_GREEN = 4;
    /** True black with warm ink. */
    public static final int STYLE_NIGHT = 5;
    /** Deep midnight blue. */
    public static final int STYLE_MIDNIGHT = 6;

    /** The wash a first launch reads in, and the wash the night switch returns to. */
    public static final int DEFAULT_STYLE = STYLE_PAPER;

    /** Every selectable wash, in the order the picker lists them. */
    public static final int[] STYLES = {
            STYLE_PAPER, STYLE_PARCHMENT, STYLE_OLIVE,
            STYLE_GREEN, STYLE_NIGHT, STYLE_MIDNIGHT,
    };

    private boolean night;
    private int style = DEFAULT_STYLE;

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

    /** The wash's own background drawable: gradient, vignette and Mushaf frame in one layer. */
    @DrawableRes
    public int background;

    public QuranTheme(@NonNull Context context, boolean night) {
        this(context, night ? STYLE_NIGHT : DEFAULT_STYLE);
    }

    public QuranTheme(@NonNull Context context, int style) {
        apply(context, style);
    }

    /** Loads one wash's palette and background into this instance. */
    public void apply(@NonNull Context context, int style) {
        this.style = normalize(style);
        this.night = isNightStyle(this.style);
        this.paper = color(context, paperColor(this.style));
        this.surface = color(context, surfaceColor(this.style));
        this.ink = color(context, inkColor(this.style));
        this.body = color(context, bodyColor(this.style));
        this.muted = color(context, mutedColor(this.style));
        this.line = color(context, lineColor(this.style));
        this.green = color(context, greenColor(this.style));
        this.greenDark = color(context, greenDarkColor(this.style));
        this.softGreen = color(context, softGreenColor(this.style));
        this.gold = color(context, goldColor(this.style));
        this.verseHighlight = color(context, R.color.quranVerseHighlight);
        this.background = backgroundOf(this.style);
    }

    /** The two-way switch the reader's night button has always been: paper ⇄ night. */
    public void apply(@NonNull Context context, boolean night) {
        apply(context, night ? STYLE_NIGHT : DEFAULT_STYLE);
    }

    public boolean isNight() {
        return this.night;
    }

    /** The wash this theme is painted in ({@code STYLE_*}). */
    public int getStyle() {
        return this.style;
    }

    /** True for the three deep washes, whose ink is light and whose icons read gold. */
    public static boolean isNightStyle(int style) {
        switch (normalize(style)) {
            case STYLE_GREEN:
            case STYLE_NIGHT:
            case STYLE_MIDNIGHT:
                return true;
            default:
                return false;
        }
    }

    /** The wash a saved preference names, falling back to paper when the value is unknown. */
    public static int normalize(int style) {
        for (int candidate : STYLES) {
            if (candidate == style) {
                return candidate;
            }
        }
        return DEFAULT_STYLE;
    }

    /** The wash the night button toggles to from the current one. */
    public int nextNightStyle() {
        return isNight() ? DEFAULT_STYLE : STYLE_NIGHT;
    }

    /** The picker's own name of a wash, in the app's current language. */
    @StringRes
    public static int nameOf(int style) {
        switch (normalize(style)) {
            case STYLE_PARCHMENT:
                return R.string.quran_bg_parchment;
            case STYLE_OLIVE:
                return R.string.quran_bg_olive;
            case STYLE_GREEN:
                return R.string.quran_bg_green;
            case STYLE_NIGHT:
                return R.string.quran_bg_night;
            case STYLE_MIDNIGHT:
                return R.string.quran_bg_midnight;
            default:
                return R.string.quran_bg_paper;
        }
    }

    /** The drawable a wash paints the reader's page with. */
    @DrawableRes
    public static int backgroundOf(int style) {
        switch (normalize(style)) {
            case STYLE_PARCHMENT:
                return R.drawable.quran_bg_parchment;
            case STYLE_OLIVE:
                return R.drawable.quran_bg_olive;
            case STYLE_GREEN:
                return R.drawable.quran_bg_green;
            case STYLE_NIGHT:
                return R.drawable.quran_bg_night;
            case STYLE_MIDNIGHT:
                return R.drawable.quran_bg_midnight;
            default:
                return R.drawable.quran_bg_paper;
        }
    }

    @ColorRes
    private static int paperColor(int style) {
        switch (style) {
            case STYLE_PARCHMENT:
                return R.color.quranParchmentPaper;
            case STYLE_OLIVE:
                return R.color.quranOlivePaper;
            case STYLE_GREEN:
                return R.color.quranEmeraldPaper;
            case STYLE_NIGHT:
                return R.color.quranNightPaper;
            case STYLE_MIDNIGHT:
                return R.color.quranMidnightPaper;
            default:
                return R.color.quranPaper;
        }
    }

    @ColorRes
    private static int surfaceColor(int style) {
        switch (style) {
            case STYLE_PARCHMENT:
                return R.color.quranParchmentSurface;
            case STYLE_OLIVE:
                return R.color.quranOliveSurface;
            case STYLE_GREEN:
                return R.color.quranEmeraldSurface;
            case STYLE_NIGHT:
                return R.color.quranNightSurface;
            case STYLE_MIDNIGHT:
                return R.color.quranMidnightSurface;
            default:
                return R.color.quranSurface;
        }
    }

    @ColorRes
    private static int inkColor(int style) {
        switch (style) {
            case STYLE_PARCHMENT:
                return R.color.quranParchmentInk;
            case STYLE_OLIVE:
                return R.color.quranOliveInk;
            case STYLE_GREEN:
                return R.color.quranEmeraldInk;
            case STYLE_NIGHT:
                return R.color.quranNightInk;
            case STYLE_MIDNIGHT:
                return R.color.quranMidnightInk;
            default:
                return R.color.quranInk;
        }
    }

    @ColorRes
    private static int bodyColor(int style) {
        switch (style) {
            case STYLE_PARCHMENT:
                return R.color.quranParchmentBody;
            case STYLE_OLIVE:
                return R.color.quranOliveBody;
            case STYLE_GREEN:
                return R.color.quranEmeraldBody;
            case STYLE_NIGHT:
                return R.color.quranNightBody;
            case STYLE_MIDNIGHT:
                return R.color.quranMidnightBody;
            default:
                return R.color.quranBody;
        }
    }

    @ColorRes
    private static int mutedColor(int style) {
        switch (style) {
            case STYLE_PARCHMENT:
                return R.color.quranParchmentMuted;
            case STYLE_OLIVE:
                return R.color.quranOliveMuted;
            case STYLE_GREEN:
                return R.color.quranEmeraldMuted;
            case STYLE_NIGHT:
                return R.color.quranNightMuted;
            case STYLE_MIDNIGHT:
                return R.color.quranMidnightMuted;
            default:
                return R.color.quranMuted;
        }
    }

    @ColorRes
    private static int lineColor(int style) {
        switch (style) {
            case STYLE_PARCHMENT:
                return R.color.quranParchmentLine;
            case STYLE_OLIVE:
                return R.color.quranOliveLine;
            case STYLE_GREEN:
                return R.color.quranEmeraldLine;
            case STYLE_NIGHT:
                return R.color.quranNightLine;
            case STYLE_MIDNIGHT:
                return R.color.quranMidnightLine;
            default:
                return R.color.quranLine;
        }
    }

    @ColorRes
    private static int greenColor(int style) {
        switch (style) {
            case STYLE_PARCHMENT:
                return R.color.quranParchmentGreen;
            case STYLE_OLIVE:
                return R.color.quranOliveGreen;
            case STYLE_GREEN:
                return R.color.quranEmeraldGreen;
            case STYLE_NIGHT:
                return R.color.quranNightGreen;
            case STYLE_MIDNIGHT:
                return R.color.quranMidnightGreen;
            default:
                return R.color.quranGreen;
        }
    }

    @ColorRes
    private static int greenDarkColor(int style) {
        switch (style) {
            case STYLE_PARCHMENT:
                return R.color.quranParchmentGreenDark;
            case STYLE_OLIVE:
                return R.color.quranOliveGreenDark;
            case STYLE_GREEN:
                return R.color.quranEmeraldGreenDark;
            case STYLE_NIGHT:
                return R.color.quranNightGreenDark;
            case STYLE_MIDNIGHT:
                return R.color.quranMidnightGreenDark;
            default:
                return R.color.quranGreenDark;
        }
    }

    @ColorRes
    private static int softGreenColor(int style) {
        switch (style) {
            case STYLE_PARCHMENT:
                return R.color.quranParchmentSoftGreen;
            case STYLE_OLIVE:
                return R.color.quranOliveSoftGreen;
            case STYLE_GREEN:
                return R.color.quranEmeraldSoftGreen;
            case STYLE_NIGHT:
                return R.color.quranNightSoftGreen;
            case STYLE_MIDNIGHT:
                return R.color.quranMidnightSoftGreen;
            default:
                return R.color.quranSoftGreen;
        }
    }

    @ColorRes
    private static int goldColor(int style) {
        switch (style) {
            case STYLE_PARCHMENT:
                return R.color.quranParchmentGold;
            case STYLE_OLIVE:
                return R.color.quranOliveGold;
            case STYLE_GREEN:
                return R.color.quranEmeraldGold;
            case STYLE_NIGHT:
                return R.color.quranNightGold;
            case STYLE_MIDNIGHT:
                return R.color.quranMidnightGold;
            default:
                return R.color.quranGold;
        }
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
    private static int color(@NonNull Context context, @ColorRes int resource) {
        return ContextCompat.getColor(context, resource);
    }
}
