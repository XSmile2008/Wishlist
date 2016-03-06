package com.company.wishlist.activity.abstracts;


import android.content.Intent;
import android.os.Bundle;

import com.company.wishlist.activity.LoginActivity;
import com.company.wishlist.model.User;
import com.company.wishlist.util.AuthUtils;
import com.company.wishlist.util.FirebaseUtils;
import com.company.wishlist.util.social.FacebookUtil;
import com.facebook.AccessToken;

public abstract class AuthActivity extends InternetActivity {

    public static final String RELOAD_DATA = "RELOAD_DATA";

    private String authToken = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkAuth();
        processFirebaseLogin();

    }


    private void processFirebaseLogin() {
        if (isConnected()) {
            if (AuthUtils.isDisconnected()) {
                AuthUtils.auth("facebook", AccessToken.getCurrentAccessToken().getToken(), null);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAuth();
        if (null != this.getIntent().getExtras()) {
            boolean reloadData = this.getIntent().getExtras().getBoolean(RELOAD_DATA, false);
            if (reloadData) {
                AuthUtils.refreshAuthData();
            }
        }
    }

    public void checkAuth() {
        if (!isAuth() && this.getClass() != AuthActivity.class) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    public User currentUser() {
        return AuthUtils.getCurrentUser();
    }

    public boolean isAuth() {
        return !AuthUtils.isDisconnected();
    }
}
