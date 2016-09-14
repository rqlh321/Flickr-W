package com.example.sic.getpicsfromflickr.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.sic.getpicsfromflickr.Activity.MainActivity;
import com.example.sic.getpicsfromflickr.FlickrService;
import com.example.sic.getpicsfromflickr.Model.FlickrResult;
import com.example.sic.getpicsfromflickr.Model.Photo;
import com.example.sic.getpicsfromflickr.R;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareButton;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import retrofit.RestAdapter;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private final String CURRENT_PAGE = "page";
    private final String GALLERY_LIST = "list";
    private final String SAVED_BUNDLE = "saved";

    GoogleMap mMap;
    ArrayList<Photo> photos = new ArrayList<>();
    HashMap<Marker, Photo> markerPhotoHashMap = new HashMap<>();
    ProgressBar progressBar;
    FloatingActionButton refresh;
    LatLng mLatLng;
    int currentPage = 2;
    private CoordinatorLayout coordinatorLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        ((SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map))
                .getMapAsync(this);

        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            Bundle bundle = savedInstanceState.getBundle(SAVED_BUNDLE);
            photos = bundle.getParcelableArrayList(GALLERY_LIST);
            currentPage = bundle.getInt(CURRENT_PAGE);
        } else {
            photos = getArguments().getParcelableArrayList(GALLERY_LIST);
            currentPage = getArguments().getInt(CURRENT_PAGE);
        }

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        progressBar = (ProgressBar) view.findViewById(R.id.loading);
        coordinatorLayout = (CoordinatorLayout) view.findViewById(R.id.coordinator_layout);

        FloatingActionButton goToList = (FloatingActionButton) view.findViewById(R.id.go_to_list);
        goToList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ListFragment listFragment = new ListFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(GALLERY_LIST, photos);
                bundle.putInt(CURRENT_PAGE, currentPage);
                listFragment.setArguments(bundle);
                getActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, listFragment)
                        .commit();
            }
        });
        final SearchView searchView = (SearchView) view.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mLatLng = null;
                progressBar.setVisibility(View.VISIBLE);
                photos.clear();
                mMap.clear();
                getNewPhotosByQuery(query, 1);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        refresh = (FloatingActionButton) view.findViewById(R.id.refresh);
        if(currentPage==2){
            refresh.setVisibility(View.INVISIBLE);
        }
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refresh.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                if (mLatLng == null) {
                    getNewPhotosByQuery(searchView.getQuery().toString(), currentPage);
                    currentPage++;
                } else {
                    getNewPhotosByCoordinates(mLatLng.latitude, mLatLng.longitude, 1);
                    currentPage++;
                }
            }
        });
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                refresh.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                photos.clear();
                mMap.clear();
                mLatLng = latLng;
                getNewPhotosByCoordinates(latLng.latitude, latLng.longitude, 1);
            }
        });
        addMarker(photos);
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Photo currentPhoto = markerPhotoHashMap.get(marker);
                android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(getContext());
                android.support.v7.app.AlertDialog alertDialog = alert.create();
                alertDialog.setTitle(getString(R.string.share));
                LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                View shareView = layoutInflater.inflate(R.layout.share_preview, null);
                ShareButton shareButton = (ShareButton) shareView.findViewById(R.id.fb_share_button);
                ShareLinkContent content = new ShareLinkContent.Builder()
                        .setContentUrl(Uri.parse("https://www.flickr.com"))
                        .setImageUrl(Uri.parse(currentPhoto.getUrl_s()))
                        .setContentTitle("Look what i found on Flickr")
                        .setContentDescription(currentPhoto.getTitle())
                        .build();
                shareButton.setShareContent(content);
                ImageView previewImage = (ImageView) shareView.findViewById(R.id.preview_image);
                Glide.with(getContext())
                        .load(currentPhoto.getUrl_n())
                        .into(previewImage);
                alertDialog.setView(shareView);
                alertDialog.show();
                return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        MainActivity.checkNetwork(connectivityManager, coordinatorLayout);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_clear:
                AlertDialog.Builder alertClearList = new AlertDialog.Builder(getContext());
                alertClearList.setTitle("Clear list?");
                alertClearList.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        photos.clear();
                        currentPage = 2;
                        mMap.clear();
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
        bundle.putParcelableArrayList(GALLERY_LIST, photos);
        //bundle.putInt(CURRENT_PAGE, currentPage);
        outState.putBundle(SAVED_BUNDLE, bundle);
        super.onSaveInstanceState(outState);
    }

    void getNewPhotosByQuery(String text, int page) {
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
                        photos.addAll(response.photos.photo);
                        addMarker(photos);
                    }
                });
    }

    void getNewPhotosByCoordinates(double lat, double lon, int page) {
        final RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(FlickrService.SERVICE_ENDPOINT)
                .build();
        FlickrService service = restAdapter.create(FlickrService.class);
        service.getNewPhotosByCoordinates(FlickrService.METHOD_SEARCH,
                FlickrService.API_KEY,
                FlickrService.EXTRAS,
                FlickrService.FORMAT,
                FlickrService.NO_JSON_CALLBACK,
                FlickrService.HAS_GEO,
                FlickrService.LIMIT,
                lat, lon, page)
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
                        photos.addAll(response.photos.photo);
                        addMarker(photos);
                    }
                });
    }

    void addMarker(final ArrayList<Photo> photos) {
        ArrayList<Photo> photosCopy = new ArrayList<>();
        photosCopy.addAll(photos);
        Observable.from(photosCopy)
                .map(new Func1<Photo, MarkerOptions>() {
                    @Override
                    public MarkerOptions call(Photo arg) {
                        LatLng adCoordinates = new LatLng(Float.valueOf(arg.getLatitude()), Float.valueOf(arg.getLongitude()));

                        IconGenerator iconGenerator = new IconGenerator(getContext());

                        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                        View markerView = layoutInflater.inflate(R.layout.marker, null);
                        try {
                            ImageView imageView = (ImageView) markerView.findViewById(R.id.image);
                            URL url = new URL(arg.getUrl_s());
                            Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                            imageView.setImageBitmap(bmp);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        TextView textView = (TextView) markerView.findViewById(R.id.text);
                        textView.setText(arg.getTitle());
                        textView.setTextColor(Color.WHITE);
                        iconGenerator.setColor(Color.rgb(52, 204, 255));
                        iconGenerator.setContentView(markerView);
                        Bitmap icon = iconGenerator.makeIcon();

                        MarkerOptions myMarkerOptions = new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromBitmap(icon))
                                .position(adCoordinates);

                        return myMarkerOptions;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<MarkerOptions>() {
                    int iterator = 0;

                    @Override
                    public void onCompleted() {
                        progressBar.setVisibility(View.GONE);
                        if (photos.size() > 0) {
                            refresh.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("Markers", e.getMessage());
                        iterator++;
                    }

                    @Override
                    public void onNext(MarkerOptions myMarkerOptions) {
                        Marker marker = mMap.addMarker(myMarkerOptions);
                        markerPhotoHashMap.put(marker, photos.get(iterator));
                        iterator++;
                    }
                });
    }

}