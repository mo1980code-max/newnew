package org.Allah_Clock_Live_Wallpaper.viewUtils;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.style.ReplacementSpan;

import androidx.annotation.NonNull;

import org.Allah_Clock_Live_Wallpaper.utils.QuranTheme;

/**
 * The end-of-ayah ornament — the ۝ rosette of a printed Mushaf — with the ayah number drawn
 * <b>inside</b> it, centred both ways by construction.
 *
 * <p><b>Why a span and not a layout:</b> the reader prints one continuous run of verses, so the
 * marker has to travel with the glyphs it belongs to and must survive re-flowing, text-size
 * changes and a night-mode repaint. A {@link ReplacementSpan} does exactly that: the layout asks
 * it for a width, then calls {@link #draw} with the line's baseline, and the whole ornament is
 * painted in one place. Nothing about its position depends on padding, gravity or a parent
 * view's geometry, which is what used to let the number drift between screen densities.</p>
 *
 * <p><b>How the digits stay centred:</b> the badge is sized in text-relative units
 * ({@value #SIZE_EM} em across), so it grows with the reader's text size and is identical at any
 * density; its vertical centre is the optical centre of the line
 * ({@code baseline - (ascent + descent) / 2}) rather than the baseline, and the digits are
 * measured with the very same paint that draws them — never a guessed offset. Numbers of two or
 * three digits are shrunk to fit the inner disc and re-centred, so ٢٨٦ looks like ٩.</p>
 */
public final class AyahBadgeSpan extends ReplacementSpan {

    /** Ornament diameter, in ems of the surrounding text. */
    private static final float SIZE_EM = 1.62f;
    /** Digits size relative to the ornament diameter, before the fit-to-disc shrink. */
    private static final float DIGIT_SCALE = 0.5f;
    /** Innermost disc diameter, as a fraction of the ornament, that the digits must fit in. */
    private static final float DISC_FIT = 0.62f;

    private final int ayahNumber;
    @NonNull
    private final QuranTheme theme;

    private final Path rosette = new Path();
    private final Paint ornament = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint digits = new Paint(Paint.ANTI_ALIAS_FLAG);

    public AyahBadgeSpan(int ayahNumber, @NonNull QuranTheme theme) {
        this.ayahNumber = ayahNumber;
        this.theme = theme;
        this.ornament.setStyle(Paint.Style.STROKE);
        this.digits.setTextAlign(Paint.Align.CENTER);
        this.digits.setTypeface(android.graphics.Typeface.SANS_SERIF);
    }

    /** The ayah this badge prints, so a tap on the ornament can be routed to its verse. */
    public int getAyahNumber() {
        return this.ayahNumber;
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end,
                       Paint.FontMetricsInt fontMetrics) {
        float size = badgeSize(paint);
        // Reserve the glyph plus a hair of optical spacing, so the badge never touches a
        // neighbouring letter while still flowing with the verse.
        return (int) Math.ceil(size + size * 0.14f);
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x,
                     int top, int baseline, int bottom, @NonNull Paint paint) {
        float size = badgeSize(paint);
        float left = x + size * 0.07f;

        Paint.FontMetrics metrics = paint.getFontMetrics();
        // Optical centre of the line: half way between the ascent and the descent of the very
        // paint the verse is set in. This is what keeps the badge on the same visual line as the
        // Arabic text instead of hanging off the baseline.
        float centreY = baseline - (metrics.ascent + metrics.descent) / 2f;
        float centreX = left + size / 2f;

        drawRosette(canvas, centreX, centreY, size / 2f);
        drawNumber(canvas, centreX, centreY, size / 2f);
    }

    private void drawRosette(@NonNull Canvas canvas, float cx, float cy, float radius) {
        this.ornament.setColor(this.theme.gold);
        this.ornament.setStyle(Paint.Style.FILL);
        this.ornament.setAlpha(38);
        buildRosette(cx, cy, radius);
        canvas.drawPath(this.rosette, this.ornament);

        this.ornament.setAlpha(255);
        this.ornament.setStrokeWidth(Math.max(1f, radius * 0.11f));
        this.ornament.setStyle(Paint.Style.STROKE);
        canvas.drawPath(this.rosette, this.ornament);

        // The inner disc the number sits in, filled so the digits stay legible over any tint.
        this.ornament.setStyle(Paint.Style.FILL);
        this.ornament.setColor(this.theme.badgeFill);
        canvas.drawCircle(cx, cy, radius * 0.66f, this.ornament);

        this.ornament.setColor(this.theme.gold);
        this.ornament.setStyle(Paint.Style.STROKE);
        this.ornament.setStrokeWidth(Math.max(1f, radius * 0.07f));
        canvas.drawCircle(cx, cy, radius * 0.66f, this.ornament);
    }

    /**
     * A sixteen-point rosette: two concentric rings of petals, the shape a Mushaf prints around
     * an ayah number. Built with plain arcs so it scales exactly with the text size and needs no
     * bitmap at any density.
     */
    private void buildRosette(float cx, float cy, float radius) {
        this.rosette.reset();
        int petals = 16;
        float inner = radius * 0.82f;
        float outer = radius;
        for (int index = 0; index < petals; index++) {
            double angle = (Math.PI * 2 * index) / petals;
            float xOuter = cx + (float) (Math.cos(angle) * outer);
            float yOuter = cy + (float) (Math.sin(angle) * outer);
            double next = angle + Math.PI / petals;
            float xInner = cx + (float) (Math.cos(next) * inner);
            float yInner = cy + (float) (Math.sin(next) * inner);
            if (index == 0) {
                this.rosette.moveTo(xOuter, yOuter);
            } else {
                this.rosette.lineTo(xOuter, yOuter);
            }
            this.rosette.lineTo(xInner, yInner);
        }
        this.rosette.close();
    }

    private void drawNumber(@NonNull Canvas canvas, float cx, float cy, float radius) {
        this.digits.setColor(this.theme.badgeNumber);
        float wanted = radius * 2f * DIGIT_SCALE;
        this.digits.setTextSize(wanted);

        String text = arabicIndic(this.ayahNumber);
        float available = radius * 2f * DISC_FIT;
        float measured = this.digits.measureText(text);
        if (measured > available && measured > 0f) {
            this.digits.setTextSize(wanted * (available / measured));
        }

        Paint.FontMetrics metrics = this.digits.getFontMetrics();
        float textBaseline = cy - (metrics.ascent + metrics.descent) / 2f;
        canvas.drawText(text, cx, textBaseline, this.digits);
    }

    /**
     * The ornament is expressed in ems of the text it is embedded in, so it scales with the
     * reader's text size — and therefore with density — instead of with a fixed pixel size.
     */
    private static float badgeSize(@NonNull Paint paint) {
        return paint.getTextSize() * SIZE_EM;
    }

    /** 6236 -> ٦٢٣٦, the way an ayah number is printed inside the ornament. */
    @NonNull
    public static String arabicIndic(int value) {
        String source = String.valueOf(value);
        StringBuilder out = new StringBuilder(source.length());
        for (int index = 0; index < source.length(); index++) {
            out.append((char) ('\u0660' + (source.charAt(index) - '0')));
        }
        return out.toString();
    }
}
