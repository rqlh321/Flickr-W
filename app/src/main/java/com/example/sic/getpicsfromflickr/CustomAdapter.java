package com.example.sic.getpicsfromflickr;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<GalleryItem> {
    private SQLiteDatabase sdb;
    private ThumbnailDownloader mThumbnailThread;
    private ListView listView;

    public CustomAdapter(Context context, ArrayList<GalleryItem> items, SQLiteDatabase sdb, ThumbnailDownloader mThumbnailThread) {
        super(context, R.layout.list_item, items);
        this.sdb=sdb;
        this.mThumbnailThread=mThumbnailThread;
    }

    private void setPicture(GalleryItem item,int position,ImageView imageView){
        Cursor cursor = sdb.query(DatabaseHelper.DATABASE_TABLE, new String[]{DatabaseHelper._ID, DatabaseHelper.URI_COLUMN},
                DatabaseHelper.URI_COLUMN + "=?", new String[]{item.getUrl()}, null, null, null);
        if (cursor.getCount() >0) {
            cursor.moveToLast();
            String content = cursor.getString(cursor.getColumnIndex(DatabaseHelper._ID));
            if(new File(MainActivity.FOLDER_TO_SAVE_PICS + "/" + content + ".jpg").exists()) {
                imageView.setImageBitmap(BitmapFactory.decodeFile(MainActivity.FOLDER_TO_SAVE_PICS + "/" + content + ".jpg"));
            }else{
                sdb.delete(DatabaseHelper.DATABASE_TABLE,DatabaseHelper.URI_COLUMN + "=?", new String[]{item.getUrl()});
                mThumbnailThread.queueThumbnail(item.getUrl(), position);
            }
        } else {
            mThumbnailThread.queueThumbnail(item.getUrl(), position);
        }
        cursor.close();
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater mInflater = LayoutInflater.from(getContext());
        View customView = mInflater.inflate(R.layout.list_item, parent, false);

        final ObjectHolder holder = getObjectHolder(customView);
        final GalleryItem item = getItem(position);

        final TextView mTextView = (TextView) customView.findViewById(R.id.picTitle);
        mTextView.setText(item.getCaption());

        ImageView imageView = (ImageView) customView.findViewById(R.id.imageView);
        setPicture(item, position, imageView);


        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.mainView.getLayoutParams();
        params.rightMargin = 0;
        params.leftMargin = 0;
        holder.mainView.setLayoutParams(params);
        customView.setOnTouchListener(new SwipeDetector(holder, position));

        return customView;
    }

    private ObjectHolder getObjectHolder(View workingView) {
        Object tag = workingView.getTag();
        ObjectHolder holder;

        if (tag == null || !(tag instanceof ObjectHolder)) {
            holder = new ObjectHolder();
            holder.mainView = (LinearLayout)workingView.findViewById(R.id.object_main_view);
            holder.deleteView = (RelativeLayout)workingView.findViewById(R.id.object_delete_view);
            workingView.setTag(holder);
        } else {
            holder = (ObjectHolder) tag;
        }

        return holder;
    }

    public static class ObjectHolder {
        public LinearLayout mainView;
        public RelativeLayout deleteView;
    }

    public void setListView(ListView view) {
        listView = view;
    }

    public class SwipeDetector implements View.OnTouchListener {

        private static final int MIN_DISTANCE = 200;
        private static final int MIN_LOCK_DISTANCE = 30; // disallow motion intercept
        private boolean motionInterceptDisallowed = false;
        private float downX, upX;
        private ObjectHolder holder;
        private int position;

        public SwipeDetector(ObjectHolder h, int pos) {
            holder = h;
            position = pos;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    downX = event.getX();
                    return true; // allow other events like Click to be processed
                }

                case MotionEvent.ACTION_MOVE: {
                    upX = event.getX();
                    float deltaX = downX - upX;

                    if (Math.abs(deltaX) > MIN_LOCK_DISTANCE && listView != null && !motionInterceptDisallowed) {
                        listView.requestDisallowInterceptTouchEvent(true);
                        motionInterceptDisallowed = true;
                    }

                    if (deltaX > 0) {
                        holder.deleteView.setVisibility(View.GONE);
                        swipe(-(int) deltaX);

                    } else {
                        // if first swiped left and then swiped right
                        holder.deleteView.setVisibility(View.VISIBLE);
                    }
                  return true;
                }

                case MotionEvent.ACTION_UP:
                    upX = event.getX();
                    float deltaX = downX-upX;
                    if (Math.abs(deltaX) > MIN_DISTANCE) {
                        if(deltaX>0){//удаляем при свайпе влево
                            swipeRemove();
                        }
                    } else {
                        swipe(0);
                    }

                    if (listView != null) {
                        listView.requestDisallowInterceptTouchEvent(false);
                        motionInterceptDisallowed = false;
                    }

                    holder.deleteView.setVisibility(View.VISIBLE);
                    return true;

                case MotionEvent.ACTION_CANCEL:
                    holder.deleteView.setVisibility(View.VISIBLE);
                    return false;
            }

            return true;
        }

        private void swipe(int distance) {
            View animationView = holder.mainView;
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) animationView.getLayoutParams();
            params.rightMargin = -distance;
            params.leftMargin = distance;
            animationView.setLayoutParams(params);
        }

        private void swipeRemove() {
            remove(getItem(position));
            notifyDataSetChanged();
        }
    }
}