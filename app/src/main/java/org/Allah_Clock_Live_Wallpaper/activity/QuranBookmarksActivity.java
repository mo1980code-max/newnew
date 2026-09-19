package org.Allah_Clock_Live_Wallpaper.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.adapter.QuranBookmarkAdapter;
import org.Allah_Clock_Live_Wallpaper.model.QuranBookmark;
import org.Allah_Clock_Live_Wallpaper.utils.LocaleHelper;
import org.Allah_Clock_Live_Wallpaper.utils.QuranRepository;
import org.Allah_Clock_Live_Wallpaper.utils.QuranStore;
import org.Allah_Clock_Live_Wallpaper.utils.UiCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Lists marks restored from the local store and opens the saved ayah with one tap. */
public final class QuranBookmarksActivity extends AppCompatActivity {

    private final ExecutorService loader = Executors.newSingleThreadExecutor();

    private QuranRepository repository;
    private QuranStore store;
    private RecyclerView list;
    private View loading;
    private View empty;
    private TextView error;
    private QuranBookmarkAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quran_bookmarks);
        UiCompat.applyEdgeToEdge(this);
        getWindow().setBackgroundDrawableResource(R.color.quranPaper);

        this.store = new QuranStore(this);
        this.list = findViewById(R.id.quranBookmarksList);
        this.loading = findViewById(R.id.quranBookmarksLoading);
        this.empty = findViewById(R.id.quranBookmarksEmpty);
        this.error = findViewById(R.id.quranBookmarksError);
        this.list.setLayoutManager(new LinearLayoutManager(this));
        findViewById(R.id.quranBookmarksBack).setOnClickListener(view -> finish());
        loadRepository();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (repository != null) {
            renderBookmarks();
        }
    }

    private void loadRepository() {
        loader.execute(() -> {
            try {
                final QuranRepository loaded = QuranRepository.get(getApplicationContext());
                runOnUiThread(() -> {
                    try {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        repository = loaded;
                        loading.setVisibility(View.GONE);
                        error.setVisibility(View.GONE);
                        renderBookmarks();
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

    private void renderBookmarks() {
        try {
            if (repository == null) {
                return;
            }
            List<QuranBookmark> valid = new ArrayList<>();
            for (QuranBookmark bookmark : store.getBookmarks()) {
                try {
                    if (repository.getAyah(bookmark.getSurahNumber(), bookmark.getAyahNumber()) != null) {
                        valid.add(bookmark);
                    } else {
                        // Clean up only invalid legacy entries; valid marks preserve their saved order.
                        store.removeBookmark(bookmark.getSurahNumber(), bookmark.getAyahNumber());
                    }
                } catch (Throwable e) {
                    Log.e("QuranActivity", "Error loading data", e);
                }
            }
            this.adapter = new QuranBookmarkAdapter(valid, repository, LocaleHelper.isArabic(this),
                    new QuranBookmarkAdapter.Listener() {
                        @Override
                        public void onOpenBookmark(@NonNull QuranBookmark bookmark) {
                            try {
                                startActivity(QuranReaderActivity.createIntent(QuranBookmarksActivity.this,
                                        bookmark.getSurahNumber(), bookmark.getAyahNumber()));
                            } catch (Throwable e) {
                                Log.e("QuranActivity", "Error loading data", e);
                            }
                        }

                        @Override
                        public void onRemoveBookmark(@NonNull QuranBookmark bookmark, int position) {
                            try {
                                if (store.removeBookmark(bookmark.getSurahNumber(), bookmark.getAyahNumber())) {
                                    adapter.removeAt(position);
                                    Toast.makeText(QuranBookmarksActivity.this,
                                            R.string.quran_bookmark_removed, Toast.LENGTH_SHORT).show();
                                    updateEmptyState();
                                }
                            } catch (Throwable e) {
                                Log.e("QuranActivity", "Error loading data", e);
                            }
                        }
                    });
            this.list.setAdapter(this.adapter);
            updateEmptyState();
        } catch (Throwable e) {
            Log.e("QuranActivity", "Error loading data", e);
            showLoadError();
        }
    }

    private void updateEmptyState() {
        try {
            boolean isEmpty = adapter == null || adapter.getItemCount() == 0;
            empty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            list.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        } catch (Throwable e) {
            Log.e("QuranActivity", "Error loading data", e);
        }
    }

    private void showLoadError() {
        try {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            loading.setVisibility(View.GONE);
            list.setVisibility(View.GONE);
            empty.setVisibility(View.GONE);
            error.setVisibility(View.VISIBLE);
        } catch (Throwable e) {
            Log.e("QuranActivity", "Error loading data", e);
        }
    }

    @Override
    protected void onDestroy() {
        loader.shutdownNow();
        super.onDestroy();
    }
}
