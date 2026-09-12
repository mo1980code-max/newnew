package org.Allah_Clock_Live_Wallpaper.viewUtils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.utils.HijriDate;
import org.Allah_Clock_Live_Wallpaper.utils.TinyDB;

/**
 * Transparent overlay drawn on top of the live wallpaper: the Hijri date near the top and
 * one short dhikr near the bottom that rotates automatically every few hours.
 *
 * <p>Both lines are opt-in ({@code showHijri} / {@code showDhikr} preferences, toggled in the
 * editor). Text uses the system typeface so Arabic is always shaped correctly, and a soft
 * shadow keeps it readable over any background. The view never consumes touches.</p>
 */
public class WallpaperOverlayView extends View {

    /** How long one dhikr stays on screen before the next one takes over. */
    private static final long DHIKR_ROTATION_MS = 3L * 60L * 60L * 1000L;

    /** Recompute the texts at most once a minute; drawing happens every second. */
    private static final long REBUILD_INTERVAL_MS = 60L * 1000L;

    private final TextPaint paint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
    private final TinyDB tinyDB;

    private boolean showHijri;
    private boolean showDhikr;
    private String hijriText = "";
    @Nullable
    private StaticLayout dhikrLayout;

    private long cacheKey = -1L;
    private int cacheWidth = -1;

    public WallpaperOverlayView(Context context) {
        this(context, null);
    }

    public WallpaperOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.tinyDB = new TinyDB(context.getApplicationContext());
        this.paint.setColor(Color.WHITE);
        this.paint.setShadowLayer(8f, 0f, 2f, 0xB3000000);
        // System typeface: guaranteed to shape Arabic on every device.
        this.paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        setClickable(false);
        setFocusable(false);
    }

    /** Forces the texts to be re-read from the preferences on the next draw. */
    public void refresh() {
        this.cacheKey = -1L;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) {
            return;
        }
        long now = System.currentTimeMillis();
        long key = now / REBUILD_INTERVAL_MS;
        if (key != this.cacheKey || width != this.cacheWidth) {
            rebuild(width, now);
            this.cacheKey = key;
            this.cacheWidth = width;
        }

        if (this.showHijri && this.hijriText.length() > 0) {
            this.paint.setTextSize(clamp(height * 0.026f, 30f, 54f));
            float textWidth = this.paint.measureText(this.hijriText);
            canvas.drawText(this.hijriText, (width - textWidth) / 2f,
                    height * 0.075f + this.paint.getTextSize(), this.paint);
        }

        if (this.showDhikr && this.dhikrLayout != null) {
            this.dhikrLayout.getPaint().setTextSize(clamp(height * 0.024f, 26f, 46f));
            canvas.save();
            canvas.translate((width - this.dhikrLayout.getWidth()) / 2f,
                    height - this.dhikrLayout.getHeight() - height * 0.055f);
            this.dhikrLayout.draw(canvas);
            canvas.restore();
        }
    }

    private void rebuild(int width, long now) {
        this.showHijri = this.tinyDB.getBoolean("showHijri");
        this.showDhikr = this.tinyDB.getBoolean("showDhikr");

        if (this.showHijri) {
            this.hijriText = HijriDate.format(getContext(), now);
        } else {
            this.hijriText = "";
        }

        if (this.showDhikr) {
            String dhikr = currentDhikr(getContext(), now);
            if (dhikr.length() > 0) {
                TextPaint dhikrPaint = new TextPaint(this.paint);
                int maxWidth = (int) (width * 0.82f);
                this.dhikrLayout = StaticLayout.Builder
                        .obtain(dhikr, 0, dhikr.length(), dhikrPaint, maxWidth)
                        .setAlignment(Layout.Alignment.ALIGN_CENTER)
                        .setLineSpacing(0f, 1.2f)
                        .setIncludePad(false)
                        .build();
            } else {
                this.dhikrLayout = null;
            }
        } else {
            this.dhikrLayout = null;
        }
    }

    /**
     * The dhikr that is "on air" at the given moment: derived purely from wall-clock time so
     * the wallpaper overlay and the home-screen widget always show the same one, with no
     * timers and no shared state.
     */
    public static String currentDhikr(Context context, long millis) {
        String[] adhkar = context.getResources().getStringArray(R.array.adhkar);
        if (adhkar.length == 0) {
            return "";
        }
        int index = (int) ((millis / DHIKR_ROTATION_MS) % adhkar.length);
        if (index < 0) {
            index = 0;
        }
        return adhkar[index];
    }

    private static float clamp(float value, float min, float max) {
        return value < min ? min : (value > max ? max : value);
    }
}
