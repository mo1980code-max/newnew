package org.Allah_Clock_Live_Wallpaper.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.ads.BannerAdController;
import org.Allah_Clock_Live_Wallpaper.ads.NativeAdListAdapter;
import org.Allah_Clock_Live_Wallpaper.adapter.CategoryWallpaperAdapter;
import org.Allah_Clock_Live_Wallpaper.model.ResponseWallpaper;
import org.Allah_Clock_Live_Wallpaper.model.ResponseWallpaperItem;
import org.Allah_Clock_Live_Wallpaper.utils.UiCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Wallpaper categories. Banner at the bottom, one native card in the middle of the grid. */
public class WallpaperCategoryActivity extends AppCompatActivity {

    private static final String TAG = "WallpaperCategory";
    private static final int GRID_SPAN = 2;
    private static final String ASSET = "wallpapernew.json";

    private CategoryWallpaperAdapter categoryWallpaperAdapter;
    private NativeAdListAdapter listAdapter;
    private BannerAdController banner;

    private ImageView ivBack;
    private RecyclerView recyclerViewCategory;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_wallpaper_category);
        UiCompat.applyEdgeToEdge(this);
        initView();

        this.banner = new BannerAdController(this, (RelativeLayout) findViewById(R.id.bannerAd));
        this.banner.load();
    }

    private void initView() {
        this.recyclerViewCategory = findViewById(R.id.recyclerViewCategory);
        this.ivBack = findViewById(R.id.ivBack);
        this.ivBack.setOnClickListener(view -> finish());

        List<ResponseWallpaperItem> categories = loadCategories();
        CategoryWallpaperAdapter adapter = new CategoryWallpaperAdapter(categories);
        this.categoryWallpaperAdapter = adapter;
        adapter.setClickListener(new CategoryWallpaperAdapter.ClickListener() {
            @Override
            public void setClick(final ResponseWallpaperItem responseWallpaperItem) {
                Intent intent = new Intent(WallpaperCategoryActivity.this, WallpaperActivity.class);
                intent.putExtra("responseWallpaperItem", responseWallpaperItem);
                startActivity(intent);
            }
        });

        this.listAdapter = new NativeAdListAdapter(this, adapter, GRID_SPAN);
        this.listAdapter.attachTo(this.recyclerViewCategory);
        this.listAdapter.loadNativeAd();
    }

    /** Never throws: a missing or malformed asset simply yields an empty list. */
    private List<ResponseWallpaperItem> loadCategories() {
        String json = loadJsonFromAsset(ASSET);
        if (json == null) {
            return new ArrayList<>();
        }
        try {
            ResponseWallpaper parsed = new Gson().fromJson(json, ResponseWallpaper.class);
            if (parsed == null || parsed.getResponseWallpaper() == null) {
                return new ArrayList<>();
            }
            return parsed.getResponseWallpaper();
        } catch (Throwable t) {
            Log.e(TAG, "could not parse " + ASSET, t);
            return new ArrayList<>();
        }
    }

    private String loadJsonFromAsset(String fileName) {
        InputStream input = null;
        try {
            input = getAssets().open(fileName);
            ByteArrayOutputStream out = new ByteArrayOutputStream(Math.max(input.available(), 1024));
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Log.e(TAG, "could not read asset " + fileName, e);
            return null;
        } catch (Throwable t) {
            Log.e(TAG, "could not read asset " + fileName, t);
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (banner != null) {
            banner.resume();
        }
        if (listAdapter != null) {
            listAdapter.loadNativeAd();
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
        if (listAdapter != null) {
            listAdapter.destroy();
            listAdapter = null;
        }
        super.onDestroy();
    }
}
