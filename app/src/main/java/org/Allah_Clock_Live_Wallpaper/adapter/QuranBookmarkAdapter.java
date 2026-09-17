package org.Allah_Clock_Live_Wallpaper.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.model.QuranAyah;
import org.Allah_Clock_Live_Wallpaper.model.QuranBookmark;
import org.Allah_Clock_Live_Wallpaper.model.QuranSurah;
import org.Allah_Clock_Live_Wallpaper.utils.QuranRepository;
import org.Allah_Clock_Live_Wallpaper.viewUtils.AyahBadgeSpan;

import java.util.ArrayList;
import java.util.List;

/** Restored local bookmarks, newest first. */
public final class QuranBookmarkAdapter
        extends RecyclerView.Adapter<QuranBookmarkAdapter.ViewHolder> {

    public interface Listener {
        void onOpenBookmark(@NonNull QuranBookmark bookmark);

        void onRemoveBookmark(@NonNull QuranBookmark bookmark, int position);
    }

    @NonNull
    private final List<QuranBookmark> items = new ArrayList<>();
    @NonNull
    private final QuranRepository repository;
    private final boolean arabicUi;
    @NonNull
    private final Listener listener;

    public QuranBookmarkAdapter(@NonNull List<QuranBookmark> bookmarks,
                                @NonNull QuranRepository repository, boolean arabicUi,
                                @NonNull Listener listener) {
        this.repository = repository;
        this.arabicUi = arabicUi;
        this.listener = listener;
        this.items.addAll(bookmarks);
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView text;
        final TextView badgeNumber;
        final ImageButton remove;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.quranBookmarkTitle);
            text = itemView.findViewById(R.id.quranBookmarkText);
            badgeNumber = itemView.findViewById(R.id.ayahBadgeNumber);
            remove = itemView.findViewById(R.id.quranBookmarkRemove);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quran_bookmark, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuranBookmark bookmark = items.get(position);
        QuranSurah surah = repository.getSurah(bookmark.getSurahNumber());
        QuranAyah ayah = repository.getAyah(bookmark.getSurahNumber(), bookmark.getAyahNumber());
        if (surah == null || ayah == null) {
            // Invalid legacy preferences are filtered by the activity, but keep this holder safe.
            holder.title.setText("");
            holder.text.setText("");
            holder.badgeNumber.setText("");
            holder.itemView.setOnClickListener(null);
            holder.remove.setOnClickListener(null);
            return;
        }
        String name = surah.getDisplayName(arabicUi);
        int page = repository.getPageForAyah(surah.getNumber(), ayah.getAyahNumber());
        int title = page > 0 ? R.string.quran_ayah_reference_page : R.string.quran_ayah_reference;
        holder.title.setText(page > 0
                ? holder.itemView.getContext().getString(title, surah.getNumber(), name,
                        ayah.getAyahNumber(), page)
                : holder.itemView.getContext().getString(title, surah.getNumber(), name,
                        ayah.getAyahNumber()));
        holder.text.setText(ayah.getText());
        holder.badgeNumber.setText(AyahBadgeSpan.arabicIndic(ayah.getAyahNumber()));
        holder.remove.setContentDescription(holder.itemView.getContext().getString(
                R.string.quran_remove_bookmark, ayah.getAyahNumber()));
        holder.itemView.setOnClickListener(view -> listener.onOpenBookmark(bookmark));
        holder.remove.setOnClickListener(view -> {
            int currentPosition = holder.getBindingAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                listener.onRemoveBookmark(bookmark, currentPosition);
            }
        });
    }

    public void removeAt(int position) {
        if (position < 0 || position >= items.size()) {
            return;
        }
        items.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
