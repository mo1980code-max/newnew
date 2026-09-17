package org.Allah_Clock_Live_Wallpaper.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.ads.AdManager;
import org.Allah_Clock_Live_Wallpaper.service.FloatingTasbeehService;
import org.Allah_Clock_Live_Wallpaper.utils.LocaleHelper;
import org.Allah_Clock_Live_Wallpaper.utils.PrayerWindow;
import org.Allah_Clock_Live_Wallpaper.utils.TinyDB;
import org.Allah_Clock_Live_Wallpaper.utils.UiCompat;

/**
 * Home screen.
 *
 * <p>Deliberately carries <b>no</b> banner: it is the entry point of the app and the four
 * big tiles are the primary action. Banners live only on the sub-screens.</p>
 *
 * <p>The Quran and the athkar each own a gold-framed, taller tile rather than the rasters the
 * clock and the wallpapers use: they open reading surfaces, not design pickers. The athkar tile
 * stays visible at all hours - the golden chip inside it is what appears only inside the
 * morning / evening window.</p>
 *
 * <p>This is also where the UMP consent flow runs, before the ads SDK is initialised, so
 * every later screen can simply ask {@link AdManager#canRequestAds()}.</p>
 */
public class MainActivity extends AppCompatActivity {

    private FrameLayout frameClock;
    private FrameLayout frameWallpaper;
    private FrameLayout frameQuran;
    private FrameLayout frameAthkar;
    private ImageView rate;
    private ImageView share;
    private ImageView privacy;
    private ImageView qibla;
    private ImageView tasbeeh;
    private ImageView language;
    private TextView athkarWindowChip;
    private TinyDB tinyDB;

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
        refreshAthkarTile();
    }

    /**
     * The golden chip inside the athkar tile appears only inside the morning / evening window
     * and hides completely outside it; the window is evaluated right here, lazily, with no
     * scheduler involved. The tile itself never disappears: it is the way into the reader.
     */
    private void refreshAthkarTile() {
        if (this.athkarWindowChip == null) {
            return;
        }
        int window = PrayerWindow.currentWindow(this, this.tinyDB, System.currentTimeMillis());
        boolean visible = window != PrayerWindow.NONE
                && this.tinyDB.getBoolean("showAthkarBadge", true);
        this.athkarWindowChip.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (visible) {
            this.athkarWindowChip.setText(window == PrayerWindow.MORNING
                    ? R.string.athkar_morning_title
                    : R.string.athkar_evening_title);
        }
    }

    private void showLanguageDialog() {
        String[] tags = {LocaleHelper.TAG_EN, LocaleHelper.TAG_AR};
        String[] labels = {getString(R.string.language_english),
                getString(R.string.language_arabic)};
        int checked = LocaleHelper.isArabic(this) ? 1 : 0;
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.choose_language)
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    LocaleHelper.apply(tags[which]);
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
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
        this.frameQuran = findViewById(R.id.frameQuran);
        this.frameAthkar = findViewById(R.id.frameAthkar);
        this.rate = findViewById(R.id.rateus);
        this.share = findViewById(R.id.share);
        this.privacy = findViewById(R.id.privacy);
        this.qibla = findViewById(R.id.qibla);
        this.tasbeeh = findViewById(R.id.tasbeeh);
        this.language = findViewById(R.id.language);
        this.athkarWindowChip = findViewById(R.id.athkarWindowChip);
        this.tinyDB = new TinyDB(this);

        this.privacy.setOnClickListener(v -> AdManager.showPrivacyOptions(MainActivity.this));

        this.qibla.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, QiblaActivity.class)));

        this.tasbeeh.setOnClickListener(v -> toggleTasbeeh());

        this.language.setOnClickListener(v -> showLanguageDialog());

        // Outside both windows the reader opens on the closest set and says so, instead of
        // closing the screen the user just asked for.
        this.frameAthkar.setOnClickListener(v -> startActivity(AthkarActivity.createIntent(
                MainActivity.this,
                PrayerWindow.windowOrUpcoming(MainActivity.this, this.tinyDB,
                        System.currentTimeMillis()))));

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

        this.frameQuran.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, QuranIndexActivity.class)));
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
