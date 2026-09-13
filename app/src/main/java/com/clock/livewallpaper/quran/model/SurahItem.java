package com.clock.livewallpaper.quran.model;

public class SurahItem {
    private int number;
    private String nameAr;
    private String nameEn;
    private boolean isMeccan;
    private int ayahCount;
    private int startPage;

    public SurahItem(int number, String nameAr, String nameEn, boolean isMeccan, int ayahCount, int startPage) {
        this.number = number;
        this.nameAr = nameAr;
        this.nameEn = nameEn;
        this.isMeccan = isMeccan;
        this.ayahCount = ayahCount;
        this.startPage = startPage;
    }

    public int getNumber() {
        return number;
    }

    public String getNameAr() {
        return nameAr;
    }

    public String getNameEn() {
        return nameEn;
    }

    public String getName(boolean isArabic) {
        return isArabic ? nameAr : nameEn;
    }

    public boolean isMeccan() {
        return isMeccan;
    }

    public String getTypeName(boolean isArabic) {
        if (isArabic) {
            return isMeccan ? "مكية" : "مدنية";
        } else {
            return isMeccan ? "Meccan" : "Medinan";
        }
    }

    public int getAyahCount() {
        return ayahCount;
    }

    public String getAyahCountText(boolean isArabic) {
        if (isArabic) {
            return ayahCount + " آيات";
        } else {
            return ayahCount + " Verses";
        }
    }

    public int getStartPage() {
        return startPage;
    }

    public String getStartPageText(boolean isArabic) {
        if (isArabic) {
            return "صفحة " + startPage;
        } else {
            return "Page " + startPage;
        }
    }
}
