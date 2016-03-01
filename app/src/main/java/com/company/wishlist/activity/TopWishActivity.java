package com.company.wishlist.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.company.wishlist.R;
import com.company.wishlist.activity.abstracts.FirebaseActivity;
import com.company.wishlist.adapter.TopWishListAdapter;
import com.company.wishlist.fragment.WishListFragment;
import com.company.wishlist.model.Wish;
import com.company.wishlist.util.DialogUtil;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import butterknife.ButterKnife;

public class TopWishActivity extends FirebaseActivity {

    TopWishListAdapter adapter;
    RecyclerView recyclerView;
    ProgressDialog progressloadingWithDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        setContentView(R.layout.activity_top_wish);

        //Setup ActionBar
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        String wishListId = getIntent().getExtras().getString(WishListFragment.WISH_LIST_ID);
        adapter = new TopWishListAdapter(this, wishListId);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        progressloadingWithDialog = DialogUtil.progressDialog(getString(R.string.app_name), getString(R.string.load_wish_progress_dialog_message), this);

        progressloadingWithDialog.show();

        loadWishes(new ValueEventListener() {

            List<Wish> wishes = new CopyOnWriteArrayList<Wish>();

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    wishes.add(postSnapshot.getValue(Wish.class));
                }


                Collections.shuffle(wishes);

                adapter.addAll(wishes);

                progressloadingWithDialog.dismiss();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });

    }

    private void loadWishes(ValueEventListener event) {
        //todo write nice query to get random wishes
        Wish.getFirebaseRef().addValueEventListener(event);
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
}
