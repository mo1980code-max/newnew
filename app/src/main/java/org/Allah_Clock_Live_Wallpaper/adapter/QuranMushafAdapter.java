package org.Allah_Clock_Live_Wallpaper.adapter;

import android.content.Context;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.model.QuranAyah;
import org.Allah_Clock_Live_Wallpaper.model.QuranPage;
import org.Allah_Clock_Live_Wallpaper.model.QuranSurah;
import org.Allah_Clock_Live_Wallpaper.utils.QuranPageBuilder;
import org.Allah_Clock_Live_Wallpaper.utils.QuranRepository;
import org.Allah_Clock_Live_Wallpaper.utils.QuranStore;
import org.Allah_Clock_Live_Wallpaper.utils.QuranTheme;
import org.Allah_Clock_Live_Wallpaper.viewUtils.QuranPageView;

import java.util.List;

/**
 * The 604 canonical Madani pages, flipped horizontally: one {@code ViewPager2} page per printed
 * Mushaf page, so the reader turns pages sideways exactly like the printed book instead of
 * scrolling one long column.
 *
 * <p>Each page is rebuilt from the bundled text when it is bound — the surah heading and the
 * Basmalah appear on the pages that open a surah, the way they do in print — and its height is
 * measured by the page view itself. A page that does not fit the screen at the reader's chosen
 * text size is scrollable inside its own pager item, which keeps every ayah reachable without
 * ever changing the canonical page boundaries.</p>
 */
public final class QuranMushafAdapter extends RecyclerView.Adapter<QuranMushafAdapter.Holder> {

    private final QuranRepository repository;
    private final QuranStore store;
    private final QuranTheme theme;
    private final TextPaint paint;
    private final float lineSpacingExtra;
    private final boolean arabicUi;
    private final QuranPageView.Listener listener;
    private final int pageCount;

    public QuranMushafAdapter(@NonNull QuranRepository repository, @NonNull QuranStore store,
                              @NonNull QuranTheme theme, @NonNull TextPaint paint,
                              float lineSpacingExtra, boolean arabicUi,
                              @NonNull QuranPageView.Listener listener) {
        this.repository = repository;
        this.store = store;
        this.theme = theme;
        this.paint = paint;
        this.lineSpacingExtra = lineSpacingExtra;
        this.arabicUi = arabicUi;
        this.listener = listener;
        this.pageCount = QuranRepository.PAGE_COUNT;
        setHasStableIds(true);
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final QuranPageView page;

        Holder(@NonNull View itemView) {
            super(itemView);
            this.page = itemView.findViewById(R.id.quranPageView);
        }
    }

    /** The pager position (0 based) of a canonical page number. */
    public static int positionOfPage(int pageNumber) {
        if (pageNumber < 1) {
            return 0;
        }
        return Math.min(pageNumber, QuranRepository.PAGE_COUNT) - 1;
    }

    /** The canonical page number shown at a pager position. */
    public static int pageAt(int position) {
        return Math.max(1, Math.min(position + 1, QuranRepository.PAGE_COUNT));
    }

    @Override
    public long getItemId(int position) {
        return pageAt(position);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quran_screen, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Context context = holder.itemView.getContext();
        int pageNumber = pageAt(position);
        QuranPage page = this.repository.getPage(pageNumber);
        if (page == null) {
            holder.page.bind("", 0, 0, java.util.Collections.<QuranPageBuilder.Verse>emptyList(),
                    this.paint, this.theme, this.lineSpacingExtra, null);
            return;
        }

        QuranPageBuilder builder = new QuranPageBuilder(this.theme);
        int firstSurah = page.getFirstAyah().getSurahNumber();
        if (page.getFirstAyah().getAyahNumber() == 1) {
            QuranSurah surah = this.repository.getSurah(firstSurah);
            if (surah != null) {
                builder.appendHeading(context.getString(R.string.quran_surah_header,
                        surah.getDisplayName(this.arabicUi)));
            }
            if (firstSurah != 1 && firstSurah != 9) {
                QuranAyah basmalah = this.repository.getAyah(1, 1);
                if (basmalah != null) {
                    builder.appendBasmalah(basmalah);
                }
            }
        }
        List<QuranAyah> ayahs = this.repository.getAyahsForPage(pageNumber);
        for (QuranAyah ayah : ayahs) {
            builder.appendAyah(ayah);
            if (this.store.isBookmarked(ayah.getSurahNumber(), ayah.getAyahNumber())) {
                builder.highlight(ayah.getAyahNumber(), this.theme.verseHighlight);
            }
        }

        holder.page.bind(builder.getText(), 0, builder.getText().length(), builder.getVerses(),
                this.paint, this.theme, this.lineSpacingExtra, this.listener);
    }

    @Override
    public int getItemCount() {
        return this.pageCount;
    }
}
