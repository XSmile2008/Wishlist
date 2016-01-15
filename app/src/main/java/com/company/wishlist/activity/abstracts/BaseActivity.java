package com.company.wishlist.activity.abstracts;

import android.content.Intent;

import com.company.wishlist.activity.LoginActivity;


public abstract class BaseActivity extends InternetActivity {

    protected void processFacebookLogin() {
        Intent i = new Intent(this, LoginActivity.class);
        this.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }

    protected void processFacebookLogout() {
        Intent logoutIntent = new Intent(getApplicationContext(), LoginActivity.class);
        logoutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        logoutIntent.setAction(LoginActivity.ACTION_LOGOUT);
        startActivity(logoutIntent);
        finish();
    }
}
