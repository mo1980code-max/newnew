package org.Allah_Clock_Live_Wallpaper.utils;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.Allah_Clock_Live_Wallpaper.R;

/**
 * The app's one place for touch feedback: a short haptic tick and the press micro-interaction
 * (a small scale-down plus a soft highlight while the finger is down).
 *
 * <p>Three screens used to grow their own copy of the vibration code, with three slightly
 * different behaviours on the same device. Everything is centralised here so a tap feels
 * identical everywhere, and so there is exactly one place to look when it does not.</p>
 *
 * <p>{@link #tick(View)} follows the platform: on API 29+ it asks for the system's own
 * {@code EFFECT_TICK}, which respects the user's haptics strength setting, and falls back to a
 * 15 ms one-shot on older releases. Nothing here ever throws: a device without a vibrator, or
 * with haptics switched off, simply stays silent.</p>
 */
public final class UiMotion {

    /** Scale factor while a pressable icon is held down (a subtle 6% dip). */
    private static final float PRESS_SCALE = 0.94f;

    private UiMotion() {
    }

    /**
     * A very short, gentle tick — the feedback a bead of the misbaha makes. Safe to call from
     * any view; the vibrator is resolved from the application context and never leaks.
     */
    public static void tick(@Nullable View source) {
        if (source == null) {
            return;
        }
        Vibrator vibrator = vibratorOf(source.getContext());
        if (vibrator == null || !vibrator.hasVibrator()) {
            return;
        }
        try {
            if (Build.VERSION.SDK_INT >= 29) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK));
            } else if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(VibrationEffect.createOneShot(15L, 80));
            } else {
                vibrator.vibrate(15L);
            }
        } catch (Throwable ignored) {
            // Some OEM builds throw when the phone is in silent mode; a tap must never fail.
        }
    }

    /**
     * Adds the press micro-interaction: a scale dip, a faint highlight and the system's own
     * click feedback, all on the view the user actually touched. Clicks keep working exactly as
     * before — this only observes the touch stream, it never consumes it.
     */
    public static void pressable(@NonNull View view) {
        view.setClickable(true);
        view.setOnTouchListener((pressed, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    animate(pressed, PRESS_SCALE, 0.85f);
                    pressed.setPressed(true);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    animate(pressed, 1f, 1f);
                    pressed.setPressed(false);
                    break;
                default:
                    break;
            }
            return false;
        });
    }

    /** Applies the scale dip on a view whose touch handling belongs to somebody else. */
    public static void setPressed(@NonNull View view, boolean pressed) {
        animate(view, pressed ? PRESS_SCALE : 1f, pressed ? 0.85f : 1f);
        view.setPressed(pressed);
    }

    private static void animate(@NonNull View view, float scale, float alpha) {
        view.animate()
                .scaleX(scale)
                .scaleY(scale)
                .alpha(alpha)
                .setDuration(view.getResources().getInteger(android.R.integer.config_shortAnimTime))
                .setInterpolator(AnimationUtils.loadInterpolator(view.getContext(),
                        android.R.interpolator.fast_out_slow_in))
                .start();
    }

    @Nullable
    private static Vibrator vibratorOf(@NonNull Context context) {
        Context application = context.getApplicationContext();
        if (Build.VERSION.SDK_INT >= 31) {
            VibratorManager manager = (VibratorManager) application
                    .getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            return manager == null ? null : manager.getDefaultVibrator();
        }
        Object service = application.getSystemService(Context.VIBRATOR_SERVICE);
        return service instanceof Vibrator ? (Vibrator) service : null;
    }

    /** The press highlight layer used by the glass tiles; kept here so it stays consistent. */
    public static int pressHighlightColor(@NonNull Context context) {
        return androidx.core.content.ContextCompat.getColor(context, R.color.glassPressedOverlay);
    }
}
