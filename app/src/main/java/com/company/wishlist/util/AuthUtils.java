package com.company.wishlist.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.company.wishlist.model.FirebaseRoot;
import com.company.wishlist.model.User;
import com.company.wishlist.util.social.facebook.FacebookUtils;
import com.facebook.login.LoginManager;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.gson.Gson;
import com.twitter.sdk.android.Twitter;

/**
 * Created by root on 06.03.2016.
 */
public class AuthUtils {

    private final static String USER_PREFS = "USER_DATA";
    private final static String FIRST_OPEN = "FIRST_OPEN";
    private static Firebase sFirebase = FirebaseRoot.get();
    private static User sCurrentUser;
    private static AuthData sData;
    private static Context sContext;

    public static void setAndroidContext(Context context) {
        AuthUtils.sContext = context;
        sData = sFirebase.getAuth();
    }

    public static void auth(String provider, String token, final Firebase.AuthResultHandler handler) {
        sFirebase.authWithOAuthToken(provider, token, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                sData = authData;
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
        sData = sFirebase.getAuth();
        saveUser(sData);
    }

    private static void saveUser(AuthData authData) {
        sCurrentUser = FacebookUtils.build(authData);
        User.getFirebaseRef().child(sCurrentUser.getId()).setValue(sCurrentUser);
        saveUserToPreferences(sCurrentUser);
    }

    public static void unauth() {
        sFirebase.unauth();
        sData = sFirebase.getAuth();//TODO: check it
        LoginManager.getInstance().logOut();
        clearPreferences();
        Twitter.logOut();
    }

    public static User getCurrentUser() {
        if (sCurrentUser == null) {
            try {
                return getUserFromPreferences();
            } catch (UserNotFoundException e) {
                return FacebookUtils.getUserFromProfile();
            }
        } else {
            return sCurrentUser;
        }
    }

    public static boolean isDisconnected() {
        return isTokenExpired();
    }

    private static boolean isExpired(long expirationDate) {
        return expirationDate <= System.currentTimeMillis() / 1000;
    }

    private static boolean isTokenExpired() {
        return (sData == null || isExpired(sData.getExpires()));
    }

    private static SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(sContext);
    }

    private static void saveUserToPreferences(User user) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        String userJson = new Gson().toJson(user);
        editor.putString(USER_PREFS, userJson);
        editor.apply();
    }

    private static User getUserFromPreferences() throws UserNotFoundException {
        String userJson = getSharedPreferences().getString(USER_PREFS, null);

        if (null == userJson) {throw new UserNotFoundException();}

        return new Gson().fromJson(userJson, User.class);
    }

    private static void clearPreferences() {
        PreferenceManager.getDefaultSharedPreferences(sContext)
                .edit().clear();
    }

    public static void firstOpen() {
        PreferenceManager.getDefaultSharedPreferences(sContext)
                .edit().putBoolean(FIRST_OPEN, false).commit();
    }

    public static boolean isFirstOpen() {
        return PreferenceManager.getDefaultSharedPreferences(sContext)
                .getBoolean(FIRST_OPEN, true);
    }
}
