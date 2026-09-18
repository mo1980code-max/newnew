package org.Allah_Clock_Live_Wallpaper.activity;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.adapter.AthkarAdapter;
import org.Allah_Clock_Live_Wallpaper.model.AthkarItem;
import org.Allah_Clock_Live_Wallpaper.utils.AthkarRepository;
import org.Allah_Clock_Live_Wallpaper.utils.LocaleHelper;
import org.Allah_Clock_Live_Wallpaper.utils.PrayerWindow;
import org.Allah_Clock_Live_Wallpaper.utils.TinyDB;
import org.Allah_Clock_Live_Wallpaper.utils.UiCompat;

import java.util.List;

/**
 * Reader for the morning / evening athkar: rounded cream cards with an earthy taupe dock.
 *
 * <p>The top toolbar carries the window's identity — "أذكار الصباح" in sky blue with a sun,
 * "أذكار المساء" in deep blue with a moon — and its three reading switches: night reading,
 * the text size (ض) and the card colour. Each dhikr's card does the counting (a tap does one
 * count, a finished dhikr settles into the muted completed tones), and its dock shares the
 * dhikr with one tap.</p>
 *
 * <p>It recomputes the window on entry so a stale badge can never show the wrong list. The
 * wallpaper badge and the athkar chip only appear inside the matching window, so they always
 * land on the right set. The home screen's athkar tile is permanent, though, so it passes the
 * window it wants: outside both windows the closest set is opened with a short note saying so
 * (see {@link PrayerWindow#windowOrUpcoming}), and a caller that passes nothing still closes
 * the screen instead.</p>
 */
public class AthkarActivity extends AppCompatActivity {

    /** The window the caller wants opened when "now" is outside both windows. */
    private static final String EXTRA_WINDOW = "athkar_window";

    /** Preference keys for the toolbar's three reading switches. */
    private static final String PREF_BILINGUAL = "athkarBilingual";
    private static final String PREF_NIGHT = "athkarNightMode";
    private static final String PREF_FONT_SP = "athkarFontSp";
    private static final String PREF_PALETTE = "athkarPalette";

    /** The ض button cycles the Arabic type through this range. */
    private static final int FONT_MIN_SP = 18;
    private static final int FONT_MAX_SP = 26;
    private static final int FONT_STEP_SP = 2;
    private static final int FONT_DEFAULT_SP = 20;

    private TinyDB tinyDB;
    private AthkarAdapter adapter;
    private TextView outsideNote;
    private View root;
    private boolean night;
    private int fontSp;
    private int palette;
    private int window;

    /** Opens the reader for the window that is active now, or closes it when there is none. */
    @NonNull
    public static Intent createIntent(@NonNull Context context) {
        return new Intent(context, AthkarActivity.class);
    }

    /**
     * Opens the reader, falling back to {@code windowWhenOutside} when "now" is outside both
     * windows. Pass {@link PrayerWindow#windowOrUpcoming} to always show something.
     */
    @NonNull
    public static Intent createIntent(@NonNull Context context, int windowWhenOutside) {
        return new Intent(context, AthkarActivity.class).putExtra(EXTRA_WINDOW, windowWhenOutside);
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.tinyDB = new TinyDB(this);

        int activeWindow = PrayerWindow.currentWindow(this, this.tinyDB, System.currentTimeMillis());
        boolean outsideWindow = activeWindow == PrayerWindow.NONE;
        int requested = getIntent().getIntExtra(EXTRA_WINDOW, PrayerWindow.NONE);
        if (outsideWindow) {
            if (requested == PrayerWindow.NONE) {
                finish();
                return;
            }
            activeWindow = requested;
        }
        this.window = activeWindow;
        List<AthkarItem> items = AthkarRepository.forWindow(this, this.window);
        if (items.isEmpty()) {
            finish();
            return;
        }

        this.night = this.tinyDB.getBoolean(PREF_NIGHT, false);
        this.fontSp = Math.max(FONT_MIN_SP, Math.min(FONT_MAX_SP,
                this.tinyDB.getInt(PREF_FONT_SP, FONT_DEFAULT_SP)));
        this.palette = this.tinyDB.getInt(PREF_PALETTE, 0);

        setContentView(R.layout.activity_athkar);
        UiCompat.applyEdgeToEdge(this);

        this.root = findViewById(R.id.athkarRoot);
        this.outsideNote = findViewById(R.id.athkarOutsideNote);
        // applyEdgeToEdge paints the window white for the list screens; this page needs its
        // own tone behind the status bar, or the strip above the top bar changes colour.
        applyScreenTheme();

        // The bar's identity: sky blue with a sun for the morning, deep blue with a moon for
        // the evening.
        View bar = findViewById(R.id.athkarBar);
        bar.setBackgroundColor(ContextCompat.getColor(this, this.window == PrayerWindow.MORNING
                ? R.color.athkarBarMorning : R.color.athkarBarEvening));
        ImageView windowIcon = findViewById(R.id.athkarWindowIcon);
        boolean evening = this.window == PrayerWindow.EVENING;
        windowIcon.setImageResource(evening ? R.drawable.ic_moon : R.drawable.ic_sun);
        findViewById(R.id.athkarCloudIcon).setVisibility(evening ? View.VISIBLE : View.GONE);

        TextView title = findViewById(R.id.athkarTitle);
        title.setText(this.window == PrayerWindow.MORNING
                ? R.string.athkar_morning_title : R.string.athkar_evening_title);

        TextView count = findViewById(R.id.athkarCount);
        count.setText(getString(R.string.athkar_count, items.size()));

        findViewById(R.id.athkarBack).setOnClickListener(view -> finish());
        findViewById(R.id.athkarNight).setOnClickListener(view -> toggleNight());
        findViewById(R.id.athkarFontButton).setOnClickListener(view -> cycleFont());
        findViewById(R.id.athkarPalette).setOnClickListener(view -> cyclePalette());
        findViewById(R.id.athkarSettings).setOnClickListener(view -> showSettingsDialog());

        if (outsideWindow) {
            this.outsideNote.setText(getString(R.string.athkar_outside_window,
                    getString(this.window == PrayerWindow.MORNING
                            ? R.string.athkar_morning_title : R.string.athkar_evening_title)));
            this.outsideNote.setVisibility(View.VISIBLE);
        }

        RecyclerView list = findViewById(R.id.athkarList);
        list.setLayoutManager(new LinearLayoutManager(this));
        // Strict single language: the app locale decides whether a row is Arabic or English.
        // The dual-language display only happens when the reader switched it on themselves.
        this.adapter = new AthkarAdapter(items, LocaleHelper.isArabic(this),
                this.tinyDB.getBoolean(PREF_BILINGUAL, false), this.night, this.fontSp,
                this.palette, this::shareDhikr);
        list.setAdapter(this.adapter);
    }

    // ═══════════════════════════════ the toolbar's switches ═══════════════════════════════

    /** Night reading on / off: the page darkens in place, the cards repaint with it. */
    private void toggleNight() {
        this.night = !this.night;
        this.tinyDB.putBoolean(PREF_NIGHT, this.night);
        applyScreenTheme();
        if (this.adapter != null) {
            this.adapter.setNight(this.night);
        }
        Toast.makeText(this, this.night ? R.string.athkar_night_on : R.string.athkar_night_off,
                Toast.LENGTH_SHORT).show();
    }

    /** The ض button: one step up the type range, wrapping back to the start at the top. */
    private void cycleFont() {
        this.fontSp = this.fontSp + FONT_STEP_SP > FONT_MAX_SP ? FONT_MIN_SP
                : this.fontSp + FONT_STEP_SP;
        this.tinyDB.putInt(PREF_FONT_SP, this.fontSp);
        if (this.adapter != null) {
            this.adapter.setTextSizeSp(this.fontSp);
        }
        Toast.makeText(this, getString(R.string.athkar_font_size, this.fontSp),
                Toast.LENGTH_SHORT).show();
    }

    /** The palette button: the next card tint, wrapping after the last one. */
    private void cyclePalette() {
        this.palette = (this.palette + 1) % AthkarAdapter.PALETTE_COUNT;
        this.tinyDB.putInt(PREF_PALETTE, this.palette);
        if (this.adapter != null) {
            this.adapter.setPalette(this.palette);
        }
        Toast.makeText(this, R.string.athkar_palette_changed, Toast.LENGTH_SHORT).show();
    }

    /** Paints the page's surfaces from the active theme. */
    private void applyScreenTheme() {
        int paper = this.night ? R.color.athkarNightPaper : R.color.athkarPaper;
        this.root.setBackgroundColor(ContextCompat.getColor(this, paper));
        getWindow().setBackgroundDrawableResource(paper);
        if (this.outsideNote != null) {
            this.outsideNote.setTextColor(ContextCompat.getColor(this, this.night
                    ? R.color.athkarNightMuted : R.color.athkarMuted));
        }
    }

    // ══════════════════════════════════════ sharing ══════════════════════════════════════

    /** The dock's share button: the dhikr's text, with the translation for a Latin reader. */
    private void shareDhikr(@NonNull AthkarItem item) {
        StringBuilder text = new StringBuilder(item.getArabicText());
        String translation = item.getEnglishText();
        if (!LocaleHelper.isArabic(this) && !translation.isEmpty()) {
            text.append('\n').append('\n').append(translation);
        }
        Intent send = new Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, text.toString());
        startActivity(Intent.createChooser(send, getString(R.string.athkar_share)));
    }

    // ═══════════════════════════════ timing settings ═══════════════════════════════

    private void showSettingsDialog() {
        View content = getLayoutInflater().inflate(R.layout.dialog_athkar_settings, null);

        RadioGroup mode = content.findViewById(R.id.rgMode);
        RadioButton rbAuto = content.findViewById(R.id.rbAuto);
        RadioButton rbFixed = content.findViewById(R.id.rbFixed);
        RadioGroup angle = content.findViewById(R.id.rgAngle);
        RadioButton rb15 = content.findViewById(R.id.rbAngle15);
        RadioButton rb18 = content.findViewById(R.id.rbAngle18);
        CheckBox bilingual = content.findViewById(R.id.cbBilingual);
        bilingual.setChecked(this.adapter != null && this.adapter.isDualLanguage());

        rbFixed.setChecked(this.tinyDB.getBoolean("athkarFixedTimes", false));
        rbAuto.setChecked(!rbFixed.isChecked());
        if (this.tinyDB.getInt("fajrAngle", 15) == 18) {
            rb18.setChecked(true);
        } else {
            rb15.setChecked(true);
        }

        // The four window slots are staged here and written to TinyDB only by the OK button, so
        // Cancel leaves the saved windows exactly as they were. They used to be written inside the
        // TimePickerDialog callback - i.e. before the reader had decided anything - and Cancel
        // could not take that back (UPGRADE_NOTES section 23).
        final String[] slotKeys = {"morningStartMin", "morningEndMin", "eveningStartMin", "eveningEndMin"};
        final int[] slotFallbacks = {270, 540, 960, 1230};
        final int[] staged = new int[slotKeys.length];
        final TextView[] slots = {
                content.findViewById(R.id.tMorningStart),
                content.findViewById(R.id.tMorningEnd),
                content.findViewById(R.id.tEveningStart),
                content.findViewById(R.id.tEveningEnd),
        };
        for (int i = 0; i < slotKeys.length; i++) {
            staged[i] = this.tinyDB.getInt(slotKeys[i], slotFallbacks[i]);
            slots[i].setText(minutesLabel(staged[i]));
            final int slot = i;
            slots[slot].setOnClickListener(v -> pickTime(slots[slot], staged, slot));
        }

        new AlertDialog.Builder(this)
                .setView(content)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    this.tinyDB.putBoolean("athkarFixedTimes", rbFixed.isChecked());
                    this.tinyDB.putInt("fajrAngle", rb18.isChecked() ? 18 : 15);
                    this.tinyDB.putBoolean(PREF_BILINGUAL, bilingual.isChecked());
                    // Nothing about the window reaches storage before this point, so Cancel or a
                    // tap outside the dialog discards a picked time along with the rest.
                    for (int i = 0; i < slotKeys.length; i++) {
                        this.tinyDB.putInt(slotKeys[i], staged[i]);
                    }
                    if (this.adapter != null
                            && this.adapter.isDualLanguage() != bilingual.isChecked()) {
                        this.adapter.setDualLanguage(bilingual.isChecked());
                        Toast.makeText(this, bilingual.isChecked()
                                        ? R.string.athkar_bilingual_on
                                        : R.string.athkar_bilingual_off,
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /**
     * Picks one window slot. The choice is staged in {@code staged} and shown on the chip only:
     * the dialog's OK button is what writes the slots to TinyDB, so Cancel discards the change
     * instead of leaving a half-saved window behind.
     */
    private void pickTime(final TextView target, final int[] staged, final int slot) {
        int current = staged[slot];
        new TimePickerDialog(this, (view, hour, minute) -> {
            staged[slot] = hour * 60 + minute;
            target.setText(minutesLabel(staged[slot]));
        }, current / 60, current % 60, true).show();
    }

    /** Follows the UI locale, so an Arabic UI reads the slots exactly like the time picker. */
    private String minutesLabel(int totalMinutes) {
        return String.format(LocaleHelper.uiLocale(this), "%02d:%02d",
                totalMinutes / 60, totalMinutes % 60);
    }
}
