package org.Allah_Clock_Live_Wallpaper.utils;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.style.ReplacementSpan;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/** A single, indivisible ayah marker; never relies on font support for U+06DD ligatures. */
public final class AyahNumberSpan extends ReplacementSpan {

    public static final int PRIMARY_GREEN = 0xFF1B4332;

    private final String number;
    private final float density;
    private final int color;
    // Private paint: drawing the marker must not change the surrounding Quran text's paint.
    private final Paint markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public AyahNumberSpan(int ayahNumber, float density, @ColorInt int color) {
        if (ayahNumber < 1 || density <= 0f) {
            throw new IllegalArgumentException("An ayah number and density must be positive");
        }
        this.number = QuranText.arabicIndic(ayahNumber);
        this.density = density;
        this.color = color;
    }

    /** Recalculate from the current text paint so reader zoom and system font scaling work. */
    private float diameter(@NonNull Paint textPaint) {
        markerPaint.set(textPaint);
        markerPaint.setAntiAlias(true);
        markerPaint.setTypeface(Typeface.DEFAULT);
        markerPaint.setTextSize(textPaint.getTextSize() * 0.55f);
        markerPaint.setTextScaleX(1f);
        markerPaint.setTextSkewX(0f);
        markerPaint.setLetterSpacing(0f);
        markerPaint.setFakeBoldText(false);
        markerPaint.setStyle(Paint.Style.FILL);
        markerPaint.setTextAlign(Paint.Align.CENTER);
        markerPaint.setColor(color);
        markerPaint.clearShadowLayer();
        Paint.FontMetrics digits = markerPaint.getFontMetrics();
        // The diagonal of the number's bounding rectangle must fit INSIDE the circle,
        // including for two/three-digit numbers (e.g. ١٠ and ٢٨٦).
        float contentDiameter = (float) Math.hypot(markerPaint.measureText(number),
                digits.descent - digits.ascent);
        return Math.max(textPaint.getTextSize() * 1.1f,
                contentDiameter + 4f * density + 2f * strokeWidth());
    }

    private float strokeWidth() {
        return 1.5f * density;
    }

    private float sidePadding(@NonNull Paint paint) {
        return Math.max(3f * density, paint.getTextSize() * 0.12f);
    }

    @Override
    public int getSize(@NonNull Paint paint, @NonNull CharSequence text, int start, int end,
                       @Nullable Paint.FontMetricsInt fm) {
        float diameter = diameter(paint);
        if (fm != null) {
            paint.getFontMetricsInt(fm);
            float centerY = (paint.ascent() + paint.descent()) / 2f;
            // Reserve the complete border in line metrics; never shrink the Arabic line.
            fm.ascent = Math.min(fm.ascent, (int) Math.floor(centerY - diameter / 2f));
            fm.descent = Math.max(fm.descent, (int) Math.ceil(centerY + diameter / 2f));
            fm.top = Math.min(fm.top, fm.ascent);
            fm.bottom = Math.max(fm.bottom, fm.descent);
        }
        return (int) Math.ceil(diameter + 2f * sidePadding(paint));
    }

    @Override
    public void draw(@NonNull Canvas canvas, @NonNull CharSequence text, int start, int end,
                     float x, int top, int y, int bottom, @NonNull Paint paint) {
        float diameter = diameter(paint);
        float width = (float) Math.ceil(diameter + 2f * sidePadding(paint));
        // Android supplies the replacement's left edge even in an RTL paragraph.
        float centerX = x + width / 2f;
        float centerY = y + (paint.ascent() + paint.descent()) / 2f;
        markerPaint.setStyle(Paint.Style.STROKE);
        markerPaint.setStrokeWidth(strokeWidth());
        canvas.drawCircle(centerX, centerY, (diameter - strokeWidth()) / 2f, markerPaint);

        markerPaint.setStyle(Paint.Style.FILL);
        Paint.FontMetrics digits = markerPaint.getFontMetrics();
        float numberBaseline = centerY - (digits.ascent + digits.descent) / 2f;
        canvas.drawText(number, centerX, numberBaseline, markerPaint);
    }
}
