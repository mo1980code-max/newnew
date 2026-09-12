package com.clock.livewallpaper.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.clock.livewallpaper.R;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.cause.ResumeFailedCause;
import com.liulishuo.okdownload.core.listener.DownloadListener1;
import com.liulishuo.okdownload.core.listener.assist.Listener1Assist;

import com.clock.livewallpaper.CustomWallpaper;
import com.clock.livewallpaper.utils.TinyDB;

import java.io.File;




public class SetWallpaperActivity extends AppCompatActivity {
    private RelativeLayout adContainer;
    private CardView cardShare;
    private ImageView imageMain;
    private AppCompatImageButton ivShare;
    private ProgressBar progressBar;
    private TextView setWallpaper;
    TinyDB tinyDB;

    @Override

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(1);
        EditorActivity.isDone = false;
        this.tinyDB = new TinyDB(this);
        getWindow().setFlags(1024, 1024);
        if (Build.VERSION.SDK_INT >= 28) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        setContentView(R.layout.activity_set_wallpaper);
        initView();
    }

    @Override
    public void onBackPressed() {


        SetWallpaperActivity.this.finish();

    }

    @Override
    protected void onResume() {
        super.onResume();
        hideMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideMenu();
    }

    public void hideMenu() {
        getWindow().getDecorView().setSystemUiVisibility(5894);
    }

    private void initView() {
        this.imageMain = (ImageView) findViewById(R.id.imageMain);
        this.setWallpaper = (TextView) findViewById(R.id.setWallpaper);
        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);
        this.cardShare = (CardView) findViewById(R.id.cardShare);
        this.ivShare = (AppCompatImageButton) findViewById(R.id.ivShare);
        this.adContainer = (RelativeLayout) findViewById(R.id.adContainer);
        final File file = new File(getIntent().getStringExtra("imageFile"));
        final File file2 = new File(getExternalCacheDir() + File.separator + file.getName());
        if (file2.exists()) {
            this.setWallpaper.setText("Set Wallpaper");
            Glide.with((FragmentActivity) this).load(file2).into(this.imageMain);
            this.cardShare.setVisibility(View.VISIBLE);
        } else {
            Glide.with((FragmentActivity) this).load(getIntent().getStringExtra("imageFile")).placeholder((int) R.drawable.placeholder).into(this.imageMain);
            this.setWallpaper.setText("Download Wallpaper");
            this.cardShare.setVisibility(View.GONE);
        }
        this.setWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (file2.exists()) {
                    SetWallpaperActivity.this.tinyDB.putString("isWallpaper", file2.getAbsolutePath());

                    EditorActivity.isDone = true;
                    Intent intent = new Intent("android.service.wallpaper.CHANGE_LIVE_WALLPAPER");
                    intent.putExtra("android.service.wallpaper.extra.LIVE_WALLPAPER_COMPONENT", new ComponentName(SetWallpaperActivity.this, CustomWallpaper.class));
                    SetWallpaperActivity.this.startActivity(intent);

                    return;
                }
                new DownloadTask.Builder(SetWallpaperActivity.this.getIntent().getStringExtra("imageFile"), SetWallpaperActivity.this.getExternalCacheDir()).setFilename(file.getName()).setMinIntervalMillisCallbackProcess(10).setPassIfAlreadyCompleted(false).build().enqueue(new DownloadListener1() {
                    @Override

                    public void retry(DownloadTask downloadTask, ResumeFailedCause resumeFailedCause) {
                    }

                    @Override

                    public void taskStart(DownloadTask downloadTask, Listener1Assist.Listener1Model listener1Model) {
                        SetWallpaperActivity.this.progressBar.setVisibility(View.GONE);
                    }

                    @Override

                    public void connected(DownloadTask downloadTask, int i, long j, long j2) {
                        SetWallpaperActivity.this.progressBar.setVisibility(View.GONE);
                    }

                    @Override

                    public void progress(DownloadTask downloadTask, long j, long j2) {
                        Log.e("TAG", "progress: " + j);
                        TextView textView = SetWallpaperActivity.this.setWallpaper;
                        textView.setText("Downloading " + ((j * 100) / j2) + "%");
                    }

                    @Override

                    public void taskEnd(DownloadTask downloadTask, EndCause endCause, Exception exc, Listener1Assist.Listener1Model listener1Model) {
                        SetWallpaperActivity.this.setWallpaper.setText("Set Wallpaper");
                        SetWallpaperActivity.this.progressBar.setVisibility(View.GONE);
                        SetWallpaperActivity.this.cardShare.setVisibility(View.VISIBLE);
                        Glide.with((FragmentActivity) SetWallpaperActivity.this).load(downloadTask.getFile()).into(SetWallpaperActivity.this.imageMain);
                    }
                });
            }
        });
        this.ivShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetWallpaperActivity setWallpaperActivity = SetWallpaperActivity.this;
                Uri uriForFile = FileProvider.getUriForFile(setWallpaperActivity, SetWallpaperActivity.this.getApplicationContext().getPackageName() + ".provider", file2);
                Intent intent = new Intent("android.intent.action.SEND");
                intent.setType("image/jpeg");
                intent.putExtra("android.intent.extra.STREAM", uriForFile);
                SetWallpaperActivity.this.startActivity(Intent.createChooser(intent, "Select"));
            }
        });
    }
}
