package org.Allah_Clock_Live_Wallpaper.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.ads.BannerAdController;
import org.Allah_Clock_Live_Wallpaper.ads.NativeAdListAdapter;
import org.Allah_Clock_Live_Wallpaper.adapter.CustomAdapter;
import org.Allah_Clock_Live_Wallpaper.adapter.SmartTextAdapter;
import org.Allah_Clock_Live_Wallpaper.adapter.TextAdapter;
import org.Allah_Clock_Live_Wallpaper.model.Clocks;
import org.Allah_Clock_Live_Wallpaper.model.SmartClocks;
import org.Allah_Clock_Live_Wallpaper.model.TextClocks;
import org.Allah_Clock_Live_Wallpaper.utils.GetClocks;
import org.Allah_Clock_Live_Wallpaper.utils.TinyDB;
import org.Allah_Clock_Live_Wallpaper.utils.UiCompat;

/**
 * Clock list (Analog / Digital / Smart depending on the {@code isWhich} extra).
 *
 * <p>Ads here: an anchored-adaptive banner at the bottom plus one native card injected in
 * the middle of the grid. The golden entries at the end of the list are gated behind a
 * rewarded video.</p>
 */
public class ClockCardActivity extends AppCompatActivity {

    private static final int GRID_SPAN = 2;

    private CustomAdapter customAdapter;
    private SmartTextAdapter smartTextAdapter;
    private TextAdapter textAdapter;
    private NativeAdListAdapter listAdapter;

    private BannerAdController banner;
    private ImageView ivBack;
    private RecyclerView recyclerViewCategory;
    private TinyDB tinyDB;
    private TextView txtTitle;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.tinyDB = new TinyDB(this);
        setContentView(R.layout.activity_clock_card);
        UiCompat.applyEdgeToEdge(this);
        initView();

        this.banner = new BannerAdController(this, (RelativeLayout) findViewById(R.id.bannerAd));
        this.banner.load();
    }

    private void initView() {
        this.txtTitle = findViewById(R.id.txtTitle);
        this.recyclerViewCategory = findViewById(R.id.recyclerViewCategory);
        this.ivBack = findViewById(R.id.ivBack);
        this.ivBack.setOnClickListener(view -> finish());

        int which = getIntent().getIntExtra("isWhich", 0);
        if (which == 0) {
            setupAnalogClocks();
        } else if (which == 1) {
            setupDigitalClocks();
        } else {
            setupSmartClocks();
        }
    }

    private void setupAnalogClocks() {
        this.txtTitle.setText(R.string.title_analog_clock);
        CustomAdapter adapter = new CustomAdapter(new GetClocks().getClocks());
        this.customAdapter = adapter;
        adapter.setClickListener(new CustomAdapter.ClickListener() {
            @Override
            public void setClick(final Clocks clocks) {
                tinyDB.putObject("clocks", clocks);
                tinyDB.putInt("clockType", 0);
                tinyDB.putBoolean("isImage", false);
                tinyDB.putBoolean("isCustomBg", false);
                tinyDB.putInt("bgColor", Color.parseColor(clocks.getBgColor()));
                startActivity(new Intent(ClockCardActivity.this, EditorActivity.class));
            }
        });
        installList(adapter);
    }

    private void setupDigitalClocks() {
        this.txtTitle.setText(R.string.title_digital_clock);
        TextAdapter adapter = new TextAdapter(new GetClocks().getTextClocks());
        this.textAdapter = adapter;
        adapter.setClickListener(new TextAdapter.ClickListener() {
            @Override
            public void setClick(final int i, final TextClocks textClocks) {
                tinyDB.putInt("textClockPosition", i);
                tinyDB.putInt("clockType", 2);
                tinyDB.putBoolean("isImage", false);
                tinyDB.putBoolean("isCustomBg", false);
                tinyDB.putInt("bgColor", Color.parseColor(textClocks.getBgColor()));
                startActivity(new Intent(ClockCardActivity.this, EditorActivity.class));
            }
        });
        installList(adapter);
    }

    private void setupSmartClocks() {
        this.txtTitle.setText(R.string.title_smart_clock);
        SmartTextAdapter adapter = new SmartTextAdapter(new GetClocks().getSmartClocks());
        this.smartTextAdapter = adapter;
        adapter.setClickListener(new SmartTextAdapter.ClickListener() {
            @Override
            public void setClick(final int i, final SmartClocks smartClocks) {
                tinyDB.putInt("customBg", smartClocks.getBgColor());
                tinyDB.putBoolean("isImage", false);
                tinyDB.putBoolean("isCustomBg", true);
                tinyDB.putInt("textClockPosition", i);
                tinyDB.putInt("clockType", 1);
                startActivity(new Intent(ClockCardActivity.this, EditorActivity.class));
            }
        });
        installList(adapter);
    }

    /** Puts the native-ad wrapper around the real adapter and shows the grid. */
    @SuppressWarnings("rawtypes")
    private void installList(RecyclerView.Adapter adapter) {
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
        // Consent can still arrive after onCreate; the call is a no-op once loaded.
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
