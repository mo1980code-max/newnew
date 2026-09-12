package org.Allah_Clock_Live_Wallpaper.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import org.Allah_Clock_Live_Wallpaper.CustomWallpaper;
import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.ads.AdManager;
import org.Allah_Clock_Live_Wallpaper.utils.TinyDB;
import org.Allah_Clock_Live_Wallpaper.utils.UiCompat;
import org.Allah_Clock_Live_Wallpaper.utils.WallpaperHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Full-screen preview of a bundled wallpaper + "set wallpaper" / "share" actions.
 *
 * <p>The image ships inside the APK, so there is no download step any more: the drawable is
 * decoded once into an app-private JPEG file (that path is what the live wallpaper service
 * reads) and can then be applied or shared instantly, online or offline.</p>
 *
 * <p>No banner here on purpose: the action button sits at the very bottom of the screen and
 * an ad next to it would be an accidental-click magnet, which AdMob treats as a policy
 * problem. The interstitial for this screen is shown <i>after</i> the system wallpaper
 * chooser confirms the wallpaper was really applied — never before, and never on exit.</p>
 */
public class SetWallpaperActivity extends AppCompatActivity {

    private static final String TAG = "SetWallpaperActivity";

    /** Intent extra: {@code R.drawable.*} id of the bundled wallpaper to preview/apply. */
    public static final String EXTRA_WALLPAPER_RES = "wallpaperRes";

    /** Longest decoded side in px; bounds the memory of a single decode. */
    private static final int MAX_DECODE_SIDE = 2048;
    private static final int JPEG_QUALITY = 92;

    private CardView cardShare;
    private ImageView imageMain;
    private AppCompatImageButton ivShare;
    private ProgressBar progressBar;
    private TextView setWallpaper;

    private TinyDB tinyDB;
    private ExecutorService worker;
    private int wallpaperRes;
    private File localFile;

    /** Runs after the system "set live wallpaper" screen comes back. */
    private final ActivityResultLauncher<Intent> wallpaperLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                boolean applied = result.getResultCode() == Activity.RESULT_OK
                        || WallpaperHelper.isOurLiveWallpaperSet(SetWallpaperActivity.this);
                if (applied) {
                    // Natural transition point: the user just finished what they came for.
                    AdManager.showInterstitial(SetWallpaperActivity.this, null);
                } else {
                    AdManager.preloadInterstitial(SetWallpaperActivity.this);
                }
            });

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_set_wallpaper);
        this.tinyDB = new TinyDB(this);
        this.worker = Executors.newSingleThreadExecutor();

        this.wallpaperRes = getIntent().getIntExtra(EXTRA_WALLPAPER_RES, 0);
        if (this.wallpaperRes == 0) {
            finish();
            return;
        }

        initView();
        UiCompat.applyImmersive(this);

        // Ready before the user can possibly come back from the system chooser.
        AdManager.preloadInterstitial(this);
        prepareFile();
    }

    private void initView() {
        this.imageMain = findViewById(R.id.imageMain);
        this.setWallpaper = findViewById(R.id.setWallpaper);
        this.progressBar = findViewById(R.id.progressBar);
        this.cardShare = findViewById(R.id.cardShare);
        this.ivShare = findViewById(R.id.ivShare);

        this.imageMain.setImageResource(this.wallpaperRes);
        this.setWallpaper.setOnClickListener(view -> applyAsLiveWallpaper());
        this.ivShare.setOnClickListener(view -> shareLocalFile());
    }

    /**
     * Decodes the bundled drawable once into {@code files/wallpapers/wallpaper.jpg}.
     * One fixed name keeps the directory self-pruning: there is never more than one copy.
     */
    private void prepareFile() {
        this.progressBar.setVisibility(View.VISIBLE);
        this.setWallpaper.setEnabled(false);
        this.worker.execute(() -> {
            final File file = writeWallpaperFile();
            runOnUiThread(() -> onFileReady(file));
        });
    }

    private void onFileReady(File file) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        this.progressBar.setVisibility(View.GONE);
        this.setWallpaper.setEnabled(true);
        this.localFile = file;
        if (file == null) {
            Toast.makeText(this, R.string.save_failed, Toast.LENGTH_LONG).show();
            return;
        }
        this.cardShare.setVisibility(View.VISIBLE);
    }

    private File writeWallpaperFile() {
        File dir = new File(getFilesDir(), "wallpapers");
        if (!dir.exists() && !dir.mkdirs()) {
            Log.e(TAG, "could not create " + dir);
            return null;
        }
        File out = new File(dir, "wallpaper.jpg");

        Bitmap bitmap = decodeSampled(this.wallpaperRes, MAX_DECODE_SIDE);
        if (bitmap == null) {
            Log.e(TAG, "could not decode drawable " + this.wallpaperRes);
            return null;
        }
        OutputStream output = null;
        try {
            output = new FileOutputStream(out);
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output);
            output.flush();
        } catch (Throwable t) {
            Log.e(TAG, "could not write " + out, t);
            return null;
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (Throwable ignored) {
                }
            }
            bitmap.recycle();
        }
        return out.length() > 0 ? out : null;
    }

    /** Decodes with the smallest power-of-two sample that fits inside {@code maxSide}. */
    private Bitmap decodeSampled(int res, int maxSide) {
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(getResources(), res, bounds);

            int sample = 1;
            while (Math.max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= maxSide) {
                sample *= 2;
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sample;
            return BitmapFactory.decodeResource(getResources(), res, options);
        } catch (Throwable t) {
            Log.e(TAG, "decode failed", t);
            return null;
        }
    }

    private void applyAsLiveWallpaper() {
        if (this.localFile == null || !this.localFile.exists() || this.localFile.length() == 0) {
            Toast.makeText(this, R.string.save_failed, Toast.LENGTH_LONG).show();
            return;
        }
        this.tinyDB.putString("isWallpaper", this.localFile.getAbsolutePath());
        Intent intent = new Intent("android.service.wallpaper.CHANGE_LIVE_WALLPAPER");
        intent.putExtra("android.service.wallpaper.extra.LIVE_WALLPAPER_COMPONENT",
                new ComponentName(this, CustomWallpaper.class));
        try {
            this.wallpaperLauncher.launch(intent);
        } catch (Throwable t) {
            Log.e(TAG, "no wallpaper chooser available", t);
            Toast.makeText(this, R.string.wallpaper_chooser_unavailable, Toast.LENGTH_LONG).show();
        }
    }

    private void shareLocalFile() {
        if (this.localFile == null || !this.localFile.exists()) {
            return;
        }
        try {
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", this.localFile);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/jpeg");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, getString(R.string.share_application)));
        } catch (Throwable t) {
            Log.e(TAG, "share failed", t);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        UiCompat.applyImmersive(this);
    }

    @Override
    protected void onDestroy() {
        if (this.worker != null) {
            this.worker.shutdownNow();
            this.worker = null;
        }
        super.onDestroy();
    }
}
