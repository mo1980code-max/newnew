package org.Allah_Clock_Live_Wallpaper.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.ads.BannerAdController;
import org.Allah_Clock_Live_Wallpaper.ads.NativeAdListAdapter;
import org.Allah_Clock_Live_Wallpaper.ads.PremiumBackgroundHelper;
import org.Allah_Clock_Live_Wallpaper.adapter.WallpaperAdapter;
import org.Allah_Clock_Live_Wallpaper.model.WallpaperCategory;
import org.Allah_Clock_Live_Wallpaper.model.WallpaperItem;
import org.Allah_Clock_Live_Wallpaper.utils.UiCompat;
import org.Allah_Clock_Live_Wallpaper.utils.WallpaperCatalog;

import java.util.ArrayList;
import java.util.List;

/** Wallpapers inside one category. Banner at the bottom, native card in the middle. */
public class WallpaperActivity extends AppCompatActivity {

    /** Intent extra: index of the category inside {@link WallpaperCatalog}. */
    public static final String EXTRA_CATEGORY_INDEX = "categoryIndex";

    private static final int GRID_SPAN = 2;

    private NativeAdListAdapter listAdapter;
    private BannerAdController banner;

    private ImageView ivBack;
    private RecyclerView recyclerViewCategory;
    private TextView txtTitle;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_wallpaper);
        UiCompat.applyEdgeToEdge(this);

        WallpaperCategory category = WallpaperCatalog.getCategory(
                getIntent().getIntExtra(EXTRA_CATEGORY_INDEX, -1));
        if (category == null) {
            finish();
            return;
        }

        initView(category);

        this.banner = new BannerAdController(this, (RelativeLayout) findViewById(R.id.bannerAd));
        this.banner.load();
    }

    private void initView(final WallpaperCategory category) {
        this.ivBack = findViewById(R.id.ivBack);
        this.txtTitle = findViewById(R.id.txtTitle);
        this.recyclerViewCategory = findViewById(R.id.recyclerViewCategory);

        this.txtTitle.setText(category.getTitleRes());
        this.ivBack.setOnClickListener(view -> finish());

        final List<WallpaperItem> images = mergedWithPremium(category);
        WallpaperAdapter adapter = new WallpaperAdapter(images);
        adapter.setClickListener(new WallpaperAdapter.ClickListener() {
            @Override
            public void setClick(final int i) {
                if (i < 0 || i >= images.size()) {
                    return;
                }
                final WallpaperItem item = images.get(i);
                PremiumBackgroundHelper.onBackgroundClick(viewOf(i), item.getDrawableRes(),
                        () -> openWallpaper(item), () -> adapter.notifyDataSetChanged());
            }
        });

        this.listAdapter = new NativeAdListAdapter(this, adapter, GRID_SPAN);
        this.listAdapter.attachTo(this.recyclerViewCategory);
        this.listAdapter.loadNativeAd();
    }

    /** The premium backgrounds first, then the category's own images. */
    @NonNull
    private List<WallpaperItem> mergedWithPremium(@NonNull WallpaperCategory category) {
        List<WallpaperItem> images = new ArrayList<>();
        for (int premiumRes : WallpaperCatalog.getPremiumBackgrounds()) {
            images.add(new WallpaperItem(premiumRes));
        }
        images.addAll(category.getItems());
        return images;
    }

    private void openWallpaper(@NonNull WallpaperItem item) {
        Intent intent = new Intent(WallpaperActivity.this, SetWallpaperActivity.class);
        intent.putExtra(SetWallpaperActivity.EXTRA_WALLPAPER_RES, item.getDrawableRes());
        startActivity(intent);
    }

    /** The view of one grid position, so the rewarded helper can find its host Activity. */
    private View viewOf(int position) {
        View cell = this.recyclerViewCategory.getLayoutManager() == null ? null
                : this.recyclerViewCategory.getLayoutManager().findViewByPosition(position);
        return cell == null ? this.recyclerViewCategory : cell;
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
        // Coming back from the rewarded video: refresh the badges so an earned background shows
        // its real state straight away.
        if (this.recyclerViewCategory != null && this.recyclerViewCategory.getAdapter() != null) {
            this.recyclerViewCategory.getAdapter().notifyDataSetChanged();
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
