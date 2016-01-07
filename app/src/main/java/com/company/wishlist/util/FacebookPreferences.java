package com.company.wishlist.util;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONObject;

/**
 * Created by v.odahovskiy on 05.01.2016.
 */
public class FacebookPreferences {
    private Activity activity;

    public FacebookPreferences(Activity activity) {
        this.activity = activity;
    }

    public void saveUserId(String userId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("userId", userId);
        editor.apply();
    }

    public String getUserId() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        return sp.getString("userId", null);
    }

    public void saveAccessToken(String token) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("token", token);
        editor.apply();
    }

    public String getToken() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        return sp.getString("token", null);
    }

    public void clearUserData() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.apply();
    }

    public String getUserAvatarPath() {
        String profileImgUrl = "https://graph.facebook.com/%s/picture?type=large";
        return String.format(profileImgUrl, getUserId());
    }

    public void saveUserJson(JSONObject object) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("json", object.toString());
        editor.apply();
    }

    public String getUserJSON() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        return sp.getString("json", null);
    }

}
