package com.example.sic.getpicsfromflickr;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    final static String FOLDER_TO_SAVE_PICS = Environment.getExternalStorageDirectory().toString() + "/" + R.string.app_name;
    private static final int REQUEST = 1;

    RecycleViewListAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        setContentView(R.layout.activity_main);

        File checkDir = new File(FOLDER_TO_SAVE_PICS);
        if (!checkDir.exists()) {
            checkDir.mkdirs();
        }
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.listView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecycleViewListAdapter(this);
        adapter.setRecycleView(recyclerView);
        recyclerView.setAdapter(adapter);
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
            String namePic = selectedImage.getLastPathSegment();
            GalleryItem galleryItem = new GalleryItem(namePic, selectedImage.toString());
            adapter.add(galleryItem);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_new:
                adapter.clear();
                new FetchItemsTask().execute("");
                return true;
            case R.id.search_in_flickr:
                AlertDialog.Builder alertSiF = new AlertDialog.Builder(this);
                final EditText editTextSiF = new EditText(this);
                alertSiF.setTitle("Search in Flickr");
                alertSiF.setView(editTextSiF);
                alertSiF.setPositiveButton("Search", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        adapter.clear();
                        new FetchItemsTask().execute(editTextSiF.getText().toString());
                    }
                });
                alertSiF.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
                alertSiF.show();
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
                        adapter.clear();
                    }
                });
                alertClearList.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
                alertClearList.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class FetchItemsTask extends AsyncTask<String, Void, ArrayList<GalleryItem>> {

        @Override
        protected ArrayList<GalleryItem> doInBackground(String... params) {
            if (params[0].length() > 0) {
                return new FlickrFetchr().search(params[0]);
            } else {
                return new FlickrFetchr().fetchItems();
            }
        }

        @Override
        protected void onPostExecute(ArrayList<GalleryItem> items) {
            adapter.refreshList(items);

        }
    }

}