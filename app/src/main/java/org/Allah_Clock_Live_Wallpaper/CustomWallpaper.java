package org.Allah_Clock_Live_Wallpaper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Looper;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.Allah_Clock_Live_Wallpaper.utils.LocaleHelper;
import org.Allah_Clock_Live_Wallpaper.utils.TinyDB;
import org.Allah_Clock_Live_Wallpaper.viewUtils.WallpaperOverlayView;

import java.io.File;

public class CustomWallpaper extends WallpaperService {

    /** Logcat tag; the clock engine reports its canvas failures under it. */
    private static final String TAG = "CustomWallpaper";

    private Context context;
    int height;
    protected ImageView imageView;
    protected WallpaperOverlayView overlayView;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    TinyDB tinyDB;
    protected WidgetGroup widgetGroup;
    int width;

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
        try {
            if (imageView != null) {
                imageView.setImageDrawable(null);
                imageView.setImageBitmap(null);
            }
            if (widgetGroup != null) {
                widgetGroup.removeAllViews();
            }
        } catch (Throwable ignored) {
        }
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public Engine onCreateEngine() {
        return new ClockEngine();
    }

    public static class WidgetGroup extends ViewGroup {
        private final String TAG = getClass().getSimpleName();

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
        private Bitmap cachedBackground;

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
            try {
                if (cachedBackground != null && !cachedBackground.isRecycled()) {
                    cachedBackground.recycle();
                }
            } catch (Throwable ignored) {
            }
            cachedBackground = null;
            try {
                if (CustomWallpaper.this.imageView != null) {
                    CustomWallpaper.this.imageView.setImageDrawable(null);
                    CustomWallpaper.this.imageView.setImageBitmap(null);
                }
            } catch (Throwable ignored) {
            }
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
            applyBackground();
            CustomWallpaper.this.imageView.layout(0, 0, CustomWallpaper.this.width, CustomWallpaper.this.height);
            CustomWallpaper.this.overlayView.layout(0, 0, CustomWallpaper.this.width, CustomWallpaper.this.height);
            CustomWallpaper.this.widgetGroup.draw(canvas);
        }

        /**
         * Smart descending compatibility chain for every legacy TinyDB state.
         * Order: isImage -> ImageString / isWallpaper file, then isCustomBg -> customBg drawable,
         * then legacy isWallpaper file, then customBg fallback, then solid bgColor.
         * Every file check validates existence before decode to avoid wasted I/O on missing paths.
         */
        private void applyBackground() {
            ImageView iv = CustomWallpaper.this.imageView;
            TinyDB db = CustomWallpaper.this.tinyDB;
            if (iv == null || db == null) {
                return;
            }
            // Clear previous drawable to prevent recycled bitmap reuse
            try {
                iv.setImageDrawable(null);
            } catch (Throwable ignored) {
            }

            boolean isImage = false;
            boolean isCustomBg = false;
            try { isImage = db.getBoolean("isImage"); } catch (Throwable ignored) {}
            try { isCustomBg = db.getBoolean("isCustomBg"); } catch (Throwable ignored) {}
            String imageString = "";
            String wallpaperPath = "";
            try { imageString = db.getString("ImageString"); } catch (Throwable ignored) {}
            try { wallpaperPath = db.getString("isWallpaper"); } catch (Throwable ignored) {}
            int customBg = 0;
            try { customBg = db.getInt("customBg"); } catch (Throwable ignored) {}
            if (imageString == null) imageString = "";
            if (wallpaperPath == null) wallpaperPath = "";

            // 1) isImage -> file
            if (isImage) {
                String path = !imageString.isEmpty() ? imageString : wallpaperPath;
                Bitmap bmp = safeDecode(path);
                if (bmp != null) {
                    replaceBitmap(bmp);
                    return;
                }
                // Fallback to drawable if file missing
                if (customBg != 0) {
                    try {
                        iv.setImageResource(customBg);
                        clearCachedBitmap();
                        return;
                    } catch (Throwable ignored) {}
                }
            } else if (isCustomBg) {
                // 2) isCustomBg -> drawable
                if (customBg != 0) {
                    try {
                        iv.setImageResource(customBg);
                        clearCachedBitmap();
                        return;
                    } catch (Throwable ignored) {}
                }
                String path = !wallpaperPath.isEmpty() ? wallpaperPath : imageString;
                Bitmap bmp = safeDecode(path);
                if (bmp != null) {
                    replaceBitmap(bmp);
                    return;
                }
            } else {
                // 3) Neither flag: try legacy file paths and drawable
                String path = !wallpaperPath.isEmpty() ? wallpaperPath : imageString;
                if (!path.isEmpty()) {
                    Bitmap bmp = safeDecode(path);
                    if (bmp != null) {
                        replaceBitmap(bmp);
                        return;
                    }
                }
                if (customBg != 0) {
                    try {
                        iv.setImageResource(customBg);
                        clearCachedBitmap();
                        return;
                    } catch (Throwable ignored) {}
                }
            }
            // 4) Solid color fallback
            iv.setImageResource(0);
            clearCachedBitmap();
            int bg = Color.WHITE;
            try { bg = db.getInt("bgColor"); } catch (Throwable ignored) {}
            // 0 is transparent - fallback to white for visibility
            if (bg == 0) bg = Color.WHITE;
            try { iv.setBackgroundColor(bg); } catch (Throwable ignored) {}
        }

        private Bitmap safeDecode(String path) {
            if (path == null || path.isEmpty()) {
                return null;
            }
            try {
                File f = new File(path);
                if (!f.exists() || f.length() == 0) {
                    return null;
                }
                return BitmapFactory.decodeFile(path);
            } catch (Throwable e) {
                Log.e(TAG, "decode failed: " + path, e);
                return null;
            } catch (OutOfMemoryError e) {
                Log.e(TAG, "OOM decoding: " + path, e);
                return null;
            }
        }

        private void replaceBitmap(Bitmap bmp) {
            try {
                if (cachedBackground != null && cachedBackground != bmp && !cachedBackground.isRecycled()) {
                    cachedBackground.recycle();
                }
            } catch (Throwable ignored) {}
            cachedBackground = bmp;
            try {
                CustomWallpaper.this.imageView.setImageBitmap(bmp);
            } catch (Throwable e) {
                Log.e(TAG, "setImageBitmap failed", e);
            }
        }

        private void clearCachedBitmap() {
            try {
                if (cachedBackground != null && !cachedBackground.isRecycled()) {
                    cachedBackground.recycle();
                }
            } catch (Throwable ignored) {}
            cachedBackground = null;
        }
    }
}
