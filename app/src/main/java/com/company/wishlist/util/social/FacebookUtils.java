package com.company.wishlist.util.social;

import android.os.Bundle;

import com.company.wishlist.model.User;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.firebase.client.AuthData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;

/**
 * Created by v.odahovskiy on 12.01.2016.
 */
public class FacebookUtils {

    public static User build(AuthData authData) {
        Map<String, Object> cachedUserProfile = (Map<String, Object>) authData.getProviderData().get("cachedUserProfile");
        Gson gson = new GsonBuilder().create();
        User user = gson.fromJson(gson.toJson(cachedUserProfile), User.class);
        user.setProvider(authData.getProvider());
        return user;
    }

    public static User getUserFromProfile() {
        User user = new User();
        user.setId(Profile.getCurrentProfile().getId());
        user.setDisplayName(String.format("%s %s", Profile.getCurrentProfile().getFirstName(), Profile.getCurrentProfile().getLastName()));
        user.setProvider("facebook");
        return user;
    }

    public static void share(String message, GraphRequest.Callback callback) {
        Bundle params = new Bundle();
        params.putString("message", message);
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/feed",
                params,
                HttpMethod.POST,
                callback
        ).executeAsync();
    }
}
