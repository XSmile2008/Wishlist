package com.company.wishlist.application;

import com.company.wishlist.R;
import com.company.wishlist.util.AuthUtils;
import com.company.wishlist.util.ConnectionUtil;
import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.firebase.client.Firebase;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import io.fabric.sdk.android.Fabric;

/**
 * Created by v.odahovskiy on 06.01.2016.
 */
public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        TwitterAuthConfig authConfig = new TwitterAuthConfig(getString(R.string.twitter_api_key), getString(R.string.twitter_secret));
        Fabric.with(this, new Twitter(authConfig), new Crashlytics());
        Firebase.getDefaultConfig().setPersistenceEnabled(true);
        Firebase.setAndroidContext(this);
        AuthUtils.setAndroidContext(this);
        FacebookSdk.sdkInitialize(getApplicationContext());
        new ConnectionUtil(this);
    }

}
