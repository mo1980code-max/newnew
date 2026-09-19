package org.Allah_Clock_Live_Wallpaper.utils;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.style.ReplacementSpan;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * The mihrab marker of an ayah of prostration (موضع سجدة), drawn the way a printed Mushaf carries
 * it in its margin: a pointed arch — the shape of a mihrab — standing after the end-of-ayah
 * number.
 *
 * <p>It is drawn from a {@link Path} rather than printed as U+06E9 (۩) on purpose: that code point
 * is missing from most device fonts, and a missing glyph would print as a box or as nothing at
 * all on exactly the fifteen ayahs where the marker matters. Drawing it keeps the marker on every
 * device, at every reader text size, and in the theme's own gold.</p>
 *
 * <p>Like {@link AyahNumberSpan} this span owns a private {@link Paint} and reserves its complete
 * border in the line metrics, so the Arabic line it sits in is never squeezed.</p>
 */
public final class SajdahMarkerSpan extends ReplacementSpan {

    /** The default gold of a Mushaf's prostration marker. */
    public static final int PRIMARY_GOLD = 0xFFB07D2B;

    private final float density;
    @ColorInt
    private final int color;
    private final Paint markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path arch = new Path();
    private final RectF archRect = new RectF();

    public SajdahMarkerSpan(float density, @ColorInt int color) {
        if (density <= 0f) {
            throw new IllegalArgumentException("density must be positive");
        }
        this.density = density;
        this.color = color;
    }

    /** Height of the arch, derived from the text paint so reader zoom scales it. */
    private float height(@NonNull Paint textPaint) {
        return textPaint.getTextSize() * 0.92f;
    }

    /** Width of the arch: a mihrab is taller than it is wide, like the printed marker. */
    private float width(@NonNull Paint textPaint) {
        return textPaint.getTextSize() * 0.52f;
    }

    private float strokeWidth() {
        return 1.4f * density;
    }

    private float sidePadding(@NonNull Paint paint) {
        return Math.max(2.5f * density, paint.getTextSize() * 0.09f);
    }

    @Override
    public int getSize(@NonNull Paint paint, @NonNull CharSequence text, int start, int end,
                       @Nullable Paint.FontMetricsInt fm) {
        float height = height(paint);
        if (fm != null) {
            paint.getFontMetricsInt(fm);
            float centerY = (paint.ascent() + paint.descent()) / 2f;
            // Reserve the complete arch in the line metrics; never shrink the Arabic line.
            fm.ascent = Math.min(fm.ascent, (int) Math.floor(centerY - height / 2f));
            fm.descent = Math.max(fm.descent, (int) Math.ceil(centerY + height / 2f));
            fm.top = Math.min(fm.top, fm.ascent);
            fm.bottom = Math.max(fm.bottom, fm.descent);
        }
        return (int) Math.ceil(width(paint) + 2f * sidePadding(paint));
    }

    @Override
    public void draw(@NonNull Canvas canvas, @NonNull CharSequence text, int start, int end,
                     float x, int top, int y, int bottom, @NonNull Paint paint) {
        float totalWidth = (float) Math.ceil(width(paint) + 2f * sidePadding(paint));
        float height = height(paint);
        float width = width(paint);
        // Android supplies the replacement's left edge even inside an RTL paragraph.
        float centerX = x + totalWidth / 2f;
        float centerY = y + (paint.ascent() + paint.descent()) / 2f;
        float left = centerX - width / 2f;
        float right = centerX + width / 2f;
        float archTop = centerY - height / 2f;
        float base = centerY + height / 2f;

        markerPaint.set(paint);
        markerPaint.setAntiAlias(true);
        markerPaint.setTypeface(Typeface.DEFAULT);
        markerPaint.setLetterSpacing(0f);
        markerPaint.setTextSkewX(0f);
        markerPaint.setTextScaleX(1f);
        markerPaint.setFakeBoldText(false);
        markerPaint.clearShadowLayer();
        markerPaint.setColor(color);

        // The pointed arch: two straight jambs and a pointed head, the shape of a mihrab.
        arch.reset();
        arch.moveTo(left, base);
        arch.lineTo(left, archTop + height * 0.38f);
        arch.quadTo(left, archTop + height * 0.10f, centerX, archTop);
        arch.quadTo(right, archTop + height * 0.10f, right, archTop + height * 0.38f);
        arch.lineTo(right, base);
        arch.close();

        markerPaint.setStyle(Paint.Style.STROKE);
        markerPaint.setStrokeWidth(strokeWidth());
        canvas.drawPath(arch, markerPaint);

        // The base line the arch stands on, and the lamp dot a Mushaf draws inside the niche.
        markerPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(left - strokeWidth(), base - strokeWidth(), right + strokeWidth(), base,
                markerPaint);
        float dotRadius = Math.max(0.9f * density, width * 0.10f);
        canvas.drawCircle(centerX, centerY + height * 0.12f, dotRadius, markerPaint);
    }
}
