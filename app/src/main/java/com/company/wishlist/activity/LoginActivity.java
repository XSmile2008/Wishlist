package com.company.wishlist.activity;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RotateDrawable;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.company.wishlist.R;
import com.company.wishlist.activity.abstracts.DebugActivity;
import com.company.wishlist.util.AuthUtils;
import com.company.wishlist.util.ConnectionUtil;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by v.odahovskiy on 05.01.2016.
 */
public class LoginActivity extends DebugActivity {

    private static String TAG = LoginActivity.class.getSimpleName();

    private CallbackManager mFacebookCallbackManager;
    private AccessTokenTracker mFacebookAccessTokenTracker;

    @BindView(R.id.coordinator_layout) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.custom_login_button) Button mCustomLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getString(R.string.logout_key).equals(getIntent().getAction())) {
            AuthUtils.unauth();
        } else if (!AuthUtils.isDisconnected()) {//TODO: fix bug that isDisconnected return false
            startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
            finish();
        } else if (AuthUtils.isFirstOpen()) {
            startActivity(new Intent(this, IntroActivity.class));
            finish();
        }

        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        final Activity activity = this;//TODO: try remove this
        mCustomLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectionUtil.isConnected()) {
                    setButtonProgressBarEnabled(true);
                    List<String> permissions = Collections.singletonList(getString(R.string.facebook_permissions));
                    LoginManager.getInstance().logInWithReadPermissions(activity, permissions);//TODO: may be use logInWithPublishPermissions
                } else {
                    Snackbar.make(mCoordinatorLayout, getString(R.string.no_internet_connection), Snackbar.LENGTH_LONG).show();
                }
            }
        });

        mFacebookCallbackManager = CallbackManager.Factory.create();
        mFacebookAccessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                Log.i(TAG, "Facebook.AccessTokenTracker.OnCurrentAccessTokenChanged");
                setButtonProgressBarEnabled(true);
                AuthUtils.auth("facebook", currentAccessToken.getToken(), new AuthResultHandler());
            }
        };
    }

    @Override
    protected void onDestroy() {
        if (mFacebookAccessTokenTracker != null) {
            mFacebookAccessTokenTracker.stopTracking();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 0) setButtonProgressBarEnabled(false);//TODO: check this result code
        mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void setButtonProgressBarEnabled(boolean enabled) {
        if (enabled) {
            RotateDrawable d = (RotateDrawable) getResources().getDrawable(R.drawable.spinner_24dp);
            mCustomLoginButton.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
            ObjectAnimator animator = ObjectAnimator.ofInt(d, "level", 0, 100000);
            animator.setRepeatCount(ObjectAnimator.INFINITE);
            animator.setDuration(5000);
            animator.start();
        } else {
            Drawable d = getResources().getDrawable(R.drawable.ic_facebook_icon);
            mCustomLoginButton.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
        }
    }

    private class AuthResultHandler implements Firebase.AuthResultHandler {

        @Override
        public void onAuthenticated(AuthData authData) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        @Override
        public void onAuthenticationError(FirebaseError firebaseError) {
            setButtonProgressBarEnabled(false);
            new AlertDialog.Builder(getApplicationContext())
                    .setTitle("Error")
                    .setMessage(firebaseError.getMessage())
                    .setPositiveButton(android.R.string.ok, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

    }

}
