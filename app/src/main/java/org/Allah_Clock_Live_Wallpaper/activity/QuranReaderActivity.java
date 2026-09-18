package org.Allah_Clock_Live_Wallpaper.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.adapter.QuranFlowAdapter;
import org.Allah_Clock_Live_Wallpaper.model.QuranAyah;
import org.Allah_Clock_Live_Wallpaper.model.QuranBookmark;
import org.Allah_Clock_Live_Wallpaper.model.QuranSurah;
import org.Allah_Clock_Live_Wallpaper.utils.LocaleHelper;
import org.Allah_Clock_Live_Wallpaper.utils.QuranRepository;
import org.Allah_Clock_Live_Wallpaper.utils.QuranStore;
import org.Allah_Clock_Live_Wallpaper.utils.QuranTheme;
import org.Allah_Clock_Live_Wallpaper.utils.TinyDB;
import org.Allah_Clock_Live_Wallpaper.utils.UiCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The Quran reader: one surah as one continuous, smooth vertical scroll.
 *
 * <p>The authentic Mushaf header opens the screen with the surah's name and its juz; the verses
 * flow as one column, each closed by its built-in end-of-ayah glyph; and the footer carries the
 * page-number pill of the Madani page under the reader's finger. Tapping a verse saves or clears
 * its bookmark, and a saved verse stays tinted while it is saved.</p>
 *
 * <p>Rotation is declared in the manifest as {@code configChanges="orientation|screenSize"}, so
 * the activity is not recreated and the scroll keeps its place; the column simply re-lays out
 * for the new width. Night reading is an in-app switch held by {@link QuranTheme}: it swaps the
 * palette and repaints the column in place, without losing the position. Reading state
 * (bookmarks, last position, text size, night mode) stays in {@code SharedPreferences} through
 * {@link QuranStore} and {@link TinyDB}.</p>
 */
public final class QuranReaderActivity extends AppCompatActivity {

    private static final String EXTRA_SURAH = "quran_reader_surah";
    private static final String EXTRA_AYAH = "quran_reader_ayah";

    private final ExecutorService loader = Executors.newSingleThreadExecutor();

    private QuranRepository repository;
    private QuranStore store;
    private QuranTheme theme;
    private QuranFlowAdapter adapter;

    private View root;
    private RecyclerView list;
    private View loading;
    private TextView error;
    private TextView title;
    private TextView surahName;
    private TextView juzChip;
    private TextView meta;
    private TextView pagePill;

    private int surahNumber;
    private int textSizeSp;
    /** Row the reader was asked to open at; consumed once the column is bound. */
    private int initialRow = -1;

    public static Intent createIntent(@NonNull Context context, int surahNumber, int ayahNumber) {
        return new Intent(context, QuranReaderActivity.class)
                .putExtra(EXTRA_SURAH, surahNumber)
                .putExtra(EXTRA_AYAH, ayahNumber);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quran_reader);
        UiCompat.applyEdgeToEdge(this);

        this.store = new QuranStore(this);
        TinyDB preferences = new TinyDB(this);
        this.theme = new QuranTheme(this, this.store.isNightMode(preferences));
        this.textSizeSp = this.store.getTextSizeSp();

        this.root = findViewById(R.id.quranReaderRoot);
        this.list = findViewById(R.id.quranReaderList);
        this.loading = findViewById(R.id.quranReaderLoading);
        this.error = findViewById(R.id.quranReaderError);
        this.title = findViewById(R.id.quranReaderTitle);
        this.surahName = findViewById(R.id.quranReaderSurahName);
        this.juzChip = findViewById(R.id.quranReaderJuz);
        this.meta = findViewById(R.id.quranReaderMeta);
        this.pagePill = findViewById(R.id.quranReaderPagePill);
        this.title.setText(R.string.title_quran);

        this.list.setLayoutManager(new LinearLayoutManager(this));
        this.list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    updateFooterForTopAyah();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                // The pill must follow the finger, not wait for the scroll to settle.
                if (dy != 0) {
                    updateFooterForTopAyah();
                }
            }
        });

        findViewById(R.id.quranReaderBack).setOnClickListener(view -> finish());
        findViewById(R.id.quranReaderNight).setOnClickListener(view -> toggleNightMode());
        findViewById(R.id.quranReaderTextSize).setOnClickListener(view -> showTextSizePicker());
        findViewById(R.id.quranReaderBookmarks).setOnClickListener(view -> startActivity(
                new Intent(this, QuranBookmarksActivity.class)));

        applyTheme();
        loadRepository();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Rebind after the saved-marks screen: the tints are the bookmark store's own.
        if (this.adapter != null) {
            this.adapter.refreshTheme();
        }
    }

    @Override
    protected void onPause() {
        saveVisiblePosition();
        super.onPause();
    }

    private void loadRepository() {
        loader.execute(() -> {
            try {
                final QuranRepository loaded = QuranRepository.get(getApplicationContext());
                runOnUiThread(() -> showRepository(loaded));
            } catch (IOException error) {
                runOnUiThread(this::showLoadError);
            }
        });
    }

    private void showRepository(@NonNull QuranRepository loaded) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        this.repository = loaded;
        QuranBookmark start = resolveStartPosition();
        QuranSurah surah = loaded.getSurah(start.getSurahNumber());
        if (surah == null) {
            showLoadError();
            return;
        }
        this.surahNumber = surah.getNumber();
        boolean arabicUi = LocaleHelper.isArabic(this);
        this.title.setText(getString(R.string.quran_surah_title, surah.getNumber(),
                surah.getDisplayName(arabicUi)));

        // The authentic Mushaf header: the surah's name (its printed, Arabic form) and its juz.
        QuranAyah firstAyah = loaded.getAyah(surah.getNumber(), 1);
        int juz = firstAyah == null ? 1 : firstAyah.getJuz();
        this.surahName.setText(surah.getArabicName());
        this.juzChip.setText(getString(R.string.quran_juz_number, juz));
        String revelation = getString(surah.isMeccan() ? R.string.quran_meccan
                : R.string.quran_medinan);
        this.meta.setText(getString(R.string.quran_surah_metadata, surah.getAyahCount(),
                revelation));

        List<QuranFlowAdapter.Row> rows = new ArrayList<>(surah.getAyahCount() + 1);
        rows.add(QuranFlowAdapter.Row.surah(surah, juz));
        for (QuranAyah ayah : loaded.getAyahs(surah.getNumber())) {
            rows.add(QuranFlowAdapter.Row.ayah(ayah));
        }
        this.initialRow = 1 + Math.max(0, start.getAyahNumber() - 1);

        this.adapter = new QuranFlowAdapter(rows, this.theme, this.store, this.textSizeSp,
                this::toggleVerse);
        this.list.setAdapter(this.adapter);
        this.list.scrollToPosition(Math.min(this.initialRow, rows.size() - 1));

        this.loading.setVisibility(View.GONE);
        this.error.setVisibility(View.GONE);
        this.list.setVisibility(View.VISIBLE);

        this.store.saveLastReading(start.getSurahNumber(), start.getAyahNumber());
        updateFooterForTopAyah();
    }

    // ══════════════════════════ header, pill and position ══════════════════════════

    /** The row of the first ayah at or under the top of the visible area, or -1. */
    private int topAyahRow() {
        if (this.adapter == null || this.adapter.getItemCount() == 0) {
            return -1;
        }
        LinearLayoutManager layout = (LinearLayoutManager) this.list.getLayoutManager();
        if (layout == null) {
            return -1;
        }
        int position = layout.findFirstVisibleItemPosition();
        if (position == RecyclerView.NO_POSITION) {
            return -1;
        }
        int last = this.adapter.getItemCount() - 1;
        while (position <= last
                && this.adapter.getItemViewType(position) != QuranFlowAdapter.TYPE_AYAH) {
            position++;
        }
        return position <= last ? position : -1;
    }

    private QuranAyah ayahAtRow(int row) {
        if (this.adapter == null || row < 0 || row >= this.adapter.getItemCount()) {
            return null;
        }
        return this.adapter.getRows().get(row).ayah;
    }

    /** Refreshes the footer's page-number pill from the verse the reader is on. */
    private void updateFooterForTopAyah() {
        QuranAyah ayah = ayahAtRow(topAyahRow());
        if (ayah == null) {
            return;
        }
        this.pagePill.setText(getString(R.string.quran_page_number, ayah.getMushafPage(),
                QuranRepository.PAGE_COUNT));
    }

    /** Writes the verse the reader is on, so reopening the reader resumes there. */
    private void saveVisiblePosition() {
        if (this.store == null || this.surahNumber <= 0) {
            return;
        }
        QuranAyah ayah = ayahAtRow(topAyahRow());
        if (ayah != null) {
            this.store.saveLastReading(ayah.getSurahNumber(), ayah.getAyahNumber());
        }
    }

    // ══════════════════════════════ bookmarks and theme ══════════════════════════════

    /** A tap toggles the save of one verse; it never moves the reading position by itself. */
    private void toggleVerse(@NonNull QuranAyah ayah) {
        boolean added = this.store.toggleBookmark(ayah.getSurahNumber(), ayah.getAyahNumber());
        if (this.adapter != null) {
            for (int position = 0; position < this.adapter.getItemCount(); position++) {
                QuranAyah bound = ayahAtRow(position);
                if (bound != null && bound.getSurahNumber() == ayah.getSurahNumber()
                        && bound.getAyahNumber() == ayah.getAyahNumber()) {
                    this.adapter.refreshAyah(position);
                    break;
                }
            }
        }
        Toast.makeText(this, added ? R.string.quran_bookmark_added : R.string.quran_bookmark_removed,
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Swaps the palette in place. Nothing is rebuilt: the column reads its colours from the same
     * {@link QuranTheme} instance, so the page turns dark in place — no re-layout, no lost
     * position.
     */
    private void toggleNightMode() {
        boolean night = !this.theme.isNight();
        this.theme.apply(this, night);
        this.store.saveNightMode(new TinyDB(this), night);
        applyTheme();
        if (this.adapter != null) {
            this.adapter.refreshTheme();
        }
        Toast.makeText(this, night ? R.string.quran_night_enabled : R.string.quran_day_enabled,
                Toast.LENGTH_SHORT).show();
    }

    /** Paints every surface of the screen from the active theme. */
    private void applyTheme() {
        if (this.theme == null) {
            return;
        }
        this.root.setBackgroundColor(this.theme.paper);
        getWindow().setBackgroundDrawableResource(this.theme.isNight()
                ? R.color.quranNightPaper : R.color.quranPaper);

        GradientDrawable card = new GradientDrawable();
        card.setShape(GradientDrawable.RECTANGLE);
        card.setColor(this.theme.surface);
        card.setCornerRadius(UiCompat.dp(this, 18f));
        card.setStroke(UiCompat.dp(this, 1f), this.theme.line);
        findViewById(R.id.quranReaderHeader).setBackground(card);

        GradientDrawable chip = new GradientDrawable();
        chip.setShape(GradientDrawable.RECTANGLE);
        chip.setColor(this.theme.softGreen);
        chip.setCornerRadius(UiCompat.dp(this, 8f));
        this.juzChip.setBackground(chip);
        this.pagePill.setBackground(chip);

        tint(R.id.quranReaderBack, this.theme.ink);
        tint(R.id.quranReaderNight, this.theme.isNight() ? this.theme.gold : this.theme.green);
        tint(R.id.quranReaderTextSize, this.theme.green);
        tint(R.id.quranReaderBookmarks, this.theme.green);

        this.title.setTextColor(this.theme.ink);
        this.surahName.setTextColor(this.theme.ink);
        this.juzChip.setTextColor(this.theme.greenDark);
        this.meta.setTextColor(this.theme.muted);
        this.pagePill.setTextColor(this.theme.greenDark);
    }

    private void tint(int viewId, int colour) {
        View view = findViewById(viewId);
        if (!(view instanceof ImageButton)) {
            return;
        }
        ImageButton button = (ImageButton) view;
        android.graphics.drawable.Drawable icon = button.getDrawable();
        if (icon != null) {
            android.graphics.drawable.Drawable wrapped = DrawableCompat.wrap(icon.mutate());
            DrawableCompat.setTint(wrapped, colour);
            button.setImageDrawable(wrapped);
        }
    }

    // ═══════════════════════════════ text size and links ═══════════════════════════════

    private void showTextSizePicker() {
        if (this.store == null) {
            return;
        }
        NumberPicker picker = new NumberPicker(this);
        picker.setMinValue(QuranStore.MIN_TEXT_SIZE_SP);
        picker.setMaxValue(QuranStore.MAX_TEXT_SIZE_SP);
        picker.setValue(this.store.getTextSizeSp());
        picker.setWrapSelectorWheel(false);
        new AlertDialog.Builder(this)
                .setTitle(R.string.quran_text_size)
                .setView(picker)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    int selected = picker.getValue();
                    this.textSizeSp = selected;
                    this.store.saveTextSizeSp(selected);
                    if (this.adapter != null) {
                        this.adapter.setTextSizeSp(selected);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @NonNull
    private QuranBookmark resolveStartPosition() {
        int requestedSurah = getIntent().getIntExtra(EXTRA_SURAH, -1);
        int requestedAyah = getIntent().getIntExtra(EXTRA_AYAH, -1);
        if (this.repository != null && this.repository.getAyah(requestedSurah, requestedAyah)
                != null) {
            return new QuranBookmark(requestedSurah, requestedAyah);
        }
        QuranBookmark last = this.store.getLastReading();
        if (last != null && this.repository != null
                && this.repository.getAyah(last.getSurahNumber(), last.getAyahNumber()) != null) {
            // The reader always opens the surah it was last reading at.
            return new QuranBookmark(last.getSurahNumber(), last.getAyahNumber());
        }
        return new QuranBookmark(1, 1);
    }

    private void showLoadError() {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        this.loading.setVisibility(View.GONE);
        this.list.setVisibility(View.GONE);
        this.error.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        loader.shutdownNow();
        super.onDestroy();
    }
}
