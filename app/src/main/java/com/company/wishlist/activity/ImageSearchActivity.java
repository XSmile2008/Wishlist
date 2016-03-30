package com.company.wishlist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.company.wishlist.R;
import com.company.wishlist.activity.abstracts.DebugActivity;
import com.company.wishlist.adapter.ImageSearchAdapter;
import com.company.wishlist.util.ConnectionUtil;
import com.company.wishlist.util.social.pinterest.PinterestUtil;
import com.company.wishlist.view.GridAutofitLayoutManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ImageSearchActivity extends DebugActivity implements ImageSearchAdapter.IOnPictureSelectedListener {

    public static final String QUERY = "com.company.wishlist.activity.QUERY";
    public static final String RESULT_DATA = "com.company.wishlist.activity.RESULT_DATA";
    private static final String RESULT_ITEMS = "RESULT_ITEMS";

    @Bind(R.id.text_view_loading) TextView mTextViewLoading;
    @Bind(R.id.recycler_view) RecyclerView mRecyclerView;
    @Bind(R.id.view_flipper) ViewFlipper mViewFlipper;
    ImageSearchAdapter mImageSearchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_search);
        ButterKnife.bind(this);

        String query = getIntent().getStringExtra(QUERY);

        //Setup ActionBar
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(query);

        mTextViewLoading.setText(getResources().getString(R.string.message_searching_for_images));

        RecyclerView.LayoutManager layoutManager = new GridAutofitLayoutManager(this, (int) getResources().getDimension(R.dimen.image_size_large_large));
        mImageSearchAdapter = new ImageSearchAdapter(this, this);
        mRecyclerView.setAdapter(mImageSearchAdapter);
        mRecyclerView.setLayoutManager(layoutManager);
        if (savedInstanceState != null && savedInstanceState.getStringArrayList(RESULT_ITEMS) != null) {
            mImageSearchAdapter.setItems(savedInstanceState.getStringArrayList(RESULT_ITEMS));
        } else {
            loadPictures(query);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(RESULT_ITEMS, (ArrayList<String>) mImageSearchAdapter.getItems());
    }

    @Override
    public void onPictureSelected(String url) {
        Intent intent = new Intent().putExtra(RESULT_DATA, url);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void loadPictures(String query) {
        if (ConnectionUtil.isConnected()) {
            mViewFlipper.setDisplayedChild(0);
            PinterestUtil.getImagesAsLinks(new PinterestUtil.IOnDoneListener() {
                @Override
                public void onDone(final List<String> urls) {
                    if (null != urls && urls.size() > 0) {
                        new Handler(getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Collections.shuffle(urls);
                                mImageSearchAdapter.setItems(urls);
                                mViewFlipper.setDisplayedChild(2);
                            }
                        });
                    }
                }
            }, query);
        } else {
            mViewFlipper.setDisplayedChild(1);
        }
    }

}

