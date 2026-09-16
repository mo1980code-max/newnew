package org.Allah_Clock_Live_Wallpaper.widget;

import org.Allah_Clock_Live_Wallpaper.R;

/** 2×2 clock widget: the clock face alone, no text column. */
public class ClockWidgetSquare extends ClockWidgetProvider {

    @Override
    protected int layoutRes() {
        return R.layout.widget_clock;
    }

    @Override
    protected boolean isWide() {
        return false;
    }
}
