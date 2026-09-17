package org.Allah_Clock_Live_Wallpaper.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.ads.AdManager;
import org.Allah_Clock_Live_Wallpaper.service.FloatingTasbeehService;
import org.Allah_Clock_Live_Wallpaper.utils.LocaleHelper;
import org.Allah_Clock_Live_Wallpaper.utils.PrayerWindow;
import org.Allah_Clock_Live_Wallpaper.utils.TinyDB;
import org.Allah_Clock_Live_Wallpaper.utils.UiCompat;
import org.Allah_Clock_Live_Wallpaper.utils.UiMotion;

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
 *
 * <p>The top action bar (misbaha, language, qibla - plus privacy, rate and share) is a row of
 * frosted-glass tiles. Each one is wired here through {@link #bindGlassAction(int, Runnable)},
 * which attaches the press micro-interaction and the haptic tick in a single place, so a new
 * button on the bar cannot arrive without either.</p>
 */
public class MainActivity extends AppCompatActivity {

    private FrameLayout frameClock;
    private FrameLayout frameWallpaper;
    private FrameLayout frameQuran;
    private FrameLayout frameAthkar;
    private ImageView privacy;
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
        // The dashboard is a gradient, so the strip the system bars are drawn over has to be the
        // same tone as its top: applyEdgeToEdge() paints that area white for the list screens.
        getWindow().setBackgroundDrawable(
                new ColorDrawable(ContextCompat.getColor(this, R.color.homeBackdropTop)));
        initView();
        playEntryAnimation();

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
        this.privacy = findViewById(R.id.privacy);
        this.athkarWindowChip = findViewById(R.id.athkarWindowChip);
        this.tinyDB = new TinyDB(this);

        bindGlassAction(R.id.privacy, () -> AdManager.showPrivacyOptions(MainActivity.this));
        bindGlassAction(R.id.qibla,
                () -> startActivity(new Intent(MainActivity.this, QiblaActivity.class)));
        bindGlassAction(R.id.tasbeeh, this::toggleTasbeeh);
        bindGlassAction(R.id.language, this::showLanguageDialog);
        bindGlassAction(R.id.rateus, this::openStorePage);
        bindGlassAction(R.id.share, this::shareApplication);

        // Outside both windows the reader opens on the closest set and says so, instead of
        // closing the screen the user just asked for.
        this.frameAthkar.setOnClickListener(v -> startActivity(AthkarActivity.createIntent(
                MainActivity.this,
                PrayerWindow.windowOrUpcoming(MainActivity.this, this.tinyDB,
                        System.currentTimeMillis()))));

        this.frameClock.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, ClockFuntionActivity.class)));

        this.frameWallpaper.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, WallpaperCategoryActivity.class)));

        this.frameQuran.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, QuranIndexActivity.class)));
    }

    /**
     * Wires one tile of the glass action bar: the press micro-interaction (a scale dip while the
     * finger is down), the haptic tick on the tap and the action itself. One helper for the whole
     * bar keeps the six tiles behaving identically.
     */
    private void bindGlassAction(int viewId, @NonNull Runnable action) {
        View tile = findViewById(viewId);
        UiMotion.pressable(tile);
        tile.setOnClickListener(view -> {
            UiMotion.tick(view);
            action.run();
        });
    }

    /** A short, quiet entrance: the title fades up and the glass bar follows it. */
    private void playEntryAnimation() {
        View title = findViewById(R.id.homeTitle);
        View bar = findViewById(R.id.language);
        View parent = bar == null ? null : (View) bar.getParent();
        long duration = getResources().getInteger(android.R.integer.config_mediumAnimTime);
        if (parent != null) {
            parent.setAlpha(0f);
            parent.setTranslationY(-UiCompat.dp(this, 8));
            parent.animate().alpha(1f).translationY(0f).setStartDelay(80L)
                    .setDuration(duration).start();
        }
        title.setAlpha(0f);
        title.setTranslationY(UiCompat.dp(this, 10));
        title.animate().alpha(1f).translationY(0f).setDuration(duration).start();
    }

    private void openStorePage() {
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
    }

    private void shareApplication() {
        String message = getString(R.string.app_name)
                + "\n\nhttps://play.google.com/store/apps/details?id=" + getPackageName();
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, message);
        startActivity(Intent.createChooser(send, getString(R.string.share_application)));
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
