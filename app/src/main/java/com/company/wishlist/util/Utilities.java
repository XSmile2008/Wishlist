package com.company.wishlist.util;

import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.util.Patterns;

import java.net.URLEncoder;

/**
 * Created by v.odahovskiy on 10.01.2016.
 */
public class Utilities {

    public static String encodeUrlForFirebase(String urlToEncode) {
        String urlEncoded = null;
        try {
            urlEncoded = URLEncoder.encode(urlToEncode, "UTF-8");
            urlEncoded = urlEncoded.replaceAll("\\.", "%2E");
        } catch (Exception e) {
            Log.v("encodeUrlForFirebase", "Catched encoding exception: " + e.getMessage());
        }
        return urlEncoded;
    }


    public static boolean isUrlValid(String urlToValidate) {
        return Patterns.WEB_URL.matcher(urlToValidate).matches();
    }

    /*public static FirebaseFragment setupFirebase(FragmentManager fm) {
        FirebaseFragment ff =
                (FirebaseFragment) fm.findFragmentByTag(FirebaseFragment.TAG_FIREBASE_FRAGMENT);
        if (ff == null) {
            ff = new FirebaseFragment();
            fm.beginTransaction().add(ff, FirebaseFragment.TAG_FIREBASE_FRAGMENT).commit();
            fm.executePendingTransactions();
        }
        return ff;
    }*/

    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static boolean isExpired(long expirationDate) {
        return expirationDate <= System.currentTimeMillis() / 1000;
    }

    public static String getUserAvatarUrl(String userId) {
        String url = "https://graph.facebook.com/%s/picture?type=large";
        return String.format(url, userId);
    }
}
