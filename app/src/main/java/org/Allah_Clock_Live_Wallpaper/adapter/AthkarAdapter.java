package org.Allah_Clock_Live_Wallpaper.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.model.AthkarItem;
import org.Allah_Clock_Live_Wallpaper.utils.UiMotion;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Rows for the morning / evening reader.
 *
 * <p>Each row is a rounded cream card: the dhikr's Arabic text (with transliteration and
 * translation for a Latin reader), and a bottom taupe dock holding the share button and the
 * remaining-repeat badge. Tapping the card does one count; the count lives in memory for the
 * session and starts full each time the reader opens, so nothing is persisted. A finished
 * dhikr is painted in the muted completed tones: the card settles to #E8E5DF, the dock to
 * #8E8880 and the card text to #8A857F (with darker equivalents in night reading).</p>
 */
public final class AthkarAdapter extends RecyclerView.Adapter<AthkarAdapter.ViewHolder> {

    /** The dock's share button: routes the dhikr to the host, which builds the share intent. */
    public interface ShareListener {
        void onShare(@NonNull AthkarItem item);
    }

    /** The card's tint palettes, day and night, cycled by the toolbar's palette button. */
    public static final int PALETTE_COUNT = 4;
    private static final int[] DAY_PALETTE = {
            R.color.athkarPalette1, R.color.athkarPalette2, R.color.athkarPalette3,
            R.color.athkarPalette4
    };
    private static final int[] NIGHT_PALETTE = {
            R.color.athkarPalette1Night, R.color.athkarPalette2Night,
            R.color.athkarPalette3Night, R.color.athkarPalette4Night
    };

    public static final class ViewHolder extends RecyclerView.ViewHolder {
        final androidx.cardview.widget.CardView card;
        final TextView index;
        final TextView progress;
        final TextView done;
        final TextView arabic;
        final TextView transliteration;
        final TextView english;
        final TextView virtue;
        final TextView reference;
        final View dock;
        final TextView shareLabel;
        final FrameLayout shareButton;
        final TextView repeatLabel;
        final TextView count;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.athkarCard);
            index = itemView.findViewById(R.id.athkarIndex);
            progress = itemView.findViewById(R.id.athkarProgress);
            done = itemView.findViewById(R.id.athkarDone);
            arabic = itemView.findViewById(R.id.athkarArabic);
            transliteration = itemView.findViewById(R.id.athkarTransliteration);
            english = itemView.findViewById(R.id.athkarEnglish);
            virtue = itemView.findViewById(R.id.athkarVirtue);
            reference = itemView.findViewById(R.id.athkarReference);
            dock = itemView.findViewById(R.id.athkarDock);
            shareLabel = itemView.findViewById(R.id.athkarShareLabel);
            shareButton = itemView.findViewById(R.id.athkarShareButton);
            repeatLabel = itemView.findViewById(R.id.athkarRepeatLabel);
            count = itemView.findViewById(R.id.athkarCount);
        }
    }

    private final List<AthkarItem> items;
    private final boolean arabicUi;
    private boolean dualLanguage;
    private final Map<Integer, Integer> left = new HashMap<>();
    private final ShareListener shareListener;
    private boolean night;
    private float textSizeSp;
    private int palette;

    public AthkarAdapter(@NonNull List<AthkarItem> items, boolean arabicUi,
                         boolean dualLanguage, boolean night, float textSizeSp, int palette,
                         @NonNull ShareListener shareListener) {
        this.items = items;
        this.arabicUi = arabicUi;
        this.dualLanguage = dualLanguage;
        this.night = night;
        this.textSizeSp = textSizeSp;
        this.palette = clampPalette(palette);
        this.shareListener = shareListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_athkar, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AthkarItem item = items.get(position);
        int key = item.getId();
        int repeat = item.getRepeat();
        int current = this.left.getOrDefault(key, repeat);

        boolean showArabic = this.arabicUi || this.dualLanguage;
        boolean showEnglish = !this.arabicUi || this.dualLanguage;
        // Strict language separation: Arabic mode is Arabic only; Latin mode shows the
        // translation and transliteration; dual mode deliberately shows both.
        holder.arabic.setVisibility(showArabic ? View.VISIBLE : View.GONE);
        holder.transliteration.setVisibility(showEnglish && !item.getTransliteration().isEmpty()
                ? View.VISIBLE : View.GONE);
        holder.english.setVisibility(showEnglish ? View.VISIBLE : View.GONE);

        holder.index.setText(holder.itemView.getContext().getString(R.string.athkar_position,
                position + 1, this.items.size()));
        holder.progress.setText(holder.itemView.getContext().getString(R.string.athkar_progress,
                repeat - current, repeat));
        holder.done.setVisibility(current == 0 ? View.VISIBLE : View.GONE);

        holder.arabic.setText(item.getArabicText());
        holder.arabic.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, this.textSizeSp);
        String transliteration = item.getTransliteration();
        holder.transliteration.setText(transliteration);
        holder.english.setText(item.getEnglishText());
        String virtue = item.getVirtue(this.arabicUi);
        holder.virtue.setText(virtue);
        holder.virtue.setVisibility(virtue.isEmpty() ? View.GONE : View.VISIBLE);
        String reference = item.getReference(this.arabicUi);
        holder.reference.setText(reference);
        holder.reference.setVisibility(reference.isEmpty() ? View.GONE : View.VISIBLE);
        holder.count.setText(String.valueOf(current));

        // Tapping the card does one count — the whole card is the counter.
        holder.card.setOnClickListener(view -> countDown(holder, item));
        // The share button only shares; it never counts a dhikr.
        holder.shareButton.setOnClickListener(view -> this.shareListener.onShare(item));

        paint(holder, current);
    }

    /** One count of one dhikr; a finished dhikr is inert until the reader opens again. */
    private void countDown(@NonNull ViewHolder holder, @NonNull AthkarItem item) {
        int key = item.getId();
        int repeat = item.getRepeat();
        int current = this.left.getOrDefault(key, repeat);
        if (current <= 0) {
            return;
        }
        int remaining = current - 1;
        this.left.put(key, remaining);
        // The same very short, gentle tick the tasbeeh uses: one count, one bead.
        UiMotion.tick(holder.itemView);
        holder.progress.setText(holder.itemView.getContext().getString(R.string.athkar_progress,
                repeat - remaining, repeat));
        holder.count.setText(String.valueOf(remaining));
        holder.done.setVisibility(remaining == 0 ? View.VISIBLE : View.GONE);
        paint(holder, remaining);
    }

    /**
     * Paints one card from its state: fresh (the active palette and the taupe dock) or
     * completed (the muted done tones the design specifies).
     */
    private void paint(@NonNull ViewHolder holder, int remaining) {
        boolean done = remaining <= 0;
        android.content.Context context = holder.itemView.getContext();

        holder.card.setCardBackgroundColor(ContextCompat.getColor(context,
                done ? (this.night ? R.color.athkarNightCardDone : R.color.athkarCardDone)
                     : (this.night ? NIGHT_PALETTE[this.palette] : DAY_PALETTE[this.palette])));
        holder.dock.setBackgroundColor(ContextCompat.getColor(context,
                done ? (this.night ? R.color.athkarNightDockDone : R.color.athkarDockDone)
                     : (this.night ? R.color.athkarNightDock : R.color.athkarDockTaupe)));

        // The card's text: ink on a fresh card, the muted done tone once finished.
        holder.arabic.setTextColor(ContextCompat.getColor(context,
                done ? (this.night ? R.color.athkarNightDoneText : R.color.athkarDoneText)
                     : (this.night ? R.color.athkarNightInk : R.color.athkarInk)));
        int bodyColor = ContextCompat.getColor(context,
                done ? (this.night ? R.color.athkarNightDoneText : R.color.athkarDoneText)
                     : (this.night ? R.color.athkarNightBody : R.color.athkarBody));
        holder.transliteration.setTextColor(bodyColor);
        holder.english.setTextColor(bodyColor);
        int mutedColor = ContextCompat.getColor(context,
                done ? (this.night ? R.color.athkarNightDoneText : R.color.athkarDoneText)
                     : (this.night ? R.color.athkarNightMuted : R.color.athkarMuted));
        holder.virtue.setTextColor(mutedColor);
        holder.index.setTextColor(mutedColor);
        holder.reference.setTextColor(mutedColor);
        holder.progress.setTextColor(mutedColor);
        holder.done.setTextColor(mutedColor);

        // The dock labels sit on the taupe: white while fresh, a soft stone once done.
        int dockLabel = done
                ? (this.night ? ContextCompat.getColor(context, R.color.athkarNightDoneText)
                              : 0xFFDAD6CF)
                : 0xFFFFFFFF;
        holder.shareLabel.setTextColor(dockLabel);
        holder.repeatLabel.setTextColor(dockLabel);
        // The counter number sits on its white badge, so it stays dark ink.
        holder.count.setTextColor(ContextCompat.getColor(context,
                this.night ? R.color.athkarNightInk : R.color.athkarInk));
    }

    private static int clampPalette(int palette) {
        return Math.max(0, Math.min(PALETTE_COUNT - 1, palette));
    }

    /** The reader's type size moved: rebind so every Arabic block takes the new size. */
    public void setTextSizeSp(float textSizeSp) {
        this.textSizeSp = textSizeSp;
        notifyDataSetChanged();
    }

    /** The card colour moved: rebind the whole list in place. */
    public void setPalette(int palette) {
        this.palette = clampPalette(palette);
        notifyDataSetChanged();
    }

    /** Night reading toggled: repaint every card, keeping each session's counts. */
    public void setNight(boolean night) {
        this.night = night;
        notifyDataSetChanged();
    }

    public boolean isDualLanguage() {
        return this.dualLanguage;
    }

    /** The reader's dual-language switch moved: rebind, keeping each session's counts. */
    public void setDualLanguage(boolean dualLanguage) {
        this.dualLanguage = dualLanguage;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
