package com.company.wishlist.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by vladstarikov on 07.03.16.
 * This is singleton class that allow check connectivity status
 */
public class ConnectionUtil {

    private static Context context;

    public ConnectionUtil(Context context) {
        ConnectionUtil.context = context;
    }

    public static boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public class ConectivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

        }

    }

}
