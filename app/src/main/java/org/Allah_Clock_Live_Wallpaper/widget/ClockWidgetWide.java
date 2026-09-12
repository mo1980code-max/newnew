package org.Allah_Clock_Live_Wallpaper.widget;

import org.Allah_Clock_Live_Wallpaper.R;

/** 4×2 clock widget: clock face plus the Hijri date and the current dhikr. */
public class ClockWidgetWide extends ClockWidgetProvider {

    @Override
    protected int layoutRes() {
        return R.layout.widget_clock;
    }

    @Override
    protected boolean isWide() {
        return true;
    }
}
