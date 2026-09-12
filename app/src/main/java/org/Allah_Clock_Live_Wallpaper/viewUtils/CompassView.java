package org.Allah_Clock_Live_Wallpaper.viewUtils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * Qibla compass dial.
 *
 * <p>The whole dial (cardinal letters + ticks + needle) is rotated by {@code -azimuth}, so
 * the letter sitting at the top of the screen is the direction the phone currently points
 * to, and the gold needle always points at the Kaaba. When the needle lines up with the
 * fixed marker at the top, the user is facing the Qibla.</p>
 */
public class CompassView extends View {

    private final Paint dialPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint letterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint needlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint kaabaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF dialRect = new RectF();
    private final Path needlePath = new Path();

    /** Degrees clockwise from true north to the Kaaba, for the selected city. */
    private float qiblaBearing;
    /** Smoothed device heading, degrees clockwise from true north. */
    private float azimuth;

    public CompassView(Context context) {
        this(context, null);
    }

    public CompassView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        this.dialPaint.setStyle(Paint.Style.STROKE);
        this.dialPaint.setColor(0x33FFFFFF);
        this.dialPaint.setStrokeWidth(dp(2f));

        this.tickPaint.setStyle(Paint.Style.STROKE);
        this.tickPaint.setColor(0x66FFFFFF);
        this.tickPaint.setStrokeWidth(dp(1.5f));

        this.letterPaint.setColor(0xCCFFFFFF);
        this.letterPaint.setTextAlign(Paint.Align.CENTER);
        this.letterPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        this.needlePaint.setStyle(Paint.Style.FILL);
        this.needlePaint.setColor(0xFFD4AF37);

        this.markerPaint.setStyle(Paint.Style.FILL);
        this.markerPaint.setColor(0xFFFFFFFF);

        this.kaabaPaint.setStyle(Paint.Style.FILL);
        this.kaabaPaint.setColor(0xFF111111);
        this.bandPaint.setStyle(Paint.Style.STROKE);
        this.bandPaint.setColor(0xFFD4AF37);
        this.bandPaint.setStrokeWidth(dp(2f));
    }

    public void setQiblaBearing(float bearing) {
        this.qiblaBearing = bearing;
        invalidate();
    }

    public void setAzimuth(float degrees) {
        this.azimuth = degrees;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) {
            return;
        }
        float size = Math.min(width, height);
        float cx = width / 2f;
        float cy = height / 2f;
        float radius = size / 2f - dp(8f);

        this.dialRect.set(cx - radius, cy - radius, cx + radius, cy + radius);
        canvas.drawOval(this.dialRect, this.dialPaint);

        this.letterPaint.setTextSize(size * 0.075f);

        canvas.save();
        canvas.rotate(-this.azimuth, cx, cy);

        // Ticks every 15°, longer on the cardinals.
        for (int deg = 0; deg < 360; deg += 15) {
            boolean cardinal = deg % 90 == 0;
            float inner = radius - (cardinal ? size * 0.06f : size * 0.03f);
            double rad = Math.toRadians(deg);
            canvas.drawLine(
                    cx + (float) Math.sin(rad) * inner,
                    cy - (float) Math.cos(rad) * inner,
                    cx + (float) Math.sin(rad) * radius,
                    cy - (float) Math.cos(rad) * radius,
                    this.tickPaint);
        }

        String[] letters = {"N", "E", "S", "W"};
        for (int i = 0; i < 4; i++) {
            double rad = Math.toRadians(i * 90);
            float letterRadius = radius - size * 0.13f;
            canvas.drawText(letters[i],
                    cx + (float) Math.sin(rad) * letterRadius,
                    cy - (float) Math.cos(rad) * letterRadius + size * 0.027f,
                    this.letterPaint);
        }

        // Gold needle towards the Kaaba heading.
        double needleRad = Math.toRadians(this.qiblaBearing);
        float tipX = cx + (float) Math.sin(needleRad) * (radius - size * 0.16f);
        float tipY = cy - (float) Math.cos(needleRad) * (radius - size * 0.16f);
        float baseX = cx - (float) Math.sin(needleRad) * (radius * 0.28f);
        float baseY = cy + (float) Math.cos(needleRad) * (radius * 0.28f);
        float perpX = (float) Math.cos(needleRad) * size * 0.028f;
        float perpY = (float) Math.sin(needleRad) * size * 0.028f;

        this.needlePath.reset();
        this.needlePath.moveTo(tipX, tipY);
        this.needlePath.lineTo(cx + perpX, cy + perpY);
        this.needlePath.lineTo(baseX, baseY);
        this.needlePath.lineTo(cx - perpX, cy - perpY);
        this.needlePath.close();
        canvas.drawPath(this.needlePath, this.needlePaint);

        // Small Kaaba cube at the needle tip.
        float cube = size * 0.075f;
        canvas.drawRect(tipX - cube / 2f, tipY - cube / 2f, tipX + cube / 2f, tipY + cube / 2f,
                this.kaabaPaint);
        canvas.drawRect(tipX - cube / 2f, tipY - cube / 6f, tipX + cube / 2f, tipY + cube / 6f,
                this.bandPaint);

        canvas.restore();

        // Fixed "you are facing this way" marker at the top of the screen.
        Path marker = new Path();
        marker.moveTo(cx, cy - radius + dp(2f));
        marker.lineTo(cx - size * 0.035f, cy - radius - size * 0.05f);
        marker.lineTo(cx + size * 0.035f, cy - radius - size * 0.05f);
        marker.close();
        canvas.drawPath(marker, this.markerPaint);

        // Centre dot.
        canvas.drawCircle(cx, cy, size * 0.015f, this.markerPaint);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
