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
import com.company.wishlist.bean.FriendBean;
import com.company.wishlist.interfaces.IOnFriendSelectedListener;
import com.company.wishlist.model.User;
import com.company.wishlist.task.FacebookMyFriendList;
import com.company.wishlist.util.DialogUtil;
import com.company.wishlist.util.FacebookPreferences;
import com.company.wishlist.util.IntentUtil;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends BaseActivity implements IOnFriendSelectedListener, View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ImageView userAvatarView;
    private TextView profileUserName;
    private Button showUserData;
    private FacebookPreferences facebookPreferences;
    private IntentUtil intentUtil;
    private FriendListAdapter friendListAdapter;
    private ImageButton updateUserProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        facebookPreferences = new FacebookPreferences(this);
        intentUtil = new IntentUtil(this);

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

        friendListAdapter = new FriendListAdapter(this, new ArrayList<FriendBean>());
        RecyclerView recyclerViewFriends = (RecyclerView) drawer.findViewById(R.id.friends_recycler_view);
        recyclerViewFriends.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewFriends.setAdapter(friendListAdapter);

        View header = findViewById(R.id.drawer_header);
        userAvatarView = (ImageView) header.findViewById(R.id.profile_user_avatar_iw);
        profileUserName = (TextView) header.findViewById(R.id.profile_user_name_tv);
        updateUserProfile = (ImageButton) header.findViewById(R.id.update_user_profile);
        showUserData = (Button) findViewById(R.id.show_user_data);//TODO: this button is invisible on screen now. Remove?

        updateUserProfile.setOnClickListener(this);

        //TEST GET USER DATA
        showUserData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONArray friendArr = new FacebookMyFriendList().execute().get();
                    Log.d(TAG, friendArr.toString());
                } catch (InterruptedException | ExecutionException e) {
                    DialogUtil.alertShow(getString(R.string.app_name), e.getMessage(), getApplicationContext());
                }

            }
        });

        updateUserData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        deleteAccessToken();
        refreshUserDataUi();
    }

    private void refreshUserDataUi() {
        User user = null;
        String json = facebookPreferences.getUserJSON();
        JSONArray friendArr = null;
        if (null != json) {
            try {
                JSONObject userData = new JSONObject(json);
                user = User.getFromJSON(userData);
                friendArr = new FacebookMyFriendList().execute().get();

            } catch (JSONException | InterruptedException | ExecutionException e) {
                DialogUtil.alertShow(getString(R.string.app_name), e.getMessage(), getApplicationContext());
            }

            if (null != user) {
                profileUserName.setText(user.getFullName());

                Glide.with(MainActivity.this)
                        .load(facebookPreferences.getUserAvatarPath())
                        .asBitmap()
                        .into(userAvatarView);

                if (null != friendArr && friendArr.length() > 0) {
                    List<FriendBean> friendBeans = getFriendListFromJSON(friendArr);
                    friendListAdapter.addAll(friendBeans);
                }

            }
        }
    }

    private List<FriendBean> getFriendListFromJSON(JSONArray friendArr) {
        List<FriendBean> result = new ArrayList<FriendBean>();

        for (int i = 0; i < friendArr.length(); i++) {
            try {
                JSONObject jsonObject = friendArr.getJSONObject(i);
                FriendBean bean = FriendBean.getFromJSON(jsonObject);
                bean.setImageUrl(facebookPreferences.getUserAvatarPath(bean.getId()));
                result.add(bean);
            } catch (JSONException e) {
                Log.d(TAG, e.getMessage());
            }
        }

        return result;
    }


    private void deleteAccessToken() {
        AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {

                if (currentAccessToken == null) {
                    //User logged out
                    facebookPreferences.clearUserData();
                    intentUtil.showLoginActivity();
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUserData();
        refreshUserDataUi();
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(getApplicationContext(), "Bla bla", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onFriendSelected(long id) {
        Toast.makeText(this, " Selected friend id is " + id, Toast.LENGTH_LONG).show();
        Log.d(TAG," Selected friend id is " + id);
    }
}
