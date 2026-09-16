package org.Allah_Clock_Live_Wallpaper.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.adapter.QuranSurahAdapter;
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

/** The offline Quran index and the entry point for search, saved marks and resumed reading. */
public final class QuranIndexActivity extends AppCompatActivity {

    private final ExecutorService loader = Executors.newSingleThreadExecutor();

    private QuranRepository repository;
    private QuranStore store;
    private RecyclerView surahList;
    private View loading;
    private TextView error;
    private LinearLayout continueCard;
    private TextView continuePosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quran_index);
        UiCompat.applyEdgeToEdge(this);
        getWindow().setBackgroundDrawableResource(R.color.quranPaper);

        this.store = new QuranStore(this);
        this.surahList = findViewById(R.id.quranSurahList);
        this.loading = findViewById(R.id.quranIndexLoading);
        this.error = findViewById(R.id.quranIndexError);
        this.continueCard = findViewById(R.id.quranContinueCard);
        this.continuePosition = findViewById(R.id.quranContinuePosition);

        findViewById(R.id.quranIndexBack).setOnClickListener(view -> finish());
        ImageButton search = findViewById(R.id.quranIndexSearch);
        search.setOnClickListener(view -> startActivity(new Intent(this, QuranSearchActivity.class)));
        ImageButton bookmarks = findViewById(R.id.quranIndexBookmarks);
        bookmarks.setOnClickListener(view -> startActivity(new Intent(this,
                QuranBookmarksActivity.class)));
        findViewById(R.id.quranIndexSource).setOnClickListener(view -> showTextSource());
        findViewById(R.id.quranMushafCard).setOnClickListener(view -> openMushaf());

        this.surahList.setLayoutManager(new LinearLayoutManager(this));
        loadRepository();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateContinueCard();
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
        this.loading.setVisibility(View.GONE);
        this.error.setVisibility(View.GONE);
        this.surahList.setVisibility(View.VISIBLE);
        this.surahList.setAdapter(new QuranSurahAdapter(loaded.getSurahs(),
                LocaleHelper.isArabic(this), this::openSurah));
        updateContinueCard();
    }

    private void showLoadError() {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        this.loading.setVisibility(View.GONE);
        this.surahList.setVisibility(View.GONE);
        this.continueCard.setVisibility(View.GONE);
        this.error.setVisibility(View.VISIBLE);
    }

    private void updateContinueCard() {
        if (this.repository == null || this.store == null) {
            return;
        }
        QuranBookmark last = this.store.getLastReading();
        if (last == null) {
            this.continueCard.setVisibility(View.GONE);
            return;
        }
        QuranSurah surah = this.repository.getSurah(last.getSurahNumber());
        QuranAyah ayah = this.repository.getAyah(last.getSurahNumber(), last.getAyahNumber());
        if (surah == null || ayah == null) {
            this.continueCard.setVisibility(View.GONE);
            return;
        }
        String name = surah.getDisplayName(LocaleHelper.isArabic(this));
        int page = this.repository.getPageForAyah(last.getSurahNumber(), last.getAyahNumber());
        String position = page > 0
                ? getString(R.string.quran_continue_position_page, name, ayah.getAyahNumber(), page)
                : getString(R.string.quran_continue_position, name, ayah.getAyahNumber());
        this.continuePosition.setText(position);
        this.continueCard.setContentDescription(position);
        this.continueCard.setOnClickListener(view -> openReader(last.getSurahNumber(),
                last.getAyahNumber()));
        this.continueCard.setVisibility(View.VISIBLE);
    }

    private void openSurah(@NonNull QuranSurah surah) {
        openReader(surah.getNumber(), 1);
    }

    private void openReader(int surahNumber, int ayahNumber) {
        startActivity(QuranReaderActivity.createIntent(this, surahNumber, ayahNumber));
    }

    private void openMushaf() {
        int page = -1;
        QuranBookmark last = this.store == null ? null : this.store.getLastReading();
        if (last != null && this.repository != null) {
            int savedPage = this.repository.getPageForAyah(last.getSurahNumber(),
                    last.getAyahNumber());
            if (savedPage > 0) {
                page = savedPage;
            }
        }
        startActivity(QuranMushafActivity.createIntent(this, page));
    }

    private void showTextSource() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.quran_text_source_title)
                .setMessage(R.string.quran_text_source_message)
                .setNeutralButton(R.string.quran_text_source_link, (dialog, which) -> {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://tanzil.net")));
                    } catch (ActivityNotFoundException ignored) {
                        // The attribution remains visible even on a device with no browser.
                    }
                })
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    @Override
    protected void onDestroy() {
        loader.shutdownNow();
        super.onDestroy();
    }
}
