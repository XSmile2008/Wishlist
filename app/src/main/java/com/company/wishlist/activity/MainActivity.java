package com.company.wishlist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.company.wishlist.R;
import com.company.wishlist.activity.abstracts.FirebaseActivity;
import com.company.wishlist.adapter.FriendListAdapter;
import com.company.wishlist.adapter.WishListPageViewAdapter;
import com.company.wishlist.fragment.FragmentWishList;
import com.company.wishlist.interfaces.IOnFriendSelectedListener;
import com.company.wishlist.model.User;
import com.company.wishlist.util.CropCircleTransformation;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.firebase.client.AuthData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends FirebaseActivity implements IOnFriendSelectedListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private FriendListAdapter friendListAdapter;
    WishListPageViewAdapter wishListPageViewAdapter;

    //NavigationDrawer
    @Bind(R.id.profile_user_avatar_iw) ImageView userAvatarView;
    @Bind(R.id.profile_user_name_tv) TextView profileUserName;
    @Bind(R.id.drawer_layout) DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.toggle_open_drawer, R.string.toggle_close_drawer);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        friendListAdapter = new FriendListAdapter(this, new ArrayList<User>());
        RecyclerView recyclerViewFriends = (RecyclerView) drawer.findViewById(R.id.friends_recycler_view);
        recyclerViewFriends.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewFriends.setAdapter(friendListAdapter);

        if (getFirebaseUtil().isAuthenticated()) {
            refreshUserDataUi();
        }

        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle("Second Activity");
        setUpTabLayout();
    }

    private void setUpTabLayout() {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.wish_tab_layout);
        ViewPager viewPager = (ViewPager) findViewById(R.id.wish_list_view_pager);
        wishListPageViewAdapter = new WishListPageViewAdapter(this, tabLayout);
        viewPager.setAdapter(wishListPageViewAdapter);
        tabLayout.setupWithViewPager(viewPager);
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
        User user = getFirebaseUtil().getCurrentUser();
        if (null != user) {
            profileUserName.setText(user.getDisplayName());

            Glide.with(this)
                    .load(user.genAvatarURL())
                    .bitmapTransform(new CropCircleTransformation(Glide.get(this).getBitmapPool()))
                    .into(userAvatarView);

            GraphRequest.newMyFriendsRequest(AccessToken.getCurrentAccessToken(),
                    new GraphRequest.GraphJSONArrayCallback() {
                        @Override
                        public void onCompleted(JSONArray objects, GraphResponse response) {
                            List<User> friends = new Gson()
                                    .fromJson(objects.toString(), new TypeToken<List<User>>() {
                                    }.getType());
                            friendListAdapter.setFriends(friends);
                        }
                    }).executeAsync();
        }
    }

    @Override
    public void onAuthenticated(AuthData authData) {
        super.onAuthenticated(authData);
        if (isConnected()) {
            refreshUserDataUi();
        }
    }

    @OnClick({R.id.fab, R.id.header_layout, R.id.button_settings})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                Intent intent = new Intent(getApplicationContext(), WishEditActivity.class);
                intent.setAction(WishEditActivity.ACTION_CREATE);
                startActivity(intent);
                return;
            case R.id.header_layout:
                onFriendSelected(getFirebaseUtil().getCurrentUser().getId());
                break;
            case R.id.button_settings:
                openSettingsActivity();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void openSettingsActivity() {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }

    @Override
    public void onFriendSelected(String id) {
        drawer.closeDrawer(GravityCompat.START);
        wishListPageViewAdapter.onFriendSelected(id);
    }
}
