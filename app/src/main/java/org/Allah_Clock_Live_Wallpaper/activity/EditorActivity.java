package org.Allah_Clock_Live_Wallpaper.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import org.Allah_Clock_Live_Wallpaper.LiveClockWallpaper;
import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.adapter.BgAdapter;
import org.Allah_Clock_Live_Wallpaper.ads.AdManager;
import org.Allah_Clock_Live_Wallpaper.model.Clocks;
import org.Allah_Clock_Live_Wallpaper.utils.FrameRate;
import org.Allah_Clock_Live_Wallpaper.utils.TinyDB;
import org.Allah_Clock_Live_Wallpaper.utils.UiCompat;
import org.Allah_Clock_Live_Wallpaper.utils.WallpaperHelper;
import org.Allah_Clock_Live_Wallpaper.viewUtils.AnalogClock;
import org.Allah_Clock_Live_Wallpaper.viewUtils.SmartClockPreview;
import org.Allah_Clock_Live_Wallpaper.viewUtils.TextClockPreview;
import org.Allah_Clock_Live_Wallpaper.viewUtils.WallpaperOverlayView;

import java.io.File;

/**
 * Clock editor: position, size, colours, background and finally "apply as live wallpaper".
 *
 * <p>The interstitial is pre-loaded on entry but only shown once the system wallpaper
 * chooser reports the wallpaper was really applied — the natural, user-expected moment.
 * Nothing is shown when the user cancels, and nothing is ever shown before the action.</p>
 *
 * <p>Gallery picks are copied into app-private storage instead of remembering a content
 * uri or a raw file path: under scoped storage (Android 10+) neither of those is readable
 * from a WallpaperService, which is a separate component from this Activity.</p>
 */
public class EditorActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "EditorActivity";
    private static final int DEFAULT_CLOCK_SIZE = 500;
    private static final int MAX_CUSTOM_BACKGROUNDS = 6;

    private AnalogClock analogClock;
    private BgAdapter bgAdapter;
    private RecyclerView bgRecyclerView;
    private Button btnColor1;
    private Button btnColor2;
    private ImageView btnOk;
    private ImageView icDone;
    private ImageView ivBackground;
    private ImageView ivCenter;
    private ImageView ivColor;
    private ImageView ivGallery;
    private ImageView ivTextColor;
    private ImageView ivZoomIn;
    private ImageView ivZoomOut;
    private ImageView ivOptions;
    private WallpaperOverlayView overlayPreview;
    private LinearLayout layoutBottom;
    private LinearLayout layoutColor;
    private int mClockSize;
    private int mHeight;
    private ImageView mIvMainScreen;
    public int mWidth;
    private AppCompatSeekBar seekBar;
    private SmartClockPreview smartClockPreview;
    private TextClockPreview textClockPreview;
    private TinyDB tinyDB;

    private float mClockPosX = 100.0f;
    private float mClockPosY = 100.0f;
    private int textClockPosition = 0;
    private boolean mIsCenterLine = false;
    private int color1 = -1;
    private int color2 = -1;

    /** Returns from the system "set live wallpaper" screen. */
    private final ActivityResultLauncher<Intent> wallpaperLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                boolean applied = result.getResultCode() == Activity.RESULT_OK
                        || WallpaperHelper.isOurLiveWallpaperSet(EditorActivity.this);
                if (applied) {
                    AdManager.showInterstitial(EditorActivity.this, EditorActivity.this::finish);
                } else {
                    finish();
                }
            });

    /** Returns from the system image picker. */
    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        importPickedImage(imageUri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_editor);
        this.tinyDB = new TinyDB(this);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        this.mWidth = displayMetrics.widthPixels;
        this.mHeight = displayMetrics.heightPixels;

        initView();
        UiCompat.applyImmersive(this);

        // Have an interstitial ready for the moment the wallpaper has been applied.
        AdManager.preloadInterstitial(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        UiCompat.applyImmersive(this);
        if (this.overlayPreview != null) {
            this.overlayPreview.refresh();
        }
    }

    private void initView() {
        this.analogClock = findViewById(R.id.analogClock);
        this.textClockPreview = findViewById(R.id.textClockPreview);
        this.smartClockPreview = findViewById(R.id.smartClockPreview);
        this.bgRecyclerView = findViewById(R.id.bgRecyclerView);
        this.layoutBottom = findViewById(R.id.layoutBottom);
        this.mIvMainScreen = findViewById(R.id.imageView);
        this.ivColor = findViewById(R.id.ivColor);
        this.ivGallery = findViewById(R.id.ivGallery);
        this.ivBackground = findViewById(R.id.ivBackground);
        this.btnOk = findViewById(R.id.btn_ok);
        this.ivZoomOut = findViewById(R.id.ivZoomOut);
        this.ivTextColor = findViewById(R.id.ivTextColor);
        this.ivZoomIn = findViewById(R.id.ivZoomIn);
        this.ivCenter = findViewById(R.id.ivCenter);
        this.icDone = findViewById(R.id.icDone);
        this.layoutColor = findViewById(R.id.layoutColor);
        this.btnColor1 = findViewById(R.id.btnColor1);
        this.btnColor2 = findViewById(R.id.btnColor2);
        this.seekBar = findViewById(R.id.seekBar);
        this.ivOptions = findViewById(R.id.ivOptions);
        this.overlayPreview = findViewById(R.id.overlayPreview);

        this.analogClock.setAutoUpdate(true);
        this.bgRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));

        getUserSettings();

        this.ivZoomOut.setOnClickListener(this);
        this.ivZoomIn.setOnClickListener(this);
        this.ivColor.setOnClickListener(this);
        this.ivCenter.setOnClickListener(this);
        this.ivGallery.setOnClickListener(this);
        this.ivBackground.setOnClickListener(this);
        this.ivTextColor.setOnClickListener(this);
        this.btnColor1.setOnClickListener(this);
        this.btnColor2.setOnClickListener(this);
        this.icDone.setOnClickListener(this);
        this.btnOk.setOnClickListener(this);
        this.ivOptions.setOnClickListener(this);

        BgAdapter adapter = new BgAdapter();
        this.bgAdapter = adapter;
        this.bgRecyclerView.setAdapter(adapter);
        this.bgAdapter.setClickListener(i -> {
            tinyDB.putInt("customBg", i);
            tinyDB.putBoolean("isImage", false);
            tinyDB.putBoolean("isCustomBg", true);
            updateClock();
        });

        this.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mClockSize = progress;
                updateClock();
            }
        });

        this.mIvMainScreen.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() != MotionEvent.ACTION_MOVE) {
                    return false;
                }
                float right = (float) ((int) (mClockPosX + ((float) (mClockSize / 2))));
                float top = (float) ((int) (mClockPosY - ((float) (mClockSize / 2))));
                float bottom = (float) ((int) (mClockPosY + ((float) (mClockSize / 2))));
                float x = motionEvent.getX();
                float y = motionEvent.getY();
                if (x <= ((float) ((int) (mClockPosX - ((float) (mClockSize / 2)))))
                        || x >= right || y <= top || y >= bottom) {
                    return false;
                }
                if (mIsCenterLine) {
                    mClockPosX = ((float) mWidth) / 2.0f;
                } else {
                    mClockPosX = x;
                }
                mClockPosY = y;
                updateClock();
                return false;
            }
        });
    }

    /**
     * Written as an if/else chain on purpose: with AGP 8 resource ids are no longer
     * compile-time constants, so {@code switch (view.getId())} with {@code case R.id.…}
     * does not compile any more.
     */
    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.ivOptions) {
            showOptionsDialog();
            return;
        }

        if (id == R.id.btnColor1) {
            showColorPicker(selected -> {
                color1 = selected;
                updateClock();
            });

        } else if (id == R.id.btnColor2) {
            showColorPicker(selected -> {
                color2 = selected;
                updateClock();
            });

        } else if (id == R.id.btn_ok) {
            applyAsLiveWallpaper();

        } else if (id == R.id.icDone) {
            this.tinyDB.putInt("textColor1", this.color1);
            this.tinyDB.putInt("textColor2", this.color2);
            showPanel(null);

        } else if (id == R.id.ivBackground) {
            showPanel(this.bgRecyclerView);

        } else if (id == R.id.ivCenter) {
            showPanel(null);
            this.mIsCenterLine = !this.mIsCenterLine;
            this.mClockPosX = ((float) this.mWidth) / 2.0f;
            updateClock();

        } else if (id == R.id.ivColor) {
            showPanel(null);
            showColorPicker(selected -> {
                tinyDB.putBoolean("isImage", false);
                tinyDB.putBoolean("isCustomBg", false);
                tinyDB.putInt("bgColor", selected);
                updateClock();
            });

        } else if (id == R.id.ivGallery) {
            showPanel(null);
            openImagePicker();

        } else if (id == R.id.ivTextColor) {
            showPanel(this.layoutColor);

        } else if (id == R.id.ivZoomIn || id == R.id.ivZoomOut) {
            showPanel(this.seekBar);
        }
    }

    /** Shows exactly one bottom panel (or none). */
    private void showPanel(@Nullable View visible) {
        this.bgRecyclerView.setVisibility(visible == this.bgRecyclerView ? View.VISIBLE : View.GONE);
        this.seekBar.setVisibility(visible == this.seekBar ? View.VISIBLE : View.GONE);
        this.layoutColor.setVisibility(visible == this.layoutColor ? View.VISIBLE : View.GONE);
    }

    private interface OnColorPicked {
        void onPicked(int color);
    }

    private void showColorPicker(final OnColorPicked callback) {
        ColorPickerDialogBuilder
                .with(this)
                .setTitle(getString(R.string.choose_color))
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setOnColorSelectedListener(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int selectedColor) {
                    }
                })
                .setPositiveButton(getString(R.string.ok), new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        UiCompat.applyImmersive(EditorActivity.this);
                        callback.onPicked(selectedColor);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UiCompat.applyImmersive(EditorActivity.this);
                    }
                })
                .build()
                .show();
    }

    private void applyAsLiveWallpaper() {
        this.bgRecyclerView.setVisibility(View.GONE);
        this.seekBar.setVisibility(View.GONE);
        this.layoutColor.setVisibility(View.VISIBLE);
        saveUserSettings();

        Intent intent = new Intent("android.service.wallpaper.CHANGE_LIVE_WALLPAPER");
        intent.putExtra("android.service.wallpaper.extra.LIVE_WALLPAPER_COMPONENT",
                new ComponentName(this, LiveClockWallpaper.class));
        try {
            this.wallpaperLauncher.launch(intent);
        } catch (Throwable t) {
            Log.e(TAG, "no wallpaper chooser available", t);
            Toast.makeText(this, R.string.wallpaper_chooser_unavailable, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    public void saveUserSettings() {
        float f = this.mClockPosX + 0.01f;
        this.mClockPosX = f;
        this.tinyDB.putFloat("prefClockPosX", f);
        this.tinyDB.putFloat("prefClockPosY", this.mClockPosY);
        this.tinyDB.putInt("prefSize", this.mClockSize);
        this.tinyDB.putInt("textClockPosition", this.textClockPosition);
        updateClock();
    }

    private void getUserSettings() {
        this.mClockPosX = this.tinyDB.getFloat("prefClockPosX", ((float) this.mWidth) / 2.0f);
        this.mClockPosY = this.tinyDB.getFloat("prefClockPosY", ((float) this.mHeight) / 2.0f);
        int storedSize = this.tinyDB.getInt("prefSize");
        this.mClockSize = storedSize == 0 ? DEFAULT_CLOCK_SIZE : storedSize;
        this.seekBar.setProgress(this.mClockSize);
        this.textClockPosition = this.tinyDB.getInt("textClockPosition");

        int clockType = this.tinyDB.getInt("clockType");
        if (clockType == 0) {
            this.analogClock.setVisibility(View.VISIBLE);
            this.textClockPreview.setVisibility(View.GONE);
            this.smartClockPreview.setVisibility(View.GONE);
            this.ivTextColor.setVisibility(View.GONE);
        } else if (clockType == 1) {
            this.analogClock.setVisibility(View.GONE);
            this.textClockPreview.setVisibility(View.GONE);
            this.smartClockPreview.setVisibility(View.VISIBLE);
            this.ivTextColor.setVisibility(View.GONE);
        } else if (clockType == 2) {
            this.analogClock.setVisibility(View.GONE);
            this.textClockPreview.setVisibility(View.VISIBLE);
            this.smartClockPreview.setVisibility(View.GONE);
        }
        saveUserSettings();
    }

    public void updateClock() {
        int clockType = this.tinyDB.getInt("clockType");
        if (clockType == 0) {
            this.analogClock.setClock((Clocks) this.tinyDB.getObject("clocks", Clocks.class));
            this.analogClock.setClockSize((float) this.mClockSize);
            this.analogClock.setTouchEnable(true);
            this.analogClock.setPosition(this.mClockPosX, this.mClockPosY);
        } else if (clockType == 1) {
            this.smartClockPreview.setTextClockPosition(this.textClockPosition);
            this.smartClockPreview.config(this.mClockPosX, this.mClockPosY, (int) (((float) this.mClockSize) * 2.0f));
        } else if (clockType == 2) {
            this.textClockPreview.setTextClockPosition(this.textClockPosition);
            this.textClockPreview.setColors(this.color1, this.color2);
            this.textClockPreview.config(this.mClockPosX, this.mClockPosY, (int) (((float) this.mClockSize) * 2.0f));
        }

        this.mIvMainScreen.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (this.tinyDB.getBoolean("isImage")) {
            String path = this.tinyDB.getString("ImageString");
            if (path != null && path.length() > 0) {
                Glide.with(this).load(new File(path)).centerCrop().into(this.mIvMainScreen);
            }
        } else if (this.tinyDB.getBoolean("isCustomBg")) {
            this.mIvMainScreen.setImageResource(this.tinyDB.getInt("customBg"));
        } else {
            this.mIvMainScreen.setImageResource(0);
            this.mIvMainScreen.setBackgroundColor(this.tinyDB.getInt("bgColor"));
        }
    }

    // ────────────────────────── gallery background ──────────────────────────

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            this.pickImageLauncher.launch(Intent.createChooser(intent, getString(R.string.select_picture)));
        } catch (Throwable t) {
            Log.e(TAG, "no image picker available", t);
            Toast.makeText(this, R.string.image_failed, Toast.LENGTH_SHORT).show();
        }
    }

    /** ACTION_GET_CONTENT hands us a temporary uri grant, so the bytes are copied locally. */
    private void importPickedImage(final Uri uri) {
        final File destination = new File(customBackgroundDir(),
                "custom_bg_" + System.currentTimeMillis() + ".img");
        final Dialog loading = UiCompat.showLoading(this, getString(R.string.image_loading));

        new Thread(() -> {
            final boolean copied = WallpaperHelper.copyUriToFile(
                    EditorActivity.this.getApplicationContext(), uri, destination);
            runOnUiThread(() -> {
                UiCompat.dismissSafely(loading);
                UiCompat.applyImmersive(EditorActivity.this);
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                if (!copied) {
                    Toast.makeText(EditorActivity.this, R.string.image_failed,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                pruneCustomBackgrounds(destination);
                tinyDB.putBoolean("isImage", true);
                tinyDB.putBoolean("isCustomBg", false);
                tinyDB.putString("ImageString", destination.getAbsolutePath());
                updateClock();
            });
        }, "import-background").start();
    }

    private File customBackgroundDir() {
        File dir = new File(getFilesDir(), "custom_backgrounds");
        if (!dir.exists() && !dir.mkdirs()) {
            Log.w(TAG, "could not create " + dir.getAbsolutePath());
        }
        return dir;
    }

    /** Keeps the newest {@value #MAX_CUSTOM_BACKGROUNDS} picks so private storage stays small. */
    private void pruneCustomBackgrounds(File keep) {
        File[] files = customBackgroundDir().listFiles();
        if (files == null || files.length <= MAX_CUSTOM_BACKGROUNDS) {
            return;
        }
        java.util.Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
        for (int i = MAX_CUSTOM_BACKGROUNDS; i < files.length; i++) {
            if (files[i].equals(keep)) {
                continue;
            }
            // Never delete the file the running wallpaper is currently reading.
            if (files[i].getAbsolutePath().equals(tinyDB.getString("ImageString"))) {
                continue;
            }
            if (!files[i].delete()) {
                Log.w(TAG, "could not delete " + files[i].getAbsolutePath());
            }
        }
    }

    /** Hijri date + rotating adhkar toggles for the live wallpaper overlay. */
    private void showOptionsDialog() {
        View content = getLayoutInflater().inflate(R.layout.dialog_wallpaper_options, null);
        SwitchCompat switchHijri = content.findViewById(R.id.switchHijri);
        SwitchCompat switchDhikr = content.findViewById(R.id.switchDhikr);
        SwitchCompat switchPower = content.findViewById(R.id.switchPower);
        SwitchCompat switchAthkarBadge = content.findViewById(R.id.switchAthkarBadge);
        switchHijri.setChecked(this.tinyDB.getBoolean("showHijri"));
        switchDhikr.setChecked(this.tinyDB.getBoolean("showDhikr"));
        switchHijri.setOnCheckedChangeListener((button, checked) -> {
            this.tinyDB.putBoolean("showHijri", checked);
            if (this.overlayPreview != null) {
                this.overlayPreview.refresh();
            }
        });
        switchDhikr.setOnCheckedChangeListener((button, checked) -> {
            this.tinyDB.putBoolean("showDhikr", checked);
            if (this.overlayPreview != null) {
                this.overlayPreview.refresh();
            }
        });
        switchPower.setChecked(FrameRate.isPowerSaver(this.tinyDB));
        switchPower.setOnCheckedChangeListener((button, checked) ->
                this.tinyDB.putBoolean("powerSaver", checked));
        switchAthkarBadge.setChecked(this.tinyDB.getBoolean("showAthkarBadge", true));
        switchAthkarBadge.setOnCheckedChangeListener((button, checked) -> {
            this.tinyDB.putBoolean("showAthkarBadge", checked);
            if (this.overlayPreview != null) {
                this.overlayPreview.refresh();
            }
        });
        new AlertDialog.Builder(this)
                .setView(content)
                .setPositiveButton(R.string.ok, null)
                .show();
    }
}
