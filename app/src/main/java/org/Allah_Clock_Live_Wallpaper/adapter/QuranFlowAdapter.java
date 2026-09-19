package org.Allah_Clock_Live_Wallpaper.adapter;

import android.os.Build;
import android.text.Layout;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.OneShotPreDrawListener;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.model.QuranAyah;
import org.Allah_Clock_Live_Wallpaper.model.QuranSurah;
import org.Allah_Clock_Live_Wallpaper.utils.AyahNumberSpan;
import org.Allah_Clock_Live_Wallpaper.utils.QuranParagraph;
import org.Allah_Clock_Live_Wallpaper.utils.QuranStore;
import org.Allah_Clock_Live_Wallpaper.utils.QuranTheme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Both readers share page-sized RTL paragraphs, not one row per verse. Only authentic page
 * boundaries and surah headings split the flow. Verse spans retain taps and bookmark tints.
 */
public final class QuranFlowAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int TYPE_PARAGRAPH = 0;
    public static final int TYPE_SURAH = 1;
    public static final int TYPE_PAGE = 2;

    public interface AyahTapListener {
        void onAyahTapped(@NonNull QuranAyah ayah);
    }

    public static final class Row {
        public final int type;
        @NonNull public final List<QuranAyah> ayahs;
        @Nullable public final QuranSurah surah;
        public final int juz;
        public final int page;

        private Row(int type, List<QuranAyah> ayahs, QuranSurah surah, int juz, int page) {
            this.type = type;
            this.ayahs = Collections.unmodifiableList(new ArrayList<>(ayahs));
            this.surah = surah;
            this.juz = juz;
            this.page = page;
        }

        private static Row paragraph(List<QuranAyah> ayahs) {
            QuranAyah first = ayahs.get(0);
            return new Row(TYPE_PARAGRAPH, ayahs, null, first.getJuz(), first.getMushafPage());
        }
    }

    /** Append complete page fragments, flushing only at page or surah boundaries. */
    public static void appendSurahRows(@NonNull List<Row> rows, @NonNull QuranSurah surah,
                                       @NonNull List<QuranAyah> ayahs, boolean pageDividers) {
        if (ayahs.isEmpty()) {
            return;
        }
        int previousPage = rows.isEmpty() ? -1 : rows.get(rows.size() - 1).page;
        rows.add(new Row(TYPE_SURAH, Collections.emptyList(), surah, ayahs.get(0).getJuz(), 0));
        int start = 0;
        while (start < ayahs.size()) {
            int page = ayahs.get(start).getMushafPage();
            int end = start + 1;
            while (end < ayahs.size() && ayahs.get(end).getMushafPage() == page) {
                end++;
            }
            if (pageDividers && page != previousPage) {
                rows.add(new Row(TYPE_PAGE, Collections.emptyList(), null, 0, page));
            }
            rows.add(Row.paragraph(ayahs.subList(start, end)));
            previousPage = page;
            start = end;
        }
    }

    static final class ParagraphHolder extends RecyclerView.ViewHolder {
        final TextView text;
        QuranParagraph paragraph;

        ParagraphHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.quranAyahText);
            text.setMovementMethod(LinkMovementMethod.getInstance());
            text.setHighlightColor(0); // Saved verses have their own per-range background.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                text.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
            }
        }
    }

    static final class SurahHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView meta;
        final ImageView ornament;

        SurahHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.quranSurahHeaderName);
            meta = itemView.findViewById(R.id.quranSurahHeaderMeta);
            ornament = itemView.findViewById(R.id.quranSurahHeaderOrnament);
        }
    }

    static final class PageHolder extends RecyclerView.ViewHolder {
        final TextView number;

        PageHolder(View itemView) {
            super(itemView);
            number = itemView.findViewById(R.id.quranPageDividerNumber);
        }
    }

    private final List<Row> rows;
    private final QuranTheme theme;
    private final QuranStore store;
    @Nullable private final AyahTapListener listener;
    private float textSizeSp;

    public QuranFlowAdapter(@NonNull List<Row> rows, @NonNull QuranTheme theme,
                            @NonNull QuranStore store, float textSizeSp,
                            @Nullable AyahTapListener listener) {
        this.rows = new ArrayList<>(rows);
        this.theme = theme;
        this.store = store;
        this.textSizeSp = textSizeSp;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_SURAH) {
            return new SurahHolder(inflater.inflate(R.layout.item_quran_surah_header, parent, false));
        }
        if (viewType == TYPE_PAGE) {
            return new PageHolder(inflater.inflate(R.layout.item_quran_page_divider, parent, false));
        }
        return new ParagraphHolder(inflater.inflate(R.layout.item_quran_ayah, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Row row = rows.get(position);
        if (holder instanceof ParagraphHolder) {
            bindParagraph((ParagraphHolder) holder, row);
        } else if (holder instanceof SurahHolder) {
            SurahHolder header = (SurahHolder) holder;
            header.name.setText(row.surah.getArabicName());
            header.name.setTextColor(theme.ink);
            // The divider under the heading is tinted, not swapped, so a wash change repaints it.
            ImageViewCompat.setImageTintList(header.ornament,
                    android.content.res.ColorStateList.valueOf(theme.gold));
            header.meta.setText(header.meta.getContext().getString(R.string.quran_surah_header_meta,
                    Math.max(1, row.juz), row.surah.getAyahCount()));
            header.meta.setTextColor(theme.muted);
        } else if (holder instanceof PageHolder) {
            ((PageHolder) holder).number.setText(String.valueOf(row.page));
            ((PageHolder) holder).number.setTextColor(theme.muted);
        }
    }

    private void bindParagraph(ParagraphHolder holder, Row row) {
        // The end-of-ayah number follows the day/night marker colour; the mihrab marker of an
        // ayah of prostration is always the wash's gold, as a Mushaf prints it.
        QuranParagraph paragraph = new QuranParagraph(row.ayahs,
                holder.text.getResources().getDisplayMetrics().density,
                theme.isNight() ? theme.gold : AyahNumberSpan.PRIMARY_GREEN, theme.gold);
        for (int i = 0; i < row.ayahs.size(); i++) {
            final QuranAyah ayah = row.ayahs.get(i);
            int start = paragraph.start(i);
            int end = paragraph.end(i);
            paragraph.text.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    if (listener != null) {
                        listener.onAyahTapped(ayah);
                    }
                }

                @Override
                public void updateDrawState(@NonNull TextPaint paint) {
                    // Keep Quran typography/colour: these are verse taps, not blue web links.
                    paint.setUnderlineText(false);
                }
            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (store.isBookmarked(ayah.getSurahNumber(), ayah.getAyahNumber())) {
                paragraph.text.setSpan(new BackgroundColorSpan(theme.verseHighlight), start, end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        holder.paragraph = paragraph;
        holder.text.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp);
        holder.text.setTextColor(theme.ink);
        holder.text.setText(paragraph.text);
    }

    /** Locate a verse inside a block instead of treating its number as a RecyclerView row. */
    public int rowForAyah(int surah, int ayah) {
        for (int i = 0; i < rows.size(); i++) {
            for (QuranAyah entry : rows.get(i).ayahs) {
                if (entry.getSurahNumber() == surah && entry.getAyahNumber() == ayah) {
                    return i;
                }
            }
        }
        return RecyclerView.NO_POSITION;
    }

    /** Track the verse at the first visible text line, even partway down a tall paragraph. */
    @Nullable
    public QuranAyah topVisibleAyah(@NonNull RecyclerView list) {
        LinearLayoutManager manager = (LinearLayoutManager) list.getLayoutManager();
        if (manager == null) {
            return null;
        }
        int first = manager.findFirstVisibleItemPosition();
        if (first == RecyclerView.NO_POSITION) {
            return null;
        }
        for (int position = first; position < rows.size(); position++) {
            Row row = rows.get(position);
            if (row.type != TYPE_PARAGRAPH) {
                continue;
            }
            RecyclerView.ViewHolder view = list.findViewHolderForAdapterPosition(position);
            if (view instanceof ParagraphHolder) {
                ParagraphHolder holder = (ParagraphHolder) view;
                Layout layout = holder.text.getLayout();
                if (layout != null && holder.paragraph != null) {
                    int textTop = holder.itemView.getTop() + holder.text.getTop()
                            + holder.text.getTotalPaddingTop();
                    int line = layout.getLineForVertical(Math.max(0, list.getPaddingTop() - textTop));
                    return holder.paragraph.ayahAtOffset(layout.getLineStart(line));
                }
            }
            return row.ayahs.get(0);
        }
        return null;
    }

    /** First lay out the target page, then position its target verse's line at the viewport top. */
    public void scrollToAyah(@NonNull RecyclerView list, int surah, int ayah) {
        int row = rowForAyah(surah, ayah);
        LinearLayoutManager manager = (LinearLayoutManager) list.getLayoutManager();
        if (row == RecyclerView.NO_POSITION || manager == null) {
            return;
        }
        OneShotPreDrawListener.add(list, () -> {
            RecyclerView.ViewHolder view = list.findViewHolderForAdapterPosition(row);
            if (!(view instanceof ParagraphHolder)) {
                return;
            }
            ParagraphHolder holder = (ParagraphHolder) view;
            Layout layout = holder.text.getLayout();
            if (holder.paragraph == null) {
                return;
            }
            int offset = holder.paragraph.offsetOf(surah, ayah);
            if (layout != null && offset >= 0) {
                int top = holder.text.getTop() + holder.text.getTotalPaddingTop()
                        + layout.getLineTop(layout.getLineForOffset(offset));
                manager.scrollToPositionWithOffset(row, -top);
            }
        });
        manager.scrollToPositionWithOffset(row, 0);
    }

    public void setTextSizeSp(float textSizeSp) {
        this.textSizeSp = textSizeSp;
        notifyDataSetChanged();
    }

    public void refreshTheme() {
        notifyDataSetChanged();
    }

    public void refreshAyah(int surah, int ayah) {
        int row = rowForAyah(surah, ayah);
        if (row != RecyclerView.NO_POSITION) {
            notifyItemChanged(row);
        }
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }
}
