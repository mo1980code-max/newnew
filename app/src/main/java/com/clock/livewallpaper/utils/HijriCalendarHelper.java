package com.clock.livewallpaper.utils;

import java.util.Calendar;
import java.util.Date;

public class HijriCalendarHelper {

    public static final String[] MONTHS_AR = {
            "محرم", "صفر", "ربيع الأول", "ربيع الآخر",
            "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
            "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    };

    public static final String[] MONTHS_EN = {
            "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
            "Jumada al-Ula", "Jumada al-Akhirah", "Rajab", "Sha'ban",
            "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    };

    public static class HijriDate {
        public final int day;
        public final int month; // 1 to 12
        public final int year;

        public HijriDate(int day, int month, int year) {
            this.day = day;
            this.month = month;
            this.year = year;
        }

        public String getMonthName(boolean isArabic) {
            if (this.month >= 1 && this.month <= 12) {
                return isArabic ? MONTHS_AR[this.month - 1] : MONTHS_EN[this.month - 1];
            }
            return "";
        }

        public String getFormatted(boolean isArabic) {
            return this.day + " " + getMonthName(isArabic) + " " + this.year + (isArabic ? " هـ" : " AH");
        }

        public boolean isWhiteDay() {
            return this.day >= 13 && this.day <= 15;
        }

        public boolean isWhiteDaysApproaching() {
            return this.day == 11 || this.day == 12;
        }

        public boolean isRamadan() {
            return this.month == 9;
        }

        public boolean isFirstTenOfDhulHijjah() {
            return this.month == 12 && this.day >= 1 && this.day <= 10;
        }

        public boolean isDayOfArafah() {
            return this.month == 12 && this.day == 9;
        }

        public boolean isAshuraSeason() {
            return this.month == 1 && (this.day == 9 || this.day == 10);
        }
    }

    public static HijriDate getTodayHijri() {
        return getHijriDate(Calendar.getInstance());
    }

    public static HijriDate getHijriDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return getHijriDate(cal);
    }

    public static HijriDate getHijriDate(Calendar cal) {
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1; // 1-12
        int day = cal.get(Calendar.DAY_OF_MONTH);

        int a = (14 - month) / 12;
        int y = year + 4800 - a;
        int m = month + 12 * a - 3;
        int jd = day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045;

        int l = jd - 1948440 + 10632;
        int n = (l - 1) / 10631;
        l = l - 10631 * n + 354;
        int j = ((10985 - l) / 5316) * ((50 * l) / 17719) + (l / 5670) * ((43 * l) / 15238);
        l = l - ((30 - j) / 15) * ((17719 * j) / 50) - (j / 16) * ((15238 * j) / 43) + 29;
        int hijriMonth = (24 * l) / 709;
        int hijriDay = l - (709 * hijriMonth) / 24;
        int hijriYear = 30 * n + j - 30;

        if (hijriDay <= 0) {
            hijriDay = 1;
        }
        if (hijriMonth <= 0) {
            hijriMonth = 1;
        } else if (hijriMonth > 12) {
            hijriMonth = 12;
        }

        return new HijriDate(hijriDay, hijriMonth, hijriYear);
    }
}
