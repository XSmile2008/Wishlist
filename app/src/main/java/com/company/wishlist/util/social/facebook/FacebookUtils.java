package com.company.wishlist.util.social.facebook;

import android.os.Bundle;
import android.util.Log;

import com.company.wishlist.model.User;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.firebase.client.AuthData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.util.List;
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

    public static void getAuthUserFriends(final FacebookFriendCallback callback){
        if (null == callback) {
            throw new IllegalArgumentException("FacebookFriendCallback should be not empty");
        }
        GraphRequest.newMyFriendsRequest(AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONArrayCallback() {
                    @Override
                    public void onCompleted(JSONArray objects, GraphResponse response) {
                        if (response.getError() == null) {
                            List<User> friends = new Gson().fromJson(objects.toString(), new TypeToken<List<User>>() {
                            }.getType());
                            callback.onSuccess(friends);

                        } else {
                           callback.onError(response.getError());
                            Log.e(FacebookUtils.class.getSimpleName(), "GraphRequestError: " + response.getError().getErrorMessage());
                        }
                    }
                }).executeAsync();
    }
}
