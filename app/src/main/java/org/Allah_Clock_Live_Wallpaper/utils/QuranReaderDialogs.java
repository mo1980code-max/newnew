package org.Allah_Clock_Live_Wallpaper.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.model.QuranAyah;
import org.Allah_Clock_Live_Wallpaper.model.QuranSurah;

/**
 * The two dialogs both Quran readers share: the reading-background picker and the explanation of
 * an ayah of prostration.
 *
 * <p>They live here rather than in each activity so that the surah reader and the 604-page Mushaf
 * offer exactly the same six washes in exactly the same order, and quote a prostration ayah in
 * exactly the same words. Both read their colours from the active {@link QuranTheme}, so a dialog
 * opened over the midnight wash reads in midnight's own ink.</p>
 */
public final class QuranReaderDialogs {

    private QuranReaderDialogs() {
    }

    /** The reader's answer to the wash picker. */
    public interface BackgroundPicked {
        void onPicked(int style);
    }

    /**
     * Lists the six reading washes with a swatch of each one's own background drawable, and marks
     * the wash the reader is already in. The picker never persists anything itself: the caller
     * owns {@link QuranStore} and decides what to save.
     */
    public static void showBackgroundPicker(@NonNull Context context, @NonNull QuranTheme theme,
                                           @NonNull final BackgroundPicked callback) {
        ScrollView scroll = new ScrollView(context);
        LinearLayout column = new LinearLayout(context);
        column.setOrientation(LinearLayout.VERTICAL);
        int pad = UiCompat.dp(context, 12f);
        column.setPadding(pad, UiCompat.dp(context, 6f), pad, pad);

        for (final int style : QuranTheme.STYLES) {
            boolean active = style == theme.getStyle();
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(UiCompat.dp(context, 8f), UiCompat.dp(context, 8f),
                    UiCompat.dp(context, 8f), UiCompat.dp(context, 8f));
            row.setBackgroundResource(R.drawable.bg_quran_pick_row);
            row.setClickable(true);
            row.setFocusable(true);

            // The swatch is the wash's real background drawable, not an approximation of it.
            View swatch = new View(context);
            Drawable wash = ContextCompat.getDrawable(context, QuranTheme.backgroundOf(style));
            if (wash != null) {
                swatch.setBackground(wash);
            }
            LinearLayout.LayoutParams swatchLayout =
                    new LinearLayout.LayoutParams(UiCompat.dp(context, 54f),
                            UiCompat.dp(context, 38f));
            swatchLayout.setMarginEnd(UiCompat.dp(context, 14f));
            swatch.setLayoutParams(swatchLayout);
            row.addView(swatch);

            TextView name = new TextView(context);
            name.setText(QuranTheme.nameOf(style));
            name.setTextSize(16f);
            name.setTextColor(theme.ink);
            name.setTypeface(active ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            row.addView(name, new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            TextView tick = new TextView(context);
            tick.setText(active ? "\u2713" : "");
            tick.setTextSize(17f);
            tick.setTextColor(theme.gold);
            tick.setTypeface(Typeface.DEFAULT_BOLD);
            row.addView(tick);

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callback.onPicked(style);
                }
            });
            column.addView(row);
        }

        scroll.addView(column);
        new AlertDialog.Builder(context)
                .setTitle(R.string.quran_background_title)
                .setView(scroll)
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /**
     * Explains one ayah of prostration: its number, whether it is obligatory or recommended, its
     * place in the Mushaf and its own text, then offers to scroll the reader to it.
     *
     * <p>The ayah text is quoted through {@link QuranText#withAyahNumber} so it carries the same
     * end-of-ayah marker it carries on the page and — being one of the fifteen — the mihrab
     * marker beside it.</p>
     */
    public static void showSajdah(@NonNull Context context, @NonNull QuranTheme theme,
                                  @NonNull QuranAyah ayah, @Nullable QuranSurah surah,
                                  @Nullable final Runnable goToAyah) {
        float density = context.getResources().getDisplayMetrics().density;
        int markerColor = theme.isNight() ? theme.gold : AyahNumberSpan.PRIMARY_GREEN;

        SpannableStringBuilder body = new SpannableStringBuilder();
        body.append(context.getString(R.string.quran_sajdah_ayah, ayah.getSurahNumber(),
                ayah.getAyahNumber(), ayah.getMushafPage()));
        body.append('\n');
        body.append(ayah.isSajdahObligatory()
                ? context.getString(R.string.quran_sajdah_obligatory)
                : context.getString(R.string.quran_sajdah_recommended));
        if (surah != null) {
            body.append('\n');
            body.append(surah.getArabicName());
        }
        body.append("\n\n");
        body.append(QuranText.withAyahNumber(ayah.getText(), ayah.getAyahNumber(), density,
                markerColor, ayah.getSajdahNumber(), theme.gold));
        body.append("\n\n");
        body.append(context.getString(R.string.quran_sajdah_howto));

        TextView text = new TextView(context);
        text.setText(body);
        text.setTextSize(15f);
        text.setTextColor(theme.body);
        text.setLineSpacing(UiCompat.dp(context, 4f), 1f);
        text.setTextDirection(View.TEXT_DIRECTION_RTL);

        int pad = UiCompat.dp(context, 20f);
        ScrollView scroll = new ScrollView(context);
        scroll.addView(text);
        scroll.setPadding(pad, pad, pad, pad);
        scroll.setClipToPadding(false);

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.quran_sajdah_title, ayah.getSajdahNumber()))
                .setView(scroll)
                .setNegativeButton(R.string.ok, null);
        if (goToAyah != null) {
            builder.setPositiveButton(R.string.quran_sajdah_go,
                    (dialog, which) -> goToAyah.run());
        }
        builder.show();
    }
}
