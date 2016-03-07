package com.company.wishlist.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.company.wishlist.activity.abstracts.InternetActivity;
import com.company.wishlist.model.FirebaseRoot;
import com.company.wishlist.model.User;
import com.company.wishlist.util.social.FacebookUtil;
import com.facebook.Profile;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.gson.Gson;

/**
 * Created by root on 06.03.2016.
 */
public class AuthUtils {

    private final static String USER_PREFS = "USER_DATA";
    private static Firebase firebase = FirebaseRoot.get();
    private static User currentUser;
    private static AuthData data;
    private static Context context;//TODO: Static context is a good idea?

    public static void setAndroidContext(Context context) {
        AuthUtils.context = context;
        data = firebase.getAuth();
    }

    public static void auth(String provider, String token, final Firebase.AuthResultHandler handler) {
        firebase.authWithOAuthToken(provider, token, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                data = authData;
                saveUser(authData);
                handler.onAuthenticated(authData);
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                handler.onAuthenticationError(firebaseError);
            }
        });
    }

    public static void refreshAuthData() {
        data = firebase.getAuth();
        saveUser(data);
    }

    private static void saveUser(AuthData authData) {
        currentUser = FacebookUtil.build(authData);
        User.getFirebaseRef().child(currentUser.getId()).setValue(currentUser);
        saveUserToPreferences(currentUser);
    }

    public static void unauth() {
        firebase.unauth();
    }

    public static User getCurrentUser() {
        User user = currentUser;
        if (null == user) {
            user = getUserFromPreferences();
        }

        if (null == user) {
            user = new User();
            user.setId(Profile.getCurrentProfile().getId());
            user.setDisplayName(String.format("%s %s", Profile.getCurrentProfile().getFirstName(), Profile.getCurrentProfile().getLastName()));
            user.setProvider("facebook");
        }

        return user;
    }

    public static boolean isDisconnected() {
        return isTokenExpired();
    }


    private static boolean isTokenExpired() {
        return (data == null || Utilities.isExpired(data.getExpires()));
    }

    private static void saveUserToPreferences(User user) {
        SharedPreferences mPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = mPrefs.edit();
        String userJson = new Gson().toJson(user);
        editor.putString(USER_PREFS, userJson);
        editor.commit();
    }

    private static User getUserFromPreferences() {
        SharedPreferences mPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        String userJson = mPrefs.getString(USER_PREFS, null);

        if (null == userJson) return null;

        return new Gson().fromJson(userJson, User.class);
    }
}
