package com.company.wishlist.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

    public static String encodeThumbnail(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP);
    }

    public static Bitmap decodeThumbnail(String thumbData) {
        byte[] bytes = Base64.decode(thumbData, Base64.NO_WRAP);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
