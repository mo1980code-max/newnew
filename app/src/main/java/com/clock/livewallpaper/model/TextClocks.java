package com.clock.livewallpaper.model;



public class TextClocks {
    String bgColor;
    private int thumb;

    public TextClocks(int i, String str) {
        this.thumb = i;
        this.bgColor = str;
    }

    public int getThumb() {
        return this.thumb;
    }

    public void setThumb(int i) {
        this.thumb = i;
    }

    public String getBgColor() {
        return this.bgColor;
    }

    public void setBgColor(String str) {
        this.bgColor = str;
    }
}
