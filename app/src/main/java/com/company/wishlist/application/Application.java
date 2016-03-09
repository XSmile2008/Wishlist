package com.company.wishlist.application;

import com.company.wishlist.util.AuthUtils;
import com.company.wishlist.util.ConnectionUtil;
import com.facebook.FacebookSdk;
import com.firebase.client.Firebase;

/**
 * Created by v.odahovskiy on 06.01.2016.
 */
public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.getDefaultConfig().setPersistenceEnabled(true);
        Firebase.setAndroidContext(this);
        AuthUtils.setAndroidContext(this);
        FacebookSdk.sdkInitialize(getApplicationContext());
        new ConnectionUtil(this);
    }
}
