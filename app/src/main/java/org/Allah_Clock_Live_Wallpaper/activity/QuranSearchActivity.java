package org.Allah_Clock_Live_Wallpaper.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.adapter.QuranSearchAdapter;
import org.Allah_Clock_Live_Wallpaper.model.QuranSearchResult;
import org.Allah_Clock_Live_Wallpaper.model.QuranSurah;
import org.Allah_Clock_Live_Wallpaper.utils.LocaleHelper;
import org.Allah_Clock_Live_Wallpaper.utils.QuranRepository;
import org.Allah_Clock_Live_Wallpaper.utils.UiCompat;

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Offline full-text search over the bundled Quran text and the 114-surah index. */
public final class QuranSearchActivity extends AppCompatActivity {

    private static final int MAX_VISIBLE_RESULTS = 100;
    private static final long SEARCH_DEBOUNCE_MS = 180L;

    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private QuranRepository repository;
    private EditText input;
    private TextView status;
    private View loading;
    private TextView empty;
    private QuranSearchAdapter adapter;
    private Runnable pendingSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quran_search);
        UiCompat.applyEdgeToEdge(this);
        getWindow().setBackgroundDrawableResource(R.color.quranPaper);

        this.input = findViewById(R.id.quranSearchInput);
        this.status = findViewById(R.id.quranSearchStatus);
        this.loading = findViewById(R.id.quranSearchLoading);
        this.empty = findViewById(R.id.quranSearchEmpty);
        RecyclerView list = findViewById(R.id.quranSearchResults);
        list.setLayoutManager(new LinearLayoutManager(this));
        this.adapter = new QuranSearchAdapter(LocaleHelper.isArabic(this), this::openResult);
        list.setAdapter(this.adapter);

        findViewById(R.id.quranSearchBack).setOnClickListener(view -> finish());
        this.input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
                // No-op.
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                // No-op; wait until the editable has the final character.
            }

            @Override
            public void afterTextChanged(Editable editable) {
                queueSearch(SEARCH_DEBOUNCE_MS);
            }
        });
        this.input.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                queueSearch(0L);
                return true;
            }
            return false;
        });
        loadRepository();
    }

    private void loadRepository() {
        worker.execute(() -> {
            try {
                final QuranRepository loaded = QuranRepository.get(getApplicationContext());
                runOnUiThread(() -> {
                    try {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        repository = loaded;
                        loading.setVisibility(View.GONE);
                        queueSearch(0L);
                    } catch (Throwable e) {
                        Log.e("QuranActivity", "Error loading data", e);
                        showLoadError();
                    }
                });
            } catch (Throwable e) {
                Log.e("QuranActivity", "Error loading data", e);
                runOnUiThread(this::showLoadError);
            }
        });
    }

    private void queueSearch(long delayMs) {
        if (pendingSearch != null) {
            handler.removeCallbacks(pendingSearch);
        }
        final String query = input == null ? "" : input.getText().toString();
        pendingSearch = () -> search(query);
        handler.postDelayed(pendingSearch, delayMs);
    }

    private void search(@NonNull String query) {
        if (repository == null) {
            return;
        }
        try {
            if (QuranRepository.normalizeForSearch(query).isEmpty()) {
                adapter.replace(Collections.emptyList());
                status.setText(R.string.quran_search_prompt);
                empty.setVisibility(View.GONE);
                return;
            }
        } catch (Throwable e) {
            Log.e("QuranActivity", "Error loading data", e);
            return;
        }
        worker.execute(() -> {
            try {
                QuranRepository.SearchResults results = repository.search(query, MAX_VISIBLE_RESULTS);
                runOnUiThread(() -> showResults(query, results));
            } catch (Throwable e) {
                Log.e("QuranActivity", "Error loading data", e);
                runOnUiThread(() -> {
                    try {
                        status.setText(R.string.quran_load_failed);
                    } catch (Throwable ignored) {}
                });
            }
        });
    }

    private void showResults(@NonNull String query, @NonNull QuranRepository.SearchResults results) {
        try {
            if (isFinishing() || isDestroyed() || !query.equals(input.getText().toString())) {
                return;
            }
            adapter.replace(results.getItems());
            if (results.getTotalMatches() == 0) {
                status.setText(R.string.quran_search_no_results);
                empty.setVisibility(View.VISIBLE);
                return;
            }
            empty.setVisibility(View.GONE);
            if (results.isTruncated()) {
                status.setText(getString(R.string.quran_search_results_limited,
                        results.getItems().size(), results.getTotalMatches()));
            } else {
                status.setText(getString(R.string.quran_search_results, results.getTotalMatches()));
            }
        } catch (Throwable e) {
            Log.e("QuranActivity", "Error loading data", e);
        }
    }

    private void openResult(@NonNull QuranSurah surah, int ayahNumber) {
        startActivity(QuranReaderActivity.createIntent(this, surah.getNumber(), ayahNumber));
    }

    private void showLoadError() {
        try {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            loading.setVisibility(View.GONE);
            empty.setText(R.string.quran_load_failed);
            empty.setVisibility(View.VISIBLE);
            status.setText(R.string.quran_load_failed);
        } catch (Throwable e) {
            Log.e("QuranActivity", "Error loading data", e);
        }
    }

    @Override
    protected void onDestroy() {
        if (pendingSearch != null) {
            handler.removeCallbacks(pendingSearch);
        }
        worker.shutdownNow();
        super.onDestroy();
    }
}
