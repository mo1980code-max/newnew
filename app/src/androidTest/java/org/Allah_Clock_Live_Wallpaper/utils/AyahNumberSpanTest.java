package org.Allah_Clock_Live_Wallpaper.utils;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextDirectionHeuristics;
import android.text.TextPaint;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/** Run on an Android device/emulator: ./gradlew connectedDebugAndroidTest */
@RunWith(AndroidJUnit4.class)
public class AyahNumberSpanTest {
    private static final String VERSE = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ";
    private static final float EPSILON = 0.01f;

    @Test
    public void appendsExactlyOneAtomicMarkerPerVerseWithoutChangingRawText() {
        assertEquals("١", QuranText.arabicIndic(1));
        assertEquals("١٠", QuranText.arabicIndic(10));
        assertEquals("٢٨٦", QuranText.arabicIndic(286));
        SpannableStringBuilder text = new SpannableStringBuilder();
        QuranText.appendAyah(text, VERSE, 1, 1f, AyahNumberSpan.PRIMARY_GREEN);
        text.append(' ');
        QuranText.appendAyah(text, VERSE, 286, 1f, AyahNumberSpan.PRIMARY_GREEN);
        assertEquals(VERSE + "\u00A0\uFFFC " + VERSE + "\u00A0\uFFFC", text.toString());
        assertFalse(text.toString().contains("\u06DD"));
        AyahNumberSpan[] spans = text.getSpans(0, text.length(), AyahNumberSpan.class);
        assertEquals(2, spans.length);
        for (AyahNumberSpan span : spans) {
            assertEquals(1, text.getSpanEnd(span) - text.getSpanStart(span));
            assertEquals(Spanned.SPAN_EXCLUSIVE_EXCLUSIVE, text.getSpanFlags(span));
        }
    }

    @Test
    public void centersDigitsAndReservesBorderAndPaddingAtAllReaderSizes() {
        for (float density : new float[]{1f, 3f}) {
            for (float sizeSp : new float[]{16f, 24f, 40f, 64f}) {
                for (int number : new int[]{1, 9, 10, 99, 100, 286}) {
                    TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
                    paint.setTextSize(sizeSp * density);
                    paint.setTypeface(Typeface.SERIF);
                    AyahNumberSpan span = new AyahNumberSpan(number, density,
                            AyahNumberSpan.PRIMARY_GREEN);
                    Paint.FontMetricsInt fm = new Paint.FontMetricsInt();
                    int width = span.getSize(paint, "\uFFFC", 0, 1, fm);
                    assertEquals(width, span.getSize(paint, "\uFFFC", 0, 1, null));
                    RecordingCanvas canvas = new RecordingCanvas();
                    span.draw(canvas, "\uFFFC", 0, 1, 20f, fm.top, 0, fm.bottom, paint);
                    assertEquals(QuranText.arabicIndic(number), canvas.number);
                    assertEquals(Paint.Align.CENTER, canvas.digitPaint.getTextAlign());
                    assertEquals(20f + width / 2f, canvas.cx, EPSILON);
                    assertEquals(canvas.cx, canvas.textX, EPSILON);
                    Paint.FontMetrics digits = canvas.digitPaint.getFontMetrics();
                    assertEquals(canvas.cy,
                            canvas.baseline + (digits.ascent + digits.descent) / 2f, EPSILON);
                    assertEquals(1.5f * density, canvas.stroke, EPSILON);
                    assertEquals(AyahNumberSpan.PRIMARY_GREEN, canvas.digitPaint.getColor());
                    float outerRadius = canvas.radius + canvas.stroke / 2f;
                    assertTrue(canvas.cy - outerRadius >= fm.top);
                    assertTrue(canvas.cy + outerRadius <= fm.bottom);
                    assertTrue(width - 2f * outerRadius >= 6f * density);
                    float diagonal = (float) Math.hypot(canvas.digitPaint.measureText(canvas.number),
                            digits.descent - digits.ascent);
                    assertTrue(diagonal < 2f * canvas.radius - canvas.stroke);
                }
            }
        }
    }

    @Test
    public void doesNotMutateTextPaintAndRemeasuresAfterZoom() {
        Paint paint = new Paint();
        paint.setTextSize(20f);
        paint.setColor(0xFF123456);
        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setStyle(Paint.Style.FILL);
        paint.setTypeface(Typeface.SERIF);
        AyahNumberSpan span = new AyahNumberSpan(286, 1f, 0xFFD9B36A);
        int small = span.getSize(paint, "\uFFFC", 0, 1, null);
        RecordingCanvas canvas = new RecordingCanvas();
        span.draw(canvas, "\uFFFC", 0, 1, 0f, 0, 30, 60, paint);
        assertEquals(20f, paint.getTextSize(), EPSILON);
        assertEquals(0xFF123456, paint.getColor());
        assertEquals(Paint.Align.RIGHT, paint.getTextAlign());
        assertEquals(Paint.Style.FILL, paint.getStyle());
        assertEquals(Typeface.SERIF, paint.getTypeface());
        assertEquals(0xFFD9B36A, canvas.digitPaint.getColor());
        paint.setTextSize(60f);
        assertTrue(span.getSize(paint, "\uFFFC", 0, 1, null) > small);
    }

    @Test
    public void markerFollowsVerseOnTheLeftInRtlLayout() {
        TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(32f);
        SpannableStringBuilder text = QuranText.withAyahNumber(VERSE, 286, 1f,
                AyahNumberSpan.PRIMARY_GREEN);
        StaticLayout layout = StaticLayout.Builder.obtain(text, 0, text.length(), paint, 1000)
                .setTextDirection(TextDirectionHeuristics.RTL)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .build();
        assertEquals(1, layout.getLineCount());
        assertEquals(-1, layout.getParagraphDirection(0));
        int marker = text.length() - 1;
        assertTrue(layout.getPrimaryHorizontal(marker) < layout.getPrimaryHorizontal(0));
        assertTrue(layout.getPrimaryHorizontal(marker + 1) < layout.getPrimaryHorizontal(marker));
    }

    /** Records the span's actual drawing calls rather than reproducing its geometry. */
    private static final class RecordingCanvas extends Canvas {
        float cx, cy, radius, stroke, textX, baseline;
        String number;
        Paint digitPaint;

        @Override
        public void drawCircle(float cx, float cy, float radius, Paint paint) {
            this.cx = cx;
            this.cy = cy;
            this.radius = radius;
            this.stroke = paint.getStrokeWidth();
            assertEquals(Paint.Style.STROKE, paint.getStyle());
        }

        @Override
        public void drawText(String text, float x, float y, Paint paint) {
            this.number = text;
            this.textX = x;
            this.baseline = y;
            this.digitPaint = new Paint(paint);
            assertEquals(Paint.Style.FILL, paint.getStyle());
        }
    }
}
