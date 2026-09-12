package org.Allah_Clock_Live_Wallpaper.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.RemoteViews;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.activity.ClockFuntionActivity;
import org.Allah_Clock_Live_Wallpaper.utils.HijriDate;
import org.Allah_Clock_Live_Wallpaper.utils.WidgetClockRenderer;
import org.Allah_Clock_Live_Wallpaper.viewUtils.WallpaperOverlayView;

/**
 * Base class of the home-screen clock widgets.
 *
 * <p><b>Battery model:</b> the widget re-renders once per minute using a single
 * non-waking {@link AlarmManager#set} alarm (deferred while the device dozes, when nobody
 * can see the widget anyway), plus immediate refreshes on screen-on, time and timezone
 * changes. That is orders of magnitude cheaper than the live wallpaper's per-second
 * surface redraw, which is exactly the point of offering a widget.</p>
 *
 * <p>Two concrete subclasses exist because each size preset needs its own
 * {@code appwidget-provider} metadata; both share every line of code below and both are
 * freely resizable, the renderer simply follows the actual widget size.</p>
 */
public abstract class ClockWidgetProvider extends AppWidgetProvider {

    /** Internal per-minute refresh tick. */
    public static final String ACTION_CLOCK_TICK =
            "org.Allah_Clock_Live_Wallpaper.action.CLOCK_TICK";

    private static final int DEFAULT_SIDE_DP = 110;
    private static final int DEFAULT_WIDE_WIDTH_DP = 250;

    /** The layout this size preset uses. */
    protected abstract int layoutRes();

    /** Preferred cell span, used only to pick sensible fallback dimensions. */
    protected abstract boolean isWide();

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        updateWidgets(context, manager, ids);
        scheduleNextMinute(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (ACTION_CLOCK_TICK.equals(action)
                || Intent.ACTION_TIME_CHANGED.equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
                || Intent.ACTION_SCREEN_ON.equals(action)
                || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            int[] ids = manager.getAppWidgetIds(new ComponentName(context, getClass()));
            if (ids.length > 0) {
                updateWidgets(context, manager, ids);
                scheduleNextMinute(context);
            }
        }
    }

    @Override
    public void onDisabled(Context context) {
        cancelTick(context);
    }

    private void updateWidgets(Context context, AppWidgetManager manager, int[] ids) {
        float density = context.getResources().getDisplayMetrics().density;
        for (int id : ids) {
            int widthDp = DEFAULT_SIDE_DP;
            int heightDp = DEFAULT_SIDE_DP;
            Bundle options = manager.getAppWidgetOptions(id);
            if (options != null) {
                widthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH,
                        isWide() ? DEFAULT_WIDE_WIDTH_DP : DEFAULT_SIDE_DP);
                heightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,
                        DEFAULT_SIDE_DP);
            }
            if (widthDp <= 0) {
                widthDp = DEFAULT_SIDE_DP;
            }
            if (heightDp <= 0) {
                heightDp = DEFAULT_SIDE_DP;
            }
            int widthPx = Math.round(widthDp * density);
            int heightPx = Math.round(heightDp * density);

            RemoteViews views = new RemoteViews(context.getPackageName(), layoutRes());
            views.setViewVisibility(R.id.widgetTextColumn,
                    isWide() ? android.view.View.VISIBLE : android.view.View.GONE);

            Bitmap clock = WidgetClockRenderer.render(context, widthPx, heightPx);
            if (clock != null) {
                views.setImageViewBitmap(R.id.widgetClockImage, clock);
            }
            long now = System.currentTimeMillis();
            views.setTextViewText(R.id.widgetHijri, HijriDate.format(context, now));
            views.setTextViewText(R.id.widgetDhikr, WallpaperOverlayView.currentDhikr(context, now));

            PendingIntent open = PendingIntent.getActivity(context, 0,
                    new Intent(context, ClockFuntionActivity.class),
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widgetRoot, open);

            manager.updateAppWidget(id, views);
        }
    }

    /** One non-waking alarm aligned to the next minute boundary. */
    private static void scheduleNextMinute(Context context) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarm == null) {
            return;
        }
        long millisToNextMinute = 60000L - (System.currentTimeMillis() % 60000L);
        PendingIntent tick = PendingIntent.getBroadcast(context, 0,
                new Intent(ACTION_CLOCK_TICK).setPackage(context.getPackageName()),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarm.set(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + millisToNextMinute, tick);
    }

    private static void cancelTick(Context context) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarm == null) {
            return;
        }
        alarm.cancel(PendingIntent.getBroadcast(context, 0,
                new Intent(ACTION_CLOCK_TICK).setPackage(context.getPackageName()),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
    }
}
