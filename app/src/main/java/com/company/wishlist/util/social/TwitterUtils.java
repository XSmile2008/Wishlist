package com.company.wishlist.util.social;

import android.widget.Toast;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;

/**
 * Created by root on 16.03.2016.
 */
public class TwitterUtils {

    public static boolean isConnected() {
        return Twitter.getSessionManager().getActiveSession() != null;
    }

    public static String userName() {
        return (isConnected()) ? Twitter.getSessionManager().getActiveSession().getUserName() : "Not connected";
    }

    public static void logout() {
        if (isConnected()) {
            Twitter.getSessionManager().clearActiveSession();
            Twitter.logOut();
        }
    }

    public static void share(String message, Callback<Tweet> callback) {
        final StatusesService statusesService = Twitter.getInstance().getApiClient().getStatusesService();
        statusesService.update(message, null, null, null, null, null, null, null, null, callback);
    }

}
