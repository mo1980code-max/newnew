package com.clock.livewallpaper.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.Key;
import com.clock.livewallpaper.AdAdmob;
import com.clock.livewallpaper.R;
import com.google.gson.Gson;

import com.clock.livewallpaper.adapter.CategoryWallpaperAdapter;
import com.clock.livewallpaper.model.ResponseWallpaper;
import com.clock.livewallpaper.model.ResponseWallpaperItem;

import java.io.IOException;
import java.io.InputStream;



public class WallpaperCategoryActivity extends AppCompatActivity {
    CategoryWallpaperAdapter categoryWallpaperAdapter;

    private ImageView ivBack;
    private RecyclerView recyclerViewCategory;

    @Override

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_wallpaper_category);
        initView();

        AdAdmob adAdmob = new AdAdmob(this);
        adAdmob.BannerAd((RelativeLayout) findViewById(R.id.bannerAd), this);

    }

    private void initView() {
        this.recyclerViewCategory = (RecyclerView) findViewById(R.id.recyclerViewCategory);
        CategoryWallpaperAdapter categoryWallpaperAdapter = new CategoryWallpaperAdapter(((ResponseWallpaper) new Gson().fromJson(loadJSONFromAsset(), ResponseWallpaper.class)).getResponseWallpaper());
        this.categoryWallpaperAdapter = categoryWallpaperAdapter;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);

        this.recyclerViewCategory.setLayoutManager(gridLayoutManager);
        this.recyclerViewCategory.setAdapter(categoryWallpaperAdapter);
        this.categoryWallpaperAdapter.setClickListener(new CategoryWallpaperAdapter.ClickListener() {
            @Override
            public void setClick(final ResponseWallpaperItem responseWallpaperItem) {

                Intent intent = new Intent(WallpaperCategoryActivity.this, WallpaperActivity.class);
                intent.putExtra("responseWallpaperItem", responseWallpaperItem);
                WallpaperCategoryActivity.this.startActivity(intent);

            }
        });
        ImageView imageView = (ImageView) findViewById(R.id.ivBack);
        this.ivBack = imageView;
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WallpaperCategoryActivity.this.onBackPressed();
            }
        });
    }


    @Override
    public void onBackPressed() {
        WallpaperCategoryActivity.this.finish();

    }

    public String loadJSONFromAsset() {
        try {
            InputStream open = getAssets().open("wallpapernew.json");
            byte[] bArr = new byte[open.available()];
            open.read(bArr);
            open.close();
            return new String(bArr, Key.STRING_CHARSET_NAME);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
