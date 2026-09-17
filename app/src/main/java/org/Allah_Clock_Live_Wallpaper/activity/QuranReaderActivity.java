package org.Allah_Clock_Live_Wallpaper.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.model.QuranAyah;
import org.Allah_Clock_Live_Wallpaper.model.QuranBookmark;
import org.Allah_Clock_Live_Wallpaper.model.QuranSurah;
import org.Allah_Clock_Live_Wallpaper.utils.LocaleHelper;
import org.Allah_Clock_Live_Wallpaper.utils.QuranRepository;
import org.Allah_Clock_Live_Wallpaper.utils.QuranStore;
import org.Allah_Clock_Live_Wallpaper.utils.UiCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Distraction-free Quran reader, laid out the way a Mushaf page lays text out.
 *
 * <p>One continuous run of verses instead of a bordered card per ayah: each verse is followed by
 * the end-of-ayah glyph U+06DD carrying its own number in Arabic-Indic digits, so the text flows
 * as a single justified page and the eye moves the way it does in print. Tapping a verse saves
 * it or clears its save, and a saved verse stays tinted while it is saved.</p>
 *
 * <p>The first visible verse is remembered locally, so reopening the Quran resumes where the
 * reader left off. All reading state stays in {@code SharedPreferences}.</p>
 */
public final class QuranReaderActivity extends AppCompatActivity {

    private static final String EXTRA_SURAH = "quran_reader_surah";
    private static final String EXTRA_AYAH = "quran_reader_ayah";

    /** U+06DD ARABIC END OF AYAH; the digits that follow it are drawn inside its enclosure. */
    private static final char END_OF_AYAH = '\u06DD';

    /** How long the reader must rest before the visible verse is written to the store. */
    private static final long POSITION_SAVE_DELAY_MS = 400L;

    private final ExecutorService loader = Executors.newSingleThreadExecutor();
    private final Handler positionSaver = new Handler(Looper.getMainLooper());
    private final Runnable savePositionTask = this::saveVisiblePosition;

    /** Offsets of one verse inside the page, in layout coordinates. */
    private static final class VerseRange {
        final int start;
        final int end;
        final int markerEnd;
        final int ayahNumber;

        VerseRange(int start, int end, int markerEnd, int ayahNumber) {
            this.start = start;
            this.end = end;
            this.markerEnd = markerEnd;
            this.ayahNumber = ayahNumber;
        }
    }

    private QuranRepository repository;
    private QuranStore store;
    private ScrollView scroll;
    private TextView page;
    private View loading;
    private TextView error;
    private TextView title;
    private TextView metadata;

    /** The page currently on screen, and where each verse sits inside it. */
    private SpannableStringBuilder pageText;
    private final List<VerseRange> verses = new ArrayList<>();
    private final Map<Integer, BackgroundColorSpan> highlights = new HashMap<>();

    private int surahNumber;
    private int textSizeSp;
    private float touchDownX;
    private float touchDownY;

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
        this.scroll = findViewById(R.id.quranReaderScroll);
        this.page = findViewById(R.id.quranReaderPage);
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

        this.page.setOnTouchListener(this::rememberTouchDown);
        this.page.setOnClickListener(view -> toggleVerseUnder(touchDownX, touchDownY));
        this.scroll.setOnScrollChangeListener(
                (view, scrollX, scrollY, oldScrollX, oldScrollY) -> schedulePositionSave());

        loadRepository();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshBookmarks();
    }

    @Override
    protected void onPause() {
        this.positionSaver.removeCallbacks(this.savePositionTask);
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
        this.textSizeSp = this.store.getTextSizeSp();

        boolean arabicUi = LocaleHelper.isArabic(this);
        String name = surah.getDisplayName(arabicUi);
        this.title.setText(getString(R.string.quran_surah_title, surah.getNumber(), name));
        String revelation = getString(surah.isMeccan() ? R.string.quran_meccan : R.string.quran_medinan);
        this.metadata.setText(getString(R.string.quran_surah_metadata, surah.getAyahCount(), revelation));

        buildPage(loaded, surah);
        this.page.setText(this.pageText);
        this.page.setTextSize(TypedValue.COMPLEX_UNIT_SP, this.textSizeSp);

        this.loading.setVisibility(View.GONE);
        this.error.setVisibility(View.GONE);
        this.scroll.setVisibility(View.VISIBLE);

        this.store.saveLastReading(start.getSurahNumber(), start.getAyahNumber());
        final int targetAyah = start.getAyahNumber();
        this.page.post(() -> scrollToAyah(targetAyah));
    }

    /**
     * Lays every verse of the surah into one run of text. The Basmalah opening is the exact
     * Uthmani text of 1:1 - never typed by hand - and it is added as a centred opening line for
     * every surah that carries it (all but Al-Fatiha, where it is verse 1, and At-Tawbah).
     */
    private void buildPage(@NonNull QuranRepository loaded, @NonNull QuranSurah surah) {
        this.pageText = new SpannableStringBuilder();
        this.verses.clear();
        this.highlights.clear();

        int goldInk = ContextCompat.getColor(this, R.color.quranGold);
        if (surah.getNumber() != 1 && surah.getNumber() != 9) {
            QuranAyah basmalah = loaded.getAyah(1, 1);
            if (basmalah != null) {
                int from = this.pageText.length();
                this.pageText.append(basmalah.getText());
                int to = this.pageText.length();
                this.pageText.setSpan(new ForegroundColorSpan(goldInk), from, to,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                // Centred like the opening line of a printed Mushaf.
                this.pageText.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                        from, to, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                this.pageText.append("\n\n");
            }
        }

        int verseTint = ContextCompat.getColor(this, R.color.quranVerseHighlight);
        for (QuranAyah ayah : loaded.getAyahs(surah.getNumber())) {
            int start = this.pageText.length();
            this.pageText.append(ayah.getText());
            int end = this.pageText.length();

            this.pageText.append(' ');
            int markerStart = this.pageText.length();
            this.pageText.append(END_OF_AYAH).append(arabicIndic(ayah.getAyahNumber()));
            int markerEnd = this.pageText.length();
            this.pageText.setSpan(new ForegroundColorSpan(goldInk), markerStart, markerEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            this.pageText.setSpan(new RelativeSizeSpan(0.8f), markerStart, markerEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            this.pageText.append(' ');

            this.verses.add(new VerseRange(start, end, markerEnd, ayah.getAyahNumber()));
            if (this.store.isBookmarked(surah.getNumber(), ayah.getAyahNumber())) {
                this.highlights.put(ayah.getAyahNumber(),
                        tint(this.pageText, this.verses.size() - 1, verseTint));
            }
        }
    }

    /** Paints the saved-verse tint over a verse that is already in the page. */
    @Nullable
    private BackgroundColorSpan tint(@NonNull SpannableStringBuilder target, int verseIndex,
                                     int colour) {
        VerseRange range = this.verses.get(verseIndex);
        BackgroundColorSpan span = new BackgroundColorSpan(colour);
        target.setSpan(span, range.start, range.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
    }

    /**
     * A tap only opens or clears the save of the verse under the finger. It never changes which
     * verse is remembered as the reading position: that follows the scroll instead, so an
     * accidental tap cannot move the resume point.
     */
    private void toggleVerseUnder(float x, float y) {
        if (this.pageText == null || this.surahNumber <= 0) {
            return;
        }
        Layout layout = this.page.getLayout();
        if (layout == null) {
            return;
        }
        float localX = x - this.page.getTotalPaddingLeft();
        float localY = y - this.page.getTotalPaddingTop();
        int line = layout.getLineForVertical((int) localY);
        if (localX < layout.getLineLeft(line) || localX > layout.getLineRight(line)) {
            return; // the finger landed on the page margin, not on a verse
        }
        VerseRange range = verseAt(layout.getOffsetForHorizontal(line, localX));
        if (range == null) {
            return;
        }
        boolean added = this.store.toggleBookmark(this.surahNumber, range.ayahNumber);
        paintBookmark(range.ayahNumber, added);
        Toast.makeText(this, added ? R.string.quran_bookmark_added : R.string.quran_bookmark_removed,
                Toast.LENGTH_SHORT).show();
    }

    /** Adds or removes the tint of one verse without touching anything else on the page. */
    private void paintBookmark(int ayahNumber, boolean saved) {
        if (this.pageText == null) {
            return;
        }
        BackgroundColorSpan span = this.highlights.get(ayahNumber);
        if (saved && span == null) {
            for (int i = 0; i < this.verses.size(); i++) {
                if (this.verses.get(i).ayahNumber == ayahNumber) {
                    this.highlights.put(ayahNumber, tint(this.pageText, i,
                            ContextCompat.getColor(this, R.color.quranVerseHighlight)));
                    break;
                }
            }
        } else if (!saved && span != null) {
            this.pageText.removeSpan(span);
            this.highlights.remove(ayahNumber);
        }
        this.page.invalidate();
    }

    /** Repaints the whole page after returning from the saved-marks screen. */
    private void refreshBookmarks() {
        if (this.pageText == null || this.surahNumber <= 0) {
            return;
        }
        for (VerseRange range : this.verses) {
            paintBookmark(range.ayahNumber,
                    this.store.isBookmarked(this.surahNumber, range.ayahNumber));
        }
    }

    /** The verse whose text or number contains {@code offset}. */
    @Nullable
    private VerseRange verseAt(int offset) {
        int low = 0;
        int high = this.verses.size() - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            VerseRange range = this.verses.get(mid);
            if (offset < range.start) {
                high = mid - 1;
            } else if (offset >= range.markerEnd) {
                low = mid + 1;
            } else {
                return range;
            }
        }
        return null;
    }

    /** The verse the reader is in: the last one that starts at or before {@code offset}. */
    @Nullable
    private VerseRange verseAtOrBefore(int offset) {
        VerseRange found = null;
        for (VerseRange range : this.verses) {
            if (range.start > offset) {
                break;
            }
            found = range;
        }
        return found;
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
                    this.page.setTextSize(TypedValue.COMPLEX_UNIT_SP, selected);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void openMushafAtVisiblePage() {
        this.positionSaver.removeCallbacks(this.savePositionTask);
        saveVisiblePosition();
        int page = -1;
        QuranBookmark reading = this.store == null ? null : this.store.getLastReading();
        if (reading != null && this.repository != null) {
            page = this.repository.getPageForAyah(reading.getSurahNumber(),
                    reading.getAyahNumber());
        }
        startActivity(QuranMushafActivity.createIntent(this, page));
    }

    /** Writes the verse at the top of the viewport, at most once per scroll pause. */
    private void schedulePositionSave() {
        this.positionSaver.removeCallbacks(this.savePositionTask);
        this.positionSaver.postDelayed(this.savePositionTask, POSITION_SAVE_DELAY_MS);
    }

    private void saveVisiblePosition() {
        if (this.store == null || this.surahNumber <= 0 || this.pageText == null) {
            return;
        }
        Layout layout = this.page.getLayout();
        if (layout == null || this.verses.isEmpty()) {
            return;
        }
        int top = this.scroll.getScrollY() - this.page.getTop() + this.page.getTotalPaddingTop();
        int line = layout.getLineForVertical(Math.max(0, top));
        VerseRange range = verseAtOrBefore(layout.getLineStart(line));
        if (range != null) {
            this.store.saveLastReading(this.surahNumber, range.ayahNumber);
        }
    }

    private void scrollToAyah(int ayahNumber) {
        Layout layout = this.page.getLayout();
        if (layout == null) {
            return;
        }
        for (VerseRange range : this.verses) {
            if (range.ayahNumber == ayahNumber) {
                int line = layout.getLineForOffset(range.start);
                this.scroll.scrollTo(0,
                        Math.max(0, layout.getLineTop(line) - UiCompat.dp(this, 8)));
                return;
            }
        }
    }

    /** Remembers where the finger went down; a click carries no coordinates of its own. */
    private boolean rememberTouchDown(View view, MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            this.touchDownX = event.getX();
            this.touchDownY = event.getY();
        }
        return false;
    }

    /** 6236 -> ٦٢٣٦, the way an ayah number is printed inside the end-of-ayah glyph. */
    @NonNull
    private static String arabicIndic(int value) {
        String digits = String.valueOf(value);
        StringBuilder out = new StringBuilder(digits.length());
        for (int i = 0; i < digits.length(); i++) {
            out.append((char) ('\u0660' + (digits.charAt(i) - '0')));
        }
        return out.toString();
    }

    private void showLoadError() {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        this.loading.setVisibility(View.GONE);
        this.scroll.setVisibility(View.GONE);
        this.error.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        this.positionSaver.removeCallbacks(this.savePositionTask);
        loader.shutdownNow();
        super.onDestroy();
    }
}
