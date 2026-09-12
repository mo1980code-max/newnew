package com.clock.livewallpaper.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.clock.livewallpaper.AdAdmob;
import com.clock.livewallpaper.R;


public class MainActivity extends AppCompatActivity {
    private RelativeLayout adContainer;
    private FrameLayout frameClock;
    private FrameLayout frameWallpaper;


    ImageView rate, share;

    @Override

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_select_function);
        initView();

        AdAdmob adAdmob = new AdAdmob(this);
        adAdmob.BannerAd((RelativeLayout) findViewById(R.id.bannerAd), this);

    }

    private void initView() {
        this.frameClock = (FrameLayout) findViewById(R.id.frameClock);
        this.frameWallpaper = (FrameLayout) findViewById(R.id.frameWallpaper);
        this.adContainer = (RelativeLayout) findViewById(R.id.adContainer);
        rate = findViewById(R.id.rateus);
        share = findViewById(R.id.share);
        rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + getPackageName())));
                } catch (ActivityNotFoundException unused) {
                    Toast.makeText(MainActivity.this, " unable to find market app", Toast.LENGTH_SHORT).show();
                }
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String string = getString(R.string.app_name);
                Intent intent2 = new Intent("android.intent.action.SEND");
                intent2.setType("text/plain");
                intent2.putExtra("android.intent.extra.TEXT", string + "\n\nOpen this Link on Play Store\n\nhttps://play.google.com/store/apps/details?id=" + getPackageName());
                startActivity(Intent.createChooser(intent2, "Share Application"));
            }
        });

        this.frameClock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MainActivity.this.startActivity(new Intent(MainActivity.this, ClockFuntionActivity.class));

            }
        });
        this.frameWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MainActivity.this.startActivity(new Intent(MainActivity.this, WallpaperCategoryActivity.class));

            }
        });
    }

    @Override
    public void onBackPressed() {

        MainActivity.this.finish();

    }
}
