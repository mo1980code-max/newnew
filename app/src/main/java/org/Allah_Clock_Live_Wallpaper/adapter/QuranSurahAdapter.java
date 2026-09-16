package org.Allah_Clock_Live_Wallpaper.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.model.QuranSurah;

import java.util.List;

/** Offline Quran table of contents: all 114 surahs, with no network or ad content. */
public final class QuranSurahAdapter
        extends RecyclerView.Adapter<QuranSurahAdapter.ViewHolder> {

    public interface Listener {
        void onOpenSurah(@NonNull QuranSurah surah);
    }

    @NonNull
    private final List<QuranSurah> items;
    private final boolean arabicUi;
    @NonNull
    private final Listener listener;

    public QuranSurahAdapter(@NonNull List<QuranSurah> items, boolean arabicUi,
                             @NonNull Listener listener) {
        this.items = items;
        this.arabicUi = arabicUi;
        this.listener = listener;
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {
        final TextView number;
        final TextView title;
        final TextView subtitle;
        final TextView metadata;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            number = itemView.findViewById(R.id.quranSurahNumber);
            title = itemView.findViewById(R.id.quranSurahTitle);
            subtitle = itemView.findViewById(R.id.quranSurahSubtitle);
            metadata = itemView.findViewById(R.id.quranSurahMetadata);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quran_surah, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuranSurah surah = items.get(position);
        holder.number.setText(String.valueOf(surah.getNumber()));
        holder.title.setText(surah.getDisplayName(arabicUi));
        holder.subtitle.setText(surah.getSecondaryName(arabicUi));
        String revelation = holder.itemView.getContext().getString(surah.isMeccan()
                ? R.string.quran_meccan : R.string.quran_medinan);
        holder.metadata.setText(holder.itemView.getContext().getString(R.string.quran_surah_metadata,
                surah.getAyahCount(), revelation));
        holder.itemView.setContentDescription(holder.itemView.getContext().getString(
                R.string.quran_open_surah, surah.getDisplayName(arabicUi)));
        holder.itemView.setOnClickListener(view -> listener.onOpenSurah(surah));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
