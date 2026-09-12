package com.clock.livewallpaper.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;



public class ImageUrlsItem implements Parcelable {
    public static final Creator<ImageUrlsItem> CREATOR = new Creator<ImageUrlsItem>() {
        
        @Override
        public ImageUrlsItem createFromParcel(Parcel parcel) {
            return new ImageUrlsItem(parcel);
        }

        
        @Override
        public ImageUrlsItem[] newArray(int i) {
            return new ImageUrlsItem[i];
        }
    };
    @SerializedName("imageUrl")
    private String imageUrl;

    @Override
    public int describeContents() {
        return 0;
    }

    protected ImageUrlsItem(Parcel parcel) {
        this.imageUrl = parcel.readString();
    }

    public void setImageUrl(String str) {
        this.imageUrl = str;
    }

    public String getImageUrl() {
        return this.imageUrl;
    }

    @Override
    public String toString() {
        return "ImageUrlsItem{imageUrl = '" + this.imageUrl + "'}";
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.imageUrl);
    }
}
