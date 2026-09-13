package com.clock.livewallpaper.model;

public class OccasionItem {
    private String id;
    private String titleAr;
    private String titleEn;
    private String subtitleAr;
    private String subtitleEn;
    private String detailsAr;
    private String detailsEn;
    private String virtueOrHadithAr;
    private String virtueOrHadithEn;
    private String suggestedDhikrAr;
    private String suggestedDhikrEn;
    private String badgeAr;
    private String badgeEn;
    private boolean activeToday;

    public OccasionItem(String id, String titleAr, String titleEn, String subtitleAr, String subtitleEn,
                        String detailsAr, String detailsEn, String virtueOrHadithAr, String virtueOrHadithEn,
                        String suggestedDhikrAr, String suggestedDhikrEn, String badgeAr, String badgeEn,
                        boolean activeToday) {
        this.id = id;
        this.titleAr = titleAr;
        this.titleEn = titleEn;
        this.subtitleAr = subtitleAr;
        this.subtitleEn = subtitleEn;
        this.detailsAr = detailsAr;
        this.detailsEn = detailsEn;
        this.virtueOrHadithAr = virtueOrHadithAr;
        this.virtueOrHadithEn = virtueOrHadithEn;
        this.suggestedDhikrAr = suggestedDhikrAr;
        this.suggestedDhikrEn = suggestedDhikrEn;
        this.badgeAr = badgeAr;
        this.badgeEn = badgeEn;
        this.activeToday = activeToday;
    }

    public String getId() {
        return id;
    }

    public String getTitle(boolean isArabic) {
        return isArabic ? titleAr : titleEn;
    }

    public String getSubtitle(boolean isArabic) {
        return isArabic ? subtitleAr : subtitleEn;
    }

    public String getDetails(boolean isArabic) {
        return isArabic ? detailsAr : detailsEn;
    }

    public String getVirtueOrHadith(boolean isArabic) {
        return isArabic ? virtueOrHadithAr : virtueOrHadithEn;
    }

    public String getSuggestedDhikr(boolean isArabic) {
        return isArabic ? suggestedDhikrAr : suggestedDhikrEn;
    }

    public String getBadge(boolean isArabic) {
        return isArabic ? badgeAr : badgeEn;
    }

    public boolean isActiveToday() {
        return activeToday;
    }

    public void setActiveToday(boolean activeToday) {
        this.activeToday = activeToday;
    }
}
