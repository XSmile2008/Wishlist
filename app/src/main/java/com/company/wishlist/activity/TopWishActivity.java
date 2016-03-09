package com.company.wishlist.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.company.wishlist.R;
import com.company.wishlist.activity.abstracts.DebugActivity;
import com.company.wishlist.adapter.TopWishListAdapter;
import com.company.wishlist.fragment.WishListFragment;
import com.company.wishlist.model.Wish;
import com.company.wishlist.util.ConnectionUtil;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import butterknife.ButterKnife;

public class TopWishActivity extends DebugActivity {

    private TopWishListAdapter adapter;
    private AlertDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        setContentView(R.layout.activity_top_wish);

        //Setup ActionBar
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.top_wish_activity_title));
        actionBar.setHomeButtonEnabled(true);

        String wishListId = getIntent().getExtras().getString(WishListFragment.WISH_LIST_ID);
        adapter = new TopWishListAdapter(this, wishListId);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        progressDialog = new ProgressDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.message_load_wish_progress_dialog)
                .setCancelable(false).show();

        if (ConnectionUtil.isConnected()) {
            loadWishes(new ValueEventListener() {

                List<Wish> wishes = new CopyOnWriteArrayList<Wish>();

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        wishes.add(postSnapshot.getValue(Wish.class));
                    }
                    Collections.shuffle(wishes);
                    adapter.addAll(wishes);
                    progressDialog.dismiss();
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {}

            });
        } else {
            progressDialog.dismiss();
            new AlertDialog.Builder(this)
                    .setTitle(R.string.no_connection)
                    .setMessage(R.string.check_connection)
                    .setIcon(R.drawable.ic_perm_scan_wifi_grey_600_24dp)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
        }

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
