package org.Allah_Clock_Live_Wallpaper.utils;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.Allah_Clock_Live_Wallpaper.model.QuranAyah;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/** Run on an Android device/emulator: ./gradlew connectedDebugAndroidTest */
@RunWith(AndroidJUnit4.class)
public class SajdahMarkerSpanTest {
    private static final String VERSE = "إِنَّ الَّذِينَ عِندَ رَبِّكَ";
    private static final float EPSILON = 0.01f;

    @Test
    public void appendsTheMihrabOnlyToAnAyahOfProstration() {
        SpannableStringBuilder plain = new SpannableStringBuilder();
        QuranText.appendAyah(plain, VERSE, 205, 1f, AyahNumberSpan.PRIMARY_GREEN,
                QuranAyah.NO_SAJDAH, SajdahMarkerSpan.PRIMARY_GOLD);
        assertEquals(VERSE + "\u00A0\uFFFC", plain.toString());
        assertEquals(0, plain.getSpans(0, plain.length(), SajdahMarkerSpan.class).length);

        SpannableStringBuilder sajdah = new SpannableStringBuilder();
        QuranText.appendAyah(sajdah, VERSE, 206, 1f, AyahNumberSpan.PRIMARY_GREEN,
                1, SajdahMarkerSpan.PRIMARY_GOLD);
        // One extra atomic replacement: the mihrab sits after the end-of-ayah number.
        assertEquals(VERSE + "\u00A0\uFFFC\uFFFC", sajdah.toString());
        assertFalse("the marker is drawn, never printed as U+06E9",
                sajdah.toString().contains("\u06E9"));
        SajdahMarkerSpan[] spans =
                sajdah.getSpans(0, sajdah.length(), SajdahMarkerSpan.class);
        assertEquals(1, spans.length);
        assertEquals(1, sajdah.getSpanEnd(spans[0]) - sajdah.getSpanStart(spans[0]));
        assertEquals(Spanned.SPAN_EXCLUSIVE_EXCLUSIVE, sajdah.getSpanFlags(spans[0]));
        // The ayah number is still there and still atomic, before the mihrab.
        AyahNumberSpan[] numbers =
                sajdah.getSpans(0, sajdah.length(), AyahNumberSpan.class);
        assertEquals(1, numbers.length);
        assertTrue(sajdah.getSpanStart(numbers[0]) < sajdah.getSpanStart(spans[0]));
    }

    @Test
    public void drawsAnArchABaseAndALampDotAndReservesItsWholeBorder() {
        for (float density : new float[]{1f, 3f}) {
            for (float sizeSp : new float[]{16f, 24f, 40f}) {
                TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
                paint.setTextSize(sizeSp * density);
                paint.setTypeface(Typeface.SERIF);
                SajdahMarkerSpan span = new SajdahMarkerSpan(density,
                        SajdahMarkerSpan.PRIMARY_GOLD);
                Paint.FontMetricsInt fm = new Paint.FontMetricsInt();
                int width = span.getSize(paint, "\uFFFC", 0, 1, fm);
                assertEquals(width, span.getSize(paint, "\uFFFC", 0, 1, null));
                RecordingCanvas canvas = new RecordingCanvas();
                span.draw(canvas, "\uFFFC", 0, 1, 20f, fm.top, 0, fm.bottom, paint);

                assertEquals("the arch is one closed path", 1, canvas.paths);
                assertEquals("the base and the lamp dot are filled", 2, canvas.rects + canvas.dots);
                assertEquals(1, canvas.rects);
                assertEquals(1, canvas.dots);
                assertEquals(SajdahMarkerSpan.PRIMARY_GOLD, canvas.paintColor);
                // Nothing is clipped by the line: the arch fits inside the metrics it reserved.
                assertTrue(canvas.archTop >= fm.top);
                assertTrue(canvas.archBottom <= fm.bottom);
                assertTrue(width > 0);
            }
        }
    }

    @Test
    public void scalesWithTheReaderTextSizeAndNeverMutatesTheTextPaint() {
        Paint paint = new Paint();
        paint.setTextSize(20f);
        paint.setColor(0xFF123456);
        paint.setStyle(Paint.Style.FILL);
        paint.setTypeface(Typeface.SERIF);
        SajdahMarkerSpan span = new SajdahMarkerSpan(1f, 0xFFD9B36A);
        int small = span.getSize(paint, "\uFFFC", 0, 1, null);
        span.draw(new RecordingCanvas(), "\uFFFC", 0, 1, 0f, 0, 30, 60, paint);
        assertEquals(20f, paint.getTextSize(), EPSILON);
        assertEquals(0xFF123456, paint.getColor());
        assertEquals(Paint.Style.FILL, paint.getStyle());
        assertEquals(Typeface.SERIF, paint.getTypeface());
        paint.setTextSize(60f);
        assertTrue(span.getSize(paint, "\uFFFC", 0, 1, null) > small);
    }

    @Test(expected = IllegalArgumentException.class)
    public void refusesANonPositiveDensity() {
        new SajdahMarkerSpan(0f, SajdahMarkerSpan.PRIMARY_GOLD);
    }

    /** Records the span's real drawing calls rather than reproducing its geometry. */
    private static final class RecordingCanvas extends Canvas {
        int paths, rects, dots;
        int paintColor;
        float archTop = Float.MAX_VALUE, archBottom = Float.MIN_VALUE;

        @Override
        public void drawPath(Path path, Paint paint) {
            paths++;
            paintColor = paint.getColor();
            assertEquals(Paint.Style.STROKE, paint.getStyle());
            android.graphics.RectF bounds = new android.graphics.RectF();
            path.computeBounds(bounds, true);
            archTop = Math.min(archTop, bounds.top);
            archBottom = Math.max(archBottom, bounds.bottom);
        }

        @Override
        public void drawRect(float left, float top, float right, float bottom, Paint paint) {
            rects++;
            paintColor = paint.getColor();
            assertEquals(Paint.Style.FILL, paint.getStyle());
            archBottom = Math.max(archBottom, bottom);
        }

        @Override
        public void drawCircle(float cx, float cy, float radius, Paint paint) {
            dots++;
            paintColor = paint.getColor();
            assertEquals(Paint.Style.FILL, paint.getStyle());
            archTop = Math.min(archTop, cy - radius);
            archBottom = Math.max(archBottom, cy + radius);
        }
    }
}
