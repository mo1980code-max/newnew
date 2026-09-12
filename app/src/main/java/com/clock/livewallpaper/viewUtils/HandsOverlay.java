package com.clock.livewallpaper.viewUtils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import java.util.Calendar;



public class HandsOverlay implements DialOverlay {
    private final Drawable mHour;
    private float mHourRot;
    private float mMinRot;
    private final Drawable mMinute;
    private final Drawable mSecond;
    private boolean mShowSeconds;
    private final boolean mUseLargeFace;
    private float scale;

    public HandsOverlay(Context context, boolean z) {
        context.getResources();
        this.mUseLargeFace = z;
        this.mHour = null;
        this.mMinute = null;
        this.mSecond = null;
    }

    public HandsOverlay(Drawable drawable, Drawable drawable2, Drawable drawable3) {
        this.mUseLargeFace = false;
        this.mHour = drawable;
        this.mMinute = drawable2;
        this.mSecond = drawable3;
    }

    public HandsOverlay withScale(float f) {
        this.scale = f;
        return this;
    }

    public HandsOverlay(Context context, int i, int i2, int i3) {
        Resources resources = context.getResources();
        this.mUseLargeFace = false;
        this.mHour = resources.getDrawable(i);
        this.mMinute = resources.getDrawable(i2);
        this.mSecond = resources.getDrawable(i3);
    }

    public static float getHourHandAngle(int i, int i2) {
        float f = (float) (i + 12);
        float f2 = AnalogClock.is24 ? 24.0f : 12.0f;
        return (((f / f2) * 360.0f) % 360.0f) + (((((float) i2) / 60.0f) * 360.0f) / f2);
    }

    @Override
    public void onDraw(Canvas canvas, int i, int i2, int i3, int i4, Calendar calendar, boolean z) {
        updateHands(calendar);
        canvas.save();
        if (!AnalogClock.hourOnTop) {
            drawHours(canvas, i, i2, i3, i4, calendar, z);
        } else {
            drawMinutes(canvas, i, i2, i3, i4, calendar, z);
        }
        canvas.restore();
        canvas.save();
        if (!AnalogClock.hourOnTop) {
            drawMinutes(canvas, i, i2, i3, i4, calendar, z);
        } else {
            drawHours(canvas, i, i2, i3, i4, calendar, z);
        }
        drawSecond(canvas, i, i2, i3, i4, calendar, z);
        canvas.restore();
    }

    private void drawMinutes(Canvas canvas, int i, int i2, int i3, int i4, Calendar calendar, boolean z) {
        canvas.rotate(this.mMinRot, (float) i, (float) i2);
        int intrinsicWidth = this.mMinute.getIntrinsicWidth();
        int i5 = intrinsicWidth / 2;
        int intrinsicHeight = this.mMinute.getIntrinsicHeight() / 2;
        this.mMinute.setBounds(i - i5, i2 - intrinsicHeight, i + i5, i2 + intrinsicHeight);
        this.mMinute.draw(canvas);
    }

    private void drawHours(Canvas canvas, int i, int i2, int i3, int i4, Calendar calendar, boolean z) {
        canvas.rotate(this.mHourRot, (float) i, (float) i2);
        int intrinsicWidth = this.mHour.getIntrinsicWidth();
        int i5 = intrinsicWidth / 2;
        int intrinsicHeight = this.mHour.getIntrinsicHeight() / 2;
        this.mHour.setBounds(i - i5, i2 - intrinsicHeight, i + i5, i2 + intrinsicHeight);
        this.mHour.draw(canvas);
    }

    private void drawSecond(Canvas canvas, int i, int i2, int i3, int i4, Calendar calendar, boolean z) {
        canvas.rotate((((float) calendar.get(13)) * 6.0f) - (((float) calendar.get(12)) * 6.0f), (float) i, (float) i2);
        int intrinsicWidth = this.mSecond.getIntrinsicWidth();
        int i5 = intrinsicWidth / 2;
        int intrinsicHeight = this.mSecond.getIntrinsicHeight() / 2;
        this.mSecond.setBounds(i - i5, i2 - intrinsicHeight, i + i5, i2 + intrinsicHeight);
        this.mSecond.draw(canvas);
    }

    private void initDrawable(Context context, Drawable drawable) {
        int intrinsicWidth = drawable.getIntrinsicWidth() / 2;
        int intrinsicHeight = drawable.getIntrinsicHeight() / 2;
        drawable.setBounds(-intrinsicWidth, -intrinsicHeight, intrinsicWidth, intrinsicHeight);
    }

    public void setShowSeconds(boolean z) {
        this.mShowSeconds = z;
    }

    private void updateHands(Calendar calendar) {
        int i = calendar.get(11);
        int i2 = calendar.get(12);
        int i3 = calendar.get(13);
        this.mHourRot = getHourHandAngle(i, i2);
        this.mMinRot = ((((float) i2) / 60.0f) * 360.0f) + (this.mShowSeconds ? ((((float) i3) / 60.0f) * 360.0f) / 60.0f : 0.0f);
    }
}
