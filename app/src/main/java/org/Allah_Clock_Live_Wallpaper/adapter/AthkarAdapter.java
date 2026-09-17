package org.Allah_Clock_Live_Wallpaper.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.model.AthkarItem;
import org.Allah_Clock_Live_Wallpaper.utils.UiMotion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Athkar reader rows on the paper page.
 *
 * <p>The whole row is the tap target — there is no counter circle to aim at. Each tap
 * decrements the number printed beside the dhikr with a gentle tick, and reaching zero fades
 * the row and stamps it as completed. Progress is deliberately kept in memory only: a dhikr
 * session is a worship act, not user data.</p>
 *
 * <p>The rows follow the layout of the athkar list they are transcribed from: the position in
 * the list and the remaining/total count on a small header line, then the dhikr with its count
 * beside it, the translation, the reward note ("من قالها حين يصبح ...") and the source. Some
 * athkar repeat 100 times, so the count is data-driven everywhere and never assumed small.</p>
 *
 * <p><b>One language at a time.</b> The dhikr content ships with its Arabic, its transliteration
 * and its English translation side by side, so the adapter is the one place that decides which of
 * them a row is allowed to show, and it follows the app locale: an Arabic UI reads Arabic text
 * with Arabic labels only, and an English UI reads the transliteration and translation instead of
 * the Arabic script - never both at once. Only the explicit dual-language switch in the reader's
 * settings (`athkarBilingual`) asks for the two together. Both flags are read per binding, and
 * {@link #setDualLanguage(boolean)} re-binds the list in place, so the counters the reader has
 * already worked through survive the switch.</p>
 */
public class AthkarAdapter extends RecyclerView.Adapter<AthkarAdapter.ViewHolder> {

    private final List<AthkarItem> items;
    private final Map<Integer, Integer> remaining = new HashMap<>();
    /** The app's language, read once per adapter: it decides the whole row. */
    private final boolean arabicUi;
    /** The reader asked for both languages at the same time; off unless they said so. */
    private boolean dualLanguage;

    public AthkarAdapter(List<AthkarItem> items, boolean arabicUi, boolean dualLanguage) {
        this.items = items;
        this.arabicUi = arabicUi;
        this.dualLanguage = dualLanguage;
    }

    /**
     * Turns the dual-language display on or off and re-binds the rows in place. The in-memory
     * repeat counters are deliberately kept: a reader who switches language mid-session must not
     * lose the dhikr they have already counted.
     */
    public void setDualLanguage(boolean dualLanguage) {
        if (this.dualLanguage == dualLanguage) {
            return;
        }
        this.dualLanguage = dualLanguage;
        notifyItemRangeChanged(0, getItemCount());
    }

    public boolean isDualLanguage() {
        return this.dualLanguage;
    }

    /** The Arabic script is shown in an Arabic UI, or when both languages were asked for. */
    boolean showsArabic() {
        return this.arabicUi || this.dualLanguage;
    }

    /** The transliteration and the translation are shown in an English UI, or in dual mode. */
    boolean showsEnglish() {
        return !this.arabicUi || this.dualLanguage;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final View card;
        private final TextView index;
        private final TextView progress;
        private final TextView count;
        private final TextView arabic;
        private final TextView transliteration;
        private final TextView english;
        private final TextView virtue;
        private final TextView reference;
        private final TextView done;

        public ViewHolder(View view) {
            super(view);
            this.card = view.findViewById(R.id.athkarCard);
            this.index = view.findViewById(R.id.athkarIndex);
            this.progress = view.findViewById(R.id.athkarProgress);
            this.count = view.findViewById(R.id.athkarCount);
            this.arabic = view.findViewById(R.id.athkarArabic);
            this.transliteration = view.findViewById(R.id.athkarTransliteration);
            this.english = view.findViewById(R.id.athkarEnglish);
            this.virtue = view.findViewById(R.id.athkarVirtue);
            this.reference = view.findViewById(R.id.athkarReference);
            this.done = view.findViewById(R.id.athkarDone);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_athkar, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final AthkarItem item = this.items.get(position);
        holder.index.setText(holder.index.getContext()
                .getString(R.string.athkar_position, position + 1, getItemCount()));
        // Strict language separation: the Arabic script and the English row never share the page
        // unless the reader explicitly asked for both.
        holder.arabic.setText(item.getArabicText());
        holder.arabic.setVisibility(showsArabic() ? View.VISIBLE : View.GONE);

        String transliteration = item.getTransliteration();
        holder.transliteration.setText(transliteration);
        holder.transliteration.setVisibility(
                showsEnglish() && !transliteration.isEmpty() ? View.VISIBLE : View.GONE);

        holder.english.setText(item.getEnglishText());
        holder.english.setVisibility(showsEnglish() ? View.VISIBLE : View.GONE);
        String virtue = item.getVirtue(this.arabicUi);
        holder.virtue.setText(virtue);
        holder.virtue.setVisibility(virtue.isEmpty() ? View.GONE : View.VISIBLE);
        String reference = item.getReference(this.arabicUi);
        holder.reference.setText(reference);
        holder.reference.setVisibility(reference.isEmpty() ? View.GONE : View.VISIBLE);

        final int left = leftOf(item);
        paint(holder, item, left);

        holder.card.setOnClickListener(view -> {
            int current = leftOf(item);
            if (current <= 0) {
                return;
            }
            current--;
            this.remaining.put(item.getId(), current);
            tick(view);
            paint(holder, item, current);
        });
    }

    private int leftOf(AthkarItem item) {
        Integer value = this.remaining.get(item.getId());
        return value == null ? item.getRepeat() : value;
    }

    private void paint(ViewHolder holder, AthkarItem item, int left) {
        int remaining = Math.max(left, 0);
        holder.count.setText(String.valueOf(remaining));
        holder.progress.setText(holder.progress.getContext()
                .getString(R.string.athkar_progress, remaining, item.getRepeat()));
        boolean finished = left <= 0;
        // Fading is the only state change the paper page allows itself: no colour, no badge.
        holder.card.setAlpha(finished ? 0.4f : 1f);
        holder.done.setVisibility(finished ? View.VISIBLE : View.GONE);
    }

    /** The same very short, gentle tick the floating tasbeeh uses; one implementation, in UiMotion. */
    private void tick(View source) {
        UiMotion.tick(source);
    }

    @Override
    public int getItemCount() {
        return this.items == null ? 0 : this.items.size();
    }
}
