package org.Allah_Clock_Live_Wallpaper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.Allah_Clock_Live_Wallpaper.utils.LocaleHelper;
import org.Allah_Clock_Live_Wallpaper.viewUtils.WallpaperOverlayView;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.Allah_Clock_Live_Wallpaper.utils.TinyDB;

import java.io.File;



public class CustomWallpaper extends WallpaperService {

    /** Logcat tag; {@code adb logcat -s CustomWallpaper CRITICAL_DEBUG} shows every failure. */
    private static final String TAG = "CustomWallpaper";

    /** Tag for the failures that used to leave the user with a blank home screen. */
    private static final String CRITICAL = "CRITICAL_DEBUG";

    /** Preference key holding the app-private JPEG the picked background was decoded into. */
    private static final String PREF_BACKGROUND_PATH = "isWallpaper";

    /**
     * Shown when the stored path is missing or unreadable, so the live wallpaper is never a black
     * rectangle with a clock on it. Any bundled wallpaper works; this one is the catalogue's
     * first category cover.
     */
    private static final int FALLBACK_BACKGROUND = R.drawable.wp_kaaba_1;

    /** Longest decoded side in px, so one background cannot exhaust the wallpaper process heap. */
    private static final int MAX_BACKGROUND_SIDE = 2048;

    private Context context;
    int height;
    protected ImageView imageView;
    protected WallpaperOverlayView overlayView;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    TinyDB tinyDB;
    protected WidgetGroup widgetGroup;
    int width;

    /** The path {@link #backgroundBitmap} was decoded from; a change re-decodes, nothing else. */
    private String backgroundPath;
    private Bitmap backgroundBitmap;

    @Override
    public void onCreate() {
        super.onCreate();
        Context applicationContext = getApplicationContext();
        this.context = applicationContext;
        this.tinyDB = new TinyDB(applicationContext);
        init(this.context);
    }

    public void init(Context context) {
        // The overlay draws the Hijri date, the rotating dhikr and the athkar badge.
        Context ui = LocaleHelper.wrap(context);
        WidgetGroup widgetGroup = new WidgetGroup(ui);
        this.widgetGroup = widgetGroup;
        widgetGroup.removeAllViews();
        ImageView imageView = new ImageView(ui);
        this.imageView = imageView;
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        this.widgetGroup.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        this.widgetGroup.setAddStatesFromChildren(true);
        this.widgetGroup.addView(this.imageView);
        this.overlayView = new WallpaperOverlayView(ui);
        this.widgetGroup.addView(this.overlayView);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Engine onCreateEngine() {
        return new ClockEngine();
    }

    /**
     * Paints the background the user picked in {@code SetWallpaperActivity}.
     *
     * <p>The previous implementation called {@code BitmapFactory.decodeFile(tinyDB.getString(
     * "isWallpaper"))} on <b>every single frame</b>. Two things follow from that, and both show
     * up as "the backgrounds do not open":</p>
     * <ol>
     *   <li>{@code TinyDB.getString} returns {@code ""} for a key nobody wrote yet, and
     *       {@code decodeFile("")} returns {@code null} — so on a fresh install, after clearing
     *       app data, or after a device transfer (Auto Backup restores {@code SharedPreferences}
     *       but <i>not</i> {@code files/wallpapers/wallpaper.jpg}), the ImageView was handed
     *       {@code null} and the home screen showed the clock over nothing at all;</li>
     *   <li>a full JPEG was decoded again every frame, which on a large image is enough to get
     *       the wallpaper process killed for memory — and a killed {@code WallpaperService} also
     *       shows as a blank background.</li>
     * </ol>
     *
     * <p>So: decode once per path, keep the bitmap, and fall back to a bundled wallpaper — with
     * the reason in logcat — whenever the stored file cannot be used.</p>
     */
    void applyBackground() {
        String path = this.tinyDB.getString(PREF_BACKGROUND_PATH);
        if (path != null && !path.isEmpty() && path.equals(this.backgroundPath)
                && this.backgroundBitmap != null) {
            this.imageView.setImageBitmap(this.backgroundBitmap);
            return;
        }
        Bitmap bitmap = decodeBackground(path);
        if (bitmap != null) {
            this.backgroundPath = path;
            this.backgroundBitmap = bitmap;
            this.imageView.setImageBitmap(bitmap);
            return;
        }
        // Never leave the surface empty: the user still has a working wallpaper, and logcat says
        // exactly which file was missing.
        this.backgroundPath = null;
        this.backgroundBitmap = null;
        this.imageView.setImageResource(FALLBACK_BACKGROUND);
    }

    /** @return the decoded background, or {@code null} with the reason logged. */
    private Bitmap decodeBackground(String path) {
        if (path == null || path.isEmpty()) {
            Log.e(CRITICAL, "no background stored under \"" + PREF_BACKGROUND_PATH
                    + "\" — showing the bundled fallback. (Set a background from the app's"
                    + " wallpaper screen; the live wallpaper can also be picked straight from"
                    + " Android's own wallpaper list, before the app ever wrote this key.)");
            return null;
        }
        File file = new File(path);
        if (!file.exists() || file.length() == 0) {
            Log.e(CRITICAL, "stored background is not readable: " + path + " (exists="
                    + file.exists() + ", length=" + file.length() + ") — showing the bundled"
                    + " fallback. The path survives a backup/restore but the file does not.");
            return null;
        }
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, bounds);
            int sample = 1;
            while (Math.max(bounds.outWidth, bounds.outHeight) / (sample * 2)
                    >= MAX_BACKGROUND_SIDE) {
                sample *= 2;
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sample;
            Bitmap bitmap = BitmapFactory.decodeFile(path, options);
            if (bitmap == null) {
                Log.e(CRITICAL, "BitmapFactory could not decode " + path + " (" + bounds.outWidth
                        + "x" + bounds.outHeight + ") — showing the bundled fallback");
            }
            return bitmap;
        } catch (Throwable t) {
            Log.e(CRITICAL, "decoding the stored background failed: " + path, t);
            return null;
        }
    }

    public static class WidgetGroup extends ViewGroup {

        public WidgetGroup(Context context) {
            super(context);
            setWillNotDraw(true);
        }

        @Override
        protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
            layout(i, i2, i3, i4);
        }
    }



    class ClockEngine extends Engine {
        private final Runnable mDrawClock = new Runnable() {
            @Override
            public void run() {
                ClockEngine.this.drawFrame();
            }
        };
        private boolean mVisible;

        ClockEngine() {
            super();
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            CustomWallpaper.this.mHandler.removeCallbacks(this.mDrawClock);
        }

        @Override
        public void onVisibilityChanged(boolean z) {
            this.mVisible = z;
            if (z) {
                drawFrame();
            } else {
                CustomWallpaper.this.mHandler.removeCallbacks(this.mDrawClock);
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
            super.onSurfaceChanged(surfaceHolder, i, i2, i3);
            CustomWallpaper.this.width = i2;
            CustomWallpaper.this.height = i3;
            drawFrame();
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder surfaceHolder) {
            super.onSurfaceCreated(surfaceHolder);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder surfaceHolder) {
            super.onSurfaceDestroyed(surfaceHolder);
            this.mVisible = false;
            CustomWallpaper.this.mHandler.removeCallbacks(this.mDrawClock);
        }

        @Override
        public void onOffsetsChanged(float f, float f2, float f3, float f4, int i, int i2) {
            drawFrame();
        }

        void drawFrame() {
            Throwable th;
            Canvas canvas;
            SurfaceHolder surfaceHolder = getSurfaceHolder();
            try {
                canvas = surfaceHolder.lockCanvas();
                if (canvas != null) {
                    try {
                        drawClock(canvas);
                    } catch (Throwable th2) {
                        th = th2;
                        if (canvas != null) {
                            try {
                                surfaceHolder.unlockCanvasAndPost(canvas);
                            } catch (IllegalArgumentException e) {
                                Log.w(TAG, "unlockCanvasAndPost after draw failure", e);
                            }
                        }
                        throw th;
                    }
                }
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    } catch (IllegalArgumentException e2) {
                        Log.w(TAG, "unlockCanvasAndPost failed", e2);
                    }
                }
                CustomWallpaper.this.mHandler.removeCallbacks(this.mDrawClock);
                if (this.mVisible) {
                    CustomWallpaper.this.mHandler.postDelayed(this.mDrawClock, 10000);
                }
            } catch (Throwable th3) {
                th = th3;
                canvas = null;
            }
        }

        void drawClock(Canvas canvas) {
            canvas.save();
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            firstClock(canvas);
            canvas.restore();
        }

        public void firstClock(Canvas canvas) {
            CustomWallpaper.this.widgetGroup.layout(0, 0, CustomWallpaper.this.width, CustomWallpaper.this.height);
            CustomWallpaper.this.applyBackground();
            CustomWallpaper.this.imageView.layout(0, 0, CustomWallpaper.this.width, CustomWallpaper.this.height);
            CustomWallpaper.this.overlayView.layout(0, 0, CustomWallpaper.this.width, CustomWallpaper.this.height);
            CustomWallpaper.this.widgetGroup.draw(canvas);
        }
    }
}
