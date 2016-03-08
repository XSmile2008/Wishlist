package com.company.wishlist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
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

import com.bumptech.glide.Glide;
import com.company.wishlist.R;
import com.company.wishlist.activity.abstracts.AuthActivity;
import com.company.wishlist.adapter.FriendListAdapter;
import com.company.wishlist.events.FriendSelectedEvent;
import com.company.wishlist.fragment.TabbedWishListFragment;
import com.company.wishlist.fragment.WishListFragment;
import com.company.wishlist.model.User;
import com.company.wishlist.service.NotificationService;
import com.company.wishlist.util.CropCircleTransformation;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AuthActivity  {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private FriendListAdapter friendListAdapter;

    //NavigationDrawer
    @Nullable DrawerLayout drawer;
    @Bind(R.id.profile_user_avatar_iw) ImageView userAvatarView;
    @Bind(R.id.profile_user_name_tv) TextView profileUserName;
    @Bind(R.id.connectivity_status) View connectivityStatus;
    @Bind(R.id.recycler_view_friends) RecyclerView recyclerViewFriends;

    private User selectedFriend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //Init Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Init navigation drawer
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.toggle_open_drawer, R.string.toggle_close_drawer);
            drawer.addDrawerListener(toggle);
            toggle.syncState();
        }

        //Setup friend list in drawer
        friendListAdapter = new FriendListAdapter(this, new ArrayList<User>());
        recyclerViewFriends.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewFriends.setAdapter(friendListAdapter);
        recyclerViewFriends.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this).build());

        //Start service
        startService(new Intent(this, NotificationService.class));

        if (savedInstanceState != null) {
            User friend = (User) savedInstanceState.getSerializable(User.class.getSimpleName());
            if (friend.getId().equals(currentUser().getId())) {
                showMyWishList();
            } else {
                showFriendWishList(friend);
            }
        } else {
            showMyWishList();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        refreshUserDataUi();//TODO:
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(User.class.getSimpleName(), this.selectedFriend);
    }

    @OnClick({R.id.header_layout, R.id.button_settings})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.header_layout:
                showMyWishList();
                break;
            case R.id.button_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }
        if (drawer != null) drawer.closeDrawer(GravityCompat.START);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void refreshUserDataUi() {
        User user = currentUser();
        if (null != user) {
            profileUserName.setText(user.getDisplayName());

            Glide.with(this)
                    .load(user.getAvatarURL())
                    .bitmapTransform(new CropCircleTransformation(Glide.get(this).getBitmapPool()))
                    .into(userAvatarView);

            GraphRequest.newMyFriendsRequest(AccessToken.getCurrentAccessToken(),
                    new GraphRequest.GraphJSONArrayCallback() {
                        @Override
                        public void onCompleted(JSONArray objects, GraphResponse response) {
                            if (response.getError() == null) {
                                List<User> friends = new Gson().fromJson(objects.toString(), new TypeToken<List<User>>() {
                                }.getType());
                                friendListAdapter.setFriends(friends);
                            } else {
                                connectivityStatus.setVisibility(View.VISIBLE);//TODO: check connection when open drawer
                                recyclerViewFriends.setVisibility(View.GONE);
                                Log.e(LOG_TAG, "GraphRequestError: " + response.getError().getErrorMessage());
                            }
                        }
                    }).executeAsync();
        }
    }

    @Subscribe
    public void onFriendSelectedEvent(FriendSelectedEvent event) {
        if (drawer != null) drawer.closeDrawer(GravityCompat.START);
        showFriendWishList(event.getFriend());

        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        Log.d(LOG_TAG, Arrays.toString(fragments.toArray()));
    }

    private void showMyWishList() {
        this.selectedFriend = currentUser();
        getSupportActionBar().setTitle(getResources().getString(R.string.my_wish_list));
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container_wish_list);
        if (fragment == null) {
            fragment = WishListFragment.newInstance(WishListFragment.MY_WISH_LIST_MODE, currentUser());
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container_wish_list, fragment)
                    .commit();
        } else if (!(fragment instanceof WishListFragment)) {
            fragment = WishListFragment.newInstance(WishListFragment.MY_WISH_LIST_MODE, currentUser());
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container_wish_list, fragment)
                    .commit();
        }
    }

    private void showFriendWishList(User friend) {
        this.selectedFriend = friend;
        getSupportActionBar().setTitle(friend.getDisplayName());
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container_wish_list);
        if (fragment == null) {
            fragment = TabbedWishListFragment.newInstance(friend);//TODO:
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container_wish_list, fragment)
                    .commit();
        } else if (!(fragment instanceof TabbedWishListFragment)) {
            fragment = TabbedWishListFragment.newInstance(friend);//TODO
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container_wish_list, fragment)
                    .commit();
        }
    }

}
