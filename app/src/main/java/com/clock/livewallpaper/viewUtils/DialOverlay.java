package com.clock.livewallpaper.viewUtils;

import android.graphics.Canvas;
import java.util.Calendar;



public interface DialOverlay {
    void onDraw(Canvas canvas, int i, int i2, int i3, int i4, Calendar calendar, boolean z);
}
