package com.clock.livewallpaper.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.clock.livewallpaper.AdAdmob;
import com.clock.livewallpaper.R;


public class ClockFuntionActivity extends AppCompatActivity {
    private RelativeLayout adContainer;
    private FrameLayout frameAnalogClock;
    private FrameLayout frameSmartClock;
    private FrameLayout frameTextClock;

    @Override

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_clock_funtion);
        initView();


        AdAdmob adAdmob = new AdAdmob(this);
        adAdmob.BannerAd((RelativeLayout) findViewById(R.id.bannerAd), this);

    }

    private void initView() {
        this.frameAnalogClock = (FrameLayout) findViewById(R.id.frameAnalogClock);
        this.frameTextClock = (FrameLayout) findViewById(R.id.frameTextClock);
        this.frameSmartClock = (FrameLayout) findViewById(R.id.frameSmartClock);
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.adContainer);
        this.adContainer = relativeLayout;
        this.frameAnalogClock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ClockFuntionActivity.this, ClockCardActivity.class);
                intent.putExtra("isWhich", 0);
                ClockFuntionActivity.this.startActivity(intent);

            }
        });
        this.frameTextClock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ClockFuntionActivity.this, ClockCardActivity.class);
                intent.putExtra("isWhich", 1);
                ClockFuntionActivity.this.startActivity(intent);

            }
        });
        this.frameSmartClock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ClockFuntionActivity.this, ClockCardActivity.class);
                intent.putExtra("isWhich", 2);
                ClockFuntionActivity.this.startActivity(intent);

            }
        });
    }

    @Override
    public void onBackPressed() {

        ClockFuntionActivity.this.finish();

    }
}
