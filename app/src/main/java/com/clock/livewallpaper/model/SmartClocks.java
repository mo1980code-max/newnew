package com.clock.livewallpaper.model;



public class SmartClocks {
    int bgColor;
    private int thumb;

    public SmartClocks(int i, int i2) {
        this.thumb = i;
        this.bgColor = i2;
    }

    public int getThumb() {
        return this.thumb;
    }

    public void setThumb(int i) {
        this.thumb = i;
    }

    public int getBgColor() {
        return this.bgColor;
    }

    public void setBgColor(int i) {
        this.bgColor = i;
    }
}
