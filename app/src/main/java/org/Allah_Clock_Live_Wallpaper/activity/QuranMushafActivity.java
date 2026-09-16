package org.Allah_Clock_Live_Wallpaper.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.adapter.QuranAyahAdapter;
import org.Allah_Clock_Live_Wallpaper.model.QuranAyah;
import org.Allah_Clock_Live_Wallpaper.model.QuranBookmark;
import org.Allah_Clock_Live_Wallpaper.model.QuranJuz;
import org.Allah_Clock_Live_Wallpaper.model.QuranPage;
import org.Allah_Clock_Live_Wallpaper.model.QuranSurah;
import org.Allah_Clock_Live_Wallpaper.utils.LocaleHelper;
import org.Allah_Clock_Live_Wallpaper.utils.QuranRepository;
import org.Allah_Clock_Live_Wallpaper.utils.QuranStore;
import org.Allah_Clock_Live_Wallpaper.utils.UiCompat;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Page-by-page Quran reading mode using the 604 canonical Madani-page boundaries.
 *
 * <p>Text is intentionally reflowed for accessibility and screen size instead of pretending
 * to reproduce a licensed printed-glyph layout. The page boundaries remain canonical, and every
 * verse keeps the same saved-mark and resumed-reading behavior as the surah reader.</p>
 */
public final class QuranMushafActivity extends AppCompatActivity {

    private static final String EXTRA_PAGE = "quran_mushaf_page";

    private final ExecutorService loader = Executors.newSingleThreadExecutor();

    private QuranRepository repository;
    private QuranStore store;
    private RecyclerView ayahList;
    private LinearLayoutManager layoutManager;
    private QuranAyahAdapter adapter;
    private View loading;
    private TextView error;
    private TextView pageLabel;
    private TextView range;
    private TextView juzLabel;
    private Button previous;
    private Button next;
    private int currentPage;

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
        getWindow().setBackgroundDrawableResource(R.color.quranPaper);

        this.store = new QuranStore(this);
        this.ayahList = findViewById(R.id.quranMushafAyahList);
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
        ImageButton bookmarks = findViewById(R.id.quranMushafBookmarks);
        bookmarks.setOnClickListener(view -> startActivity(new Intent(this,
                QuranBookmarksActivity.class)));
        this.previous.setOnClickListener(view -> showPage(this.currentPage - 1));
        this.next.setOnClickListener(view -> showPage(this.currentPage + 1));
        this.pageLabel.setOnClickListener(view -> showPagePicker());
        this.juzLabel.setOnClickListener(view -> showJuzPicker());

        this.layoutManager = new LinearLayoutManager(this);
        this.ayahList.setLayoutManager(this.layoutManager);
        this.ayahList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    saveVisiblePosition();
                }
            }
        });
        loadRepository();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.adapter != null) {
            this.adapter.refreshBookmarks();
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
            QuranBookmark last = this.store.getLastReading();
            if (last != null) {
                requested = loaded.getPageForAyah(last.getSurahNumber(), last.getAyahNumber());
            }
        }
        showPage(requested > 0 ? requested : 1);
    }

    private void showPage(int pageNumber) {
        showPage(pageNumber, null);
    }

    /** Opens a canonical page and optionally aligns a direct juz jump to its first ayah. */
    private void showPage(int pageNumber, @Nullable QuranAyah focusedAyah) {
        if (this.repository == null || this.repository.getPage(pageNumber) == null) {
            return;
        }
        saveVisiblePosition();
        QuranPage page = this.repository.getPage(pageNumber);
        if (page == null) {
            return;
        }
        this.currentPage = pageNumber;
        List<QuranAyah> pageAyahs = this.repository.getAyahsForPage(pageNumber);
        int focusedPosition = 0;
        if (focusedAyah != null) {
            for (int index = 0; index < pageAyahs.size(); index++) {
                if (pageAyahs.get(index).getKey().equals(focusedAyah.getKey())) {
                    focusedPosition = index;
                    break;
                }
            }
        }
        this.adapter = new QuranAyahAdapter(pageAyahs, this.store,
                new QuranAyahAdapter.Listener() {
                    @Override
                    public void onReadingPosition(@NonNull QuranAyah ayah) {
                        store.saveLastReading(ayah.getSurahNumber(), ayah.getAyahNumber());
                    }

                    @Override
                    public void onBookmarkChanged(@NonNull QuranAyah ayah, boolean added) {
                        Toast.makeText(QuranMushafActivity.this, added
                                ? R.string.quran_bookmark_added : R.string.quran_bookmark_removed,
                                Toast.LENGTH_SHORT).show();
                    }
                }, this.store.getTextSizeSp());
        this.ayahList.setAdapter(this.adapter);
        this.ayahList.scrollToPosition(focusedPosition);
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
        this.previous.setEnabled(pageNumber > 1);
        this.previous.setAlpha(pageNumber > 1 ? 1f : 0.42f);
        this.next.setEnabled(pageNumber < QuranRepository.PAGE_COUNT);
        this.next.setAlpha(pageNumber < QuranRepository.PAGE_COUNT ? 1f : 0.42f);
        this.loading.setVisibility(View.GONE);
        this.error.setVisibility(View.GONE);
        this.ayahList.setVisibility(View.VISIBLE);

        QuranAyah start = pageAyahs.get(focusedPosition);
        this.store.saveLastReading(start.getSurahNumber(), start.getAyahNumber());
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
                    store.saveTextSizeSp(selected);
                    adapter.setTextSizeSp(selected);
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
        int currentJuz = this.repository.getJuzForPage(this.currentPage);
        picker.setValue(currentJuz > 0 ? currentJuz : 1);
        picker.setWrapSelectorWheel(false);
        new AlertDialog.Builder(this)
                .setTitle(R.string.quran_choose_juz)
                .setView(picker)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    QuranJuz juz = repository.getJuz(picker.getValue());
                    if (juz != null) {
                        showPage(juz.getPageNumber(), juz.getFirstAyah());
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
        picker.setValue(Math.max(1, this.currentPage));
        picker.setWrapSelectorWheel(false);
        new AlertDialog.Builder(this)
                .setTitle(R.string.quran_choose_page)
                .setView(picker)
                .setPositiveButton(R.string.ok, (dialog, which) -> showPage(picker.getValue()))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void saveVisiblePosition() {
        if (this.layoutManager == null || this.adapter == null || this.store == null) {
            return;
        }
        int position = this.layoutManager.findFirstVisibleItemPosition();
        if (position >= 0 && position < this.adapter.getItemCount()) {
            QuranAyah ayah = this.adapter.getItem(position);
            this.store.saveLastReading(ayah.getSurahNumber(), ayah.getAyahNumber());
        }
    }

    private void showLoadError() {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        this.loading.setVisibility(View.GONE);
        this.ayahList.setVisibility(View.GONE);
        this.error.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        loader.shutdownNow();
        super.onDestroy();
    }
}
