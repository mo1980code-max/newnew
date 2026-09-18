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
import org.Allah_Clock_Live_Wallpaper.model.QuranJuz;
import org.Allah_Clock_Live_Wallpaper.model.QuranSurah;
import org.Allah_Clock_Live_Wallpaper.utils.QuranRepository;
import org.Allah_Clock_Live_Wallpaper.utils.QuranStore;
import org.Allah_Clock_Live_Wallpaper.utils.QuranTheme;
import org.Allah_Clock_Live_Wallpaper.utils.TinyDB;
import org.Allah_Clock_Live_Wallpaper.utils.UiCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Mushaf mode: the complete 604-page Madani Mushaf as one continuous, smooth vertical scroll.
 *
 * <p>Surahs open with their authentic heading (name + juz), Madani page boundaries are marked by
 * a hairline carrying the printed page number, and the footer's page-number pill follows the
 * position under the reader's finger. Tapping a verse saves or clears its bookmark; tapping the
 * pill or the juz chip scrolls to the chosen page or juz. Rotation is absorbed by the manifest's
 * {@code configChanges}, so the scroll keeps its place, and the night reading switch repaints
 * the column in place through the shared {@link QuranTheme}.</p>
 */
public final class QuranMushafActivity extends AppCompatActivity {

    private static final String EXTRA_PAGE = "quran_mushaf_page";

    private final ExecutorService loader = Executors.newSingleThreadExecutor();

    private QuranRepository repository;
    private QuranStore store;
    private QuranTheme theme;
    private QuranFlowAdapter adapter;

    private View root;
    private RecyclerView list;
    private View loading;
    private TextView error;
    private TextView surahName;
    private TextView juzChip;
    private TextView pagePill;

    private int textSizeSp;
    private int pendingPage = -1;
    /** Row of every Madani page boundary, for the page and juz pickers. */
    private final Map<Integer, Integer> pageToRow = new HashMap<>();

    @NonNull
    public static Intent createIntent(@NonNull Context context, int pageNumber) {
        return new Intent(context, QuranMushafActivity.class)
                .putExtra(EXTRA_PAGE, pageNumber);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quran_mushaf);
        UiCompat.applyEdgeToEdge(this);

        this.store = new QuranStore(this);
        TinyDB preferences = new TinyDB(this);
        this.theme = new QuranTheme(this, this.store.isNightMode(preferences));
        this.textSizeSp = this.store.getTextSizeSp();

        this.root = findViewById(R.id.quranMushafRoot);
        this.list = findViewById(R.id.quranMushafList);
        this.loading = findViewById(R.id.quranMushafLoading);
        this.error = findViewById(R.id.quranMushafError);
        this.surahName = findViewById(R.id.quranMushafSurahName);
        this.juzChip = findViewById(R.id.quranMushafJuz);
        this.pagePill = findViewById(R.id.quranMushafPagePill);

        this.list.setLayoutManager(new LinearLayoutManager(this));
        this.list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    updateHeaderForTopAyah();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy != 0) {
                    updateHeaderForTopAyah();
                }
            }
        });

        findViewById(R.id.quranMushafBack).setOnClickListener(view -> finish());
        findViewById(R.id.quranMushafNight).setOnClickListener(view -> toggleNightMode());
        findViewById(R.id.quranMushafTextSize).setOnClickListener(view -> showTextSizePicker());
        findViewById(R.id.quranMushafBookmarks).setOnClickListener(view -> startActivity(
                new Intent(this, QuranBookmarksActivity.class)));
        this.juzChip.setOnClickListener(view -> showJuzPicker());
        this.pagePill.setOnClickListener(view -> showPagePicker());

        applyTheme();
        loadRepository();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
            } catch (IOException failure) {
                runOnUiThread(this::showLoadError);
            }
        });
    }

    private void showRepository(@NonNull QuranRepository loaded) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        this.repository = loaded;

        // One row per verse of the complete Mushaf, with the surah headings and the Madani page
        // boundaries interleaved exactly where the printed book has them.
        List<QuranFlowAdapter.Row> rows = new ArrayList<>(QuranRepository.AYAH_COUNT
                + QuranRepository.SURAH_COUNT + QuranRepository.PAGE_COUNT);
        this.pageToRow.clear();
        int previousPage = 0;
        for (QuranSurah surah : loaded.getSurahs()) {
            QuranAyah first = loaded.getAyah(surah.getNumber(), 1);
            rows.add(QuranFlowAdapter.Row.surah(surah, first == null ? 1 : first.getJuz()));
            for (QuranAyah ayah : loaded.getAyahs(surah.getNumber())) {
                if (ayah.getMushafPage() != previousPage) {
                    rows.add(QuranFlowAdapter.Row.page(ayah.getMushafPage()));
                    this.pageToRow.put(ayah.getMushafPage(), rows.size() - 1);
                    previousPage = ayah.getMushafPage();
                }
                rows.add(QuranFlowAdapter.Row.ayah(ayah));
            }
        }

        int requested = getIntent().getIntExtra(EXTRA_PAGE, -1);
        if (!loaded.hasPage(requested)) {
            QuranBookmark last = this.store.getLastReading();
            if (last != null) {
                requested = loaded.getPageForAyah(last.getSurahNumber(), last.getAyahNumber());
            }
        }
        this.pendingPage = requested > 0 ? requested : 1;

        this.adapter = new QuranFlowAdapter(rows, this.theme, this.store, this.textSizeSp,
                this::toggleVerse);
        this.list.setAdapter(this.adapter);
        scrollToPage(this.pendingPage, false);

        this.loading.setVisibility(View.GONE);
        this.error.setVisibility(View.GONE);
        this.list.setVisibility(View.VISIBLE);
        updateHeaderForTopAyah();
    }

    // ══════════════════════════════ header and pill ══════════════════════════════

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

    /** Refreshes the authentic header (surah + juz) and the footer pill from the top verse. */
    private void updateHeaderForTopAyah() {
        QuranAyah ayah = ayahAtRow(topAyahRow());
        if (ayah == null || this.repository == null) {
            return;
        }
        QuranSurah surah = this.repository.getSurah(ayah.getSurahNumber());
        this.surahName.setText(surah == null ? "" : surah.getArabicName());
        this.juzChip.setText(getString(R.string.quran_juz_number, ayah.getJuz()));
        this.pagePill.setText(getString(R.string.quran_page_number, ayah.getMushafPage(),
                QuranRepository.PAGE_COUNT));
        this.pendingPage = ayah.getMushafPage();
    }

    /** Turns to a Madani page; out-of-range requests are ignored rather than clamped. */
    private void scrollToPage(int pageNumber, boolean smooth) {
        Integer row = this.pageToRow.get(pageNumber);
        if (row == null || this.adapter == null) {
            return;
        }
        this.pendingPage = pageNumber;
        if (smooth) {
            this.list.smoothScrollToPosition(row);
        } else {
            this.list.scrollToPosition(row);
        }
    }

    /** Writes the verse the reader is on, so reopening the Mushaf resumes there. */
    private void saveVisiblePosition() {
        if (this.store == null || this.repository == null) {
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

    /** Swaps the palette in place: the column repaints without losing its position. */
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
        findViewById(R.id.quranMushafHeader).setBackground(card);

        GradientDrawable chip = new GradientDrawable();
        chip.setShape(GradientDrawable.RECTANGLE);
        chip.setColor(this.theme.softGreen);
        chip.setCornerRadius(UiCompat.dp(this, 8f));
        this.juzChip.setBackground(chip);
        this.pagePill.setBackground(chip);

        tint(R.id.quranMushafBack, this.theme.ink);
        tint(R.id.quranMushafNight, this.theme.isNight() ? this.theme.gold : this.theme.green);
        tint(R.id.quranMushafTextSize, this.theme.green);
        tint(R.id.quranMushafBookmarks, this.theme.green);
        tint(R.id.quranMushafTitle, this.theme.ink);

        this.surahName.setTextColor(this.theme.ink);
        this.juzChip.setTextColor(this.theme.greenDark);
        ((TextView) findViewById(R.id.quranMushafHint)).setTextColor(this.theme.muted);
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

    // ═══════════════════════════════ pickers and size ═══════════════════════════════

    private void showTextSizePicker() {
        if (this.store == null || this.adapter == null) {
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
                    this.adapter.setTextSizeSp(selected);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showJuzPicker() {
        if (this.repository == null || this.adapter == null) {
            return;
        }
        NumberPicker picker = new NumberPicker(this);
        picker.setMinValue(1);
        picker.setMaxValue(QuranRepository.JUZ_COUNT);
        int currentJuz = this.repository.getJuzForPage(this.pendingPage);
        picker.setValue(currentJuz > 0 ? currentJuz : 1);
        picker.setWrapSelectorWheel(false);
        new AlertDialog.Builder(this)
                .setTitle(R.string.quran_choose_juz)
                .setView(picker)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    QuranJuz juz = this.repository.getJuz(picker.getValue());
                    if (juz != null) {
                        scrollToPage(juz.getPageNumber(), true);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showPagePicker() {
        if (this.repository == null || this.adapter == null) {
            return;
        }
        NumberPicker picker = new NumberPicker(this);
        picker.setMinValue(1);
        picker.setMaxValue(QuranRepository.PAGE_COUNT);
        picker.setValue(Math.max(1, this.pendingPage));
        picker.setWrapSelectorWheel(false);
        new AlertDialog.Builder(this)
                .setTitle(R.string.quran_choose_page)
                .setView(picker)
                .setPositiveButton(R.string.ok, (dialog, which) ->
                        scrollToPage(picker.getValue(), true))
                .setNegativeButton(R.string.cancel, null)
                .show();
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
