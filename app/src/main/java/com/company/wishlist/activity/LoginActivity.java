package com.company.wishlist.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.company.wishlist.R;
import com.company.wishlist.activity.abstracts.DebugActivity;
import com.company.wishlist.util.AuthUtils;
import com.company.wishlist.util.ConnectionUtil;
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
public class LoginActivity extends DebugActivity {

    private static String TAG = LoginActivity.class.getSimpleName();
    public static final String AUTH_TOKEN_EXTRA = "AUTH_TOKEN_EXTRA";
    public static final String ACTION_LOGOUT = "LOGOUT";

    private AuthData mAuthData;//Data from the authenticated user
    private CallbackManager mFacebookCallbackManager;//The callback manager for Facebook
    private AccessTokenTracker mFacebookAccessTokenTracker;//Used to track user logging in/out off Facebook

    private android.app.AlertDialog progressDialog;

    @Bind(R.id.custom_login_button)
    Button customLoginButton;

    LoginButton loginButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        loginButton = new LoginButton(this);
        loginButton.setReadPermissions(Collections.singletonList(getString(R.string.facebook_permissions)));

        customLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectionUtil.isConnected()) {
                    loginButton.callOnClick();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.no_connection, Toast.LENGTH_LONG).show();//TODO:
                }
            }
        });

        mFacebookCallbackManager = CallbackManager.Factory.create();
        mFacebookAccessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                Log.i(TAG, "Facebook.AccessTokenTracker.OnCurrentAccessTokenChanged");
                onFacebookAccessTokenChange(currentAccessToken);
            }
        };

        if (!AuthUtils.isDisconnected() && !isLogout(getIntent())) {
            startMainActivity();
        } else {
            logOut();
        }
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

    private boolean isLogout(Intent intent) {
        return null != intent.getAction() && intent.getAction().equals(ACTION_LOGOUT);
    }

    private void logOut() {
        AuthUtils.unauth();
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

    private void startMainActivity() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));//TODO: no animation if user already be authorized, and do animation when new user auth
        finish();
    }

    private void onFacebookAccessTokenChange(AccessToken token) {
        if (token != null) {
            loginButton.setVisibility(View.INVISIBLE);
            progressDialog = new ProgressDialog.Builder(this)
                    .setTitle(getString(R.string.app_name))
                    .setMessage("Signing in...")
                    .setCancelable(false)
                    .show();
            AuthUtils.auth("facebook", token.getToken(), new AuthResultHandler());
        } else {
            if (this.mAuthData != null && this.mAuthData.getProvider().equals("facebook")) {
                AuthUtils.unauth();
                loginButton.setVisibility(View.VISIBLE);
            }
        }
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

}
