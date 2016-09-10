package com.example.sic.getpicsfromflickr.Model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Photo implements Parcelable {
    public static final Creator<Photo> CREATOR = new Creator<Photo>() {
        @Override
        public Photo createFromParcel(Parcel in) {
            return new Photo(in);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };
    @SerializedName("title")
    private String title;
    @SerializedName("url_s")
    private String url_s;

    public Photo(String title, String url_s) {
        this.title = title;
        this.url_s = url_s;
    }

    protected Photo(Parcel in) {
        String[] data = new String[2];
        in.readStringArray(data);
        title = data[0];
        url_s = data[1];
    }

    public String getCaption() {
        return title;
    }

    public String getUrl() {
        return url_s;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringArray(new String[]{title, url_s});
    }
}
