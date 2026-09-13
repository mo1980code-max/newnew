package com.clock.livewallpaper.model;

import java.util.HashMap;
import java.util.Map;

public class TasbihDayStat {
    private String dateIso;
    private String dayNameAr;
    private String dayNameEn;
    private int totalCount;
    private Map<String, Integer> breakdown;

    public TasbihDayStat(String dateIso, String dayNameAr, String dayNameEn, int totalCount, Map<String, Integer> breakdown) {
        this.dateIso = dateIso;
        this.dayNameAr = dayNameAr;
        this.dayNameEn = dayNameEn;
        this.totalCount = totalCount;
        this.breakdown = breakdown == null ? new HashMap<String, Integer>() : breakdown;
    }

    public String getDateIso() {
        return dateIso;
    }

    public String getDayName(boolean isArabic) {
        return isArabic ? dayNameAr : dayNameEn;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public Map<String, Integer> getBreakdown() {
        return breakdown;
    }
}
