package com.company.wishlist.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by vladstarikov on 07.03.16.
 * This is singleton class that allow check connectivity status
 */
public class ConnectionUtil {

    private static Context mContext;

    public ConnectionUtil(Context context) {
        ConnectionUtil.mContext = context;
    }

    public static boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

}
