package com.example.sic.getpicsfromflickr;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class SearchResultActivity extends MainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        mListView= (ListView) findViewById(R.id.listView2);
        mItems=getIntent().getParcelableArrayListExtra("list");
        if (mItems!=null) {
            mAdapter = new CustomAdapter(this, mItems, sdb, mThumbnailThread);
            mListView.setAdapter(mAdapter);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mThumbnailThread.quit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
}
