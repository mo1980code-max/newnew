package com.clock.livewallpaper.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;



public class ResponseWallpaperItem implements Parcelable {
    public static final Creator<ResponseWallpaperItem> CREATOR = new Creator<ResponseWallpaperItem>() {

        @Override
        public ResponseWallpaperItem createFromParcel(Parcel parcel) {
            return new ResponseWallpaperItem(parcel);
        }


        @Override
        public ResponseWallpaperItem[] newArray(int i) {
            return new ResponseWallpaperItem[i];
        }
    };
    @SerializedName("categoryId")
    private int categoryId;
    @SerializedName("categoryName")
    private String categoryName;
    @SerializedName("imageUrls")
    private ArrayList<ImageUrlsItem> imageUrls;

    @Override
    public int describeContents() {
        return 0;
    }

    protected ResponseWallpaperItem(Parcel parcel) {
        this.imageUrls = parcel.createTypedArrayList(ImageUrlsItem.CREATOR);
        this.categoryId = parcel.readInt();
        this.categoryName = parcel.readString();
    }

    public void setImageUrls(ArrayList<ImageUrlsItem> arrayList) {
        this.imageUrls = arrayList;
    }

    public ArrayList<ImageUrlsItem> getImageUrls() {
        return this.imageUrls;
    }

    public void setCategoryId(int i) {
        this.categoryId = i;
    }

    public int getCategoryId() {
        return this.categoryId;
    }

    public void setCategoryName(String str) {
        this.categoryName = str;
    }

    public String getCategoryName() {
        return this.categoryName;
    }

    @Override
    public String toString() {
        return "ResponseWallpaperItem{imageUrls = '" + this.imageUrls + "',categoryId = '" + this.categoryId + "',categoryName = '" + this.categoryName + "'}";
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(this.imageUrls);
        parcel.writeInt(this.categoryId);
        parcel.writeString(this.categoryName);
    }
}
