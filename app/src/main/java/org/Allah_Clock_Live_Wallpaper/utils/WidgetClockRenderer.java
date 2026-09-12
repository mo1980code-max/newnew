package org.Allah_Clock_Live_Wallpaper.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import org.Allah_Clock_Live_Wallpaper.model.Clocks;
import org.Allah_Clock_Live_Wallpaper.viewUtils.AnalogClock;
import org.Allah_Clock_Live_Wallpaper.viewUtils.SmartClockPreview;
import org.Allah_Clock_Live_Wallpaper.viewUtils.TextClockPreview;

/**
 * Renders the user's currently selected clock design into a Bitmap.
 *
 * <p>The home-screen widget cannot embed the app's custom views (RemoteViews only accepts a
 * fixed list of framework widgets), so instead of duplicating the ten digital and eleven
 * smart styles, this class instantiates the <i>very same</i> view the live wallpaper uses,
 * configures it exactly like the wallpaper does and draws it onto an off-screen canvas.
 * The widget therefore always shows the identical design the user picked in the editor,
 * and new clock styles reach the widget for free.</p>
 */
public final class WidgetClockRenderer {

    private static final String TAG = "WidgetClockRenderer";

    private WidgetClockRenderer() {
    }

    /**
     * @param widthPx  target width in pixels
     * @param heightPx target height in pixels
     * @return the rendered clock, or {@code null} if rendering is impossible
     */
    @Nullable
    public static Bitmap render(Context context, int widthPx, int heightPx) {
        if (widthPx <= 0 || heightPx <= 0) {
            return null;
        }
        try {
            View clockView = buildClockView(context, widthPx, heightPx);
            if (clockView == null) {
                return null;
            }
            clockView.measure(
                    View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(heightPx, View.MeasureSpec.EXACTLY));
            clockView.layout(0, 0, widthPx, heightPx);

            Bitmap bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            clockView.draw(canvas);
            return bitmap;
        } catch (Throwable t) {
            // A widget must never crash the launcher process.
            Log.e(TAG, "render failed", t);
            return null;
        }
    }

    @Nullable
    private static View buildClockView(Context context, int widthPx, int heightPx) {
        TinyDB tinyDB = new TinyDB(context);
        float centerX = widthPx / 2f;
        float centerY = heightPx / 2f;
        int diameter = Math.min(widthPx, heightPx);
        int clockType = tinyDB.getInt("clockType");
        int styleIndex = tinyDB.getInt("textClockPosition");

        if (clockType == 0) {
            AnalogClock analog = new AnalogClock(context);
            analog.setAutoUpdate(false);
            Clocks clocks = (Clocks) tinyDB.getObject("clocks", Clocks.class);
            if (clocks == null) {
                clocks = new org.Allah_Clock_Live_Wallpaper.utils.GetClocks().getClocks().get(0);
            }
            analog.setClock(clocks);
            analog.setPosition(centerX, centerY);
            analog.setClockSize(diameter);
            return analog;
        }
        if (clockType == 1) {
            SmartClockPreview smart = new SmartClockPreview(context);
            smart.setTextClockPosition(styleIndex);
            smart.config(centerX, centerY, diameter);
            return smart;
        }
        TextClockPreview digital = new TextClockPreview(context);
        digital.setTextClockPosition(styleIndex);
        digital.setColors(tinyDB.getInt("textColor1", -1),
                tinyDB.getInt("textColor2", Color.WHITE));
        digital.config(centerX, centerY, diameter);
        return digital;
    }
}
