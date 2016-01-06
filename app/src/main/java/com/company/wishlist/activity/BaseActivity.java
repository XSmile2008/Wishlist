package com.company.wishlist.activity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.company.wishlist.R;
import com.company.wishlist.task.FacebookProfileData;
import com.company.wishlist.util.DialogUtil;
import com.company.wishlist.util.FacebookPreferences;
import com.company.wishlist.util.IntentUtil;

import org.json.JSONObject;

import java.util.concurrent.ExecutionException;


public abstract class BaseActivity extends AppCompatActivity {

    private FacebookPreferences facebookPreferences;
    private IntentUtil intentUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        facebookPreferences = new FacebookPreferences(this);
        intentUtil = new IntentUtil(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkOnlineAndSignIn();
    }

    private void checkOnlineAndSignIn() {
        if (isOnline()) {
            if (!isSignIn()) {
                intentUtil.showLoginActivity();
            }
        } else {
            DialogUtil.alertShow(getString(R.string.internet_connection), getString(R.string.no_internet_connection_msg), this);
            if (!isSignIn()) {
                intentUtil.showLoginActivity();
            }
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        return null != ni;
    }

    public boolean isSignIn() {
        return null != facebookPreferences.getToken();
    }

    public void updateUserData() {
        if (isOnline() && isSignIn()) {
            try {
                JSONObject data = new FacebookProfileData().execute().get();
                if (null != data) {
                    facebookPreferences.saveUserJson(data);
                }
            } catch (InterruptedException | ExecutionException e) {
                DialogUtil.alertShow(getString(R.string.app_name), getString(R.string.profile_data_error_msg), getApplicationContext());
            }
        }
    }
}
