package com.clock.livewallpaper.model;



public class Clocks {
    public int backroundImage;
    public String bgColor;
    public int hourHand;
    public int minuteHand;
    public int secondHand;
    public String textColor;

    public Clocks(int i, int i2, int i3, int i4, String str, String str2) {
        this.backroundImage = i;
        this.hourHand = i2;
        this.minuteHand = i3;
        this.secondHand = i4;
        this.textColor = str;
        this.bgColor = str2;
    }

    public String getBgColor() {
        return this.bgColor;
    }

    public void setBgColor(String str) {
        this.bgColor = str;
    }

    public String getTextColor() {
        return this.textColor;
    }

    public void setTextColor(String str) {
        this.textColor = str;
    }

    public int getBackroundImage() {
        return this.backroundImage;
    }

    public void setBackroundImage(int i) {
        this.backroundImage = i;
    }

    public int getHourHand() {
        return this.hourHand;
    }

    public void setHourHand(int i) {
        this.hourHand = i;
    }

    public int getMinuteHand() {
        return this.minuteHand;
    }

    public void setMinuteHand(int i) {
        this.minuteHand = i;
    }

    public int getSecondHand() {
        return this.secondHand;
    }

    public void setSecondHand(int i) {
        this.secondHand = i;
    }
}
