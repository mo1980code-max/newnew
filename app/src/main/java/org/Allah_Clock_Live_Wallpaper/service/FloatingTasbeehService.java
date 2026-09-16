package org.Allah_Clock_Live_Wallpaper.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.utils.LocaleHelper;
import org.Allah_Clock_Live_Wallpaper.utils.TinyDB;

/**
 * Floating digital tasbeeh (dhikr counter) drawn over other apps.
 *
 * <p><b>Why a plain started service and not a foreground service:</b> on targetSdk 36 a
 * foreground service must declare a type (which for this use case would be
 * {@code specialUse}, i.e. a Play Console justification) and shows a permanent notification.
 * A started service needs neither, draws nothing in the status bar and costs nothing while
 * idle; the trade-off is that aggressive OEM task killers may stop it, which is acceptable
 * for an explicitly user-started tool and is documented in UPGRADE_NOTES.md.</p>
 *
 * <p><b>Pocket protection, three layers:</b></p>
 * <ul>
 *   <li>{@link Sensor#TYPE_PROXIMITY}: while covered (pocket, face-down) every touch and
 *       every vibration is rejected immediately.</li>
 *   <li>{@link Intent#ACTION_SCREEN_OFF}: sensors are unregistered and all pending handler
 *       work is dropped, so nothing runs — and nothing can wake the CPU — while the screen
 *       is off. {@link Intent#ACTION_USER_PRESENT} restores everything.</li>
 *   <li>A 1.5 s long-press freezes the counter for users who want to move the widget around
 *       without counting.</li>
 * </ul>
 *
 * <p><b>Power while active:</b> there is no loop of any kind. The only scheduled work is a
 * single 4-second delayed fade, and the proximity sensor is the cheapest sensor on the
 * device. No wake lock is ever held.</p>
 */
public class FloatingTasbeehService extends Service implements SensorEventListener {

    private static final String PREF_COUNT = "tasbeehCount";

    private static final long FADE_DELAY_MS = 4000L;
    private static final float ACTIVE_ALPHA = 0.92f;
    private static final float IDLE_ALPHA = 0.35f;
    private static final long LOCK_LONG_PRESS_MS = 1500L;
    private static final long RESET_LONG_PRESS_MS = 700L;
    private static final int EDGE_DOCK_DP = 6;
    private static final int EDGE_TRIGGER_DP = 56;

    private static volatile boolean running;

    /** @return whether the floating counter is currently on screen. */
    public static boolean isRunning() {
        return running;
    }

    private final Handler handler = new Handler(Looper.getMainLooper());

    private WindowManager windowManager;
    private WindowManager.LayoutParams params;
    private View root;
    private TextView txtCount;
    private View lockBadge;

    private SensorManager sensorManager;
    private Sensor proximity;
    private Vibrator vibrator;
    private TinyDB tinyDB;

    private int count;
    private boolean locked;
    private boolean pocketCovered;
    private boolean screenOn = true;
    private boolean attached;

    private float downRawX;
    private float downRawY;
    private int downParamX;
    private int downParamY;
    private boolean dragging;
    private boolean lockToggled;

    private final Runnable fadeRunnable = () -> {
        if (root != null && !pocketCovered) {
            root.animate().alpha(IDLE_ALPHA).setDuration(400L).start();
        }
    };

    private final Runnable lockRunnable = () -> {
        lockToggled = true;
        locked = !locked;
        updateLockBadge();
        tick(locked ? 2 : 1);
        toast(locked ? R.string.tasbeeh_locked : R.string.tasbeeh_unlocked);
    };

    private final BroadcastReceiver screenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                screenOn = false;
                pauseEverything();
            } else if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
                screenOn = true;
                resumeEverything();
            }
        }
    };

    // ══════════════════════════════════ lifecycle ══════════════════════════════════

    @Override
    public void onCreate() {
        super.onCreate();
        this.tinyDB = new TinyDB(this);
        this.count = this.tinyDB.getInt(PREF_COUNT);
        this.windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        this.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (this.sensorManager != null) {
            this.proximity = this.sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }
        this.vibrator = obtainVibrator();

        try {
            buildAndAttachView();
        } catch (Throwable t) {
            // Overlay permission revoked between the check and here: never crash.
            stopSelf();
            return;
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(this.screenReceiver, filter);

        registerProximity();
        running = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        running = false;

        handler.removeCallbacksAndMessages(null);

        try {
            unregisterReceiver(this.screenReceiver);
        } catch (Throwable ignored) {
        }
        if (this.sensorManager != null) {
            this.sensorManager.unregisterListener(this);
        }
        if (this.vibrator != null) {
            this.vibrator.cancel();
        }
        if (this.attached && this.root != null && this.windowManager != null) {
            try {
                this.windowManager.removeView(this.root);
            } catch (Throwable ignored) {
            }
        }
        this.attached = false;
        this.root = null;
        this.windowManager = null;
        this.sensorManager = null;
        this.vibrator = null;
        super.onDestroy();
    }

    // ═══════════════════════════════════ the view ═══════════════════════════════════

    private void buildAndAttachView() {
        this.root = View.inflate(this, R.layout.view_floating_tasbeeh, null);
        this.txtCount = this.root.findViewById(R.id.tasbeehCount);
        this.lockBadge = this.root.findViewById(R.id.tasbeehLockBadge);
        ImageView reset = this.root.findViewById(R.id.tasbeehReset);

        this.txtCount.setText(String.valueOf(this.count));
        updateLockBadge();

        int type = Build.VERSION.SDK_INT >= 26
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;
        this.params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        this.params.gravity = Gravity.TOP | Gravity.START;
        this.params.x = dp(16);
        this.params.y = dp(80);

        this.windowManager.addView(this.root, this.params);
        this.attached = true;
        this.root.setAlpha(ACTIVE_ALPHA);
        scheduleFade();

        this.root.setOnTouchListener(this::onRootTouch);

        // A stray single tap must never destroy a count, so reset is a long press.
        reset.setOnClickListener(v -> {
        });
        reset.setOnLongClickListener(v -> {
            if (!touchesAllowed()) {
                return true;
            }
            markActive();
            this.count = 0;
            this.tinyDB.putInt(PREF_COUNT, 0);
            this.txtCount.setText("0");
            tick(2);
            toast(R.string.tasbeeh_reset_done);
            return true;
        });
    }

    /** @return true when a tap may count and haptics may fire. */
    private boolean touchesAllowed() {
        return screenOn && !pocketCovered && !locked;
    }

    private boolean onRootTouch(View view, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                markActive();
                this.downRawX = event.getRawX();
                this.downRawY = event.getRawY();
                this.downParamX = this.params.x;
                this.downParamY = this.params.y;
                this.dragging = false;
                this.lockToggled = false;
                handler.postDelayed(this.lockRunnable, LOCK_LONG_PRESS_MS);
                return true;

            case MotionEvent.ACTION_MOVE:
                float dx = event.getRawX() - this.downRawX;
                float dy = event.getRawY() - this.downRawY;
                if (!this.dragging && Math.hypot(dx, dy) > dp(12)) {
                    this.dragging = true;
                    handler.removeCallbacks(this.lockRunnable);
                }
                if (this.dragging) {
                    moveWindow(this.downParamX + (int) dx, this.downParamY + (int) dy);
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                handler.removeCallbacks(this.lockRunnable);
                if (this.dragging) {
                    dockToNearestEdge();
                } else if (event.getActionMasked() == MotionEvent.ACTION_UP
                        && !this.lockToggled && touchesAllowed()) {
                    increment();
                }
                scheduleFade();
                return true;

            default:
                return true;
        }
    }

    private void increment() {
        this.count++;
        this.tinyDB.putInt(PREF_COUNT, this.count);
        this.txtCount.setText(String.valueOf(this.count));
        tick(1);
    }

    // ═══════════════════════════ movement, docking, fading ═══════════════════════════

    private void moveWindow(int x, int y) {
        int maxWidth = Math.max(0, windowWidth() - this.root.getWidth());
        int maxHeight = Math.max(0, windowHeight() - this.root.getHeight());
        this.params.x = clamp(x, 0, maxWidth);
        this.params.y = clamp(y, 0, maxHeight);
        try {
            this.windowManager.updateViewLayout(this.root, this.params);
        } catch (Throwable ignored) {
        }
    }

    /** Smoothly glides to the closest horizontal edge when released near a border. */
    private void dockToNearestEdge() {
        if (this.root == null) {
            return;
        }
        int width = this.root.getWidth();
        int screen = windowWidth();
        int margin = dp(EDGE_DOCK_DP);
        int trigger = dp(EDGE_TRIGGER_DP);

        int nearRight = screen - width - margin;
        boolean closeToLeft = this.params.x <= trigger;
        boolean closeToRight = this.params.x + width >= screen - trigger;
        if (!closeToLeft && !closeToRight) {
            return;
        }
        int target = closeToLeft ? margin : nearRight;
        int from = this.params.x;
        if (from == target) {
            return;
        }
        animateDock(from, target);
    }

    private void animateDock(int from, int target) {
        android.animation.ValueAnimator animator =
                android.animation.ValueAnimator.ofInt(from, target);
        animator.setDuration(180L);
        animator.addUpdateListener(animation ->
                moveWindow((int) animation.getAnimatedValue(), this.params.y));
        animator.start();
    }

    private void markActive() {
        handler.removeCallbacks(this.fadeRunnable);
        if (this.root != null) {
            this.root.animate().alpha(ACTIVE_ALPHA).setDuration(150L).start();
        }
    }

    private void scheduleFade() {
        handler.removeCallbacks(this.fadeRunnable);
        handler.postDelayed(this.fadeRunnable, FADE_DELAY_MS);
    }

    // ═════════════════════════════ pocket & screen guarding ═════════════════════════════

    private void registerProximity() {
        if (this.sensorManager != null && this.proximity != null) {
            this.sensorManager.registerListener(this, this.proximity,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    /** Screen off: nothing may run, sense or vibrate until the user unlocks again. */
    private void pauseEverything() {
        handler.removeCallbacksAndMessages(null);
        if (this.sensorManager != null) {
            this.sensorManager.unregisterListener(this);
        }
        if (this.vibrator != null) {
            this.vibrator.cancel();
        }
        this.pocketCovered = false;
    }

    private void resumeEverything() {
        registerProximity();
        markActive();
        scheduleFade();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_PROXIMITY) {
            return;
        }
        boolean covered = event.values.length > 0
                && event.values[0] < event.sensor.getMaximumRange();
        if (covered == this.pocketCovered) {
            return;
        }
        this.pocketCovered = covered;
        if (covered) {
            if (this.vibrator != null) {
                this.vibrator.cancel();
            }
            handler.removeCallbacks(this.fadeRunnable);
            if (this.root != null) {
                this.root.animate().alpha(IDLE_ALPHA).setDuration(120L).start();
            }
        } else {
            markActive();
            scheduleFade();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // irrelevant for proximity
    }

    // ═══════════════════════════════════ haptics ═══════════════════════════════════

    /** One very short, gentle tick. {@code pulses} = 2 confirms lock / reset actions. */
    private void tick(int pulses) {
        if (!screenOn || pocketCovered || this.vibrator == null
                || !this.vibrator.hasVibrator()) {
            return;
        }
        vibrateOnce();
        if (pulses > 1) {
            handler.postDelayed(this::vibrateOnce, 80L);
        }
    }

    private void vibrateOnce() {
        if (!screenOn || pocketCovered || this.vibrator == null
                || !this.vibrator.hasVibrator()) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 29) {
            this.vibrator.vibrate(VibrationEffect.createPredefined(
                    VibrationEffect.EFFECT_TICK));
        } else if (Build.VERSION.SDK_INT >= 26) {
            this.vibrator.vibrate(VibrationEffect.createOneShot(15L, 80));
        } else {
            this.vibrator.vibrate(15L);
        }
    }

    @Nullable
    private Vibrator obtainVibrator() {
        if (Build.VERSION.SDK_INT >= 31) {
            VibratorManager manager =
                    (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            return manager != null ? manager.getDefaultVibrator() : null;
        }
        return (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    // ═══════════════════════════════════ helpers ═══════════════════════════════════

    private void updateLockBadge() {
        if (this.lockBadge != null) {
            this.lockBadge.setVisibility(locked ? View.VISIBLE : View.GONE);
        }
    }

    private void toast(int messageRes) {
        // A service context speaks the device language; the toast must speak the app language.
        Toast.makeText(LocaleHelper.wrap(this), messageRes, Toast.LENGTH_SHORT).show();
    }

    private int windowWidth() {
        return getResources().getDisplayMetrics().widthPixels;
    }

    private int windowHeight() {
        return getResources().getDisplayMetrics().heightPixels;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static int clamp(int value, int min, int max) {
        return value < min ? min : (value > max ? max : value);
    }
}
