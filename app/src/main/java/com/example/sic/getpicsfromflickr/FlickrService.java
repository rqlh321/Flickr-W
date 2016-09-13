package com.example.sic.getpicsfromflickr;

import com.example.sic.getpicsfromflickr.Model.FlickrResult;

import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by sic on 10.09.2016.
 */
public interface FlickrService {
    String SERVICE_ENDPOINT = "https://api.flickr.com/services";
    String METHOD_SEARCH = "flickr.photos.search";
    String API_KEY = "be00e7f9fb70df90a8037ed1e3ea2e66";
    String EXTRAS = "url_s,url_n,geo";
    String FORMAT = "json";
    String HAS_GEO = "1";
    String NO_JSON_CALLBACK = "1";
    String LIMIT = "20";

    @GET("/rest/")
    Observable<FlickrResult> getNewPhotos(
            @Query("method")String method,
            @Query("api_key")String apiKey,
            @Query("extras")String extras,
            @Query("format")String format,
            @Query("nojsoncallback")String nojsoncallback,
            @Query("has_geo")String hasGeo,
            @Query("per_page")String limit,
            @Query("page")int page,
            @Query("text")String text);
    @GET("/rest/")
    Observable<FlickrResult> getNewPhotosByCoordinates(
            @Query("method")String method,
            @Query("api_key")String apiKey,
            @Query("extras")String extras,
            @Query("format")String format,
            @Query("nojsoncallback")String nojsoncallback,
            @Query("has_geo")String hasGeo,
            @Query("per_page")String limit,
            @Query("lat")double latitude,
            @Query("lon")double longitude,
            @Query("page")int page);
}
