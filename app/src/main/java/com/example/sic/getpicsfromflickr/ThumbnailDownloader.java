package com.example.sic.getpicsfromflickr;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ThumbnailDownloader extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;
    Handler mHandler;
    Map<String, Integer> requestMap =
            Collections.synchronizedMap(new HashMap<String, Integer>());
    Handler mResponseHandler;
    Listener mListener;

    public interface Listener {
        void onThumbnailDownloaded(Integer position, Bitmap thumbnail);
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    Log.i(TAG, "Got a request for url: " +  msg.obj);
                    handleRequest((String) msg.obj);
                }
            }
        };
    }

    public void queueThumbnail(String url, Integer position) {
        Log.i(TAG, "Got a URL: " + url+"AND POSITION:" +position);
        requestMap.put(url,position);
        mHandler
                .obtainMessage(MESSAGE_DOWNLOAD, url)
                .sendToTarget();
    }

    private void handleRequest(final String url) {
        try {
            if (url == null) return;
            byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
            Log.i(TAG, "Bitmap created");
            mResponseHandler.post(new Runnable() {
                public void run() {
                    mListener.onThumbnailDownloaded(requestMap.get(url), bitmap);
                }
            });
        } catch (IOException ioe) {
            Log.e(TAG, "Error downloading image", ioe);
        }
    }
}
