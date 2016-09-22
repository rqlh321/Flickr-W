package com.example.sic.getpicsfromflickr.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.sic.getpicsfromflickr.activity.MainActivity;
import com.example.sic.getpicsfromflickr.EndlessRecyclerOnScrollListener;
import com.example.sic.getpicsfromflickr.FlickrService;
import com.example.sic.getpicsfromflickr.model.FlickrResult;
import com.example.sic.getpicsfromflickr.model.Photo;
import com.example.sic.getpicsfromflickr.R;
import com.example.sic.getpicsfromflickr.RecycleViewListAdapter;

import java.util.ArrayList;

import retrofit.RestAdapter;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by sic on 03.09.2016.
 */
public class ListFragment extends Fragment {
    private final String CURRENT_PAGE = "page";
    private final String GALLERY_LIST = "list";
    private final String SAVED_BUNDLE = "saved";

    RecycleViewListAdapter adapter;
    ProgressBar progressBar;
    int currentPage = 2;
    private CoordinatorLayout coordinatorLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        progressBar = (ProgressBar) view.findViewById(R.id.loading);
        coordinatorLayout = (CoordinatorLayout) view.findViewById(R.id.coordinator_layout);

        FloatingActionButton goToMap = (FloatingActionButton) view.findViewById(R.id.go_to_map);
        goToMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MapFragment mapFragment = new MapFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(GALLERY_LIST, adapter.getList());
                mapFragment.setArguments(bundle);
                getActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, mapFragment)
                        .commit();
            }
        });

        final SearchView searchView = (SearchView) view.findViewById(R.id.search_view);
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

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.listView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecycleViewListAdapter(getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(layoutManager) {
            @Override
            public void onLoadMore() {
                if (searchView.getQuery().length() > 0) {
                    progressBar.setVisibility(View.VISIBLE);
                    getNewPhotos(searchView.getQuery().toString(), currentPage);
                    currentPage++;
                }
            }
        });

        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            Bundle bundle = savedInstanceState.getBundle(SAVED_BUNDLE);
            ArrayList<Photo> photos = bundle.getParcelableArrayList(GALLERY_LIST);
            currentPage = bundle.getInt(CURRENT_PAGE);
            adapter.addAll(photos);
        } else {
            ArrayList<Photo> photos = getArguments().getParcelableArrayList(GALLERY_LIST);
            adapter.addAll(photos);
        }
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        MainActivity.checkNetwork(connectivityManager,coordinatorLayout);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_clear:
                AlertDialog.Builder alertClearList = new AlertDialog.Builder(getContext());
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
    public void onSaveInstanceState(Bundle outState) {
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
                FlickrService.EXTRAS,
                FlickrService.FORMAT,
                FlickrService.NO_JSON_CALLBACK,
                FlickrService.HAS_GEO,
                FlickrService.LIMIT,
                page, text)
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
