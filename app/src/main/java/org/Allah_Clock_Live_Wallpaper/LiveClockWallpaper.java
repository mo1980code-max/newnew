package org.Allah_Clock_Live_Wallpaper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.Allah_Clock_Live_Wallpaper.model.Clocks;
import org.Allah_Clock_Live_Wallpaper.utils.FrameRate;
import org.Allah_Clock_Live_Wallpaper.utils.LocaleHelper;
import org.Allah_Clock_Live_Wallpaper.utils.TinyDB;
import org.Allah_Clock_Live_Wallpaper.viewUtils.AnalogClock;
import org.Allah_Clock_Live_Wallpaper.viewUtils.SmartClockPreview;
import org.Allah_Clock_Live_Wallpaper.viewUtils.TextClockPreview;
import org.Allah_Clock_Live_Wallpaper.viewUtils.WallpaperOverlayView;

import java.io.File;

public class LiveClockWallpaper extends WallpaperService {

    /** Logcat tag; the clock engine reports its canvas failures under it. */
    private static final String TAG = "LiveClockWallpaper";

    protected TextClockPreview cat1Clock;
    private Context context;
    int height;
    protected ImageView imageView;
    protected AnalogClock imageViewBase;
    private int mClockSize;
    private int mHalfWidth;
    protected SmartClockPreview smartClockPreview;
    protected WallpaperOverlayView overlayView;
    TinyDB tinyDB;
    protected WidgetGroup widgetGroup;
    int width;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private float mClockPosX = -1.0f;
    private float mClockPosY = -1.0f;

    @Override
    public void onCreate() {
        super.onCreate();
        Context applicationContext = getApplicationContext();
        this.context = applicationContext;
        this.tinyDB = new TinyDB(applicationContext);
        init(this.context);
    }

    public void init(Context context) {
        // Views read their text (weekday, month, Hijri date, dhikr, athkar badge) from
        // resources, so they must be built with a context that speaks the in-app language.
        Context ui = LocaleHelper.wrap(context);
        WidgetGroup widgetGroup = new WidgetGroup(ui);
        this.widgetGroup = widgetGroup;
        widgetGroup.removeAllViews();
        this.imageViewBase = new AnalogClock(ui);
        ImageView imageView = new ImageView(ui);
        this.imageView = imageView;
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        this.cat1Clock = new TextClockPreview(ui);
        this.smartClockPreview = new SmartClockPreview(ui);
        // The engine drives the frame rate itself (see FrameRate), so the view must not
        // run its own 800ms ticker on top of it.
        this.imageViewBase.setAutoUpdate(false);
        this.widgetGroup.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        this.widgetGroup.setAddStatesFromChildren(true);
        this.widgetGroup.addView(this.imageView);
        this.widgetGroup.addView(this.imageViewBase);
        this.imageViewBase.setVisibility(View.GONE);
        this.widgetGroup.addView(this.cat1Clock);
        this.widgetGroup.addView(this.smartClockPreview);
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
        /**
         * The gallery / wallpaper-file bitmap currently shown by {@link #imageView}, or
         * {@code null} while a bundled drawable or a solid colour is shown instead.
         * It is kept between frames so the same file is not decoded up to 30 times a second
         * in smooth mode, and dropped the moment it is stale: a different file, the same file
         * rewritten ({@code SetWallpaperActivity} always writes {@code wallpaper.jpg}), or the
         * wallpaper becoming visible again after the user left the editor.
         */
        Bitmap aa;
        /** Absolute path {@link #aa} was decoded from; {@code null} when nothing is cached. */
        private String aaPath;
        /** {@link File#length()} and {@link File#lastModified()} of that file at decode time. */
        private long aaLength;
        private long aaModified;
        /** Screen size the sub-sampling was chosen for; 0 = decoded at full resolution. */
        private int aaTarget;
        /**
         * Key (path, size, mtime) of the last file that could not be decoded, so a corrupt or
         * too-large file is not retried - and its error not logged - on every single frame.
         * Cleared when the file changes or the wallpaper becomes visible again.
         */
        private String failedPath;
        private long failedLength;
        private long failedModified;
        /** Last {@code customBg} id reported as unloadable, so the warning is logged once. */
        private int loggedBadResId;

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
            LiveClockWallpaper.this.mHandler.removeCallbacks(this.mDrawClock);
            releaseBitmap();
            try {
                if (LiveClockWallpaper.this.imageView != null) {
                    LiveClockWallpaper.this.imageView.setImageDrawable(null);
                    LiveClockWallpaper.this.imageView.setBackground(null);
                }
            } catch (Throwable ignored) {
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            this.mVisible = visible;
            if (visible) {
                // The user may have just picked a new background in the editor or the
                // wallpaper gallery: drop the cached bitmap so the very next frame re-reads
                // TinyDB and decodes whatever is current instead of showing the old image.
                invalidateBackgroundCache();
                drawFrame();
            } else {
                LiveClockWallpaper.this.mHandler.removeCallbacks(this.mDrawClock);
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
            super.onSurfaceChanged(surfaceHolder, i, i2, i3);
            LiveClockWallpaper.this.width = i2;
            LiveClockWallpaper.this.height = i3;
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
            LiveClockWallpaper.this.mHandler.removeCallbacks(this.mDrawClock);
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
                LiveClockWallpaper.this.mHandler.removeCallbacks(this.mDrawClock);
                if (this.mVisible) {
                    LiveClockWallpaper.this.mHandler.postDelayed(this.mDrawClock,
                            FrameRate.liveClockDelayMs(LiveClockWallpaper.this.tinyDB,
                                    LiveClockWallpaper.this.tinyDB.getInt("clockType")));
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
            LiveClockWallpaper.this.widgetGroup.layout(0, 0, LiveClockWallpaper.this.width, LiveClockWallpaper.this.height);
            LiveClockWallpaper liveClockWallpaper = LiveClockWallpaper.this;
            liveClockWallpaper.mClockPosX = liveClockWallpaper.tinyDB.getFloat("prefClockPosX", ((float) LiveClockWallpaper.this.width) / 2.0f);
            LiveClockWallpaper liveClockWallpaper2 = LiveClockWallpaper.this;
            liveClockWallpaper2.mClockPosY = liveClockWallpaper2.tinyDB.getFloat("prefClockPosY", ((float) LiveClockWallpaper.this.height) / 2.0f);
            LiveClockWallpaper liveClockWallpaper3 = LiveClockWallpaper.this;
            liveClockWallpaper3.mClockSize = liveClockWallpaper3.tinyDB.getInt("prefSize");
            Clocks clocks = (Clocks) LiveClockWallpaper.this.tinyDB.getObject("clocks", Clocks.class);
            int i = LiveClockWallpaper.this.tinyDB.getInt("textClockPosition");
            applyBackground();
            LiveClockWallpaper.this.imageView.layout(0, 0, LiveClockWallpaper.this.width, LiveClockWallpaper.this.height);
            LiveClockWallpaper.this.overlayView.layout(0, 0, LiveClockWallpaper.this.width, LiveClockWallpaper.this.height);
            if (LiveClockWallpaper.this.tinyDB.getInt("clockType") == 0) {
                LiveClockWallpaper.this.imageViewBase.setClock(clocks);
                LiveClockWallpaper.this.imageViewBase.setTime(System.currentTimeMillis());
                LiveClockWallpaper.this.imageViewBase.setClockSize((float) LiveClockWallpaper.this.mClockSize);
                LiveClockWallpaper.this.imageViewBase.setPosition(LiveClockWallpaper.this.mClockPosX, LiveClockWallpaper.this.mClockPosY);
                LiveClockWallpaper.this.imageViewBase.layout(0, 0, LiveClockWallpaper.this.width, LiveClockWallpaper.this.height);
                LiveClockWallpaper.this.imageViewBase.setVisibility(View.VISIBLE);
                LiveClockWallpaper.this.smartClockPreview.setVisibility(View.GONE);
                LiveClockWallpaper.this.cat1Clock.setVisibility(View.GONE);
            } else if (LiveClockWallpaper.this.tinyDB.getInt("clockType") == 1) {
                LiveClockWallpaper.this.smartClockPreview.layout(0, 0, LiveClockWallpaper.this.width, LiveClockWallpaper.this.height);
                LiveClockWallpaper.this.smartClockPreview.setTextClockPosition(i);
                LiveClockWallpaper.this.smartClockPreview.config(LiveClockWallpaper.this.mClockPosX, LiveClockWallpaper.this.mClockPosY, LiveClockWallpaper.this.mClockSize * 2);
                LiveClockWallpaper.this.imageViewBase.setVisibility(View.GONE);
                LiveClockWallpaper.this.smartClockPreview.setVisibility(View.VISIBLE);
                LiveClockWallpaper.this.cat1Clock.setVisibility(View.GONE);
            } else if (LiveClockWallpaper.this.tinyDB.getInt("clockType") == 2) {
                LiveClockWallpaper.this.cat1Clock.layout(0, 0, LiveClockWallpaper.this.width, LiveClockWallpaper.this.height);
                LiveClockWallpaper.this.cat1Clock.setTextClockPosition(i);
                LiveClockWallpaper.this.cat1Clock.setColors(LiveClockWallpaper.this.tinyDB.getInt("textColor1", -1), LiveClockWallpaper.this.tinyDB.getInt("textColor2", Color.WHITE));
                LiveClockWallpaper.this.cat1Clock.config(LiveClockWallpaper.this.mClockPosX, LiveClockWallpaper.this.mClockPosY, LiveClockWallpaper.this.mClockSize * 2);
                LiveClockWallpaper.this.imageViewBase.setVisibility(View.GONE);
                LiveClockWallpaper.this.smartClockPreview.setVisibility(View.GONE);
                LiveClockWallpaper.this.cat1Clock.setVisibility(View.VISIBLE);
            }
            LiveClockWallpaper.this.widgetGroup.draw(canvas);
        }

        /**
         * Paints the wallpaper background exactly as {@code EditorActivity.updateClock()} previews
         * it, in this order:
         * <ol>
         *   <li>{@code isImage}: the gallery / wallpaper file ({@code ImageString}, else the legacy
         *       {@code isWallpaper}), falling back to {@code customBg} if the file is gone;</li>
         *   <li>{@code isCustomBg}: the bundled {@code customBg} drawable, falling back to a file
         *       path left over from an older version;</li>
         *   <li>neither flag (prefs written by an older version): a legacy file path if any;</li>
         *   <li>otherwise the solid {@code bgColor}.</li>
         * </ol>
         * Every branch resets the whole ImageView (cached bitmap, drawable resource and background
         * colour), so leftovers of the previous choice can never hide the new one.
         */
        private void applyBackground() {
            ImageView iv = LiveClockWallpaper.this.imageView;
            TinyDB db = LiveClockWallpaper.this.tinyDB;
            if (iv == null || db == null) {
                return;
            }
            boolean isImage = false;
            boolean isCustomBg = false;
            String imageString = "";
            String wallpaperPath = "";
            int customBg = 0;
            try { isImage = db.getBoolean("isImage"); } catch (Throwable ignored) {}
            try { isCustomBg = db.getBoolean("isCustomBg"); } catch (Throwable ignored) {}
            try { imageString = db.getString("ImageString"); } catch (Throwable ignored) {}
            try { wallpaperPath = db.getString("isWallpaper"); } catch (Throwable ignored) {}
            try { customBg = db.getInt("customBg"); } catch (Throwable ignored) {}
            if (imageString == null) imageString = "";
            if (wallpaperPath == null) wallpaperPath = "";

            if (isImage) {
                String path = !imageString.isEmpty() ? imageString : wallpaperPath;
                if (showImageFile(iv, path)) {
                    return;
                }
                if (customBg != 0 && showResource(iv, customBg)) {
                    return;
                }
            } else if (isCustomBg) {
                if (customBg != 0 && showResource(iv, customBg)) {
                    return;
                }
                String path = !wallpaperPath.isEmpty() ? wallpaperPath : imageString;
                if (showImageFile(iv, path)) {
                    return;
                }
            } else {
                String path = !wallpaperPath.isEmpty() ? wallpaperPath : imageString;
                if (showImageFile(iv, path)) {
                    return;
                }
            }
            showSolidColor(iv, db);
        }

        /**
         * Shows the image file at {@code path} - the freshly decoded bitmap, or the cached one
         * when it is still that exact file - with no colour behind it. Returns {@code false}
         * (and leaves nothing recycled in the view) when the file is missing or undecodable.
         */
        private boolean showImageFile(ImageView iv, String path) {
            Bitmap bmp = loadBackgroundBitmap(path);
            if (bmp == null) {
                return false;
            }
            try {
                if (!showsBitmap(iv, bmp)) {
                    iv.setImageDrawable(null);
                    iv.setImageBitmap(bmp);
                }
                iv.setBackground(null);
                return true;
            } catch (Throwable e) {
                Log.e(TAG, "setImageBitmap failed for " + path, e);
                releaseBitmap();
                return false;
            }
        }

        /**
         * Shows a bundled drawable instead of any cached bitmap or colour. Returns {@code false}
         * when {@code resId} no longer names a drawable (ids stored by an older build can drift),
         * so the caller can fall back rather than paint nothing.
         */
        private boolean showResource(ImageView iv, int resId) {
            releaseBitmap();
            Throwable failure = null;
            try {
                iv.setImageBitmap(null);
                iv.setBackground(null);
                iv.setImageResource(resId);
                if (iv.getDrawable() != null) {
                    return true;
                }
            } catch (Throwable e) {
                failure = e;
            }
            if (resId != loggedBadResId) {
                loggedBadResId = resId;
                Log.w(TAG, "customBg resource " + resId + " could not be loaded", failure);
            }
            try { iv.setImageDrawable(null); } catch (Throwable ignored) {}
            return false;
        }

        /** Shows the solid {@code bgColor}: no cached bitmap, no drawable, just the colour. */
        private void showSolidColor(ImageView iv, TinyDB db) {
            releaseBitmap();
            try { iv.setImageDrawable(null); } catch (Throwable ignored) {}
            int bg = Color.WHITE;
            try { bg = db.getInt("bgColor"); } catch (Throwable ignored) {}
            // 0 is transparent - never a deliberate choice - so fall back to white for visibility.
            if (bg == 0) bg = Color.WHITE;
            try { iv.setBackgroundColor(bg); } catch (Throwable ignored) {}
        }

        /**
         * Returns the bitmap for {@code path}, reusing {@link #aa} only while it holds this exact
         * file (same path, size and modification time) at a size that still covers the screen.
         * Anything else - a new gallery pick, {@code wallpaper.jpg} rewritten in place, or the
         * cache dropped by {@link #onVisibilityChanged} - recycles the old bitmap and decodes the
         * file afresh. Returns {@code null} when the file is missing or cannot be decoded.
         */
        private Bitmap loadBackgroundBitmap(String path) {
            if (path == null || path.isEmpty()) {
                return null;
            }
            long length;
            long modified;
            try {
                File file = new File(path);
                length = file.length();
                modified = file.lastModified();
            } catch (Throwable e) {
                Log.w(TAG, "cannot stat " + path, e);
                return null;
            }
            if (length <= 0L) {
                releaseBitmap();
                return null;
            }
            int target = Math.max(LiveClockWallpaper.this.width, LiveClockWallpaper.this.height);
            boolean sameFile = aa != null && !aa.isRecycled() && path.equals(aaPath)
                    && length == aaLength && modified == aaModified;
            boolean bigEnough = aaTarget == 0 || aaTarget >= target;
            if (sameFile && bigEnough) {
                return aa;
            }
            releaseBitmap();
            if (path.equals(failedPath) && length == failedLength && modified == failedModified) {
                // This exact file already failed; the caller falls back until it changes.
                return null;
            }
            int[] sample = new int[1];
            Bitmap decoded = decodeFile(path, target, sample);
            if (decoded == null) {
                failedPath = path;
                failedLength = length;
                failedModified = modified;
                return null;
            }
            failedPath = null;
            aa = decoded;
            aaPath = path;
            aaLength = length;
            aaModified = modified;
            aaTarget = sample[0] > 1 ? target : 0;
            Log.d(TAG, "background decoded: " + path + " " + decoded.getWidth() + "x"
                    + decoded.getHeight() + " (sample " + sample[0] + ")");
            return decoded;
        }

        /**
         * {@link BitmapFactory#decodeFile} with the largest power-of-two sub-sample that keeps
         * both sides at least {@code target} pixels, so CENTER_CROP never has to upscale in either
         * orientation while a 48-megapixel gallery photo no longer needs ~200 MB to display.
         */
        private Bitmap decodeFile(String path, int target, int[] sampleOut) {
            int sample = 1;
            try {
                if (target > 0) {
                    BitmapFactory.Options bounds = new BitmapFactory.Options();
                    bounds.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(path, bounds);
                    if (bounds.outWidth > 0 && bounds.outHeight > 0) {
                        while (bounds.outWidth / (sample * 2) >= target
                                && bounds.outHeight / (sample * 2) >= target) {
                            sample *= 2;
                        }
                    }
                }
                sampleOut[0] = sample;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = sample;
                return BitmapFactory.decodeFile(path, options);
            } catch (OutOfMemoryError e) {
                Log.e(TAG, "out of memory decoding " + path + " (sample " + sample + ")", e);
                return null;
            } catch (Throwable e) {
                Log.e(TAG, "decode failed: " + path, e);
                return null;
            }
        }

        /** {@code true} when {@code iv} is currently drawing exactly {@code bmp}. */
        private boolean showsBitmap(ImageView iv, Bitmap bmp) {
            Drawable d = iv.getDrawable();
            return d instanceof BitmapDrawable && ((BitmapDrawable) d).getBitmap() == bmp;
        }

        /**
         * Drops everything remembered about the background - the cached bitmap and the
         * "this file cannot be decoded" note - so the next frame starts from TinyDB alone.
         */
        private void invalidateBackgroundCache() {
            releaseBitmap();
            failedPath = null;
            failedLength = 0L;
            failedModified = 0L;
        }

        /**
         * Forgets {@link #aa}: detaches it from the ImageView first (a recycled bitmap must never
         * reach {@code Canvas.drawBitmap}), then recycles it and clears the cache key.
         */
        private void releaseBitmap() {
            Bitmap old = aa;
            aa = null;
            aaPath = null;
            aaLength = 0L;
            aaModified = 0L;
            aaTarget = 0;
            if (old == null) {
                return;
            }
            try {
                ImageView iv = LiveClockWallpaper.this.imageView;
                if (iv != null && showsBitmap(iv, old)) {
                    iv.setImageDrawable(null);
                }
            } catch (Throwable ignored) {}
            try {
                if (!old.isRecycled()) {
                    old.recycle();
                }
            } catch (Throwable ignored) {}
        }
    }
}
