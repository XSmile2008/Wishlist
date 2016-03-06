package com.company.wishlist.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.company.wishlist.R;
import com.company.wishlist.activity.abstracts.InternetActivity;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.Collections;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by v.odahovskiy on 05.01.2016.
 */
public class LoginActivity extends InternetActivity {

    private static String TAG = LoginActivity.class.getSimpleName();
    public static final String AUTH_TOKEN_EXTRA = "AUTH_TOKEN_EXTRA";
    public static final String ACTION_LOGOUT = "LOGOUT";

    @Bind(R.id.login_button) LoginButton loginButton;

    /* A reference to the Firebase */
    private Firebase mFirebaseRef;

    /* Data from the authenticated user */
    private AuthData mAuthData;

    /* The callback manager for Facebook */
    private CallbackManager mFacebookCallbackManager;
    /* Used to track user logging in/out off Facebook */
    private AccessTokenTracker mFacebookAccessTokenTracker;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        loginButton.setReadPermissions(Collections.singletonList(getString(R.string.facebook_permissions)));

        mFacebookCallbackManager = CallbackManager.Factory.create();
        mFacebookAccessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                Log.i(TAG, "Facebook.AccessTokenTracker.OnCurrentAccessTokenChanged");
                LoginActivity.this.onFacebookAccessTokenChange(currentAccessToken);
            }
        };

        /* Create the Firebase ref that is used for all authentication with Firebase */
        mFirebaseRef = new Firebase(getResources().getString(R.string.firebase_url));

        mFirebaseRef.addAuthStateListener(new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                if (authData != null) {
                    startMainActivity();
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isLogout(getIntent())) {
            logOut();
        }
    }

    private boolean isLogout(Intent intent) {
        return null != intent.getAction() && intent.getAction().equals(ACTION_LOGOUT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // if user logged in with Facebook, stop tracking their token
        if (mFacebookAccessTokenTracker != null) {
            mFacebookAccessTokenTracker.stopTracking();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void logOut() {
        mFirebaseRef.unauth();
        LoginManager.getInstance().logOut();
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public ProgressDialog getDialog() {
        loginButton.setVisibility(View.INVISIBLE);
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle(getString(R.string.app_name));
        dialog.setMessage("Signing in...");
        dialog.setCancelable(false);
        return dialog;
    }

    private class AuthResultHandler implements Firebase.AuthResultHandler {

        @Override
        public void onAuthenticated(AuthData authData) {
            progressDialog.hide();
            mAuthData = authData;
            startMainActivity();
        }

        @Override
        public void onAuthenticationError(FirebaseError firebaseError) {
            showErrorDialog(firebaseError.toString());
        }

    }

    private void startMainActivity() {
        getApplicationContext()
                .startActivity(new Intent(getApplicationContext(), MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }

    private void onFacebookAccessTokenChange(AccessToken token) {
        if (token != null) {
            progressDialog = getDialog();
            progressDialog.show();
            mFirebaseRef.authWithOAuthToken("facebook", token.getToken(), new AuthResultHandler());
        } else {
            if (this.mAuthData != null && this.mAuthData.getProvider().equals("facebook")) {
                mFirebaseRef.unauth();
                loginButton.setVisibility(View.VISIBLE);
            }
        }
    }
}
