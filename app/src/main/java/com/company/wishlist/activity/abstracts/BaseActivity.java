package com.company.wishlist.activity.abstracts;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.view.View;

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

    public void showSnackbar(String message) {
        Snackbar.make(getCoordinatorLayoutView(), message, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Abstract methods that MUST be implemented by the extending class
     */
    protected abstract int getLayoutResourceId();

    protected abstract View getCoordinatorLayoutView();
}
