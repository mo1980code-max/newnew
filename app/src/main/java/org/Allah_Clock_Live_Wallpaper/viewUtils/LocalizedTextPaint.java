package org.Allah_Clock_Live_Wallpaper.viewUtils;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextPaint;

import org.Allah_Clock_Live_Wallpaper.utils.LocaleHelper;

/**
 * TextPaint for the date vocabulary the clock faces draw: weekday, day number, month and
 * the AM/PM marker.
 *
 * <p>Two things break the moment the UI language is Arabic:</p>
 * <ol>
 *   <li>the bundled display faces (Montserrat, Rubik, Open Sans, Pathway Gothic, Dela Gothic)
 *       contain no Arabic glyphs at all, so Arabic would silently fall back glyph by glyph and
 *       look mismatched next to the rest of the dial;</li>
 *   <li>Arabic weekday and month names are wider than {@code SUN} / {@code SEP}, while the
 *       designs place those words in fixed slots around the dial.</li>
 * </ol>
 *
 * <p>Both are solved here, in one class, instead of editing the 21 design branches: the
 * typeface is swapped for a system face that always shapes Arabic, and the text size is scaled
 * down so the localized word still fits its original slot. Time digits deliberately keep the
 * design typeface and Western digits — they are the graphic identity of each clock.</p>
 *
 * <p>The locale is read on every call, so switching the language takes effect on the next
 * frame without recreating the view.</p>
 */
final class LocalizedTextPaint extends TextPaint {

    private final Context context;
    private final float arabicScale;

    /**
     * @param arabicScale text-size multiplier applied only while the UI is Arabic
     *                    (1.0f keeps the original size, e.g. for the one-letter ص / م markers).
     */
    LocalizedTextPaint(Context context, float arabicScale) {
        super();
        this.context = context.getApplicationContext();
        this.arabicScale = arabicScale;
    }

    @Override
    public void setTextSize(float textSize) {
        super.setTextSize(LocaleHelper.isArabic(this.context)
                ? textSize * this.arabicScale
                : textSize);
    }

    @Override
    public Typeface setTypeface(Typeface typeface) {
        if (!LocaleHelper.isArabic(this.context)) {
            return super.setTypeface(typeface);
        }
        // Keep the requested weight (bold stays bold), drop the Latin-only family.
        int style = typeface == null ? Typeface.NORMAL : typeface.getStyle();
        return super.setTypeface(Typeface.create(Typeface.SANS_SERIF, style));
    }
}
