package com.clock.livewallpaper.quran.model;

public class JuzItem {
    private int juzNumber;
    private String nameAr;
    private String nameEn;
    private String startSurahAr;
    private String startSurahEn;
    private String startAyahQuoteAr;
    private String startAyahQuoteEn;
    private int startPage;

    public JuzItem(int juzNumber, String nameAr, String nameEn, String startSurahAr, String startSurahEn,
                   String startAyahQuoteAr, String startAyahQuoteEn, int startPage) {
        this.juzNumber = juzNumber;
        this.nameAr = nameAr;
        this.nameEn = nameEn;
        this.startSurahAr = startSurahAr;
        this.startSurahEn = startSurahEn;
        this.startAyahQuoteAr = startAyahQuoteAr;
        this.startAyahQuoteEn = startAyahQuoteEn;
        this.startPage = startPage;
    }

    public int getJuzNumber() {
        return juzNumber;
    }

    public String getName(boolean isArabic) {
        return isArabic ? nameAr : nameEn;
    }

    public String getStartSurah(boolean isArabic) {
        return isArabic ? startSurahAr : startSurahEn;
    }

    public String getStartAyahQuote(boolean isArabic) {
        return isArabic ? startAyahQuoteAr : startAyahQuoteEn;
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
