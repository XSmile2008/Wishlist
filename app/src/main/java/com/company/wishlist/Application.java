package com.company.wishlist;

import com.facebook.FacebookSdk;

/**
 * Created by v.odahovskiy on 06.01.2016.
 */
public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());
    }
}
