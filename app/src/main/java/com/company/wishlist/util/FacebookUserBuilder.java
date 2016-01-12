package com.company.wishlist.util;

import com.company.wishlist.model.User;
import com.firebase.client.AuthData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;

/**
 * Created by v.odahovskiy on 12.01.2016.
 */
public class FacebookUserBuilder {

    public static User build(AuthData authData) {
        Map<String, Object> cachedUserProfile = (Map<String, Object>) authData.getProviderData().get("cachedUserProfile");
        Gson gson = new GsonBuilder().create();
        User user = gson.fromJson(gson.toJson(cachedUserProfile), User.class);
        user.setProvider(authData.getProvider());
        return user;
    }
}
