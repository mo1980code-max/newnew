package org.Allah_Clock_Live_Wallpaper.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
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
import org.Allah_Clock_Live_Wallpaper.adapter.QuranScreenAdapter;
import org.Allah_Clock_Live_Wallpaper.model.QuranAyah;
import org.Allah_Clock_Live_Wallpaper.model.QuranBookmark;
import org.Allah_Clock_Live_Wallpaper.model.QuranSurah;
import org.Allah_Clock_Live_Wallpaper.utils.LocaleHelper;
import org.Allah_Clock_Live_Wallpaper.utils.QuranPageBuilder;
import org.Allah_Clock_Live_Wallpaper.utils.QuranPageMetrics;
import org.Allah_Clock_Live_Wallpaper.utils.QuranPaginator;
import org.Allah_Clock_Live_Wallpaper.utils.QuranRepository;
import org.Allah_Clock_Live_Wallpaper.utils.QuranStore;
import org.Allah_Clock_Live_Wallpaper.utils.QuranTheme;
import org.Allah_Clock_Live_Wallpaper.utils.TinyDB;
import org.Allah_Clock_Live_Wallpaper.utils.UiCompat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The Quran reader: one surah, printed as a Mushaf page and <b>flipped</b> sideways.
 *
 * <p>Verses run on as a single justified block, each closed by the numbered end-of-ayah
 * ornament, and {@link QuranPaginator} cuts that block into screen-sized pages. The
 * {@code ViewPager2} then turns those pages horizontally — the same motion as turning a printed
 * page, in either direction — instead of scrolling one endless column. Tapping a verse saves or
 * clears its bookmark, and a saved verse stays tinted while it is saved.</p>
 *
 * <p>Night reading is a switch inside the reader, not the system dark mode: it swaps the palette
 * held by {@link QuranTheme} and repaints. Because the ornaments, the sheet and the text all read
 * their colours from that one shared object, the page turns dark in place — no rebuild, no
 * pagination jump, no lost position.</p>
 *
 * <p>Reading state (bookmarks, last position, text size, night mode) stays in
 * {@code SharedPreferences} through {@link QuranStore} and {@link TinyDB}.</p>
 */
public final class QuranReaderActivity extends AppCompatActivity {

    private static final String EXTRA_SURAH = "quran_reader_surah";
    private static final String EXTRA_AYAH = "quran_reader_ayah";

    /** How long to wait for the pager to be measured, and how many times to ask again. */
    private static final long MEASURE_RETRY_MS = 60L;
    private static final int MEASURE_RETRIES = 20;

    private final ExecutorService loader = Executors.newSingleThreadExecutor();

    private QuranRepository repository;
    private QuranStore store;

    private View root;
    private ViewPager2 pager;
    private View loading;
    private TextView error;
    private TextView title;
    private TextView metadata;
    private TextView range;
    private Button previous;
    private Button next;

    /** The built surah and the pages it was cut into; both are replaced on re-pagination. */
    private QuranPageBuilder builder;
    private List<QuranPaginator.Screen> screens = Collections.emptyList();
    private QuranScreenAdapter adapter;
    private QuranTheme theme;
    private TextPaint paint;
    private float lineSpacingExtraPx;

    private int surahNumber;
    private int textSizeSp;
    private int targetAyah = 1;
    private int measureAttempts;

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
        this.lineSpacingExtraPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                QuranPageBuilder.LINE_SPACING_EXTRA_DP, getResources().getDisplayMetrics());

        this.root = findViewById(R.id.quranReaderRoot);
        this.pager = findViewById(R.id.quranReaderPager);
        this.loading = findViewById(R.id.quranReaderLoading);
        this.error = findViewById(R.id.quranReaderError);
        this.title = findViewById(R.id.quranReaderTitle);
        this.metadata = findViewById(R.id.quranReaderMeta);
        this.range = findViewById(R.id.quranReaderRange);
        this.previous = findViewById(R.id.quranReaderPrevious);
        this.next = findViewById(R.id.quranReaderNext);
        this.title.setText(R.string.title_quran);

        findViewById(R.id.quranReaderBack).setOnClickListener(view -> finish());
        ImageButton textSize = findViewById(R.id.quranReaderTextSize);
        textSize.setOnClickListener(view -> showTextSizePicker());
        ImageButton night = findViewById(R.id.quranReaderNight);
        night.setOnClickListener(view -> toggleNightMode());
        ImageButton mushaf = findViewById(R.id.quranReaderMushaf);
        mushaf.setOnClickListener(view -> openMushafAtVisiblePage());
        ImageButton bookmarks = findViewById(R.id.quranReaderBookmarks);
        bookmarks.setOnClickListener(view -> startActivity(new Intent(this,
                QuranBookmarksActivity.class)));

        this.previous.setOnClickListener(view -> turnPage(-1));
        this.next.setOnClickListener(view -> turnPage(1));

        // Horizontal, right-to-left page turning with a real page-curl-free slide: the reader
        // must feel like paper, so the pager keeps the default slide and stays RTL aware.
        this.pager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        this.pager.setOffscreenPageLimit(1);
        this.pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateFooter();
                saveVisiblePosition();
            }
        });

        applyTheme();
        loadRepository();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // A rotation only changes how much text fits, so re-cut the surah and stay put.
        this.pager.post(this::paginate);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshBookmarks();
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
        this.targetAyah = start.getAyahNumber();
        this.textSizeSp = this.store.getTextSizeSp();
        this.paint = this.theme.newTextPaint(spToPx(this.textSizeSp));

        boolean arabicUi = LocaleHelper.isArabic(this);
        String name = surah.getDisplayName(arabicUi);
        this.title.setText(getString(R.string.quran_surah_title, surah.getNumber(), name));
        String revelation = getString(surah.isMeccan() ? R.string.quran_meccan : R.string.quran_medinan);
        this.metadata.setText(getString(R.string.quran_surah_metadata, surah.getAyahCount(), revelation));

        buildPage(loaded, surah);
        applyTheme();

        this.loading.setVisibility(View.GONE);
        this.error.setVisibility(View.GONE);
        this.pager.setVisibility(View.VISIBLE);

        this.store.saveLastReading(start.getSurahNumber(), start.getAyahNumber());
        // The pages can only be cut once the pager has a real size, so measure it first.
        this.pager.post(this::paginate);
    }

    /**
     * Lays every verse of the surah into one run of text. The Basmalah opening is the exact
     * Uthmani text of 1:1 - never typed by hand - and it is added as a centred opening line for
     * every surah that carries it (all but Al-Fatiha, where it is verse 1, and At-Tawbah).
     */
    private void buildPage(@NonNull QuranRepository loaded, @NonNull QuranSurah surah) {
        this.builder = new QuranPageBuilder(this.theme);
        if (surah.getNumber() != 1 && surah.getNumber() != 9) {
            QuranAyah basmalah = loaded.getAyah(1, 1);
            if (basmalah != null) {
                this.builder.appendBasmalah(basmalah);
            }
        }
        for (QuranAyah ayah : loaded.getAyahs(surah.getNumber())) {
            this.builder.appendAyah(ayah);
            if (this.store.isBookmarked(surah.getNumber(), ayah.getAyahNumber())) {
                this.builder.highlight(ayah.getAyahNumber(), this.theme.verseHighlight);
            }
        }
    }

    // ═══════════════════════════════ horizontal paging ═══════════════════════════════

    /**
     * Cuts the surah into screen-sized pages and hands them to the pager. Called on the first
     * layout, after a text-size change and after a rotation; the verse the reader was on is
     * carried over, so a re-cut never loses their place.
     */
    private void paginate() {
        if (this.builder == null || this.repository == null || isFinishing() || isDestroyed()) {
            return;
        }
        int restoreAyah = this.targetAyah;
        int currentPosition = this.adapter == null ? -1 : this.pager.getCurrentItem();
        if (this.adapter != null && currentPosition >= 0 && currentPosition < this.screens.size()) {
            int visible = this.screens.get(currentPosition).lastAyah;
            if (visible > 0) {
                restoreAyah = visible;
            }
        }

        QuranPageMetrics metrics = QuranPageMetrics.measure(getLayoutInflater(), this.pager, this);
        if (metrics == null) {
            // The pager has no size yet (first frame, or a transition in progress): ask again on
            // the next frames, but never in an open-ended loop.
            if (this.measureAttempts++ < MEASURE_RETRIES) {
                this.pager.postDelayed(this::paginate, MEASURE_RETRY_MS);
            }
            return;
        }
        this.measureAttempts = 0;
        this.paint.setColor(this.theme.ink);
        this.paint.setTextSize(spToPx(this.textSizeSp));
        this.screens = QuranPaginator.slice(this.builder.getText(), this.builder.getVerses(),
                this.paint, metrics.textWidth, metrics.textHeight, this.lineSpacingExtraPx);
        this.adapter = new QuranScreenAdapter(this.builder.getText(), this.builder.getVerses(),
                this.screens, this.paint, this.theme, this.lineSpacingExtraPx, this::toggleVerse);
        this.pager.setAdapter(this.adapter);

        int index = pageOfAyah(restoreAyah);
        this.pager.setCurrentItem(index, false);
        updateFooter();
        this.targetAyah = restoreAyah;
    }

    /** The page that carries {@code ayahNumber}; falls back to the first page. */
    private int pageOfAyah(int ayahNumber) {
        for (int index = 0; index < this.screens.size(); index++) {
            QuranPaginator.Screen screen = this.screens.get(index);
            if (ayahNumber >= screen.firstAyah && ayahNumber <= screen.lastAyah) {
                return index;
            }
        }
        return 0;
    }

    private void turnPage(int delta) {
        if (this.adapter == null || this.adapter.getItemCount() == 0) {
            return;
        }
        int target = this.pager.getCurrentItem() + delta;
        target = Math.max(0, Math.min(target, this.adapter.getItemCount() - 1));
        if (target != this.pager.getCurrentItem()) {
            this.pager.setCurrentItem(target, true);
        }
    }

    /** States which verses the open page carries and whether there are pages on either side. */
    private void updateFooter() {
        if (this.screens.isEmpty()) {
            return;
        }
        int position = Math.max(0, Math.min(this.pager.getCurrentItem(), this.screens.size() - 1));
        QuranPaginator.Screen screen = this.screens.get(position);
        this.range.setText(getString(R.string.quran_screen_ayahs, screen.firstAyah, screen.lastAyah));
        boolean hasPrevious = position > 0;
        boolean hasNext = position < this.screens.size() - 1;
        this.previous.setEnabled(hasPrevious);
        this.previous.setAlpha(hasPrevious ? 1f : 0.42f);
        this.next.setEnabled(hasNext);
        this.next.setAlpha(hasNext ? 1f : 0.42f);
        this.pager.setUserInputEnabled(this.screens.size() > 1);
    }

    // ══════════════════════════════ night reading theme ══════════════════════════════

    /**
     * Swaps the palette in place. Nothing is rebuilt: the ornaments inside the page text read
     * their colours from the same {@link QuranTheme} instance, so they simply repaint, and the
     * page the reader is on stays the page they are on.
     */
    private void toggleNightMode() {
        boolean night = !this.theme.isNight();
        this.theme.apply(this, night);
        this.store.saveNightMode(new TinyDB(this), night);
        applyTheme();
        if (this.adapter != null) {
            this.adapter.refreshBookmarks();
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
        if (this.paint != null) {
            this.paint.setColor(this.theme.ink);
        }

        GradientDrawable card = new GradientDrawable();
        card.setShape(GradientDrawable.RECTANGLE);
        card.setColor(this.theme.surface);
        card.setCornerRadius(dp(18f));
        card.setStroke(dp(1f), this.theme.line);
        findViewById(R.id.quranReaderHeader).setBackground(card);

        GradientDrawable footer = new GradientDrawable();
        footer.setShape(GradientDrawable.RECTANGLE);
        footer.setColor(this.theme.surface);
        footer.setCornerRadius(dp(18f));
        footer.setStroke(dp(1f), this.theme.line);
        findViewById(R.id.quranReaderFooter).setBackground(footer);

        GradientDrawable chip = new GradientDrawable();
        chip.setShape(GradientDrawable.RECTANGLE);
        chip.setColor(this.theme.softGreen);
        chip.setCornerRadius(dp(12f));
        this.range.setBackground(chip);

        GradientDrawable button = new GradientDrawable();
        button.setShape(GradientDrawable.RECTANGLE);
        button.setColor(this.theme.softGreen);
        button.setCornerRadius(dp(12f));
        button.setStroke(dp(1f), this.theme.line);
        this.previous.setBackground(button);
        this.next.setBackground(button);

        tint(R.id.quranReaderBack, this.theme.ink);
        tint(R.id.quranReaderNight, this.theme.isNight() ? this.theme.gold : this.theme.green);
        tint(R.id.quranReaderTextSize, this.theme.green);
        tint(R.id.quranReaderBookmarks, this.theme.green);
        tint(R.id.quranReaderMushaf, this.theme.gold);

        this.title.setTextColor(this.theme.ink);
        this.metadata.setTextColor(this.theme.muted);
        this.range.setTextColor(this.theme.greenDark);
        this.previous.setTextColor(this.theme.greenDark);
        this.next.setTextColor(this.theme.greenDark);
        ((TextView) findViewById(R.id.quranReaderHint)).setTextColor(this.theme.body);
    }

    private void tint(int viewId, int colour) {
        View view = findViewById(viewId);
        if (!(view instanceof AppCompatImageButton)) {
            return;
        }
        AppCompatImageButton button = (AppCompatImageButton) view;
        Drawable icon = button.getDrawable();
        if (icon != null) {
            Drawable wrapped = DrawableCompat.wrap(icon.mutate());
            DrawableCompat.setTint(wrapped, colour);
            button.setImageDrawable(wrapped);
        }
    }

    private int dp(float value) {
        return UiCompat.dp(this, value);
    }

    private float spToPx(int sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                getResources().getDisplayMetrics());
    }

    // ═════════════════════════════ bookmarks and position ═════════════════════════════

    /** A tap toggles the save of one verse; it never moves the reading position by itself. */
    private void toggleVerse(int ayahNumber) {
        if (this.surahNumber <= 0 || this.builder == null) {
            return;
        }
        boolean added = this.store.toggleBookmark(this.surahNumber, ayahNumber);
        paintBookmark(ayahNumber, added);
        Toast.makeText(this, added ? R.string.quran_bookmark_added : R.string.quran_bookmark_removed,
                Toast.LENGTH_SHORT).show();
    }

    /** Adds or removes the tint of one verse without touching anything else on the page. */
    private void paintBookmark(int ayahNumber, boolean saved) {
        if (this.builder == null) {
            return;
        }
        QuranPageBuilder.Verse verse = this.builder.verse(ayahNumber);
        if (saved) {
            this.builder.highlight(ayahNumber, this.theme.verseHighlight);
        } else if (verse != null) {
            this.builder.clearHighlight(ayahNumber);
        }
        if (this.adapter != null) {
            this.adapter.refreshBookmarks();
        }
    }

    /** Repaints the whole surah after returning from the saved-marks screen. */
    private void refreshBookmarks() {
        if (this.builder == null || this.surahNumber <= 0) {
            return;
        }
        this.builder.clearHighlights();
        for (QuranPageBuilder.Verse verse : this.builder.getVerses()) {
            if (this.store.isBookmarked(this.surahNumber, verse.ayahNumber)) {
                this.builder.highlight(verse.ayahNumber, this.theme.verseHighlight);
            }
        }
        if (this.adapter != null) {
            this.adapter.refreshBookmarks();
        }
    }

    @NonNull
    private QuranBookmark resolveStartPosition() {
        int requestedSurah = getIntent().getIntExtra(EXTRA_SURAH, -1);
        int requestedAyah = getIntent().getIntExtra(EXTRA_AYAH, -1);
        if (this.repository != null && this.repository.getAyah(requestedSurah, requestedAyah) != null) {
            return new QuranBookmark(requestedSurah, requestedAyah);
        }
        QuranBookmark last = this.store.getLastReading();
        if (last != null && this.repository != null
                && this.repository.getAyah(last.getSurahNumber(), last.getAyahNumber()) != null) {
            return last;
        }
        return new QuranBookmark(1, 1);
    }

    /** Writes the verse shown on the open page, so reopening the reader resumes there. */
    private void saveVisiblePosition() {
        if (this.store == null || this.surahNumber <= 0 || this.screens.isEmpty()) {
            return;
        }
        int position = this.pager.getCurrentItem();
        if (position < 0 || position >= this.screens.size()) {
            return;
        }
        int ayah = this.screens.get(position).lastAyah;
        if (ayah > 0) {
            this.store.saveLastReading(this.surahNumber, ayah);
            this.targetAyah = ayah;
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
                    // A bigger type needs different page breaks: re-cut and keep the place.
                    this.pager.post(this::paginate);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void openMushafAtVisiblePage() {
        int page = -1;
        QuranBookmark reading = this.store == null ? null : this.store.getLastReading();
        if (reading != null && this.repository != null) {
            page = this.repository.getPageForAyah(reading.getSurahNumber(),
                    reading.getAyahNumber());
        }
        startActivity(QuranMushafActivity.createIntent(this, page));
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
