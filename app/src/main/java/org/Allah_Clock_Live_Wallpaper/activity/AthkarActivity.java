package org.Allah_Clock_Live_Wallpaper.activity;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

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
 * <p>It can only be opened while the matching window is active (the badge that leads here is
 * hidden otherwise), and it recomputes the window on entry so a stale badge can never show
 * the wrong list.</p>
 *
 * <p>An opaque page rather than a sheet floating over the live wallpaper: the wallpaper is a
 * mosque photograph, and behind 31 athkar of dense vowel-marked text it competed with the
 * reading instead of framing it. The page is ink on paper with no accent colours, closed by
 * an explicit button (there is no "tap outside" area left to tap).</p>
 */
public class AthkarActivity extends AppCompatActivity {

    private TinyDB tinyDB;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.tinyDB = new TinyDB(this);

        int window = PrayerWindow.currentWindow(this, this.tinyDB, System.currentTimeMillis());
        if (window == PrayerWindow.NONE) {
            finish();
            return;
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

        RecyclerView list = findViewById(R.id.athkarList);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(new AthkarAdapter(items, LocaleHelper.isArabic(this), this));
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
