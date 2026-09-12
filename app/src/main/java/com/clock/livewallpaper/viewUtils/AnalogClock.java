package com.clock.livewallpaper.viewUtils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.clock.livewallpaper.R;
import com.clock.livewallpaper.model.Clocks;
import com.clock.livewallpaper.utils.GetClocks;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.TimeZone;



public class AnalogClock extends View {
    static final  boolean $assertionsDisabled = false;
    public static boolean hourOnTop;
    public static boolean is24;
    private boolean autoUpdate;
    private Clocks clocks;
    private int mBottom;
    private Calendar mCalendar;
    Context mContext;
    private int mDialHeight;
    private int mDialWidth;
    private Drawable mFace;
    private HandsOverlay mHandsOverlay;
    private Drawable mHourHand;
    private int mLeft;
    private Drawable mMinuteHand;
    private int mRight;
    private Drawable mSecondHand;
    private boolean mSizeChanged;
    private int mTop;
    private int radius;
    private final ArrayList<DialOverlay> mDialOverlay = new ArrayList<>();
    private float sizeScale = 1.0f;
    public float mClockPosX = 250.0f;
    public float mClockSize = 500.0f;
    public float mClockPosY = 250.0f;
    private boolean isTouchEnable = false;
    private String[] stringsDays = {"Sun", "Mon", "Tue", "Wed", "thu", "Fri", "Sat"};

    public void setTouchEnable(boolean z) {
        this.isTouchEnable = z;
    }

    public AnalogClock(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        handleAttrs(context, attributeSet);
    }

    public AnalogClock(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        handleAttrs(context, attributeSet);
    }

    public AnalogClock(Context context) {
        super(context);
        init(context);
    }

    public AnalogClock(Context context, boolean z) {
        super(context);
        if (z) {
            init(context);
        }
    }

    private void handleAttrs(Context context, AttributeSet attributeSet) {
        TypedArray obtainStyledAttributes = getContext().obtainStyledAttributes(attributeSet, R.styleable.CustomAnalogClock, 0, 0);
        if (!obtainStyledAttributes.hasValue(0) || obtainStyledAttributes.getBoolean(0, true)) {
            init(context);
            obtainStyledAttributes.recycle();
            return;
        }
        obtainStyledAttributes.recycle();
    }

    public void init(Context context) {
        this.mContext = context;
        setClock(new GetClocks().getClocks().get(10));
        init(context, 0, false, false);
    }

    public void setScale(float f) {
        if (f > 0.0f) {
            this.sizeScale = f;
            this.mHandsOverlay.withScale(f);
            invalidate();
            return;
        }
        throw new IllegalArgumentException("Scale must be bigger than 0");
    }

    public void setFace(int i) {
        setFace(getResources().getDrawable(i));
    }

    public void init(Context context, int i, boolean z, boolean z2) {
        is24 = z;
        hourOnTop = z2;
        if (i > 0) {
            this.mHourHand.setAlpha(i);
        }
        this.mCalendar = Calendar.getInstance();
        this.mHandsOverlay = new HandsOverlay(this.mHourHand, this.mMinuteHand, this.mSecondHand).withScale(this.sizeScale);
    }

    public void setFace(Drawable drawable) {
        this.mFace = drawable;
        this.mSizeChanged = true;
        this.mDialHeight = drawable.getIntrinsicHeight();
        int intrinsicWidth = this.mFace.getIntrinsicWidth();
        this.mDialWidth = intrinsicWidth;
        this.radius = Math.max(this.mDialHeight, intrinsicWidth);
        invalidate();
    }

    public void setTime(long j) {
        this.mCalendar.setTimeInMillis(j);
        invalidate();
    }

    public void setTime(Calendar calendar) {
        this.mCalendar = calendar;
        invalidate();
        if (this.autoUpdate) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    AnalogClock.this.setTime(Calendar.getInstance());
                }
            }, 800);
        }
    }

    public void setAutoUpdate(boolean z) {
        this.autoUpdate = z;
        setTime(Calendar.getInstance());
    }

    public void setTimezone(TimeZone timeZone) {
        this.mCalendar = Calendar.getInstance(timeZone);
    }

    public void setHandsOverlay(HandsOverlay handsOverlay) {
        this.mHandsOverlay = handsOverlay;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        boolean z;
        super.onDraw(canvas);
        this.mSizeChanged = false;
        float f = this.mClockSize;
        int i = (int) f;
        int i2 = (int) f;
        int i3 = (int) this.mClockPosX;
        int i4 = (int) this.mClockPosY;
        int i5 = this.mDialWidth;
        int i6 = this.mDialHeight;
        canvas.drawColor(0);
        if (i < i5 || i2 < i6) {
            float min = Math.min(((float) i) / ((float) i5), ((float) i2) / ((float) i6));
            canvas.save();
            canvas.scale(min, min, (float) i3, (float) i4);
            z = true;
        } else {
            z = false;
        }
        int i7 = i5 / 2;
        int i8 = i6 / 2;
        this.mFace.setBounds(i3 - i7, i4 - i8, i7 + i3, i8 + i4);
        this.mFace.draw(canvas);
        Iterator<DialOverlay> it = this.mDialOverlay.iterator();
        while (it.hasNext()) {
            it.next().onDraw(canvas, i3, i4, i5, i6, this.mCalendar, true);
        }
        this.mHandsOverlay.onDraw(canvas, i3, i4, i5, i6, this.mCalendar, true);
        TextPaint textPaint = new TextPaint();
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.parseColor(this.clocks.textColor));
        double d = (double) this.radius;
        Double.isNaN(d);
        textPaint.setTextSize((float) (d * 0.065d));
        textPaint.setTypeface(Typeface.create("Arial", Typeface.BOLD));
        double d2 = (double) i4;
        double d3 = (double) this.radius;
        Double.isNaN(d3);
        Double.isNaN(d2);
        canvas.drawText(this.mCalendar.get(5) + " " + this.stringsDays[this.mCalendar.get(7) - 1], (float) ((double) i3), (float) (d2 - (d3 * 0.4d)), textPaint);
        if (z) {
            canvas.restore();
        }
    }

    @Override
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        this.mRight = i3;
        this.mLeft = i;
        this.mTop = i2;
        this.mBottom = i4;
    }

    public void addDialOverlay(DialOverlay dialOverlay) {
        this.mDialOverlay.add(dialOverlay);
    }

    public void removeDialOverlay(DialOverlay dialOverlay) {
        this.mDialOverlay.remove(dialOverlay);
    }

    public void clearDialOverlays() {
        this.mDialOverlay.clear();
    }

    public void setClockSize(float f) {
        this.mClockSize = f;
        invalidate();
    }

    public void setPosition(float f, float f2) {
        this.mClockPosX = f;
        this.mClockPosY = f2;
        invalidate();
    }

    public void setClock(Clocks clocks) {
        this.clocks = clocks;
        setFace(clocks.backroundImage);
        this.mHourHand = this.mContext.getResources().getDrawable(clocks.getHourHand());
        this.mMinuteHand = this.mContext.getResources().getDrawable(clocks.getMinuteHand());
        this.mSecondHand = this.mContext.getResources().getDrawable(clocks.getSecondHand());
        this.mDialWidth = this.mFace.getIntrinsicWidth();
        this.mDialHeight = this.mFace.getIntrinsicHeight();
        setHandsOverlay(new HandsOverlay(this.mHourHand, this.mMinuteHand, this.mSecondHand));
        invalidate();
    }
}
