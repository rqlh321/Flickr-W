package com.example.sic.getpicsfromflickr;

import android.os.Parcel;
import android.os.Parcelable;

public class GalleryItem implements Parcelable {
    public static final Creator<GalleryItem> CREATOR = new Creator<GalleryItem>() {
        @Override
        public GalleryItem createFromParcel(Parcel in) {
            return new GalleryItem(in);
        }

        @Override
        public GalleryItem[] newArray(int size) {
            return new GalleryItem[size];
        }
    };
    private String mCaption;
    private String mUrl;

    public GalleryItem(String name, String url) {
        this.mCaption = name;
        this.mUrl = url;
    }

    protected GalleryItem(Parcel in) {
        String[] data = new String[2];
        in.readStringArray(data);
        mCaption = data[0];
        mUrl = data[1];
    }

    public String getCaption() {
        return mCaption;
    }

    public String getUrl() {
        return mUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringArray(new String[]{mCaption, mUrl});
    }
}
