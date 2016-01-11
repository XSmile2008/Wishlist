package com.company.wishlist.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.company.wishlist.FirebaseFragment;
import com.company.wishlist.R;
import com.company.wishlist.model.User;
import com.company.wishlist.util.Utilities;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;


public abstract class BaseActivity extends InternetActivity implements FirebaseFragment.Callbacks {

    private FirebaseFragment mFirebaseFragment;
    private Firebase mFirebase;
    private User mUser;
    private ProgressDialog mAuthProgressDialog;
    private String mAuthToken;

    public static final String RELOAD_DATA = "RELOAD_DATA";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupFirebase();

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(LoginActivity.AUTH_TOKEN_EXTRA)) {
            mAuthToken = extras.getString(LoginActivity.AUTH_TOKEN_EXTRA);
            processFirebaseLogin();
        } else {
            if (!mFirebaseFragment.isAuthenticated() || isTokenExpired()) {
                mFirebase.unauth();
                processFacebookLogin();
            }
        }
    }

    private void processFacebookLogin() {
        Intent i = new Intent(this, LoginActivity.class);
        this.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }

    private void processFirebaseLogin() {
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle(getString(R.string.app_name));
        mAuthProgressDialog.setMessage("Reconnection");
        mAuthProgressDialog.setCancelable(false);
        mAuthProgressDialog.show();
        if (!mFirebaseFragment.isAuthenticated()) {
            mFirebaseFragment.authenticate("facebook", mAuthToken);
        } else {
            mAuthProgressDialog.hide();
        }
    }

    private void setupFirebase() {
        mFirebaseFragment = Utilities.setupFirebase(getSupportFragmentManager());
        mFirebase = mFirebaseFragment.getFirebase();
        mUser = mFirebaseFragment.getUser();
    }

    private boolean isTokenExpired() {
        return (mFirebaseFragment.getAuthdata() == null
                || Utilities.isExpired(mFirebaseFragment.getAuthdata().getExpires()));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null != this.getIntent().getExtras()) {
            boolean reloadData = this.getIntent().getExtras().getBoolean(RELOAD_DATA, false);
            if (reloadData) {
                getFirebaseFragment().reloadData();
            }
        }
    }

    /**
     * FirebaseFragment.Callbacks implementation
     */
    @Override
    public void onAuthenticated(AuthData authData) {
        invalidateOptionsMenu();
        mAuthProgressDialog.hide();
    }

    @Override
    public void onAuthenticationError(FirebaseError error) {
        invalidateOptionsMenu();
        mAuthProgressDialog.hide();
        showErrorDialog(error.getMessage());
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.app_name))
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * Getters and setters
     */
    protected boolean isAuthenticated() {
        return mFirebaseFragment.isAuthenticated();
    }

    public Firebase getFirebase() {
        return mFirebase;
    }

    public User getUser() {
        return mUser;
    }

    public FirebaseFragment getFirebaseFragment() {
        return mFirebaseFragment;
    }

    public void logout() {
        if (mFirebaseFragment.isAuthenticated()) {
            mFirebase.unauth();
            processFacebookLogout();
        }
    }

    private void processFacebookLogout() {
        Intent logoutIntent = new Intent(getApplicationContext(), LoginActivity.class);
        logoutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        logoutIntent.putExtra(LoginActivity.INTENT_SIGNOUT, true);
        startActivity(logoutIntent);
        finish();
    }
}
