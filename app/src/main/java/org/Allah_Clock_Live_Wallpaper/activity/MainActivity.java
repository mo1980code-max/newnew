package org.Allah_Clock_Live_Wallpaper.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.ads.AdManager;
import org.Allah_Clock_Live_Wallpaper.service.FloatingTasbeehService;
import org.Allah_Clock_Live_Wallpaper.utils.UiCompat;

/**
 * Home screen.
 *
 * <p>Deliberately carries <b>no</b> banner: it is the entry point of the app and the two
 * big tiles are the primary action. Banners live only on the sub-screens.</p>
 *
 * <p>This is also where the UMP consent flow runs, before the ads SDK is initialised, so
 * every later screen can simply ask {@link AdManager#canRequestAds()}.</p>
 */
public class MainActivity extends AppCompatActivity {

    private FrameLayout frameClock;
    private FrameLayout frameWallpaper;
    private ImageView rate;
    private ImageView share;
    private ImageView privacy;
    private ImageView qibla;
    private ImageView tasbeeh;

    /** Returns from the “display over other apps” settings screen. */
    private final ActivityResultLauncher<Intent> overlayLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (Settings.canDrawOverlays(this)) {
                            startTasbeehService();
                        }
                    });

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_select_function);
        UiCompat.applyEdgeToEdge(this);
        initView();

        AdManager.requestConsentAndInitialize(this, this::refreshPrivacyEntry);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshPrivacyEntry();
    }

    /**
     * Google requires a persistent, easy-to-find "Privacy options" control whenever the
     * UMP SDK reports that privacy options are required.
     */
    private void refreshPrivacyEntry() {
        if (privacy == null) {
            return;
        }
        privacy.setVisibility(AdManager.isPrivacyOptionsRequired() ? View.VISIBLE : View.GONE);
    }

    private void initView() {
        this.frameClock = findViewById(R.id.frameClock);
        this.frameWallpaper = findViewById(R.id.frameWallpaper);
        this.rate = findViewById(R.id.rateus);
        this.share = findViewById(R.id.share);
        this.privacy = findViewById(R.id.privacy);
        this.qibla = findViewById(R.id.qibla);
        this.tasbeeh = findViewById(R.id.tasbeeh);

        this.privacy.setOnClickListener(v -> AdManager.showPrivacyOptions(MainActivity.this));

        this.qibla.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, QiblaActivity.class)));

        this.tasbeeh.setOnClickListener(v -> toggleTasbeeh());

        this.rate.setOnClickListener(v -> {
            String packageName = getPackageName();
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + packageName)));
            } catch (ActivityNotFoundException unused) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                            "https://play.google.com/store/apps/details?id=" + packageName)));
                } catch (ActivityNotFoundException ignored) {
                    Toast.makeText(MainActivity.this, R.string.rate_unavailable,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        this.share.setOnClickListener(v -> {
            String message = getString(R.string.app_name)
                    + "\n\nhttps://play.google.com/store/apps/details?id=" + getPackageName();
            Intent send = new Intent(Intent.ACTION_SEND);
            send.setType("text/plain");
            send.putExtra(Intent.EXTRA_TEXT, message);
            startActivity(Intent.createChooser(send, getString(R.string.share_application)));
        });

        this.frameClock.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, ClockFuntionActivity.class)));

        this.frameWallpaper.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, WallpaperCategoryActivity.class)));
    }

    /**
     * The floating tasbeeh is strictly opt-in: it never starts by itself, and the sensitive
     * SYSTEM_ALERT_WINDOW permission is only requested here, on an explicit user tap.
     */
    private void toggleTasbeeh() {
        if (FloatingTasbeehService.isRunning()) {
            stopService(new Intent(this, FloatingTasbeehService.class));
            Toast.makeText(this, R.string.tasbeeh_stopped, Toast.LENGTH_SHORT).show();
            return;
        }
        if (Settings.canDrawOverlays(this)) {
            startTasbeehService();
            return;
        }
        Toast.makeText(this, R.string.tasbeeh_permission_needed, Toast.LENGTH_LONG).show();
        try {
            this.overlayLauncher.launch(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())));
        } catch (Throwable t) {
            try {
                this.overlayLauncher.launch(
                        new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
            } catch (Throwable ignored) {
            }
        }
    }

    private void startTasbeehService() {
        try {
            startService(new Intent(this, FloatingTasbeehService.class));
            Toast.makeText(this, R.string.tasbeeh_started, Toast.LENGTH_SHORT).show();
        } catch (Throwable t) {
            Toast.makeText(this, R.string.tasbeeh_stopped, Toast.LENGTH_LONG).show();
        }
    }
}
