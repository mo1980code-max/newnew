package org.Allah_Clock_Live_Wallpaper.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.adapter.QuranAyahAdapter;
import org.Allah_Clock_Live_Wallpaper.model.QuranAyah;
import org.Allah_Clock_Live_Wallpaper.model.QuranBookmark;
import org.Allah_Clock_Live_Wallpaper.model.QuranSurah;
import org.Allah_Clock_Live_Wallpaper.utils.LocaleHelper;
import org.Allah_Clock_Live_Wallpaper.utils.QuranRepository;
import org.Allah_Clock_Live_Wallpaper.utils.QuranStore;
import org.Allah_Clock_Live_Wallpaper.utils.UiCompat;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Distraction-free Quran reader. The first visible ayah is saved locally so reopening the
 * Quran resumes exactly where the reader left off; individual ayahs have separate marks.
 */
public final class QuranReaderActivity extends AppCompatActivity {

    private static final String EXTRA_SURAH = "quran_reader_surah";
    private static final String EXTRA_AYAH = "quran_reader_ayah";

    private final ExecutorService loader = Executors.newSingleThreadExecutor();

    private QuranRepository repository;
    private QuranStore store;
    private RecyclerView ayahList;
    private LinearLayoutManager layoutManager;
    private QuranAyahAdapter adapter;
    private View loading;
    private TextView error;
    private TextView title;
    private TextView metadata;

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
        getWindow().setBackgroundDrawableResource(R.color.quranPaper);

        this.store = new QuranStore(this);
        this.ayahList = findViewById(R.id.quranAyahList);
        this.loading = findViewById(R.id.quranReaderLoading);
        this.error = findViewById(R.id.quranReaderError);
        this.title = findViewById(R.id.quranReaderTitle);
        this.metadata = findViewById(R.id.quranReaderMeta);
        this.title.setText(R.string.title_quran);

        findViewById(R.id.quranReaderBack).setOnClickListener(view -> finish());
        ImageButton textSize = findViewById(R.id.quranReaderTextSize);
        textSize.setOnClickListener(view -> showTextSizePicker());
        ImageButton mushaf = findViewById(R.id.quranReaderMushaf);
        mushaf.setOnClickListener(view -> openMushafAtVisiblePage());
        ImageButton bookmarks = findViewById(R.id.quranReaderBookmarks);
        bookmarks.setOnClickListener(view -> startActivity(new Intent(this,
                QuranBookmarksActivity.class)));

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

        boolean arabicUi = LocaleHelper.isArabic(this);
        String name = surah.getDisplayName(arabicUi);
        this.title.setText(getString(R.string.quran_surah_title, surah.getNumber(), name));
        String revelation = getString(surah.isMeccan() ? R.string.quran_meccan : R.string.quran_medinan);
        this.metadata.setText(getString(R.string.quran_surah_metadata, surah.getAyahCount(), revelation));

        this.adapter = new QuranAyahAdapter(loaded.getAyahs(surah.getNumber()), this.store,
                new QuranAyahAdapter.Listener() {
                    @Override
                    public void onReadingPosition(@NonNull QuranAyah ayah) {
                        store.saveLastReading(ayah.getSurahNumber(), ayah.getAyahNumber());
                    }

                    @Override
                    public void onBookmarkChanged(@NonNull QuranAyah ayah, boolean added) {
                        Toast.makeText(QuranReaderActivity.this, added
                                ? R.string.quran_bookmark_added : R.string.quran_bookmark_removed,
                                Toast.LENGTH_SHORT).show();
                    }
                }, this.store.getTextSizeSp());
        this.ayahList.setAdapter(this.adapter);
        this.loading.setVisibility(View.GONE);
        this.error.setVisibility(View.GONE);
        this.ayahList.setVisibility(View.VISIBLE);

        this.store.saveLastReading(start.getSurahNumber(), start.getAyahNumber());
        final int targetIndex = Math.max(0, start.getAyahNumber() - 1);
        this.ayahList.post(() -> {
            if (!isFinishing() && this.layoutManager != null) {
                this.layoutManager.scrollToPositionWithOffset(targetIndex, UiCompat.dp(this, 4));
            }
        });
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

    private void openMushafAtVisiblePage() {
        saveVisiblePosition();
        int page = -1;
        QuranBookmark reading = this.store == null ? null : this.store.getLastReading();
        if (reading != null && this.repository != null) {
            page = this.repository.getPageForAyah(reading.getSurahNumber(),
                    reading.getAyahNumber());
        }
        startActivity(QuranMushafActivity.createIntent(this, page));
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
