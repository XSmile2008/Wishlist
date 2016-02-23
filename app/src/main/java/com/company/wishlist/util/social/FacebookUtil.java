package com.company.wishlist.util.social;

import android.content.Context;
import android.content.Intent;

import com.company.wishlist.activity.LoginActivity;
import com.company.wishlist.model.User;
import com.firebase.client.AuthData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;

/**
 * Created by v.odahovskiy on 12.01.2016.
 */
public class FacebookUtil {

    public static User build(AuthData authData) {
        Map<String, Object> cachedUserProfile = (Map<String, Object>) authData.getProviderData().get("cachedUserProfile");
        Gson gson = new GsonBuilder().create();
        User user = gson.fromJson(gson.toJson(cachedUserProfile), User.class);
        user.setProvider(authData.getProvider());
        return user;
    }

    public static void processFacebookLogin(Context context) {
        Intent i = new Intent(context, LoginActivity.class);
        context.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    public static void processFacebookLogout(Context context) {
        Intent logoutIntent = new Intent(context, LoginActivity.class);
        logoutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        logoutIntent.setAction(LoginActivity.ACTION_LOGOUT);
        context.startActivity(logoutIntent);
    }
}
