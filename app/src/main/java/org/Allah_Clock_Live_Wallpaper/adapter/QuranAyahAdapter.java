package org.Allah_Clock_Live_Wallpaper.adapter;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.model.QuranAyah;
import org.Allah_Clock_Live_Wallpaper.utils.QuranStore;

import java.util.List;

/** Reader rows. Each verse can be saved independently as a local bookmark. */
public final class QuranAyahAdapter extends RecyclerView.Adapter<QuranAyahAdapter.ViewHolder> {

    public interface Listener {
        void onReadingPosition(@NonNull QuranAyah ayah);

        void onBookmarkChanged(@NonNull QuranAyah ayah, boolean added);
    }

    @NonNull
    private final List<QuranAyah> items;
    @NonNull
    private final QuranStore store;
    @NonNull
    private final Listener listener;
    private int textSizeSp;

    public QuranAyahAdapter(@NonNull List<QuranAyah> items, @NonNull QuranStore store,
                            @NonNull Listener listener, int textSizeSp) {
        this.items = items;
        this.store = store;
        this.listener = listener;
        this.textSizeSp = textSizeSp;
        setHasStableIds(true);
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {
        final TextView number;
        final TextView text;
        final ImageButton bookmark;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            number = itemView.findViewById(R.id.quranAyahNumber);
            text = itemView.findViewById(R.id.quranAyahText);
            bookmark = itemView.findViewById(R.id.quranAyahBookmark);
        }
    }

    @Override
    public long getItemId(int position) {
        QuranAyah ayah = items.get(position);
        return ayah.getSurahNumber() * 1000L + ayah.getAyahNumber();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quran_ayah, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuranAyah ayah = items.get(position);
        holder.number.setText(String.valueOf(ayah.getAyahNumber()));
        holder.text.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp);
        holder.text.setText(ayah.getText());
        paintBookmark(holder, ayah, store.isBookmarked(ayah.getSurahNumber(), ayah.getAyahNumber()));

        holder.itemView.setOnClickListener(view -> listener.onReadingPosition(ayah));
        holder.itemView.setOnLongClickListener(view -> {
            toggle(holder, ayah);
            return true;
        });
        holder.bookmark.setOnClickListener(view -> toggle(holder, ayah));
    }

    private void toggle(@NonNull ViewHolder holder, @NonNull QuranAyah ayah) {
        boolean added = store.toggleBookmark(ayah.getSurahNumber(), ayah.getAyahNumber());
        paintBookmark(holder, ayah, added);
        listener.onBookmarkChanged(ayah, added);
    }

    private static void paintBookmark(@NonNull ViewHolder holder, @NonNull QuranAyah ayah,
                                      boolean saved) {
        holder.bookmark.setImageResource(saved
                ? R.drawable.ic_bookmark : R.drawable.ic_bookmark_border);
        holder.bookmark.setContentDescription(holder.itemView.getContext().getString(saved
                ? R.string.quran_remove_bookmark : R.string.quran_add_bookmark,
                ayah.getAyahNumber()));
    }

    /** Repaints saved icons after returning to the saved-marks screen. */
    public void refreshBookmarks() {
        notifyDataSetChanged();
    }

    /** Applies a locally persisted accessible size without replacing the current reading list. */
    public void setTextSizeSp(int textSizeSp) {
        if (this.textSizeSp != textSizeSp) {
            this.textSizeSp = textSizeSp;
            notifyDataSetChanged();
        }
    }

    @NonNull
    public QuranAyah getItem(int position) {
        return items.get(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
