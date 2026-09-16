package org.Allah_Clock_Live_Wallpaper.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.model.QuranAyah;
import org.Allah_Clock_Live_Wallpaper.model.QuranSearchResult;
import org.Allah_Clock_Live_Wallpaper.model.QuranSurah;

import java.util.ArrayList;
import java.util.List;

/** Mixed list for surah-name and full-text ayah search results. */
public final class QuranSearchAdapter extends RecyclerView.Adapter<QuranSearchAdapter.ViewHolder> {

    public interface Listener {
        void onOpenResult(@NonNull QuranSurah surah, int ayahNumber);
    }

    @NonNull
    private final List<QuranSearchResult> items = new ArrayList<>();
    private final boolean arabicUi;
    @NonNull
    private final Listener listener;

    public QuranSearchAdapter(boolean arabicUi, @NonNull Listener listener) {
        this.arabicUi = arabicUi;
        this.listener = listener;
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {
        final TextView type;
        final TextView title;
        final TextView text;
        final TextView reference;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            type = itemView.findViewById(R.id.quranSearchResultType);
            title = itemView.findViewById(R.id.quranSearchResultTitle);
            text = itemView.findViewById(R.id.quranSearchResultText);
            reference = itemView.findViewById(R.id.quranSearchResultReference);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quran_search_result, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuranSearchResult result = items.get(position);
        QuranSurah surah = result.getSurah();
        String name = surah.getDisplayName(arabicUi);
        holder.title.setText(holder.itemView.getContext().getString(R.string.quran_surah_title,
                surah.getNumber(), name));

        @Nullable QuranAyah ayah = result.getAyah();
        if (result.getType() == QuranSearchResult.TYPE_SURAH || ayah == null) {
            holder.type.setText(R.string.quran_search_type_surah);
            holder.text.setText(surah.getSecondaryName(arabicUi));
            String revelation = holder.itemView.getContext().getString(surah.isMeccan()
                    ? R.string.quran_meccan : R.string.quran_medinan);
            holder.reference.setText(holder.itemView.getContext().getString(
                    R.string.quran_surah_metadata, surah.getAyahCount(), revelation));
            holder.itemView.setOnClickListener(view -> listener.onOpenResult(surah, 1));
        } else {
            final int ayahNumber = ayah.getAyahNumber();
            holder.type.setText(R.string.quran_search_type_ayah);
            holder.text.setText(ayah.getText());
            holder.reference.setText(holder.itemView.getContext().getString(
                    R.string.quran_ayah_reference, surah.getNumber(), name, ayahNumber));
            holder.itemView.setOnClickListener(view -> listener.onOpenResult(surah, ayahNumber));
        }
    }

    public void replace(@NonNull List<QuranSearchResult> results) {
        items.clear();
        items.addAll(results);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
