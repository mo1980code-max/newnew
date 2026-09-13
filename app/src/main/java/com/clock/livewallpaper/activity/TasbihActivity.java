package com.clock.livewallpaper.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.clock.livewallpaper.R;
import com.clock.livewallpaper.model.OccasionItem;
import com.clock.livewallpaper.model.TasbihDayStat;
import com.clock.livewallpaper.utils.HijriCalendarHelper;
import com.clock.livewallpaper.utils.LocaleHelper;
import com.clock.livewallpaper.utils.OccasionDhikrManager;
import com.clock.livewallpaper.utils.TasbihManager;

import java.util.Calendar;
import java.util.List;

public class TasbihActivity extends AppCompatActivity {

    private TasbihManager tasbihManager;
    private boolean isArabic;

    // Header
    private ImageView btnBack;
    private ImageView btnHeaderShare;
    private TextView tvTasbihHeader;

    // Occasion Card
    private View cardOccasion;
    private TextView tvOccasionBadge;
    private TextView tvHijriDate;
    private TextView tvOccasionTitle;
    private TextView tvOccasionVirtue;
    private Button btnApplyOccasionDhikr;

    // Counter Section
    private LinearLayout layoutDhikrPills;
    private TextView tvCurrentDhikr;
    private FrameLayout btnTasbihTap;
    private TextView tvCountBig;
    private TextView tvTargetProgress;
    private TextView tvLapCount;
    private TextView tvTodaySummary;

    // Controls
    private Button btnTarget33;
    private Button btnTarget100;
    private Button btnTargetOpen;
    private ImageButton btnToggleVibe;
    private ImageButton btnResetCount;

    // Stats Section
    private TextView tvStatToday;
    private TextView tvStatWeek;
    private TextView tvStatMonth;
    private TextView tvStatStreak;
    private LinearLayout layoutWeeklyBars;
    private Button btnShareReport;
    private Button btnExportCsv;
    private ImageButton btnClearStats;

    // All Occasions Section
    private LinearLayout layoutOccasionsList;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasbih);

        this.tasbihManager = new TasbihManager(this);
        this.isArabic = LocaleHelper.isArabic(this);

        initViews();
        setupOccasionBanner();
        setupDhikrPills();
        updateCounterUI();
        setupTargetButtons();
        setupStatsUI();
        setupOccasionsList();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnHeaderShare = findViewById(R.id.btnHeaderShare);
        tvTasbihHeader = findViewById(R.id.tvTasbihHeader);

        cardOccasion = findViewById(R.id.cardOccasion);
        tvOccasionBadge = findViewById(R.id.tvOccasionBadge);
        tvHijriDate = findViewById(R.id.tvHijriDate);
        tvOccasionTitle = findViewById(R.id.tvOccasionTitle);
        tvOccasionVirtue = findViewById(R.id.tvOccasionVirtue);
        btnApplyOccasionDhikr = findViewById(R.id.btnApplyOccasionDhikr);

        layoutDhikrPills = findViewById(R.id.layoutDhikrPills);
        tvCurrentDhikr = findViewById(R.id.tvCurrentDhikr);
        btnTasbihTap = findViewById(R.id.btnTasbihTap);
        tvCountBig = findViewById(R.id.tvCountBig);
        tvTargetProgress = findViewById(R.id.tvTargetProgress);
        tvLapCount = findViewById(R.id.tvLapCount);
        tvTodaySummary = findViewById(R.id.tvTodaySummary);

        btnTarget33 = findViewById(R.id.btnTarget33);
        btnTarget100 = findViewById(R.id.btnTarget100);
        btnTargetOpen = findViewById(R.id.btnTargetOpen);
        btnToggleVibe = findViewById(R.id.btnToggleVibe);
        btnResetCount = findViewById(R.id.btnResetCount);

        tvStatToday = findViewById(R.id.tvStatToday);
        tvStatWeek = findViewById(R.id.tvStatWeek);
        tvStatMonth = findViewById(R.id.tvStatMonth);
        tvStatStreak = findViewById(R.id.tvStatStreak);
        layoutWeeklyBars = findViewById(R.id.layoutWeeklyBars);
        btnShareReport = findViewById(R.id.btnShareReport);
        btnExportCsv = findViewById(R.id.btnExportCsv);
        btnClearStats = findViewById(R.id.btnClearStats);

        layoutOccasionsList = findViewById(R.id.layoutOccasionsList);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnHeaderShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareTasbihReport();
            }
        });

        btnTasbihTap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTasbihTap();
            }
        });

        btnTarget33.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTarget(33);
            }
        });
        btnTarget100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTarget(100);
            }
        });
        btnTargetOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTarget(0);
            }
        });

        btnToggleVibe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean next = !tasbihManager.isVibrationEnabled();
                tasbihManager.setVibrationEnabled(next);
                updateVibeButton();
                if (next) {
                    tasbihManager.vibrate(60);
                }
            }
        });
        updateVibeButton();

        btnResetCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tasbihManager.resetSession();
                updateCounterUI();
            }
        });

        btnShareReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareTasbihReport();
            }
        });

        btnExportCsv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareCsvExport();
            }
        });

        btnClearStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showClearConfirmDialog();
            }
        });
    }

    private void updateVibeButton() {
        if (tasbihManager.isVibrationEnabled()) {
            btnToggleVibe.setBackgroundResource(R.drawable.pill_selected);
            btnToggleVibe.setColorFilter(Color.WHITE);
        } else {
            btnToggleVibe.setBackgroundResource(R.drawable.pill_unselected);
            btnToggleVibe.setColorFilter(Color.parseColor("#8f94ff"));
        }
    }

    private void setTarget(int target) {
        tasbihManager.setTarget(target);
        setupTargetButtons();
        updateCounterUI();
    }

    private void setupTargetButtons() {
        int t = tasbihManager.getTarget();
        btnTarget33.setBackgroundResource(t == 33 ? R.drawable.pill_selected : R.drawable.pill_unselected);
        btnTarget100.setBackgroundResource(t == 100 ? R.drawable.pill_selected : R.drawable.pill_unselected);
        btnTargetOpen.setBackgroundResource(t == 0 ? R.drawable.pill_selected : R.drawable.pill_unselected);
    }

    private void setupOccasionBanner() {
        HijriCalendarHelper.HijriDate hijri = HijriCalendarHelper.getTodayHijri();
        tvHijriDate.setText(hijri.getFormatted(isArabic));

        final OccasionItem todayOccasion = OccasionDhikrManager.getPrimaryOccasionToday();
        if (todayOccasion != null) {
            tvOccasionBadge.setText(todayOccasion.getBadge(isArabic));
            tvOccasionTitle.setText(todayOccasion.getTitle(isArabic) + " — " + todayOccasion.getSubtitle(isArabic));
            tvOccasionVirtue.setText(todayOccasion.getVirtueOrHadith(isArabic));

            btnApplyOccasionDhikr.setText(getString(R.string.tasbih_use_occasion_dhikr));
            btnApplyOccasionDhikr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectDhikr(todayOccasion.getSuggestedDhikr(isArabic));
                }
            });
            cardOccasion.setVisibility(View.VISIBLE);
        } else {
            cardOccasion.setVisibility(View.GONE);
        }
    }

    private void setupDhikrPills() {
        layoutDhikrPills.removeAllViews();
        String[] presets = isArabic ? TasbihManager.PRESET_DHIKRS_AR : TasbihManager.PRESET_DHIKRS_EN;
        final String current = tasbihManager.getCurrentDhikr();

        for (final String dhikr : presets) {
            final TextView pill = new TextView(this);
            pill.setText(dhikr);
            pill.setTextSize(13);
            pill.setPadding(dpToPx(14), dpToPx(8), dpToPx(14), dpToPx(8));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
            pill.setLayoutParams(params);

            boolean isSelected = dhikr.equals(current);
            pill.setBackgroundResource(isSelected ? R.drawable.pill_selected : R.drawable.pill_unselected);
            pill.setTextColor(Color.WHITE);

            pill.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectDhikr(dhikr);
                }
            });

            layoutDhikrPills.addView(pill);
        }

        // Add custom dhikr button
        TextView addCustomPill = new TextView(this);
        addCustomPill.setText("+ " + getString(R.string.add));
        addCustomPill.setTextSize(13);
        addCustomPill.setPadding(dpToPx(14), dpToPx(8), dpToPx(14), dpToPx(8));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
        addCustomPill.setLayoutParams(params);
        addCustomPill.setBackgroundResource(R.drawable.pill_unselected);
        addCustomPill.setTextColor(Color.parseColor("#ffd479"));
        addCustomPill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddCustomDhikrDialog();
            }
        });
        layoutDhikrPills.addView(addCustomPill);
    }

    private void selectDhikr(String dhikr) {
        tasbihManager.setCurrentDhikr(dhikr);
        setupDhikrPills();
        updateCounterUI();
    }

    private void showAddCustomDhikrDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.custom_dhikr_title);

        final EditText input = new EditText(this);
        input.setHint(R.string.custom_dhikr_hint);
        input.setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12));
        builder.setView(input);

        builder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String custom = input.getText().toString().trim();
                if (!custom.isEmpty()) {
                    selectDhikr(custom);
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void onTasbihTap() {
        // Animation
        ScaleAnimation scale = new ScaleAnimation(1.0f, 0.94f, 1.0f, 0.94f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(70);
        scale.setRepeatCount(1);
        scale.setRepeatMode(ScaleAnimation.REVERSE);
        btnTasbihTap.startAnimation(scale);

        boolean targetReached = tasbihManager.increment();
        if (targetReached) {
            Toast.makeText(this, R.string.tasbih_target_reached, Toast.LENGTH_SHORT).show();
        }

        updateCounterUI();
        setupStatsUI();
    }

    private void updateCounterUI() {
        tvCurrentDhikr.setText(tasbihManager.getCurrentDhikr());
        int count = tasbihManager.getCurrentCount();
        tvCountBig.setText(String.valueOf(count));

        int target = tasbihManager.getTarget();
        if (target > 0) {
            tvTargetProgress.setText((isArabic ? "الهدف: " : "Target: ") + target);
        } else {
            tvTargetProgress.setText((isArabic ? "مفتوح ∞" : "Open ∞"));
        }

        tvLapCount.setText((isArabic ? "الدورة: " : "Round: ") + tasbihManager.getCurrentLap());
        tvTodaySummary.setText((isArabic ? "اليوم: " : "Today: ") + tasbihManager.getTodayTotal());
    }

    private void setupStatsUI() {
        tvStatToday.setText(String.valueOf(tasbihManager.getTodayTotal()));
        tvStatWeek.setText(String.valueOf(tasbihManager.getWeeklyTotal()));
        tvStatMonth.setText(String.valueOf(tasbihManager.getMonthlyTotal()));
        tvStatStreak.setText(tasbihManager.getStreak() + " 🔥");

        // Weekly Bars
        layoutWeeklyBars.removeAllViews();
        List<TasbihDayStat> week = tasbihManager.getWeeklyHistory();

        int max = 1;
        for (TasbihDayStat d : week) {
            if (d.getTotalCount() > max) {
                max = d.getTotalCount();
            }
        }

        for (TasbihDayStat day : week) {
            LinearLayout col = new LinearLayout(this);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams colParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
            col.setLayoutParams(colParams);

            // Count label
            TextView tvVal = new TextView(this);
            tvVal.setText(day.getTotalCount() > 0 ? String.valueOf(day.getTotalCount()) : "");
            tvVal.setTextSize(9);
            tvVal.setTextColor(Color.parseColor("#8f94ff"));
            tvVal.setGravity(Gravity.CENTER);
            col.addView(tvVal);

            // Bar view
            int heightPx = (int) Math.max(dpToPx(6), ((float) day.getTotalCount() / (float) max) * dpToPx(70));
            View bar = new View(this);
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(dpToPx(16), heightPx);
            barParams.setMargins(0, dpToPx(2), 0, dpToPx(4));
            bar.setLayoutParams(barParams);
            bar.setBackgroundResource(R.drawable.stat_bar_bg);
            if (day.getTotalCount() == 0) {
                bar.setAlpha(0.25f);
            }
            col.addView(bar);

            // Day name label
            TextView tvDay = new TextView(this);
            tvDay.setText(day.getDayName(isArabic));
            tvDay.setTextSize(10);
            tvDay.setTextColor(Color.parseColor("#a9abe0"));
            tvDay.setGravity(Gravity.CENTER);
            col.addView(tvDay);

            layoutWeeklyBars.addView(col);
        }
    }

    private void setupOccasionsList() {
        layoutOccasionsList.removeAllViews();
        Calendar cal = Calendar.getInstance();
        List<OccasionItem> list = OccasionDhikrManager.getOccasionsForCalendar(cal);

        for (final OccasionItem item : list) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(R.drawable.pill_unselected);
            card.setPadding(dpToPx(14), dpToPx(12), dpToPx(14), dpToPx(12));
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, dpToPx(10));
            card.setLayoutParams(cardParams);

            // Title + Badge row
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);

            TextView title = new TextView(this);
            title.setText(item.getTitle(isArabic));
            title.setTextColor(Color.parseColor("#ffd479"));
            title.setTextSize(14);
            title.setTypeface(null, android.graphics.Typeface.BOLD);
            LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            title.setLayoutParams(titleParams);
            row.addView(title);

            if (item.isActiveToday()) {
                TextView activeBadge = new TextView(this);
                activeBadge.setText(isArabic ? "نشط اليوم" : "Active");
                activeBadge.setTextColor(Color.WHITE);
                activeBadge.setTextSize(10);
                activeBadge.setBackgroundResource(R.drawable.pill_selected);
                activeBadge.setPadding(dpToPx(8), dpToPx(2), dpToPx(8), dpToPx(2));
                row.addView(activeBadge);
            }

            card.addView(row);

            // Details
            TextView details = new TextView(this);
            details.setText(item.getDetails(isArabic));
            details.setTextColor(Color.parseColor("#c9ccff"));
            details.setTextSize(12);
            details.setPadding(0, dpToPx(4), 0, dpToPx(4));
            card.addView(details);

            // Hadith
            TextView hadith = new TextView(this);
            hadith.setText(item.getVirtueOrHadith(isArabic));
            hadith.setTextColor(Color.parseColor("#5fe08f"));
            hadith.setTextSize(12);
            card.addView(hadith);

            // Dhikr button
            Button btnSelect = new Button(this);
            btnSelect.setText((isArabic ? "تسبيح: " : "Tasbih: ") + item.getSuggestedDhikr(isArabic));
            btnSelect.setTextSize(12);
            btnSelect.setTextColor(Color.WHITE);
            btnSelect.setBackgroundResource(R.drawable.pill_selected);
            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(36)
            );
            btnParams.setMargins(0, dpToPx(8), 0, 0);
            btnSelect.setLayoutParams(btnParams);
            btnSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectDhikr(item.getSuggestedDhikr(isArabic));
                    Toast.makeText(TasbihActivity.this, item.getSuggestedDhikr(isArabic), Toast.LENGTH_SHORT).show();
                }
            });
            card.addView(btnSelect);

            layoutOccasionsList.addView(card);
        }
    }

    private void shareTasbihReport() {
        String report = tasbihManager.exportAsText(isArabic);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, isArabic ? "تقرير وِرد التسبيح" : "Tasbih Report");
        intent.putExtra(Intent.EXTRA_TEXT, report);
        startActivity(Intent.createChooser(intent, getString(R.string.tasbih_export_text)));
    }

    private void shareCsvExport() {
        String csv = tasbihManager.exportAsCsv();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_SUBJECT, "tasbih_log.csv");
        intent.putExtra(Intent.EXTRA_TEXT, csv);
        startActivity(Intent.createChooser(intent, getString(R.string.tasbih_export_csv)));
    }

    private void showClearConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.tasbih_clear_confirm_title);
        builder.setMessage(R.string.tasbih_clear_confirm_msg);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                tasbihManager.clearAllData();
                updateCounterUI();
                setupStatsUI();
                Toast.makeText(TasbihActivity.this, R.string.tasbih_clear_done, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}
