package com.clock.livewallpaper.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.clock.livewallpaper.AdAdmob;
import com.clock.livewallpaper.R;

import com.clock.livewallpaper.adapter.CustomAdapter;
import com.clock.livewallpaper.adapter.SmartTextAdapter;
import com.clock.livewallpaper.adapter.TextAdapter;
import com.clock.livewallpaper.model.Clocks;
import com.clock.livewallpaper.model.SmartClocks;
import com.clock.livewallpaper.model.TextClocks;
import com.clock.livewallpaper.utils.GetClocks;
import com.clock.livewallpaper.utils.TinyDB;



public class ClockCardActivity extends AppCompatActivity {
    private CustomAdapter customAdapter;

    private ImageView ivBack;
    private RecyclerView recyclerViewCategory;
    private SmartTextAdapter smartTextAdapter;
    private TextAdapter textAdapter;
    private TinyDB tinyDB;
    private TextView txtTitle;

    @Override

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.tinyDB = new TinyDB(this);
        setContentView(R.layout.activity_clock_card);
        initView();

        AdAdmob adAdmob = new AdAdmob(this);
        adAdmob.BannerAd((RelativeLayout) findViewById(R.id.bannerAd), this);
        adAdmob.FullscreenAd(this);

    }


    private void initView() {
        this.txtTitle = (TextView) findViewById(R.id.txtTitle);
        this.recyclerViewCategory = (RecyclerView) findViewById(R.id.recyclerViewCategory);
        if (getIntent().getIntExtra("isWhich", 0) == 0) {
            GetClocks getClocks = new GetClocks();
            this.txtTitle.setText("Analog Clock");
            CustomAdapter customAdapter = new CustomAdapter(getClocks.getClocks());
            this.customAdapter = customAdapter;
            GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);

            this.recyclerViewCategory.setLayoutManager(gridLayoutManager);
            this.recyclerViewCategory.setAdapter(customAdapter);
            this.customAdapter.setClickListener(new CustomAdapter.ClickListener() {
                @Override
                public void setClick(final Clocks clocks) {

                    ClockCardActivity.this.tinyDB.putObject("clocks", clocks);
                    ClockCardActivity.this.tinyDB.putInt("clockType", 0);
                    ClockCardActivity.this.tinyDB.putBoolean("isImage", false);
                    ClockCardActivity.this.tinyDB.putBoolean("isCustomBg", false);
                    ClockCardActivity.this.tinyDB.putInt("bgColor", Color.parseColor(clocks.getBgColor()));
                    ClockCardActivity.this.startActivity(new Intent(ClockCardActivity.this, EditorActivity.class));

                }
            });
        } else if (getIntent().getIntExtra("isWhich", 0) == 1) {
            TextAdapter textAdapter = new TextAdapter(new GetClocks().getTextClocks());
            this.textAdapter = textAdapter;
            this.txtTitle.setText("Digital Clock");
            GridLayoutManager gridLayoutManager2 = new GridLayoutManager(this, 2);

            this.recyclerViewCategory.setLayoutManager(gridLayoutManager2);
            this.recyclerViewCategory.setAdapter(textAdapter);
            this.textAdapter.setClickListener(new TextAdapter.ClickListener() {
                @Override
                public void setClick(final int i, final TextClocks textClocks) {

                    ClockCardActivity.this.tinyDB.putInt("textClockPosition", i);
                    ClockCardActivity.this.tinyDB.putInt("clockType", 2);
                    ClockCardActivity.this.tinyDB.putBoolean("isImage", false);
                    ClockCardActivity.this.tinyDB.putBoolean("isCustomBg", false);
                    ClockCardActivity.this.tinyDB.putInt("bgColor", Color.parseColor(textClocks.getBgColor()));
                    ClockCardActivity.this.startActivity(new Intent(ClockCardActivity.this, EditorActivity.class));

                }
            });
        } else if (getIntent().getIntExtra("isWhich", 0) == 2) {
            SmartTextAdapter smartTextAdapter = new SmartTextAdapter(new GetClocks().getSmartClocks());
            this.smartTextAdapter = smartTextAdapter;
            this.txtTitle.setText("Smart Clock");
            GridLayoutManager gridLayoutManager3 = new GridLayoutManager(this, 2);

            this.recyclerViewCategory.setLayoutManager(gridLayoutManager3);
            this.recyclerViewCategory.setAdapter(smartTextAdapter);
            this.smartTextAdapter.setClickListener(new SmartTextAdapter.ClickListener() {
                @Override
                public void setClick(final int i, final SmartClocks smartClocks) {

                    ClockCardActivity.this.tinyDB.putInt("customBg", smartClocks.getBgColor());
                    ClockCardActivity.this.tinyDB.putBoolean("isImage", false);
                    ClockCardActivity.this.tinyDB.putBoolean("isCustomBg", true);
                    ClockCardActivity.this.tinyDB.putInt("textClockPosition", i);
                    ClockCardActivity.this.tinyDB.putInt("clockType", 1);
                    ClockCardActivity.this.startActivity(new Intent(ClockCardActivity.this, EditorActivity.class));

                }
            });
        }
        ImageView imageView = (ImageView) findViewById(R.id.ivBack);
        this.ivBack = imageView;
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClockCardActivity.this.onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {

        ClockCardActivity.this.finish();

    }
}
