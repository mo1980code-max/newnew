package com.clock.livewallpaper.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;
import java.util.List;



public class ResponseWallpaper implements Parcelable {
    public static final Creator<ResponseWallpaper> CREATOR = new Creator<ResponseWallpaper>() {

        @Override
        public ResponseWallpaper createFromParcel(Parcel parcel) {
            return new ResponseWallpaper(parcel);
        }


        @Override
        public ResponseWallpaper[] newArray(int i) {
            return new ResponseWallpaper[i];
        }
    };
    @SerializedName("ResponseWallpaper")
    private List<ResponseWallpaperItem> responseWallpaper;

    @Override
    public int describeContents() {
        return 0;
    }

    protected ResponseWallpaper(Parcel parcel) {
        this.responseWallpaper = parcel.createTypedArrayList(ResponseWallpaperItem.CREATOR);
    }

    public void setResponseWallpaper(List<ResponseWallpaperItem> list) {
        this.responseWallpaper = list;
    }

    public List<ResponseWallpaperItem> getResponseWallpaper() {
        return this.responseWallpaper;
    }

    @Override
    public String toString() {
        return "ResponseWallpaper{responseWallpaper = '" + this.responseWallpaper + "'}";
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(this.responseWallpaper);
    }
}
