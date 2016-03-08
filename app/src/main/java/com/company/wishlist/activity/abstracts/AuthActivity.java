package com.company.wishlist.activity.abstracts;

import android.content.Intent;
import android.os.Bundle;

import com.company.wishlist.activity.LoginActivity;
import com.company.wishlist.model.User;
import com.company.wishlist.util.AuthUtils;
import com.company.wishlist.util.ConnectionUtil;
import com.facebook.AccessToken;

public abstract class AuthActivity extends DebugActivity {

    public static final String RELOAD_DATA = "RELOAD_DATA";

    private ConnectionUtil connection = new ConnectionUtil(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkAuth();
        processFirebaseLogin();
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

    private void processFirebaseLogin() {
        if (connection.isConnected()) {
            if (AuthUtils.isDisconnected()) {
                AuthUtils.auth("facebook", AccessToken.getCurrentAccessToken().getToken(), null);
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
