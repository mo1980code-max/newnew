package com.clock.livewallpaper.activity;

import android.content.Intent;
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
import com.clock.livewallpaper.adapter.WallpaperAdapter;
import com.clock.livewallpaper.model.ResponseWallpaperItem;




public class WallpaperActivity extends AppCompatActivity {

    private ImageView ivBack;
    private RecyclerView recyclerViewCategory;
    private TextView txtTitle;

    @Override

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_wallpaper);
        initView();

        AdAdmob adAdmob = new AdAdmob(this);
        adAdmob.BannerAd((RelativeLayout) findViewById(R.id.bannerAd), this);
        adAdmob.FullscreenAd(this);

    }

    private void initView() {
        this.ivBack = (ImageView) findViewById(R.id.ivBack);
        this.txtTitle = (TextView) findViewById(R.id.txtTitle);
        final ResponseWallpaperItem responseWallpaperItem = (ResponseWallpaperItem) getIntent().getParcelableExtra("responseWallpaperItem");
        this.txtTitle.setText(responseWallpaperItem.getCategoryName());
        this.recyclerViewCategory = (RecyclerView) findViewById(R.id.recyclerViewCategory);
        WallpaperAdapter wallpaperAdapter = new WallpaperAdapter(responseWallpaperItem.getImageUrls());
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        this.recyclerViewCategory.setLayoutManager(gridLayoutManager);
        this.recyclerViewCategory.setAdapter(wallpaperAdapter);
        wallpaperAdapter.setClickListener(new WallpaperAdapter.ClickListener() {
            @Override
            public void setClick(final int i) {

                Intent intent = new Intent(WallpaperActivity.this, SetWallpaperActivity.class);
                intent.putExtra("imageFile", responseWallpaperItem.getImageUrls().get(i).getImageUrl());
                WallpaperActivity.this.startActivity(intent);

            }
        });
        this.ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WallpaperActivity.this.onBackPressed();
            }
        });
    }


    @Override
    public void onBackPressed() {

        WallpaperActivity.this.finish();

    }
}
