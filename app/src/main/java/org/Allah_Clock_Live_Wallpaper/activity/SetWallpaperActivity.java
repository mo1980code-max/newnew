package org.Allah_Clock_Live_Wallpaper.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
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

import com.bumptech.glide.Glide;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.cause.ResumeFailedCause;
import com.liulishuo.okdownload.core.listener.DownloadListener1;
import com.liulishuo.okdownload.core.listener.assist.Listener1Assist;

import org.Allah_Clock_Live_Wallpaper.CustomWallpaper;
import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.ads.AdManager;
import org.Allah_Clock_Live_Wallpaper.utils.TinyDB;
import org.Allah_Clock_Live_Wallpaper.utils.UiCompat;
import org.Allah_Clock_Live_Wallpaper.utils.WallpaperHelper;

import java.io.File;

/**
 * Full-screen wallpaper preview + "download / set wallpaper" action.
 *
 * <p>No banner here on purpose: the action button sits at the very bottom of the screen and
 * an ad next to it would be an accidental-click magnet, which AdMob treats as a policy
 * problem. The interstitial for this screen is shown <i>after</i> the system wallpaper
 * chooser confirms the wallpaper was really applied — never before, and never on exit.</p>
 */
public class SetWallpaperActivity extends AppCompatActivity {

    private static final String TAG = "SetWallpaperActivity";

    private CardView cardShare;
    private ImageView imageMain;
    private AppCompatImageButton ivShare;
    private ProgressBar progressBar;
    private TextView setWallpaper;

    private TinyDB tinyDB;
    private File localFile;
    private String remoteUrl;
    private boolean downloading;

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

        this.remoteUrl = getIntent().getStringExtra("imageFile");
        if (this.remoteUrl == null || this.remoteUrl.length() == 0) {
            finish();
            return;
        }
        this.localFile = new File(downloadDir(), new File(this.remoteUrl).getName());

        initView();
        UiCompat.applyImmersive(this);

        // Ready before the user can possibly come back from the system chooser.
        AdManager.preloadInterstitial(this);
    }

    /** App-private external cache when available, internal cache otherwise. Never null. */
    private File downloadDir() {
        File external = getExternalCacheDir();
        return external != null ? external : getCacheDir();
    }

    private void initView() {
        this.imageMain = findViewById(R.id.imageMain);
        this.setWallpaper = findViewById(R.id.setWallpaper);
        this.progressBar = findViewById(R.id.progressBar);
        this.cardShare = findViewById(R.id.cardShare);
        this.ivShare = findViewById(R.id.ivShare);

        if (this.localFile.exists() && this.localFile.length() > 0) {
            this.setWallpaper.setText(R.string.set_wallpaper);
            Glide.with(this).load(this.localFile).centerCrop().into(this.imageMain);
            this.cardShare.setVisibility(View.VISIBLE);
        } else {
            Glide.with(this).load(this.remoteUrl).centerCrop()
                    .placeholder(R.drawable.placeholder).error(R.drawable.placeholder)
                    .into(this.imageMain);
            this.setWallpaper.setText(R.string.download_wallpaper);
            this.cardShare.setVisibility(View.GONE);
        }

        this.setWallpaper.setOnClickListener(view -> onActionButtonClicked());
        this.ivShare.setOnClickListener(view -> shareLocalFile());
    }

    private void onActionButtonClicked() {
        if (this.localFile.exists() && this.localFile.length() > 0) {
            applyAsLiveWallpaper();
            return;
        }
        if (!downloading) {
            startDownload();
        }
    }

    private void applyAsLiveWallpaper() {
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

    private void startDownload() {
        this.downloading = true;
        this.progressBar.setVisibility(View.VISIBLE);
        this.setWallpaper.setText(R.string.downloading);

        new DownloadTask.Builder(this.remoteUrl, downloadDir())
                .setFilename(this.localFile.getName())
                .setMinIntervalMillisCallbackProcess(50)
                .setPassIfAlreadyCompleted(false)
                .build()
                .enqueue(new DownloadListener1() {
                    @Override
                    public void retry(DownloadTask downloadTask, ResumeFailedCause resumeFailedCause) {
                    }

                    @Override
                    public void taskStart(DownloadTask downloadTask, Listener1Assist.Listener1Model listener1Model) {
                        progressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void connected(DownloadTask downloadTask, int i, long j, long j2) {
                        progressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void progress(DownloadTask downloadTask, long current, long total) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        int percent = total > 0 ? (int) ((current * 100) / total) : 0;
                        setWallpaper.setText(getString(R.string.downloading_percent, percent));
                    }

                    @Override
                    public void taskEnd(DownloadTask downloadTask, EndCause endCause,
                                        Exception exc, Listener1Assist.Listener1Model listener1Model) {
                        downloading = false;
                        progressBar.setVisibility(View.GONE);
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        File downloaded = downloadTask.getFile();
                        if (downloaded == null || !downloaded.exists() || downloaded.length() == 0) {
                            setWallpaper.setText(R.string.download_wallpaper);
                            Toast.makeText(SetWallpaperActivity.this, R.string.download_failed,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        localFile = downloaded;
                        setWallpaper.setText(R.string.set_wallpaper);
                        cardShare.setVisibility(View.VISIBLE);
                        Glide.with(SetWallpaperActivity.this).load(downloaded).centerCrop()
                                .into(imageMain);
                    }
                });
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
}
