package com.company.wishlist.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.company.wishlist.R;
import com.company.wishlist.activity.abstracts.DebugActivity;
import com.company.wishlist.adapter.TopWishAdapter;
import com.company.wishlist.model.Wish;
import com.company.wishlist.model.WishList;
import com.company.wishlist.util.ConnectionUtil;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TopWishActivity extends DebugActivity {

    @Bind(R.id.text_view_loading) TextView mTextViewLoading;
    @Bind(R.id.recycler_view) RecyclerView mRecyclerView;
    @Bind(R.id.view_flipper) ViewFlipper mViewFlipper;
    private TopWishAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_wish);
        ButterKnife.bind(this);

        //Setup ActionBar
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.top_wish_activity_title));
        actionBar.setHomeButtonEnabled(true);

        mTextViewLoading.setText(getResources().getString(R.string.message_loading_top_wishes));

        //Init recycler view
        WishList wishList = (WishList) getIntent().getExtras().getSerializable(WishList.class.getSimpleName());
        mAdapter = new TopWishAdapter(this, wishList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);

        loadWishes();
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

    @OnClick(R.id.no_connection_view)
    public void onClick() {
        loadWishes();
    }

    private void loadWishes() {
        //todo write nice query to get random wishes
        if (ConnectionUtil.isConnected()) {
            mViewFlipper.setDisplayedChild(0);
            Wish.getFirebaseRef().addValueEventListener(new ValueEventListener() {

                List<Wish> wishes = new CopyOnWriteArrayList<>();

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        wishes.add(postSnapshot.getValue(Wish.class));
                    }
                    Collections.shuffle(wishes);
                    mAdapter.addAll(wishes);
                    mViewFlipper.setDisplayedChild(2);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                }

            });
        } else {
            mViewFlipper.setDisplayedChild(1);
        }
    }

}
