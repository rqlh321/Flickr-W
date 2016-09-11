package com.example.sic.getpicsfromflickr.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;

import com.example.sic.getpicsfromflickr.EndlessRecyclerOnScrollListener;
import com.example.sic.getpicsfromflickr.FlickrService;
import com.example.sic.getpicsfromflickr.Model.FlickrResult;
import com.example.sic.getpicsfromflickr.Model.Photo;
import com.example.sic.getpicsfromflickr.R;
import com.example.sic.getpicsfromflickr.RecycleViewListAdapter;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import java.util.ArrayList;

import retrofit.RestAdapter;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private final static String CURRENT_PAGE = "page";
    private final static String GALLERY_LIST = "list";
    private final static String SAVED_BUNDLE = "saved";

    RecycleViewListAdapter adapter;
    ProgressBar progressBar;

    int currentPage = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        setContentView(R.layout.activity_main);

        Toolbar toolbar= (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progressBar = (ProgressBar) findViewById(R.id.loading);
        final SearchView searchView= (SearchView) findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.clear();
                progressBar.setVisibility(View.VISIBLE);
                getNewPhotos(query, 1);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.listView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecycleViewListAdapter(this);
        recyclerView.setAdapter(adapter);
        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            Bundle bundle = savedInstanceState.getBundle(SAVED_BUNDLE);
            ArrayList<Photo> savedPhotos = bundle.getParcelableArrayList(GALLERY_LIST);
            currentPage = bundle.getInt(CURRENT_PAGE);
            adapter.addAll(savedPhotos);
        }
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(layoutManager) {
            @Override
            public void onLoadMore() {
                progressBar.setVisibility(View.VISIBLE);
                getNewPhotos(searchView.getQuery().toString(), currentPage);
                currentPage++;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_clear:
                AlertDialog.Builder alertClearList = new AlertDialog.Builder(this);
                alertClearList.setTitle("Clear list?");
                alertClearList.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        adapter.clear();
                        currentPage = 2;
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(GALLERY_LIST, adapter.getList());
        bundle.putInt(CURRENT_PAGE, currentPage);
        outState.putBundle(SAVED_BUNDLE, bundle);
        super.onSaveInstanceState(outState);
    }

    void getNewPhotos(String text, int page) {
        final RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(FlickrService.SERVICE_ENDPOINT)
                .build();
        FlickrService service = restAdapter.create(FlickrService.class);
        service.getNewPhotos(FlickrService.METHOD_SEARCH,
                FlickrService.API_KEY,
                FlickrService.EXTRA_SMALL_URL,
                FlickrService.FORMAT,
                FlickrService.NO_JSON_CALLBACK, page, text)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<FlickrResult>() {
                    @Override
                    public final void onCompleted() {
                        // do nothing
                    }

                    @Override
                    public final void onError(Throwable e) {
                        Log.e("Flickr", e.getMessage());
                    }

                    @Override
                    public final void onNext(FlickrResult response) {
                        Log.i("Flickr", "it worked");
                        progressBar.setVisibility(View.GONE);
                        adapter.addAll(response.photos.photo);
                    }
                });
    }

}