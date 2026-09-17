package org.Allah_Clock_Live_Wallpaper.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.viewpager2.widget.ViewPager2;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.adapter.QuranMushafAdapter;
import org.Allah_Clock_Live_Wallpaper.model.QuranAyah;
import org.Allah_Clock_Live_Wallpaper.model.QuranJuz;
import org.Allah_Clock_Live_Wallpaper.model.QuranPage;
import org.Allah_Clock_Live_Wallpaper.model.QuranSurah;
import org.Allah_Clock_Live_Wallpaper.utils.LocaleHelper;
import org.Allah_Clock_Live_Wallpaper.utils.QuranPageBuilder;
import org.Allah_Clock_Live_Wallpaper.utils.QuranRepository;
import org.Allah_Clock_Live_Wallpaper.utils.QuranStore;
import org.Allah_Clock_Live_Wallpaper.utils.QuranTheme;
import org.Allah_Clock_Live_Wallpaper.utils.TinyDB;
import org.Allah_Clock_Live_Wallpaper.utils.UiCompat;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Page-by-page Quran reading using the 604 canonical Madani-page boundaries, flipped sideways.
 *
 * <p>One {@code ViewPager2} page per printed page: swiping (or the buttons) turns to the previous
 * and next page exactly like leafing through a Mushaf, rather than scrolling a list of ayah cards.
 * The canonical page boundaries are untouched — the text inside a page is reflowed for the screen
 * (this is not a scanned Mushaf glyph layout), but which ayahs belong to page 42 is the printed
 * answer.</p>
 *
 * <p>Every page carries the same saved-mark behaviour and the same night reading switch as the
 * surah reader, driven by the shared {@link QuranTheme} and {@link QuranStore}.</p>
 */
public final class QuranMushafActivity extends AppCompatActivity {

    private static final String EXTRA_PAGE = "quran_mushaf_page";

    private final ExecutorService loader = Executors.newSingleThreadExecutor();

    private QuranRepository repository;
    private QuranStore store;
    private QuranTheme theme;
    private TextPaint paint;
    private float lineSpacingExtraPx;

    private View root;
    private ViewPager2 pager;
    private QuranMushafAdapter adapter;
    private View loading;
    private TextView error;
    private TextView pageLabel;
    private TextView range;
    private TextView juzLabel;
    private Button previous;
    private Button next;

    private int textSizeSp;
    private int pendingPage = -1;

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
        this.paint = this.theme.newTextPaint(spToPx(this.textSizeSp));
        this.lineSpacingExtraPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                QuranPageBuilder.LINE_SPACING_EXTRA_DP, getResources().getDisplayMetrics());

        this.root = findViewById(R.id.quranMushafRoot);
        this.pager = findViewById(R.id.quranMushafPager);
        this.loading = findViewById(R.id.quranMushafLoading);
        this.error = findViewById(R.id.quranMushafError);
        this.pageLabel = findViewById(R.id.quranMushafPageLabel);
        this.range = findViewById(R.id.quranMushafRange);
        this.juzLabel = findViewById(R.id.quranMushafJuz);
        this.previous = findViewById(R.id.quranMushafPrevious);
        this.next = findViewById(R.id.quranMushafNext);

        findViewById(R.id.quranMushafBack).setOnClickListener(view -> finish());
        ImageButton textSize = findViewById(R.id.quranMushafTextSize);
        textSize.setOnClickListener(view -> showTextSizePicker());
        ImageButton night = findViewById(R.id.quranMushafNight);
        night.setOnClickListener(view -> toggleNightMode());
        ImageButton bookmarks = findViewById(R.id.quranMushafBookmarks);
        bookmarks.setOnClickListener(view -> startActivity(new Intent(this,
                QuranBookmarksActivity.class)));
        this.previous.setOnClickListener(view -> showPage(currentPage() - 1));
        this.next.setOnClickListener(view -> showPage(currentPage() + 1));
        this.pageLabel.setOnClickListener(view -> showPagePicker());
        this.juzLabel.setOnClickListener(view -> showJuzPicker());

        this.pager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        this.pager.setOffscreenPageLimit(1);
        this.pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                onPageShown(QuranMushafAdapter.pageAt(position));
            }
        });

        applyTheme();
        loadRepository();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Only the amount of text per page changed, not the canonical boundaries.
        this.pager.post(() -> showPage(currentPage()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.adapter != null) {
            this.adapter.notifyDataSetChanged();
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
        int requested = getIntent().getIntExtra(EXTRA_PAGE, -1);
        if (loaded.getPage(requested) == null) {
            org.Allah_Clock_Live_Wallpaper.model.QuranBookmark last = this.store.getLastReading();
            if (last != null) {
                requested = loaded.getPageForAyah(last.getSurahNumber(), last.getAyahNumber());
            }
        }
        installPager();
        showPage(requested > 0 ? requested : 1);
    }

    /** Builds the 604-page pager; pages are rendered on demand from the bundled text. */
    private void installPager() {
        this.adapter = new QuranMushafAdapter(this.repository, this.store, this.theme, this.paint,
                this.lineSpacingExtraPx, LocaleHelper.isArabic(this), this::toggleVerse);
        this.pager.setAdapter(this.adapter);
        this.loading.setVisibility(View.GONE);
        this.error.setVisibility(View.GONE);
        this.pager.setVisibility(View.VISIBLE);
    }

    /** Turns to a canonical page; out-of-range requests are ignored rather than clamped. */
    private void showPage(int pageNumber) {
        if (this.repository == null || this.repository.getPage(pageNumber) == null) {
            return;
        }
        saveVisiblePosition();
        this.pendingPage = pageNumber;
        this.pager.setCurrentItem(QuranMushafAdapter.positionOfPage(pageNumber), false);
        onPageShown(pageNumber);
    }

    private int currentPage() {
        return QuranMushafAdapter.pageAt(this.pager.getCurrentItem());
    }

    /** Refreshes the header of the page that is now open. */
    private void onPageShown(int pageNumber) {
        if (this.repository == null) {
            return;
        }
        QuranPage page = this.repository.getPage(pageNumber);
        if (page == null) {
            return;
        }
        this.pendingPage = pageNumber;
        this.pageLabel.setText(getString(R.string.quran_page_number, pageNumber,
                QuranRepository.PAGE_COUNT));
        this.range.setText(pageRange(page));
        int firstJuz = this.repository.getJuzForAyah(page.getFirstAyah().getSurahNumber(),
                page.getFirstAyah().getAyahNumber());
        int lastJuz = this.repository.getJuzForAyah(page.getLastAyah().getSurahNumber(),
                page.getLastAyah().getAyahNumber());
        this.juzLabel.setText(firstJuz <= 0 ? "" : firstJuz == lastJuz
                ? getString(R.string.quran_juz_number, firstJuz)
                : getString(R.string.quran_juz_range, firstJuz, lastJuz));
        boolean hasPrevious = pageNumber > 1;
        boolean hasNext = pageNumber < QuranRepository.PAGE_COUNT;
        this.previous.setEnabled(hasPrevious);
        this.previous.setAlpha(hasPrevious ? 1f : 0.42f);
        this.next.setEnabled(hasNext);
        this.next.setAlpha(hasNext ? 1f : 0.42f);
        this.juzLabel.setEnabled(firstJuz > 0);

        this.store.saveLastReading(page.getFirstAyah().getSurahNumber(),
                page.getFirstAyah().getAyahNumber());
    }

    @NonNull
    private String pageRange(@NonNull QuranPage page) {
        boolean arabicUi = LocaleHelper.isArabic(this);
        QuranSurah firstSurah = this.repository.getSurah(page.getFirstAyah().getSurahNumber());
        QuranSurah lastSurah = this.repository.getSurah(page.getLastAyah().getSurahNumber());
        String firstName = firstSurah == null ? "" : firstSurah.getDisplayName(arabicUi);
        String lastName = lastSurah == null ? "" : lastSurah.getDisplayName(arabicUi);
        return getString(R.string.quran_page_range, firstName, page.getFirstAyah().getAyahNumber(),
                lastName, page.getLastAyah().getAyahNumber());
    }

    // ══════════════════════════════ marks, theme, size ══════════════════════════════

    /** A tap saves or clears the verse it landed on; the page itself never moves. */
    private void toggleVerse(int ayahNumber) {
        int surahNumber = surahOfAyah(ayahNumber);
        if (surahNumber <= 0) {
            return;
        }
        boolean added = this.store.toggleBookmark(surahNumber, ayahNumber);
        if (this.adapter != null) {
            this.adapter.notifyDataSetChanged();
        }
        Toast.makeText(this, added ? R.string.quran_bookmark_added : R.string.quran_bookmark_removed,
                Toast.LENGTH_SHORT).show();
    }

    /** The surah an ayah number belongs to on the open page (an ayah number shows up once). */
    private int surahOfAyah(int ayahNumber) {
        if (this.repository == null) {
            return -1;
        }
        for (QuranAyah ayah : this.repository.getAyahsForPage(currentPage())) {
            if (ayah.getAyahNumber() == ayahNumber) {
                return ayah.getSurahNumber();
            }
        }
        return -1;
    }

    /**
     * Swaps the palette in place. Because the page builders read their colours from the shared
     * theme when they are bound, re-binding the visible pages is all a theme change needs.
     */
    private void toggleNightMode() {
        boolean night = !this.theme.isNight();
        this.theme.apply(this, night);
        this.store.saveNightMode(new TinyDB(this), night);
        applyTheme();
        if (this.adapter != null) {
            this.adapter.notifyDataSetChanged();
        }
        Toast.makeText(this, night ? R.string.quran_night_enabled : R.string.quran_day_enabled,
                Toast.LENGTH_SHORT).show();
    }

    private void applyTheme() {
        if (this.theme == null) {
            return;
        }
        this.root.setBackgroundColor(this.theme.paper);
        getWindow().setBackgroundDrawableResource(this.theme.isNight()
                ? R.color.quranNightPaper : R.color.quranPaper);
        this.paint.setColor(this.theme.ink);

        GradientDrawable card = new GradientDrawable();
        card.setShape(GradientDrawable.RECTANGLE);
        card.setColor(this.theme.surface);
        card.setCornerRadius(UiCompat.dp(this, 18f));
        card.setStroke(UiCompat.dp(this, 1f), this.theme.line);
        findViewById(R.id.quranMushafHeader).setBackground(card);

        GradientDrawable chip = new GradientDrawable();
        chip.setShape(GradientDrawable.RECTANGLE);
        chip.setColor(this.theme.softGreen);
        chip.setCornerRadius(UiCompat.dp(this, 12f));
        this.pageLabel.setBackground(chip);
        this.juzLabel.setBackground(chip);

        GradientDrawable button = new GradientDrawable();
        button.setShape(GradientDrawable.RECTANGLE);
        button.setColor(this.theme.softGreen);
        button.setCornerRadius(UiCompat.dp(this, 12f));
        button.setStroke(UiCompat.dp(this, 1f), this.theme.line);
        this.previous.setBackground(button);
        this.next.setBackground(button);

        tint(R.id.quranMushafBack, this.theme.ink);
        tint(R.id.quranMushafNight, this.theme.isNight() ? this.theme.gold : this.theme.green);
        tint(R.id.quranMushafTextSize, this.theme.green);
        tint(R.id.quranMushafBookmarks, this.theme.green);
        tint(R.id.quranMushafTitle, this.theme.ink);

        this.pageLabel.setTextColor(this.theme.greenDark);
        this.juzLabel.setTextColor(this.theme.greenDark);
        this.range.setTextColor(this.theme.muted);
        this.previous.setTextColor(this.theme.greenDark);
        this.next.setTextColor(this.theme.greenDark);
        ((TextView) findViewById(R.id.quranMushafSource)).setTextColor(this.theme.muted);
    }

    private void tint(int viewId, int colour) {
        View view = findViewById(viewId);
        if (!(view instanceof AppCompatImageButton)) {
            return;
        }
        AppCompatImageButton button = (AppCompatImageButton) view;
        android.graphics.drawable.Drawable icon = button.getDrawable();
        if (icon != null) {
            android.graphics.drawable.Drawable wrapped = DrawableCompat.wrap(icon.mutate());
            DrawableCompat.setTint(wrapped, colour);
            button.setImageDrawable(wrapped);
        }
    }

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
                    this.paint.setTextSize(spToPx(selected));
                    this.adapter.notifyDataSetChanged();
                    showPage(this.pendingPage > 0 ? this.pendingPage : currentPage());
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showJuzPicker() {
        if (this.repository == null) {
            return;
        }
        NumberPicker picker = new NumberPicker(this);
        picker.setMinValue(1);
        picker.setMaxValue(QuranRepository.JUZ_COUNT);
        int currentJuz = this.repository.getJuzForPage(currentPage());
        picker.setValue(currentJuz > 0 ? currentJuz : 1);
        picker.setWrapSelectorWheel(false);
        new AlertDialog.Builder(this)
                .setTitle(R.string.quran_choose_juz)
                .setView(picker)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    QuranJuz juz = repository.getJuz(picker.getValue());
                    if (juz != null) {
                        showPage(juz.getPageNumber());
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showPagePicker() {
        if (this.repository == null) {
            return;
        }
        NumberPicker picker = new NumberPicker(this);
        picker.setMinValue(1);
        picker.setMaxValue(QuranRepository.PAGE_COUNT);
        picker.setValue(Math.max(1, currentPage()));
        picker.setWrapSelectorWheel(false);
        new AlertDialog.Builder(this)
                .setTitle(R.string.quran_choose_page)
                .setView(picker)
                .setPositiveButton(R.string.ok, (dialog, which) -> showPage(picker.getValue()))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void saveVisiblePosition() {
        if (this.store == null || this.repository == null) {
            return;
        }
        QuranPage page = this.repository.getPage(this.pendingPage);
        if (page == null) {
            return;
        }
        QuranAyah first = page.getFirstAyah();
        this.store.saveLastReading(first.getSurahNumber(), first.getAyahNumber());
    }

    private float spToPx(int sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                getResources().getDisplayMetrics());
    }

    private void showLoadError() {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        this.loading.setVisibility(View.GONE);
        this.pager.setVisibility(View.GONE);
        this.error.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        loader.shutdownNow();
        super.onDestroy();
    }
}
