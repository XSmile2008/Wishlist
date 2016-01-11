package com.company.wishlist.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.company.wishlist.R;
import com.company.wishlist.adapter.FriendListAdapter;
import com.company.wishlist.interfaces.IOnFriendSelectedListener;
import com.company.wishlist.model.User;
import com.company.wishlist.util.CropCircleTransformation;
import com.firebase.client.AuthData;

import java.util.ArrayList;

public class MainActivity extends BaseActivity implements IOnFriendSelectedListener, View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ImageView userAvatarView;
    private TextView profileUserName;
    private FriendListAdapter friendListAdapter;
    private ImageButton updateUserProfile;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.toggle_open_drawer, R.string.toggle_close_drawer);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        friendListAdapter = new FriendListAdapter(this, new ArrayList<User>());
        RecyclerView recyclerViewFriends = (RecyclerView) drawer.findViewById(R.id.friends_recycler_view);
        recyclerViewFriends.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewFriends.setAdapter(friendListAdapter);

        View header = findViewById(R.id.drawer_header);
        userAvatarView = (ImageView) header.findViewById(R.id.profile_user_avatar_iw);
        profileUserName = (TextView) header.findViewById(R.id.profile_user_name_tv);
        updateUserProfile = (ImageButton) header.findViewById(R.id.update_user_profile);
        logoutButton = (Button) header.findViewById(R.id.logout_button);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        updateUserProfile.setOnClickListener(this);

        if (isAuthenticated()) {
            if (isConnected()) {
                refreshUserDataUi();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshUserDataUi() {
        User user = getUser();
        if (null != user) {
            profileUserName.setText(user.getDisplayName());

            Glide.with(MainActivity.this)
                    .load(user.getAvatarUrl())
                    .bitmapTransform(new CropCircleTransformation(Glide.get(this).getBitmapPool()))
                    .into(userAvatarView);
            friendListAdapter.addAll(user.getFriends());
        }
    }


    @Override
    public void onAuthenticated(AuthData authData) {
        super.onAuthenticated(authData);
        if (isConnected()) {
            refreshUserDataUi();
        }
    }

    @Override
    public void onMissingConnection() {

    }

    @Override
    public void onClick(View v) {
        Toast.makeText(getApplicationContext(), "Bla bla", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onFriendSelected(long id) {
        Toast.makeText(this, " Selected friend id is " + id, Toast.LENGTH_LONG).show();
        Log.d(TAG, " Selected friend id is " + id);
    }
}
