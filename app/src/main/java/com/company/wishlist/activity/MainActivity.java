package com.company.wishlist.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.company.wishlist.R;
import com.company.wishlist.model.User;
import com.company.wishlist.task.FacebookProfileData;
import com.company.wishlist.util.DialogUtil;
import com.company.wishlist.util.FacebookPreferences;
import com.company.wishlist.util.IntentUtil;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView info;
    private ImageView profileImgView;
    private Button showUserData;

    private FacebookPreferences facebookPreferences;
    private IntentUtil intentUtil;

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

        showUserData = (Button) findViewById(R.id.show_user_data);
        info = (TextView) findViewById(R.id.info);
        profileImgView = (ImageView) findViewById(R.id.profile_img);

        //TEST GET USER DATA
        showUserData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    new GraphRequest(
                            AccessToken.getCurrentAccessToken(),
                            "/me/friends",
                            null,
                            HttpMethod.GET,
                            new GraphRequest.Callback() {
                                public void onCompleted(GraphResponse response) {
                                    Log.d(TAG, response.getJSONObject().toString());
                                }
                            }
                    ).executeAsync();

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
        if (null != json) {
            try {
                JSONObject userData = new JSONObject(json);
                user = User.getFromJSON(userData);
            } catch (JSONException e) {
                DialogUtil.alertShow(getString(R.string.app_name), e.getMessage(), getApplicationContext());
            }

            if (null != user){
                info.setText(user.getFullName());

                Glide.with(MainActivity.this)
                        .load(facebookPreferences.getUserAvatarPath())
                        .into(profileImgView);
            }
        }
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
                    clearUserArea();
                    intentUtil.showLoginActivity();
                }
            }
        };
    }

    private void clearUserArea() {
        info.setText("");
        profileImgView.setImageDrawable(null);
    }



    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "ActivityTwo: onRestart()");
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUserData();
        refreshUserDataUi();
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "ActivityTwo: onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "ActivityTwo: onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "ActivityTwo: onDestroy()");
    }
}
