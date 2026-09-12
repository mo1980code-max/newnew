package com.clock.livewallpaper.viewUtils;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;



public class ClockView extends View {
    private int screenWidth = 10000;
    private int screenHeight = 1000;

    public ClockView(Context context) {
        super(context);
    }

    public ClockView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setOnTouchListener(new OnTouchListener() {
            private int lastX = 0;
            private int lastY = 0;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                if (action == 0) {
                    this.lastX = (int) motionEvent.getRawX();
                    this.lastY = (int) motionEvent.getRawY();
                    return true;
                } else if (action != 2) {
                    return true;
                } else {
                    int rawX = ((int) motionEvent.getRawX()) - this.lastX;
                    int rawY = ((int) motionEvent.getRawY()) - this.lastY;
                    int left = view.getLeft() + rawX;
                    int top = view.getTop() + rawY;
                    int right = view.getRight() + rawX;
                    int bottom = view.getBottom() + rawY;
                    if (left < 0) {
                        right = view.getWidth() + 0;
                        left = 0;
                    }
                    if (right > ClockView.this.screenWidth) {
                        right = ClockView.this.screenWidth;
                        left = right - view.getWidth();
                    }
                    if (top < 0) {
                        bottom = view.getHeight() + 0;
                        top = 0;
                    }
                    if (bottom > ClockView.this.screenHeight) {
                        bottom = ClockView.this.screenHeight;
                        top = bottom - view.getHeight();
                    }
                    view.layout(left, top, right, bottom);
                    this.lastX = (int) motionEvent.getRawX();
                    this.lastY = (int) motionEvent.getRawY();
                    Log.e("TAG", "onTouchEvent.........: " + this.lastX + "  " + this.lastY);
                    return true;
                }
            }
        });
    }
}
