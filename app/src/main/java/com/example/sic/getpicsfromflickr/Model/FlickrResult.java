package com.example.sic.getpicsfromflickr.Model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by sic on 10.09.2016.
 */
public class FlickrResult {

    @SerializedName("photos")
    public FlickrPhotos photos;

    public class FlickrPhotos {
        public int page;
        public String pages;
        public int perpage;
        public String total;
        public ArrayList<Photo> photo;
        public String stat;
    }

}
