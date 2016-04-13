package com.company.wishlist.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.company.wishlist.R;
import com.company.wishlist.adapter.FriendListAdapter;
import com.company.wishlist.events.FriendSelectedEvent;
import com.company.wishlist.fragment.TabbedWishListFragment;
import com.company.wishlist.fragment.WishListFragment;
import com.company.wishlist.model.User;
import com.company.wishlist.service.NotificationService;
import com.company.wishlist.util.AuthUtils;
import com.company.wishlist.util.ConnectionUtil;
import com.company.wishlist.util.social.facebook.FacebookFriendCallback;
import com.company.wishlist.util.social.facebook.FacebookUtils;
import com.company.wishlist.view.CropCircleTransformation;
import com.facebook.FacebookRequestError;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private ConnectionStateReceiver mReceiver = new ConnectionStateReceiver();

    private FriendListAdapter mFriendListAdapter;

    private User mSelectedFriend;

    //NavigationDrawer
    @Nullable DrawerLayout drawer;
    @Bind(R.id.image_view_avatar) ImageView mImageViewAvatar;
    @Bind(R.id.text_view_user_name) TextView mTextViewUserName;
    @Bind(R.id.connectivity_status) View mConnectivityStatus;
    @Bind(R.id.recycler_view) RecyclerView mRecyclerViewFriends;

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
        mFriendListAdapter = new FriendListAdapter(this, new ArrayList<User>());
        mRecyclerViewFriends.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerViewFriends.setAdapter(mFriendListAdapter);
        mRecyclerViewFriends.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this).build());

        //Start service
        startNotificationService();

        if (savedInstanceState != null) {
            User friend = (User) savedInstanceState.getSerializable(User.class.getSimpleName());
            if (friend.getId().equals(AuthUtils.getCurrentUser().getId())) {
                showMyWishList();
            } else {
                showFriendWishList(friend);
            }
        } else {
            showMyWishList();
        }
    }

    private void startNotificationService() {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.notification_enabled_key), false)) {
            startService(new Intent(this, NotificationService.class));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        registerReceiver(mReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        refreshUserDataUi();//TODO: saveInstanceState
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        unregisterReceiver(mReceiver);
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(User.class.getSimpleName(), this.mSelectedFriend);
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
        User user = AuthUtils.getCurrentUser();
        if (null != user) {
            mTextViewUserName.setText(user.getDisplayName());

            Glide.with(this)
                    .load(user.getAvatarURL())
                    .bitmapTransform(new CropCircleTransformation(Glide.get(this).getBitmapPool()))
                    .placeholder(R.drawable.ic_account_circle_80dp)
                    .into(mImageViewAvatar);

            FacebookUtils.getAuthUserFriends(new FacebookFriendCallback() {
                @Override
                public void onSuccess(List<User> friends) {
                    mConnectivityStatus.setVisibility(View.GONE);
                    mRecyclerViewFriends.setVisibility(View.VISIBLE);
                    mFriendListAdapter.setFriends(friends);
                }

                @Override
                public void onError(FacebookRequestError error) {
                    mConnectivityStatus.setVisibility(View.VISIBLE);
                    mRecyclerViewFriends.setVisibility(View.GONE);
                }
            });
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
        this.mSelectedFriend = AuthUtils.getCurrentUser();
        getSupportActionBar().setTitle(getResources().getString(R.string.my_wish_list));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            findViewById(R.id.appBar).setElevation(getResources().getDimension(R.dimen.toolbar_elevation));
        }
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container_wish_list);
        if (fragment == null) {
            fragment = WishListFragment.newInstance(WishListFragment.MY_WISH_LIST_MODE, mSelectedFriend);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container_wish_list, fragment)
                    .commit();
        } else if (!(fragment instanceof WishListFragment)) {
            fragment = WishListFragment.newInstance(WishListFragment.MY_WISH_LIST_MODE, mSelectedFriend);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container_wish_list, fragment)
                    .commit();
        }
    }

    private void showFriendWishList(User friend) {
        this.mSelectedFriend = friend;
        getSupportActionBar().setTitle(friend.getDisplayName());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            findViewById(R.id.appBar).setElevation(0);
        }
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container_wish_list);
        if (fragment == null) {
            fragment = TabbedWishListFragment.newInstance(mSelectedFriend);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container_wish_list, fragment)
                    .commit();
        } else if (!(fragment instanceof TabbedWishListFragment)) {
            fragment = TabbedWishListFragment.newInstance(mSelectedFriend);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container_wish_list, fragment)
                    .commit();
        }
    }

    public class ConnectionStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectionUtil.isConnected()) {
                Log.d(LOG_TAG, "connected");
                refreshUserDataUi();
            } else {
                Log.d(LOG_TAG, "not connected");
                mConnectivityStatus.setVisibility(View.VISIBLE);
                mRecyclerViewFriends.setVisibility(View.GONE);
            }
        }

    }

}
