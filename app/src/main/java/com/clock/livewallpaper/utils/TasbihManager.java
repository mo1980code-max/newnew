package com.clock.livewallpaper.utils;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import com.clock.livewallpaper.model.TasbihDayStat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TasbihManager {

    private static final String PREF_HISTORY = "tasbih_history_json";
    private static final String PREF_CURRENT_DHIKR = "tasbih_current_dhikr";
    private static final String PREF_CURRENT_COUNT = "tasbih_current_count";
    private static final String PREF_CURRENT_LAP = "tasbih_current_lap";
    private static final String PREF_TARGET = "tasbih_target";
    private static final String PREF_VIBRATION = "tasbih_vibration";

    public static final String[] PRESET_DHIKRS_AR = {
            "سبحان الله",
            "الحمد لله",
            "لا إله إلا الله",
            "الله أكبر",
            "أستغفر الله وأتوب إليه",
            "اللهم صلّ وسلّم على نبينا محمد",
            "لا حول ولا قوة إلا بالله",
            "سبحان الله وبحمده، سبحان الله العظيم",
            "لا إله إلا الله وحده لا شريك له، له الملك وله الحمد وهو على كل شيء قدير",
            "سبحان الله والحمد لله ولا إله إلا الله والله أكبر",
            "يا حي يا قيوم برحمتك أستغيث",
            "اللهم إنك عفو تحب العفو فاعفُ عني"
    };

    public static final String[] PRESET_DHIKRS_EN = {
            "Subhan Allah",
            "Alhamdulillah",
            "La ilaha illa Allah",
            "Allahu Akbar",
            "Astaghfirullah wa atubu ilayh",
            "Allahumma salli wa sallim 'ala Muhammad",
            "La hawla wa la quwwata illa billah",
            "Subhan Allahi wa bihamdihi, Subhan Allahil Azim",
            "La ilaha illallah wahdahu la sharika lah...",
            "Subhan Allah wal-hamdulillah wa la ilaha illallah wallahu Akbar",
            "Ya Hayyu Ya Qayyumu bi-rahmatika astagheeth",
            "Allahumma innaka 'afuwwun tuhibbul-'afwa fa'fu 'anni"
    };

    private static final String[] DAYS_AR = {"الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت"};
    private static final String[] DAYS_EN = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

    private TinyDB tinyDB;
    private Context context;
    private Gson gson;

    public TasbihManager(Context context) {
        this.context = context.getApplicationContext();
        this.tinyDB = new TinyDB(context);
        this.gson = new Gson();
    }

    public String getCurrentDhikr() {
        String saved = tinyDB.getString(PREF_CURRENT_DHIKR);
        if (saved == null || saved.trim().isEmpty()) {
            return PRESET_DHIKRS_AR[0];
        }
        return saved;
    }

    public void setCurrentDhikr(String dhikr) {
        if (dhikr != null && !dhikr.trim().isEmpty()) {
            tinyDB.putString(PREF_CURRENT_DHIKR, dhikr);
            tinyDB.putInt(PREF_CURRENT_COUNT, 0);
            tinyDB.putInt(PREF_CURRENT_LAP, 1);
        }
    }

    public int getTarget() {
        return tinyDB.getInt(PREF_TARGET, 33);
    }

    public void setTarget(int target) {
        tinyDB.putInt(PREF_TARGET, target);
    }

    public int getCurrentCount() {
        return tinyDB.getInt(PREF_CURRENT_COUNT, 0);
    }

    public int getCurrentLap() {
        return tinyDB.getInt(PREF_CURRENT_LAP, 1);
    }

    public boolean isVibrationEnabled() {
        return tinyDB.getBoolean(PREF_VIBRATION);
    }

    public void setVibrationEnabled(boolean enabled) {
        tinyDB.putBoolean(PREF_VIBRATION, enabled);
    }

    public void resetSession() {
        tinyDB.putInt(PREF_CURRENT_COUNT, 0);
        tinyDB.putInt(PREF_CURRENT_LAP, 1);
    }

    /**
     * Increments count for current dhikr.
     * Returns true if target was reached.
     */
    public boolean increment() {
        int count = getCurrentCount() + 1;
        int target = getTarget();
        int lap = getCurrentLap();
        boolean targetReached = false;

        if (target > 0 && count >= target) {
            targetReached = true;
            count = 0;
            lap += 1;
        }

        tinyDB.putInt(PREF_CURRENT_COUNT, count);
        tinyDB.putInt(PREF_CURRENT_LAP, lap);

        // Record in daily history
        recordHit(getCurrentDhikr());

        if (targetReached && isVibrationEnabled()) {
            vibrate(100);
        }

        return targetReached;
    }

    public void vibrate(long millis) {
        try {
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null && v.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    v.vibrate(millis);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static String getIsoDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date);
    }

    private Map<String, Map<String, Integer>> loadHistory() {
        String json = tinyDB.getString(PREF_HISTORY);
        if (json == null || json.trim().isEmpty()) {
            return new HashMap<String, Map<String, Integer>>();
        }
        try {
            Type type = new TypeToken<Map<String, Map<String, Integer>>>() {}.getType();
            Map<String, Map<String, Integer>> map = gson.fromJson(json, type);
            return map != null ? map : new HashMap<String, Map<String, Integer>>();
        } catch (Exception e) {
            return new HashMap<String, Map<String, Integer>>();
        }
    }

    private void saveHistory(Map<String, Map<String, Integer>> history) {
        try {
            tinyDB.putString(PREF_HISTORY, gson.toJson(history));
        } catch (Exception ignored) {
        }
    }

    private synchronized void recordHit(String dhikr) {
        String todayIso = getIsoDate(new Date());
        Map<String, Map<String, Integer>> history = loadHistory();

        Map<String, Integer> dayMap = history.get(todayIso);
        if (dayMap == null) {
            dayMap = new HashMap<String, Integer>();
            history.put(todayIso, dayMap);
        }

        int current = dayMap.containsKey(dhikr) ? dayMap.get(dhikr) : 0;
        dayMap.put(dhikr, current + 1);

        saveHistory(history);
    }

    public int getTodayTotal() {
        String todayIso = getIsoDate(new Date());
        Map<String, Map<String, Integer>> history = loadHistory();
        Map<String, Integer> dayMap = history.get(todayIso);
        if (dayMap == null) {
            return 0;
        }
        int sum = 0;
        for (int c : dayMap.values()) {
            sum += c;
        }
        return sum;
    }

    public List<TasbihDayStat> getWeeklyHistory() {
        List<TasbihDayStat> list = new ArrayList<TasbihDayStat>();
        Map<String, Map<String, Integer>> history = loadHistory();
        Calendar cal = Calendar.getInstance();

        for (int i = 6; i >= 0; i--) {
            Calendar c = (Calendar) cal.clone();
            c.add(Calendar.DAY_OF_YEAR, -i);
            String iso = getIsoDate(c.getTime());
            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - 1; // 0=Sunday
            String dayAr = DAYS_AR[dayOfWeek];
            String dayEn = DAYS_EN[dayOfWeek];

            Map<String, Integer> breakdown = history.get(iso);
            int total = 0;
            if (breakdown != null) {
                for (int val : breakdown.values()) {
                    total += val;
                }
            } else {
                breakdown = new HashMap<String, Integer>();
            }

            list.add(new TasbihDayStat(iso, dayAr, dayEn, total, breakdown));
        }

        return list;
    }

    public int getWeeklyTotal() {
        List<TasbihDayStat> week = getWeeklyHistory();
        int sum = 0;
        for (TasbihDayStat stat : week) {
            sum += stat.getTotalCount();
        }
        return sum;
    }

    public int getWeeklyAverage() {
        return getWeeklyTotal() / 7;
    }

    public int getMonthlyTotal() {
        Map<String, Map<String, Integer>> history = loadHistory();
        Calendar cal = Calendar.getInstance();
        int sum = 0;

        for (int i = 0; i < 30; i++) {
            Calendar c = (Calendar) cal.clone();
            c.add(Calendar.DAY_OF_YEAR, -i);
            String iso = getIsoDate(c.getTime());
            Map<String, Integer> dayMap = history.get(iso);
            if (dayMap != null) {
                for (int val : dayMap.values()) {
                    sum += val;
                }
            }
        }
        return sum;
    }

    public int getAllTimeTotal() {
        Map<String, Map<String, Integer>> history = loadHistory();
        int sum = 0;
        for (Map<String, Integer> dayMap : history.values()) {
            if (dayMap != null) {
                for (int val : dayMap.values()) {
                    sum += val;
                }
            }
        }
        return sum;
    }

    public int getStreak() {
        Map<String, Map<String, Integer>> history = loadHistory();
        Calendar cal = Calendar.getInstance();
        int streak = 0;

        // Check today first; if today is 0, start from yesterday
        String todayIso = getIsoDate(cal.getTime());
        Map<String, Integer> todayMap = history.get(todayIso);
        int todaySum = 0;
        if (todayMap != null) {
            for (int v : todayMap.values()) todaySum += v;
        }

        int offset = 0;
        if (todaySum == 0) {
            offset = 1; // start checking from yesterday
        }

        for (int i = offset; i < 365; i++) {
            Calendar c = (Calendar) cal.clone();
            c.add(Calendar.DAY_OF_YEAR, -i);
            String iso = getIsoDate(c.getTime());
            Map<String, Integer> dayMap = history.get(iso);
            int daySum = 0;
            if (dayMap != null) {
                for (int v : dayMap.values()) daySum += v;
            }
            if (daySum > 0) {
                streak++;
            } else {
                break;
            }
        }

        return streak;
    }

    public List<Map.Entry<String, Integer>> getTopDhikrs(int limit) {
        Map<String, Map<String, Integer>> history = loadHistory();
        Map<String, Integer> aggregated = new HashMap<String, Integer>();

        for (Map<String, Integer> dayMap : history.values()) {
            if (dayMap != null) {
                for (Map.Entry<String, Integer> entry : dayMap.entrySet()) {
                    int curr = aggregated.containsKey(entry.getKey()) ? aggregated.get(entry.getKey()) : 0;
                    aggregated.put(entry.getKey(), curr + entry.getValue());
                }
            }
        }

        List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(aggregated.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        if (list.size() > limit && limit > 0) {
            return list.subList(0, limit);
        }
        return list;
    }

    public String exportAsText(boolean isArabic) {
        StringBuilder sb = new StringBuilder();
        String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());

        if (isArabic) {
            sb.append("📊 تقرير وِرد التسبيح اليومي والأسبوعي\n");
            sb.append("📅 التاريخ: ").append(dateStr).append("\n");
            sb.append("──────────────────────\n");
            sb.append("✨ إحصائيات الإنجاز:\n");
            sb.append("• مجموع تسبيحات اليوم: ").append(getTodayTotal()).append("\n");
            sb.append("• مجموع الأسبوع (7 أيام): ").append(getWeeklyTotal()).append("\n");
            sb.append("• متوسط التسبيح اليومي: ").append(getWeeklyAverage()).append("\n");
            sb.append("• مجموع الشهر (30 يوماً): ").append(getMonthlyTotal()).append("\n");
            sb.append("• الإجمالي الكلي: ").append(getAllTimeTotal()).append("\n");
            sb.append("• أيام الالتزام المتتالية: ").append(getStreak()).append(" أيام 🔥\n");
            sb.append("──────────────────────\n");
            sb.append("📿 تفصيل الأذكار:\n");
            List<Map.Entry<String, Integer>> top = getTopDhikrs(8);
            if (top.isEmpty()) {
                sb.append("لم يتم تسجيل تسبيحات بعد.\n");
            } else {
                int rank = 1;
                for (Map.Entry<String, Integer> entry : top) {
                    sb.append(rank).append(". ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" مرة\n");
                    rank++;
                }
            }
            sb.append("──────────────────────\n");
            sb.append("تطبيق ساعة الخلفية الحية والمسبحة الإلكترونية");
        } else {
            sb.append("📊 Tasbih & Wird Daily/Weekly Report\n");
            sb.append("📅 Date: ").append(dateStr).append("\n");
            sb.append("──────────────────────\n");
            sb.append("✨ Performance Summary:\n");
            sb.append("• Today's Total: ").append(getTodayTotal()).append("\n");
            sb.append("• Last 7 Days Total: ").append(getWeeklyTotal()).append("\n");
            sb.append("• Daily Average: ").append(getWeeklyAverage()).append("\n");
            sb.append("• Last 30 Days Total: ").append(getMonthlyTotal()).append("\n");
            sb.append("• All-Time Total: ").append(getAllTimeTotal()).append("\n");
            sb.append("• Streak: ").append(getStreak()).append(" days 🔥\n");
            sb.append("──────────────────────\n");
            sb.append("📿 Dhikr Breakdown:\n");
            List<Map.Entry<String, Integer>> top = getTopDhikrs(8);
            if (top.isEmpty()) {
                sb.append("No tasbih recorded yet.\n");
            } else {
                int rank = 1;
                for (Map.Entry<String, Integer> entry : top) {
                    sb.append(rank).append(". ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" times\n");
                    rank++;
                }
            }
            sb.append("──────────────────────\n");
            sb.append("Clock Live Wallpaper & Digital Tasbih");
        }

        return sb.toString();
    }

    public String exportAsCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("Date,Dhikr,Count\n");
        Map<String, Map<String, Integer>> history = loadHistory();

        List<String> dates = new ArrayList<String>(history.keySet());
        Collections.sort(dates);

        for (String date : dates) {
            Map<String, Integer> dayMap = history.get(date);
            if (dayMap != null) {
                for (Map.Entry<String, Integer> entry : dayMap.entrySet()) {
                    sb.append(date).append(",\"")
                            .append(entry.getKey().replace("\"", "\"\""))
                            .append("\",")
                            .append(entry.getValue())
                            .append("\n");
                }
            }
        }
        return sb.toString();
    }

    public void clearAllData() {
        tinyDB.putString(PREF_HISTORY, "");
        tinyDB.putInt(PREF_CURRENT_COUNT, 0);
        tinyDB.putInt(PREF_CURRENT_LAP, 1);
    }
}
