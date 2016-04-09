package com.example.sic.getpicsfromflickr;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    final static String FOLDER_TO_SAVE_PICS = Environment.getExternalStorageDirectory().toString()+"/"+R.string.app_name;
    private static final int REQUEST = 1;

    SQLiteDatabase sdb;

    ListView mListView;
    ArrayAdapter<GalleryItem> mAdapter;
    ArrayList<GalleryItem> mItems = new ArrayList<>();

    ThumbnailDownloader mThumbnailThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File checkDir = new File(FOLDER_TO_SAVE_PICS);
        if(!checkDir.exists()){
            checkDir.mkdirs();
        }

        DatabaseHelper dbHelper;
        dbHelper = new DatabaseHelper(this);
        sdb = dbHelper.getReadableDatabase();

        mThumbnailThread = new ThumbnailDownloader(new Handler());
        mThumbnailThread.setListener(new ThumbnailDownloader.Listener() {
            public void onThumbnailDownloaded(Integer position, Bitmap thumbnail) {
                checkAndSave(position, thumbnail);
            }
        });
        mThumbnailThread.start();
        mThumbnailThread.getLooper();

        mListView = (ListView) findViewById(R.id.listView);
        CustomAdapter customAdapter =  new CustomAdapter(this, mItems,sdb,mThumbnailThread);
        mAdapter =customAdapter;
        mListView.setAdapter(mAdapter);
        customAdapter.setListView(mListView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            String namePic=selectedImage.getLastPathSegment();
            mItems.add(0,new GalleryItem(namePic,selectedImage.toString()));
            try {
                Bitmap img = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                checkAndSave(0,img);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_new:
                mAdapter.clear();
                new FetchItemsTask().execute("");
                return  true;
            case R.id.show_my_cached:
                mAdapter.clear();
                Cursor cursor = sdb.query(DatabaseHelper.DATABASE_TABLE, new String[]{DatabaseHelper.URI_COLUMN,DatabaseHelper.NAME_COLUMN}, null, null, null, null, null);
                while(cursor.moveToNext()) {
                    String cacheURI = cursor.getString(cursor.getColumnIndex(DatabaseHelper.URI_COLUMN));
                    String cacheNAME = cursor.getString(cursor.getColumnIndex(DatabaseHelper.NAME_COLUMN));
                    GalleryItem gi = new GalleryItem(cacheNAME, cacheURI);
                    mAdapter.add(gi);
                }
                cursor.close();
                return  true;
            case R.id.search_in_flickr:
                AlertDialog.Builder alertSiF = new AlertDialog.Builder(this);
                final EditText editTextSiF = new EditText(this);
                alertSiF.setTitle("Search in Flickr");
                alertSiF.setView(editTextSiF);
                alertSiF.setPositiveButton("Search", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mAdapter.clear();
                        new FetchItemsTask().execute(editTextSiF.getText().toString());
                    }
                });

                alertSiF.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {}
                });
                alertSiF.show();
                return true;
            case R.id.search_in_list:
                AlertDialog.Builder alertSiL = new AlertDialog.Builder(this);
                final EditText editTextSiL = new EditText(this);
                alertSiL.setTitle("Search in list");
                alertSiL.setView(editTextSiL);
                alertSiL.setPositiveButton("Search", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        ArrayList<GalleryItem> mSearchResult = new ArrayList<>();
                        for (GalleryItem gi : mItems) {
                            if (gi.getCaption().contains(editTextSiL.getText())) {
                                mSearchResult.add(gi);
                            }
                        }
                        Intent intent = new Intent(MainActivity.this, SearchResultActivity.class);
                        intent.putExtra("list", mSearchResult);
                        startActivity(intent);
                    }
                });
                alertSiL.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
                alertSiL.show();
                return true;
            case R.id.add_from_gallery:
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("image/*");
                startActivityForResult(i, REQUEST);
                return true;
            case R.id.menu_item_clear:
                AlertDialog.Builder alertClearList = new AlertDialog.Builder(this);
                alertClearList.setTitle("Clear list?");
                alertClearList.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mAdapter.clear();
                    }
                });
                alertClearList.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {}
                });
                alertClearList.show();
                return true;
            case R.id.cache_clear:
                AlertDialog.Builder alertClearCache = new AlertDialog.Builder(this);
                alertClearCache.setTitle("Clear cache?");
                alertClearCache.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mAdapter.clear();
                        sdb.delete(DatabaseHelper.DATABASE_TABLE,null, null);
                        File dir = new File(FOLDER_TO_SAVE_PICS);
                        File[] files = dir.listFiles();
                        for(File file:files){
                            file.delete();
                        }
                    }
                });
                alertClearCache.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {}
                });
                alertClearCache.show();
                return  true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailThread.quit();
    }

    private class FetchItemsTask extends AsyncTask<String, Void, ArrayList<GalleryItem>> {

        @Override
        protected ArrayList<GalleryItem> doInBackground(String... params) {
           if(params[0].length()>0) {
               return new FlickrFetchr().search(params[0]);
           }
            else {
               return new FlickrFetchr().fetchItems();
           }
        }

        @Override
        protected void onPostExecute(ArrayList<GalleryItem> items) {
                mAdapter.addAll(items);
        }
}

    private String savePicture(Bitmap bitmap, String name){
        try {
            File file = new File(FOLDER_TO_SAVE_PICS,name+".jpg");
            OutputStream fOut = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
            //MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName()); // регистрация в фотоальбоме
        }
        catch (Exception e)
        {
            return e.getMessage();
        }
        return "";
    }

    public void checkAndSave(Integer position, Bitmap thumbnail){
        Cursor c = sdb.query(DatabaseHelper.DATABASE_TABLE, new String[]{DatabaseHelper._ID, DatabaseHelper.URI_COLUMN},
                DatabaseHelper.URI_COLUMN + "=?", new String[]{mItems.get(position).getUrl()}, null, null,null);
        if(c.getCount()==0) {//необходимо ли добавлять картинку на sd
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.URI_COLUMN, mItems.get(position).getUrl());
            values.put(DatabaseHelper.NAME_COLUMN,mItems.get(position).getCaption());
            sdb.insert(DatabaseHelper.DATABASE_TABLE, null, values);

            Cursor cursor = sdb.query(DatabaseHelper.DATABASE_TABLE, new String[]{DatabaseHelper._ID}, null, null, null, null, null);
            cursor.moveToLast();
            String content = cursor.getString(cursor.getColumnIndex(DatabaseHelper._ID));
            cursor.close();
            savePicture(thumbnail, content);
        }
        c.close();
        mAdapter.notifyDataSetChanged();
    }
}