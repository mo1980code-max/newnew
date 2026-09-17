package org.Allah_Clock_Live_Wallpaper.adapter;

import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.utils.QuranPageBuilder;
import org.Allah_Clock_Live_Wallpaper.utils.QuranPaginator;
import org.Allah_Clock_Live_Wallpaper.utils.QuranTheme;
import org.Allah_Clock_Live_Wallpaper.viewUtils.QuranPageView;

import java.util.List;

/**
 * The flipped pages of one surah: one {@link QuranPageView} per screen-sized slice, driven by
 * the {@code ViewPager2} the reader hosts.
 *
 * <p>The screens and the text they point at come from {@link QuranPaginator}, so this adapter
 * never decides where a page breaks — it only draws what it was measured.</p>
 */
public final class QuranScreenAdapter extends RecyclerView.Adapter<QuranScreenAdapter.Holder> {

    private final CharSequence text;
    private final List<QuranPageBuilder.Verse> verses;
    private final List<QuranPaginator.Screen> screens;
    private final TextPaint paint;
    private final QuranTheme theme;
    private final float lineSpacingExtra;
    private final QuranPageView.Listener listener;

    public QuranScreenAdapter(@NonNull CharSequence text,
                              @NonNull List<QuranPageBuilder.Verse> verses,
                              @NonNull List<QuranPaginator.Screen> screens,
                              @NonNull TextPaint paint, @NonNull QuranTheme theme,
                              float lineSpacingExtra, @NonNull QuranPageView.Listener listener) {
        this.text = text;
        this.verses = verses;
        this.screens = screens;
        this.paint = paint;
        this.theme = theme;
        this.lineSpacingExtra = lineSpacingExtra;
        this.listener = listener;
        setHasStableIds(true);
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final QuranPageView page;

        Holder(@NonNull View itemView) {
            super(itemView);
            this.page = itemView.findViewById(R.id.quranPageView);
        }
    }

    @Override
    public long getItemId(int position) {
        QuranPaginator.Screen screen = this.screens.get(position);
        // Start offset * 1e6 + end: distinct slices always give distinct ids, and the arithmetic
        // is done in long so a full surah (hundreds of thousands of characters) cannot overflow.
        return (long) screen.start * 1000000L + screen.end;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quran_screen, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        QuranPaginator.Screen screen = this.screens.get(position);
        holder.page.bind(this.text, screen.start, screen.end, this.verses, this.paint,
                this.theme, this.lineSpacingExtra, this.listener);
    }

    /**
     * Repaints after a bookmark toggled. The spans live in the shared text, so the page views
     * only have to be rebound — the pagination itself stays valid and no page break moves.
     */
    public void refreshBookmarks() {
        notifyItemRangeChanged(0, getItemCount());
    }

    @Override
    public int getItemCount() {
        return this.screens.size();
    }
}
