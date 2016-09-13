package com.example.sic.getpicsfromflickr.Activity;

import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.example.sic.getpicsfromflickr.Fragment.ListFragment;
import com.example.sic.getpicsfromflickr.Model.Photo;
import com.example.sic.getpicsfromflickr.R;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private final String GALLERY_LIST = "list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            ListFragment listFragment = new ListFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(GALLERY_LIST, new ArrayList<Photo>());
            listFragment.setArguments(bundle);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, listFragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
    }

    public static void checkNetwork(ConnectivityManager cm, CoordinatorLayout coordinatorLayout) {
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (!(networkInfo != null && networkInfo.isConnected())) {
            Snackbar snackbar = Snackbar.make(coordinatorLayout, "No internet connection!", Snackbar.LENGTH_LONG);
            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.YELLOW);
            snackbar.show();
        }
    }
}