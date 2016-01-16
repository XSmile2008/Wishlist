package com.company.wishlist.util;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;

import java.io.ByteArrayOutputStream;
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

    public static boolean isBlank(String s) {
        return s == null && s.isEmpty();
    }

    public static boolean isExpired(long expirationDate) {
        return expirationDate <= System.currentTimeMillis() / 1000;
    }

    public static String getEncoded64ImageStringFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        byte[] byteFormat = stream.toByteArray();
        // get the base 64 string
        String imgString = Base64.encodeToString(byteFormat, Base64.NO_WRAP);

        return imgString;
    }

}
