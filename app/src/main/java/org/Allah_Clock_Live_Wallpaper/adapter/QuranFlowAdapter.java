package org.Allah_Clock_Live_Wallpaper.adapter;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.model.QuranAyah;
import org.Allah_Clock_Live_Wallpaper.model.QuranSurah;
import org.Allah_Clock_Live_Wallpaper.utils.QuranStore;
import org.Allah_Clock_Live_Wallpaper.utils.QuranTheme;

import java.util.List;

/**
 * The continuous vertical scroll both Quran readers share: ayahs as flowing text, each closed by
 * its built-in end-of-ayah glyph, with the authentic surah headings and the Madani page
 * boundaries interleaved where the flow calls for them.
 *
 * <p>Every colour a row shows comes from the shared {@link QuranTheme}, so the night reading
 * switch repaints the whole column in place, and the saved-verse tint is a soft rounded highlight
 * behind the text — the same state the bookmark store holds.</p>
 */
public final class QuranFlowAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_AYAH = 0;
    public static final int TYPE_SURAH = 1;
    public static final int TYPE_PAGE = 2;

    /** A tap on a verse, routed to the host so it can save or clear the bookmark. */
    public interface AyahTapListener {
        void onAyahTapped(@NonNull QuranAyah ayah);
    }

    /** One line of the scroll: a verse, a surah heading or a Madani page boundary. */
    public static final class Row {
        public final int type;
        @Nullable
        public final QuranAyah ayah;
        @Nullable
        public final QuranSurah surah;
        /** TYPE_SURAH only: the juz the surah opens in. */
        public final int juz;
        /** TYPE_PAGE only: the printed Madani page number. */
        public final int page;

        private Row(int type, @Nullable QuranAyah ayah, @Nullable QuranSurah surah, int juz,
                    int page) {
            this.type = type;
            this.ayah = ayah;
            this.surah = surah;
            this.juz = juz;
            this.page = page;
        }

        @NonNull
        public static Row ayah(@NonNull QuranAyah ayah) {
            return new Row(TYPE_AYAH, ayah, null, 0, 0);
        }

        @NonNull
        public static Row surah(@NonNull QuranSurah surah, int juz) {
            return new Row(TYPE_SURAH, null, surah, juz, 0);
        }

        @NonNull
        public static Row page(int page) {
            return new Row(TYPE_PAGE, null, null, 0, page);
        }
    }

    static final class AyahHolder extends RecyclerView.ViewHolder {
        final View row;
        final TextView text;

        AyahHolder(@NonNull View itemView) {
            super(itemView);
            row = itemView;
            text = itemView.findViewById(R.id.quranAyahText);
        }
    }

    static final class SurahHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView meta;

        SurahHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.quranSurahHeaderName);
            meta = itemView.findViewById(R.id.quranSurahHeaderMeta);
        }
    }

    static final class PageHolder extends RecyclerView.ViewHolder {
        final TextView number;

        PageHolder(@NonNull View itemView) {
            super(itemView);
            number = itemView.findViewById(R.id.quranPageDividerNumber);
        }
    }

    @NonNull
    private final List<Row> rows;
    @NonNull
    private final QuranTheme theme;
    @NonNull
    private final QuranStore store;
    @Nullable
    private final AyahTapListener listener;
    private float textSizeSp;

    public QuranFlowAdapter(@NonNull List<Row> rows, @NonNull QuranTheme theme,
                            @NonNull QuranStore store, float textSizeSp,
                            @Nullable AyahTapListener listener) {
        this.rows = rows;
        this.theme = theme;
        this.store = store;
        this.textSizeSp = textSizeSp;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return this.rows.get(position).type;
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
        return new AyahHolder(inflater.inflate(R.layout.item_quran_ayah, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Row row = this.rows.get(position);
        if (holder instanceof AyahHolder) {
            bindAyah((AyahHolder) holder, row.ayah);
        } else if (holder instanceof SurahHolder) {
            bindSurah((SurahHolder) holder, row.surah, row.juz);
        } else if (holder instanceof PageHolder) {
            bindPage((PageHolder) holder, row.page);
        }
    }

    private void bindAyah(@NonNull AyahHolder holder, @NonNull QuranAyah ayah) {
        holder.text.setText(ayah.getText());
        holder.text.setTextSize(TypedValue.COMPLEX_UNIT_SP, this.textSizeSp);
        holder.text.setTextColor(this.theme.ink);
        boolean saved = this.store.isBookmarked(ayah.getSurahNumber(), ayah.getAyahNumber());
        holder.row.setBackground(highlightBackground(holder.itemView.getContext(), saved));
        holder.itemView.setOnClickListener(view -> {
            if (this.listener != null) {
                this.listener.onAyahTapped(ayah);
            }
        });
    }

    private void bindSurah(@NonNull SurahHolder holder, @NonNull QuranSurah surah, int juz) {
        holder.name.setText(surah.getArabicName());
        holder.name.setTextColor(this.theme.ink);
        holder.meta.setText(holder.meta.getContext().getString(R.string.quran_surah_header_meta,
                Math.max(1, juz), surah.getAyahCount()));
        holder.meta.setTextColor(this.theme.muted);
    }

    private void bindPage(@NonNull PageHolder holder, int page) {
        holder.number.setText(String.valueOf(page));
        holder.number.setTextColor(this.theme.muted);
    }

    /** The saved-verse tint: a soft rounded highlight, or nothing at all when unsaved. */
    @NonNull
    private android.graphics.drawable.Drawable highlightBackground(@NonNull android.content.Context context,
                                                                   boolean saved) {
        if (!saved) {
            return new ColorDrawable(0);
        }
        GradientDrawable tint = new GradientDrawable();
        tint.setShape(GradientDrawable.RECTANGLE);
        tint.setColor(this.theme.verseHighlight);
        tint.setCornerRadius(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f,
                context.getResources().getDisplayMetrics()));
        return tint;
    }

    /** Sets the reader's type size and re-binds the column in place. */
    public void setTextSizeSp(float textSizeSp) {
        this.textSizeSp = textSizeSp;
        notifyDataSetChanged();
    }

    /** The palette moved under the column (night switch): repaint everything in place. */
    public void refreshTheme() {
        notifyDataSetChanged();
    }

    /** One verse's bookmark moved: repaint only that row. */
    public void refreshAyah(int position) {
        if (position >= 0 && position < this.rows.size()
                && this.rows.get(position).type == TYPE_AYAH) {
            notifyItemChanged(position);
        }
    }

    @Override
    public int getItemCount() {
        return this.rows.size();
    }

    /** The flow's rows, read-only; the host maps its own navigation targets onto them. */
    @NonNull
    public List<Row> getRows() {
        return java.util.Collections.unmodifiableList(this.rows);
    }
}
