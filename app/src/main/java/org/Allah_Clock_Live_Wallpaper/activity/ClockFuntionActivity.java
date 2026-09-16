package org.Allah_Clock_Live_Wallpaper.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.ads.BannerAdController;
import org.Allah_Clock_Live_Wallpaper.utils.UiCompat;

/** Clock type picker: Analog / Digital / Smart. */
public class ClockFuntionActivity extends AppCompatActivity {

    private FrameLayout frameAnalogClock;
    private FrameLayout frameTextClock;
    private FrameLayout frameSmartClock;
    private BannerAdController banner;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_clock_funtion);
        UiCompat.applyEdgeToEdge(this);
        initView();

        this.banner = new BannerAdController(this, (RelativeLayout) findViewById(R.id.bannerAd));
        this.banner.load();
    }

    private void initView() {
        this.frameAnalogClock = findViewById(R.id.frameAnalogClock);
        this.frameTextClock = findViewById(R.id.frameTextClock);
        this.frameSmartClock = findViewById(R.id.frameSmartClock);

        this.frameAnalogClock.setOnClickListener(view -> openClockList(0));
        this.frameTextClock.setOnClickListener(view -> openClockList(1));
        this.frameSmartClock.setOnClickListener(view -> openClockList(2));
    }

    private void openClockList(int which) {
        Intent intent = new Intent(ClockFuntionActivity.this, ClockCardActivity.class);
        intent.putExtra("isWhich", which);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (banner != null) {
            banner.resume();
        }
    }

    @Override
    protected void onPause() {
        if (banner != null) {
            banner.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (banner != null) {
            banner.destroy();
            banner = null;
        }
        super.onDestroy();
    }
}
