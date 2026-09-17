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
 * Reader for the morning / evening athkar, as a classic paper page.
 *
 * <p>It recomputes the window on entry so a stale badge can never show the wrong list. The
 * wallpaper badge and the athkar chip only appear inside the matching window, so they always
 * land on the right set. The home screen's athkar tile is permanent, though, so it passes the
 * window it wants: outside both windows the closest set is opened with a short note saying so
 * (see {@link PrayerWindow#windowOrUpcoming}), and a caller that passes nothing still closes
 * the screen instead.</p>
 *
 * <p>An opaque page rather than a sheet floating over the live wallpaper: the wallpaper is a
 * mosque photograph, and behind 31 athkar of dense vowel-marked text it competed with the
 * reading instead of framing it. The page is ink on paper with no accent colours, closed by
 * an explicit button (there is no "tap outside" area left to tap).</p>
 */
public class AthkarActivity extends AppCompatActivity {

    /** The window the caller wants opened when "now" is outside both windows. */
    private static final String EXTRA_WINDOW = "athkar_window";

    /** Preference key: show the Arabic text together with the translation. */
    private static final String PREF_BILINGUAL = "athkarBilingual";

    private TinyDB tinyDB;
    private AthkarAdapter adapter;

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

        int window = PrayerWindow.currentWindow(this, this.tinyDB, System.currentTimeMillis());
        boolean outsideWindow = window == PrayerWindow.NONE;
        int requested = getIntent().getIntExtra(EXTRA_WINDOW, PrayerWindow.NONE);
        if (outsideWindow) {
            if (requested == PrayerWindow.NONE) {
                finish();
                return;
            }
            window = requested;
        }
        List<AthkarItem> items = AthkarRepository.forWindow(this, window);
        if (items.isEmpty()) {
            finish();
            return;
        }

        setContentView(R.layout.activity_athkar);
        UiCompat.applyEdgeToEdge(this);
        // applyEdgeToEdge paints the window white for the list screens; the paper page needs
        // its own tone behind the status bar, or the strip above the top bar changes colour.
        getWindow().setBackgroundDrawableResource(R.color.athkarPaper);

        TextView title = findViewById(R.id.athkarTitle);
        title.setText(window == PrayerWindow.MORNING
                ? R.string.athkar_morning_title
                : R.string.athkar_evening_title);

        TextView count = findViewById(R.id.athkarCount);
        count.setText(getString(R.string.athkar_count, items.size()));

        findViewById(R.id.athkarClose).setOnClickListener(view -> finish());
        ImageView settings = findViewById(R.id.athkarSettings);
        settings.setOnClickListener(view -> showSettingsDialog());

        TextView outsideNote = findViewById(R.id.athkarOutsideNote);
        if (outsideWindow) {
            outsideNote.setText(getString(R.string.athkar_outside_window,
                    getString(window == PrayerWindow.MORNING
                            ? R.string.athkar_morning_title
                            : R.string.athkar_evening_title)));
            outsideNote.setVisibility(View.VISIBLE);
        }

        RecyclerView list = findViewById(R.id.athkarList);
        list.setLayoutManager(new LinearLayoutManager(this));
        // Strict single language: the app locale decides whether a row is Arabic or English.
        // The dual-language display only happens when the reader switched it on themselves.
        this.adapter = new AthkarAdapter(items, LocaleHelper.isArabic(this),
                this.tinyDB.getBoolean(PREF_BILINGUAL, false));
        list.setAdapter(this.adapter);
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

        final TextView morningStart = content.findViewById(R.id.tMorningStart);
        final TextView morningEnd = content.findViewById(R.id.tMorningEnd);
        final TextView eveningStart = content.findViewById(R.id.tEveningStart);
        final TextView eveningEnd = content.findViewById(R.id.tEveningEnd);
        morningStart.setText(minutesLabel(this.tinyDB.getInt("morningStartMin", 270)));
        morningEnd.setText(minutesLabel(this.tinyDB.getInt("morningEndMin", 540)));
        eveningStart.setText(minutesLabel(this.tinyDB.getInt("eveningStartMin", 960)));
        eveningEnd.setText(minutesLabel(this.tinyDB.getInt("eveningEndMin", 1230)));

        morningStart.setOnClickListener(v -> pickTime(morningStart, "morningStartMin", 270));
        morningEnd.setOnClickListener(v -> pickTime(morningEnd, "morningEndMin", 540));
        eveningStart.setOnClickListener(v -> pickTime(eveningStart, "eveningStartMin", 960));
        eveningEnd.setOnClickListener(v -> pickTime(eveningEnd, "eveningEndMin", 1230));

        new AlertDialog.Builder(this)
                .setView(content)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    this.tinyDB.putBoolean("athkarFixedTimes", rbFixed.isChecked());
                    this.tinyDB.putInt("fajrAngle", rb18.isChecked() ? 18 : 15);
                    this.tinyDB.putBoolean(PREF_BILINGUAL, bilingual.isChecked());
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

    private void pickTime(final TextView target, final String prefKey, int fallback) {
        int current = this.tinyDB.getInt(prefKey, fallback);
        new TimePickerDialog(this, (view, hour, minute) -> {
            this.tinyDB.putInt(prefKey, hour * 60 + minute);
            target.setText(minutesLabel(hour * 60 + minute));
        }, current / 60, current % 60, true).show();
    }

    /** Follows the UI locale, so an Arabic UI reads the slots exactly like the time picker. */
    private String minutesLabel(int totalMinutes) {
        return String.format(LocaleHelper.uiLocale(this), "%02d:%02d",
                totalMinutes / 60, totalMinutes % 60);
    }
}
