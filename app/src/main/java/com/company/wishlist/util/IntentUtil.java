package com.company.wishlist.util;

import android.app.Activity;
import android.content.Intent;

import com.company.wishlist.activity.LoginActivity;
import com.company.wishlist.activity.MainActivity;
import com.company.wishlist.activity.NoInternetConnectionActivity;

/**
 * Created by v.odahovskiy on 05.01.2016.
 */
public class IntentUtil {

    private Activity activity;

    public IntentUtil(Activity activity) {
        this.activity = activity;
    }

    public void showLoginActivity() {
        Intent i = new Intent(activity, LoginActivity.class);
        activity.startActivity(i
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    public void showMainActivity() {
        Intent i = new Intent(activity, MainActivity.class);
        activity.startActivity(i);
    }

    public void showNoIntentConnectionActivity() {
        Intent i = new Intent(activity, NoInternetConnectionActivity.class);
        activity.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}
