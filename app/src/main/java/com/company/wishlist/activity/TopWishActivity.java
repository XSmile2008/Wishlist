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
import com.company.wishlist.adapter.TopWishAdapter;
import com.company.wishlist.fragment.WishListFragment;
import com.company.wishlist.model.Wish;
import com.company.wishlist.model.WishList;
import com.company.wishlist.util.ConnectionUtil;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import butterknife.ButterKnife;

public class TopWishActivity extends DebugActivity {

    private TopWishAdapter adapter;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        setContentView(R.layout.activity_top_wish);

        //Setup ActionBar
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.top_wish_activity_title));
        actionBar.setHomeButtonEnabled(true);

        //Init recycler view
        WishList wishList = (WishList) getIntent().getExtras().getSerializable(WishList.class.getSimpleName());
        adapter = new TopWishAdapter(this, wishList);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        //Start progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.app_name);
        progressDialog.setMessage(this.getResources().getString(R.string.message_loading_pints_dialog));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        //Load wishes
        if (ConnectionUtil.isConnected()) {
            loadWishes();
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

    private void loadWishes() {
        //todo write nice query to get random wishes
        Wish.getFirebaseRef().addValueEventListener(new ValueEventListener() {

            List<Wish> wishes = new CopyOnWriteArrayList<>();

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
