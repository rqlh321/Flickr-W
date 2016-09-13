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
    @SerializedName("url_n")
    private String url_n;
    @SerializedName("latitude")
    private String latitude;
    @SerializedName("longitude")
    private String longitude;

    public Photo(String title, String url_s, String url_n, String lat, String lon) {
        this.title = title;
        this.url_s = url_s;
        this.url_n = url_n;
        this.latitude = lat;
        this.longitude = lon;
    }

    protected Photo(Parcel in) {
        String[] data = new String[5];
        in.readStringArray(data);
        title = data[0];
        url_s = data[1];
        url_n = data[2];
        latitude = data[3];
        longitude = data[4];
    }

    public String getTitle() {
        return title;
    }

    public String getUrl_s() {
        return url_s;
    }

    public String getUrl_n() {
        return url_n;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringArray(new String[]{title, url_s, url_n, latitude, longitude});
    }
}
