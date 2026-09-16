package org.Allah_Clock_Live_Wallpaper.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.ads.BannerAdController;
import org.Allah_Clock_Live_Wallpaper.ads.NativeAdListAdapter;
import org.Allah_Clock_Live_Wallpaper.adapter.CategoryWallpaperAdapter;
import org.Allah_Clock_Live_Wallpaper.model.WallpaperCategory;
import org.Allah_Clock_Live_Wallpaper.utils.UiCompat;
import org.Allah_Clock_Live_Wallpaper.utils.WallpaperCatalog;

import java.util.List;

/**
 * Wallpaper categories. Banner at the bottom, one native card in the middle of the grid.
 *
 * <p>The catalogue is fully bundled with the APK ({@link WallpaperCatalog}); nothing is
 * fetched from the network here.</p>
 */
public class WallpaperCategoryActivity extends AppCompatActivity {

    private static final int GRID_SPAN = 2;

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

        final List<WallpaperCategory> categories = WallpaperCatalog.getCategories();
        CategoryWallpaperAdapter adapter = new CategoryWallpaperAdapter(categories);
        adapter.setClickListener(new CategoryWallpaperAdapter.ClickListener() {
            @Override
            public void setClick(int position) {
                if (position < 0 || position >= categories.size()) {
                    return;
                }
                Intent intent = new Intent(WallpaperCategoryActivity.this, WallpaperActivity.class);
                intent.putExtra(WallpaperActivity.EXTRA_CATEGORY_INDEX, position);
                startActivity(intent);
            }
        });

        this.listAdapter = new NativeAdListAdapter(this, adapter, GRID_SPAN);
        this.listAdapter.attachTo(this.recyclerViewCategory);
        this.listAdapter.loadNativeAd();
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
