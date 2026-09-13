package org.Allah_Clock_Live_Wallpaper.adapter;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.model.AthkarItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Athkar reader cards.
 *
 * <p>Each card carries its own countdown: tapping the golden circle decrements it with a
 * gentle tick, and reaching zero fades the card and stamps it as completed. Progress is
 * deliberately kept in memory only — a dhikr session is a worship act, not user data.</p>
 */
public class AthkarAdapter extends RecyclerView.Adapter<AthkarAdapter.ViewHolder> {

    private final List<AthkarItem> items;
    private final Map<Integer, Integer> remaining = new HashMap<>();
    private final boolean arabicUi;
    private final Vibrator vibrator;

    public AthkarAdapter(List<AthkarItem> items, boolean arabicUi, Context context) {
        this.items = items;
        this.arabicUi = arabicUi;
        if (Build.VERSION.SDK_INT >= 31) {
            VibratorManager manager =
                    (VibratorManager) context.getApplicationContext()
                            .getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            this.vibrator = manager != null ? manager.getDefaultVibrator() : null;
        } else {
            this.vibrator = (Vibrator) context.getApplicationContext()
                    .getSystemService(Context.VIBRATOR_SERVICE);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final View card;
        private final TextView arabic;
        private final TextView transliteration;
        private final TextView english;
        private final TextView reference;
        private final TextView counter;
        private final TextView repeatInfo;
        private final TextView done;

        public ViewHolder(View view) {
            super(view);
            this.card = view.findViewById(R.id.athkarCard);
            this.arabic = view.findViewById(R.id.athkarArabic);
            this.transliteration = view.findViewById(R.id.athkarTransliteration);
            this.english = view.findViewById(R.id.athkarEnglish);
            this.reference = view.findViewById(R.id.athkarReference);
            this.counter = view.findViewById(R.id.athkarCounter);
            this.repeatInfo = view.findViewById(R.id.athkarRepeatInfo);
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
        holder.arabic.setText(item.getArabicText());
        // A transliteration only helps a reader who cannot read Arabic script, so an Arabic UI
        // drops it and keeps the English translation (the reader is bilingual by design).
        holder.transliteration.setText(item.getTransliteration());
        holder.transliteration.setVisibility(this.arabicUi ? View.GONE : View.VISIBLE);
        holder.english.setText(item.getEnglishText());
        holder.reference.setText(item.getReference(this.arabicUi));
        holder.repeatInfo.setText(holder.repeatInfo.getContext()
                .getString(R.string.athkar_repeat_total, item.getRepeat()));

        final int left = leftOf(item);
        paint(holder, item, left);

        holder.counter.setOnClickListener(view -> {
            int current = leftOf(item);
            if (current <= 0) {
                return;
            }
            current--;
            this.remaining.put(item.getId(), current);
            tick();
            paint(holder, item, current);
        });
    }

    private int leftOf(AthkarItem item) {
        Integer value = this.remaining.get(item.getId());
        return value == null ? item.getRepeat() : value;
    }

    private void paint(ViewHolder holder, AthkarItem item, int left) {
        holder.counter.setText(String.valueOf(left));
        boolean finished = left <= 0;
        holder.card.setAlpha(finished ? 0.45f : 1f);
        holder.done.setVisibility(finished ? View.VISIBLE : View.GONE);
        holder.counter.setEnabled(!finished);
    }

    /** The same very short, gentle tick the floating tasbeeh uses. */
    private void tick() {
        if (this.vibrator == null || !this.vibrator.hasVibrator()) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 29) {
            this.vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK));
        } else if (Build.VERSION.SDK_INT >= 26) {
            this.vibrator.vibrate(VibrationEffect.createOneShot(15L, 80));
        } else {
            this.vibrator.vibrate(15L);
        }
    }

    @Override
    public int getItemCount() {
        return this.items == null ? 0 : this.items.size();
    }
}
